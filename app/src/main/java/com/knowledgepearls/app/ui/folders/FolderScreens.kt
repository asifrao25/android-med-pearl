package com.knowledgepearls.app.ui.folders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.knowledgepearls.app.data.local.model.FolderWithCount
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.feed.FeedAuthorContext
import com.knowledgepearls.app.ui.feed.FeedViewModel
import com.knowledgepearls.app.ui.feed.PearlDeleteConfirmationDialog
import com.knowledgepearls.app.ui.feed.PearlDetailScreen
import com.knowledgepearls.app.ui.feed.PearlList
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderContentsScreen(
    folderId: String,
    folderName: String,
    viewModel: FoldersViewModel,
    feedAuthorContext: FeedAuthorContext,
    onResolveAvatarUrl: suspend (String) -> String?,
    onClose: () -> Unit,
    onSignInRequired: () -> Unit = {},
    onOpenUserProfile: (String) -> Unit = {},
    feedViewModel: FeedViewModel = hiltViewModel(),
    accountViewModel: com.knowledgepearls.app.ui.account.AccountViewModel = hiltViewModel(),
) {
    val accountState by accountViewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val theme = TabTheme.Folders
    val darkTheme = isPearlDarkTheme()
    val pearls by viewModel.observePearlsInFolder(folderId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var deleteTarget by remember { mutableStateOf<PearlWithMedia?>(null) }
    var folderPickerPearl by remember { mutableStateOf<PearlWithMedia?>(null) }
    var memberFolderIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val folders by viewModel.foldersWithCounts.collectAsStateWithLifecycle()
    val folderPearlId = folderPickerPearl?.pearl?.id

    androidx.compose.runtime.LaunchedEffect(folderPearlId) {
        if (folderPearlId == null) {
            memberFolderIds = emptySet()
        } else {
            viewModel.observePearlFolderIds(folderPearlId).collect { memberFolderIds = it }
        }
    }

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = theme, intensity = 0.5f)

        NavHost(
            navController = navController,
            startDestination = "folder_list",
            modifier = Modifier.fillMaxSize(),
        ) {
            composable("folder_list") {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onClose) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close")
                        }
                        Text(
                            text = folderName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PearlColors.heroPrimary(darkTheme),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    PearlList(
                        pearls = pearls,
                        feedAuthorContext = feedAuthorContext,
                        onResolveAvatarUrl = onResolveAvatarUrl,
                        onPearlClick = { navController.navigate("pearl/${it.pearl.id}") },
                        onDeleteRequest = { deleteTarget = it },
                        onFoldersRequest = { folderPickerPearl = it },
                        modifier = Modifier.weight(1f),
                        theme = theme,
                    )
                }
            }
            composable("pearl/{pearlId}") { entry ->
                val pearlId = entry.arguments?.getString("pearlId").orEmpty()
                PearlDetailScreen(
                    pearlId = pearlId,
                    viewModel = feedViewModel,
                    feedAuthorContext = feedAuthorContext,
                    onResolveAvatarUrl = onResolveAvatarUrl,
                    onBack = { navController.popBackStack() },
                    isSignedIn = accountState.isSignedIn,
                    onSignInRequired = onSignInRequired,
                    onOpenUserProfile = onOpenUserProfile,
                )
            }
        }

        deleteTarget?.let { target ->
            PearlDeleteConfirmationDialog(
                pearlTitle = target.pearl.title,
                onConfirm = {
                    viewModel.deletePearl(target.pearl.id)
                    deleteTarget = null
                },
                onDismiss = { deleteTarget = null },
            )
        }

        folderPickerPearl?.let { pearl ->
            FolderPickerOverlay(
                pearl = pearl,
                folders = folders,
                memberFolderIds = memberFolderIds,
                onDismiss = { folderPickerPearl = null },
                onToggleFolder = { folderId ->
                    viewModel.togglePearlFolderMembership(pearl.pearl.id, folderId)
                },
                onCreateFolder = { name ->
                    viewModel.createFolderAndAddPearl(name, pearl.pearl.id)
                },
            )
        }
    }
}

@Composable
fun FolderPickerOverlay(
    pearl: PearlWithMedia,
    folders: List<FolderWithCount>,
    memberFolderIds: Set<String>,
    onDismiss: () -> Unit,
    onToggleFolder: (String) -> Unit,
    onCreateFolder: (String) -> Unit,
) {
    val theme = TabTheme.Feed
    val darkTheme = isPearlDarkTheme()
    var showCreateDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PearlColors.scrim(darkTheme, 0.42f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter,
    ) {
        GlassSurface(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = PearlLayout.tabBarOverlayInset)
                .fillMaxWidth()
                .clickable(enabled = false, onClick = {}),
            cornerRadius = 22.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Move to Folder",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PearlColors.heroPrimary(darkTheme),
                    )
                    Text(
                        pearl.pearl.title.ifBlank { "Untitled pearl" },
                        style = MaterialTheme.typography.bodySmall,
                        color = PearlColors.heroSecondary(darkTheme),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (folders.isEmpty()) {
                    Text(
                        "No folders yet. Create one below to organise this pearl.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PearlColors.heroSecondary(darkTheme),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .height(260.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        folders.forEach { folder ->
                            FolderPickerRow(
                                folder = folder,
                                isMember = folder.folder.id in memberFolderIds,
                                onClick = { onToggleFolder(folder.folder.id) },
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(theme.primary.copy(alpha = 0.14f))
                        .clickable { showCreateDialog = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = theme.primary)
                    Text(
                        "New Folder",
                        modifier = Modifier.padding(start = 8.dp),
                        color = theme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showCreateDialog) {
        FolderNameDialog(
            title = "New Folder",
            message = "Enter a name for the new folder.",
            initialName = "",
            confirmLabel = "Create",
            onConfirm = { name ->
                if (name.isNotBlank()) onCreateFolder(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }
}

@Composable
private fun FolderPickerRow(
    folder: FolderWithCount,
    isMember: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = if (isMember) {
                    "${folder.folder.name}, added to folder"
                } else {
                    folder.folder.name
                }
                role = Role.Checkbox
                this.selected = isMember
            }
            .background(FolderPalette.gradient(folder.folder.id))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (isMember) Icons.Default.CheckCircle else Icons.Default.Folder,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.padding(end = 10.dp),
        )
        Text(
            text = folder.folder.name,
            modifier = Modifier.weight(1f),
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (isMember) {
            Text(
                "Added",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.22f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}
