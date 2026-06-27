package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.local.model.clinicalCasePayload
import com.knowledgepearls.app.data.local.model.isClinicalCase
import androidx.hilt.navigation.compose.hiltViewModel
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.folders.FolderPickerOverlay
import com.knowledgepearls.app.ui.folders.FoldersViewModel
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun PearlDetailScreen(
    pearlId: String,
    viewModel: FeedViewModel,
    onBack: () -> Unit,
    foldersViewModel: FoldersViewModel = hiltViewModel(),
) {
    val pearl by viewModel.observePearl(pearlId).collectAsStateWithLifecycle(initialValue = null)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val folders by foldersViewModel.foldersWithCounts.collectAsStateWithLifecycle()
    val memberFolderIds by foldersViewModel.observePearlFolderIds(pearlId)
        .collectAsStateWithLifecycle(initialValue = emptySet())
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showFolderPicker by remember { mutableStateOf(false) }
    val theme = TabTheme.Feed
    val darkTheme = isPearlDarkTheme()

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = theme, intensity = 0.5f)

        if (pearl == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading…", color = PearlColors.heroSecondary(darkTheme))
            }
        } else {
            val item = pearl!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = item.pearl.title.ifBlank { "Pearl" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PearlColors.heroPrimary(darkTheme),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showFolderPicker = true }) {
                        Icon(Icons.Default.Folder, contentDescription = "Folders", tint = theme.primary)
                    }
                    IconButton(onClick = { viewModel.toggleFavourite(item.pearl.id) }) {
                        Icon(
                            if (item.pearl.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favourite",
                            tint = theme.primary,
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF453A))
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = PearlLayout.screenHorizontalPadding)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (item.pearl.isClinicalCase()) {
                        ClinicalCaseDetailContent(item)
                    } else {
                        StandardPearlDetailContent(item)
                    }

                    if (item.mediaItems.isNotEmpty()) {
                        Text(
                            text = "Attachments",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PearlColors.heroPrimary(darkTheme),
                        )
                        item.mediaItems.forEach { media ->
                            MediaAttachmentRow(
                                filename = media.filename.ifBlank { media.type },
                                type = media.type,
                                onClick = { /* Stage 5: full viewer in follow-up */ },
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(
                            start = PearlLayout.screenHorizontalPadding,
                            end = PearlLayout.screenHorizontalPadding,
                            top = 12.dp,
                            bottom = PearlLayout.tabBarOverlayInset,
                        ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onBack,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                    ) {
                        Text("Done")
                    }
                }
            }
        }

        if (showDeleteDialog && pearl != null) {
            PearlDeleteConfirmationDialog(
                pearlTitle = pearl!!.pearl.title,
                onConfirm = {
                    showDeleteDialog = false
                    viewModel.confirmDeleteForPearl(pearlId)
                    onBack()
                },
                onDismiss = { showDeleteDialog = false },
            )
        }

        if (showFolderPicker && pearl != null) {
            FolderPickerOverlay(
                pearl = pearl!!,
                folders = folders,
                memberFolderIds = memberFolderIds,
                onDismiss = { showFolderPicker = false },
                onToggleFolder = { folderId ->
                    foldersViewModel.togglePearlFolderMembership(pearlId, folderId)
                },
                onCreateFolder = { name ->
                    foldersViewModel.createFolderAndAddPearl(name, pearlId)
                },
            )
        }

        uiState.actionSuccessMessage?.let { message ->
            PearlActionSuccessBanner(
                message = message,
                theme = theme,
                onDismiss = viewModel::dismissActionSuccess,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun StandardPearlDetailContent(pearl: PearlWithMedia) {
    val darkTheme = isPearlDarkTheme()
    val entity = pearl.pearl
    DetailSection("Notes", entity.notes, darkTheme)
    if (entity.sourceReference.isNotBlank()) {
        DetailSection("Source", entity.sourceReference, darkTheme)
    }
    entity.sourceURL?.takeIf { it.isNotBlank() }?.let { url ->
        DetailSection("Link", url, darkTheme)
    }
    if (entity.tags.isNotEmpty()) {
        DetailSection("Tags", entity.tags.joinToString(", "), darkTheme)
    }
}

@Composable
private fun ClinicalCaseDetailContent(pearl: PearlWithMedia) {
    val darkTheme = isPearlDarkTheme()
    val payload = pearl.pearl.clinicalCasePayload()
    DetailSection("History", payload.history, darkTheme)
    DetailSection("Examination", payload.examination, darkTheme)
    DetailSection("Investigation", payload.investigation, darkTheme)
    DetailSection("Diagnosis", payload.diagnosis, darkTheme)
    DetailSection("Discussion", payload.discussion, darkTheme)
    DetailSection("References", payload.references, darkTheme)
}

@Composable
private fun DetailSection(title: String, body: String, darkTheme: Boolean) {
    if (body.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = TabTheme.Feed.primary,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = PearlColors.heroPrimary(darkTheme),
        )
    }
}

@Composable
private fun MediaAttachmentRow(
    filename: String,
    type: String,
    onClick: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        cornerRadius = 14.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(filename, fontWeight = FontWeight.SemiBold, color = PearlColors.heroPrimary(darkTheme))
                Text(type.uppercase(), style = MaterialTheme.typography.labelSmall, color = PearlColors.heroSecondary(darkTheme))
            }
            Text("View", color = TabTheme.Feed.primary, fontWeight = FontWeight.Bold)
        }
    }
}
