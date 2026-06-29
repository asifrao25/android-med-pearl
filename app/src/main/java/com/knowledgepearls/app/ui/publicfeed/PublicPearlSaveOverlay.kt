package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.local.model.FolderWithCount
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.folders.FolderNameDialog
import com.knowledgepearls.app.ui.folders.FolderPalette
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

private enum class PublicPearlSaveStep {
    Destination,
    Folder,
}

@Composable
fun PublicPearlSaveOverlay(
    folders: List<FolderWithCount>,
    theme: TabTheme,
    onSaveToMyFeed: () -> Unit,
    onSaveToFolder: (FolderWithCount) -> Unit,
    onCreateFolder: (String) -> Unit,
    onDismiss: () -> Unit,
    bottomInset: androidx.compose.ui.unit.Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    var step by remember { mutableStateOf(PublicPearlSaveStep.Destination) }
    var showCreateDialog by remember { mutableStateOf(false) }

    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .fillMaxSize()
            .background(PearlColors.scrim(darkTheme, 0.42f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter,
    ) {
        GlassSurface(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = bottomInset + 16.dp, top = 24.dp)
                .fillMaxWidth(0.82f)
                .clickable(enabled = false, onClick = {}),
            cornerRadius = 22.dp,
            opaque = true,
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = if (step == PublicPearlSaveStep.Destination) "Save Pearl" else "Choose Folder",
                    fontWeight = FontWeight.Bold,
                    color = PearlColors.heroPrimary(darkTheme),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                )

                when (step) {
                    PublicPearlSaveStep.Destination -> {
                        SaveDestinationOption(
                            icon = Icons.Default.Inbox,
                            title = "Save to My Feed",
                            colors = listOf(theme.primary, theme.secondary),
                            onClick = onSaveToMyFeed,
                        )
                        SaveDestinationOption(
                            icon = Icons.Default.Folder,
                            title = "Save to Folder",
                            colors = listOf(TabTheme.Folders.primary, TabTheme.Folders.secondary),
                            onClick = { step = PublicPearlSaveStep.Folder },
                        )
                    }
                    PublicPearlSaveStep.Folder -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { step = PublicPearlSaveStep.Destination }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = PearlColors.heroSecondary(darkTheme),
                            )
                            Text("Back", color = PearlColors.heroSecondary(darkTheme), fontWeight = FontWeight.SemiBold)
                        }

                        if (folders.isEmpty()) {
                            Text(
                                "No folders yet. Create one below.",
                                color = PearlColors.heroSecondary(darkTheme),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .heightIn(max = 260.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                folders.forEach { folder ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(FolderPalette.gradient(folder.folder.id))
                                            .clickable { onSaveToFolder(folder) }
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = folder.folder.name,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Text(
                                            text = folder.pearlCount.toString(),
                                            color = Color.White.copy(alpha = 0.82f),
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    }
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
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = theme.primary)
                            Text(
                                "New Folder",
                                modifier = Modifier.padding(start = 8.dp),
                                color = theme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
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
private fun SaveDestinationOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    colors: List<Color>,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(colors))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}
