package com.knowledgepearls.app.ui.favourites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.ui.components.TabScreenHeader
import com.knowledgepearls.app.ui.feed.FeedAuthorContext
import com.knowledgepearls.app.ui.feed.PearlDeleteConfirmationDialog
import com.knowledgepearls.app.ui.feed.PearlList
import com.knowledgepearls.app.ui.folders.FolderPickerOverlay
import com.knowledgepearls.app.ui.folders.FoldersViewModel
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun FavouritesScreen(
    viewModel: FavouritesViewModel,
    feedAuthorContext: FeedAuthorContext,
    onResolveAvatarUrl: suspend (String) -> String?,
    onOpenSettings: () -> Unit,
    isSignedIn: Boolean = false,
    inboxBadgeCount: Int = 0,
    onOpenInbox: () -> Unit = {},
    onPearlClick: (String) -> Unit,
    foldersViewModel: FoldersViewModel = hiltViewModel(),
) {
    val theme = TabTheme.Favourites
    val darkTheme = isPearlDarkTheme()
    val favourites by viewModel.favourites.collectAsStateWithLifecycle()
    val folders by foldersViewModel.foldersWithCounts.collectAsStateWithLifecycle()
    var deleteTarget by remember { mutableStateOf<PearlWithMedia?>(null) }
    var folderPickerPearl by remember { mutableStateOf<PearlWithMedia?>(null) }
    var memberFolderIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val folderPearlId = folderPickerPearl?.pearl?.id

    LaunchedEffect(folderPearlId) {
        if (folderPearlId == null) {
            memberFolderIds = emptySet()
        } else {
            foldersViewModel.observePearlFolderIds(folderPearlId).collect { memberFolderIds = it }
        }
    }

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = theme)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            TabScreenHeader(
                title = "Favourites",
                subtitle = "Saved pearls",
                theme = theme,
                onSettingsClick = onOpenSettings,
            )

            if (favourites.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(horizontal = 32.dp),
                    ) {
                        Icon(
                            Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = theme.primary,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        Text(
                            "No Favourites Yet",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PearlColors.heroPrimary(darkTheme),
                        )
                        Text(
                            "Tap the heart on any pearl\nto save it here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PearlColors.heroSecondary(darkTheme),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                PearlList(
                    pearls = favourites,
                    feedAuthorContext = feedAuthorContext,
                    onResolveAvatarUrl = onResolveAvatarUrl,
                    onPearlClick = { onPearlClick(it.pearl.id) },
                    onDeleteRequest = { deleteTarget = it },
                    onFoldersRequest = { folderPickerPearl = it },
                    modifier = Modifier.fillMaxSize(),
                    theme = theme,
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
                    foldersViewModel.togglePearlFolderMembership(pearl.pearl.id, folderId)
                },
                onCreateFolder = { name ->
                    foldersViewModel.createFolderAndAddPearl(name, pearl.pearl.id)
                },
            )
        }
    }
}
