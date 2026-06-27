package com.knowledgepearls.app.ui.folders

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.local.model.FolderWithCount
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun FolderNameDialog(
    title: String,
    message: String,
    initialName: String,
    confirmLabel: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    val darkTheme = isPearlDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = PearlColors.heroPrimary(darkTheme)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(message, color = PearlColors.heroSecondary(darkTheme))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    placeholder = { Text("Folder name") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim()) }) {
                Text(confirmLabel, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun FolderDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete this folder?", color = PearlColors.heroPrimary(darkTheme)) },
        text = {
            Text(
                "Pearls inside this folder will not be deleted.",
                color = PearlColors.heroSecondary(darkTheme),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = Color(0xFFFF453A), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderFloatingMenu(
    folders: List<FolderWithCount>,
    onSelectFolder: (FolderWithCount) -> Unit,
    onDismiss: () -> Unit,
    onCreateFolder: (String) -> Unit,
    onRenameFolder: (FolderWithCount, String) -> Unit,
    onDeleteFolder: (FolderWithCount) -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = TabTheme.Folders
    val darkTheme = isPearlDarkTheme()
    var showCreateDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<FolderWithCount?>(null) }
    var deleteTarget by remember { mutableStateOf<FolderWithCount?>(null) }

    GlassSurface(
        modifier = modifier.width(280.dp),
        cornerRadius = 22.dp,
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.28f)),
                )
            }

            Text(
                text = "Folders",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PearlColors.heroPrimary(darkTheme),
            )

            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (folders.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "No folders yet",
                            fontWeight = FontWeight.SemiBold,
                            color = PearlColors.heroPrimary(darkTheme),
                        )
                        Text(
                            "Create one to organise pearls.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PearlColors.heroSecondary(darkTheme),
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .height(280.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        folders.forEach { folder ->
                            FolderMenuRow(
                                folder = folder,
                                onClick = { onSelectFolder(folder) },
                                onRename = { renameTarget = folder },
                                onDelete = { deleteTarget = folder },
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
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = theme.primary)
                    Text(
                        "Add Folder",
                        modifier = Modifier.padding(start = 8.dp),
                        color = theme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
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

    renameTarget?.let { folder ->
        FolderNameDialog(
            title = "Rename Folder",
            message = "Enter a new name for this folder.",
            initialName = folder.folder.name,
            confirmLabel = "Save",
            onConfirm = { name ->
                if (name.isNotBlank()) onRenameFolder(folder, name)
                renameTarget = null
            },
            onDismiss = { renameTarget = null },
        )
    }

    deleteTarget?.let { folder ->
        FolderDeleteDialog(
            onConfirm = {
                onDeleteFolder(folder)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderMenuRow(
    folder: FolderWithCount,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var showActions by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(FolderPalette.gradient(folder.folder.id))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showActions = true },
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Folder, contentDescription = null, tint = Color.White, modifier = Modifier.padding(end = 10.dp))
        Text(
            text = folder.folder.name,
            modifier = Modifier.weight(1f),
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = folder.pearlCount.toString(),
            color = Color.White.copy(alpha = 0.82f),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(start = 8.dp),
        )
    }

    if (showActions) {
        AlertDialog(
            onDismissRequest = { showActions = false },
            title = { Text(folder.folder.name) },
            text = { Text("Folder actions") },
            confirmButton = {
                TextButton(onClick = {
                    showActions = false
                    onRename()
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showActions = false
                    onDelete()
                }) { Text("Delete", color = Color(0xFFFF453A)) }
            },
        )
    }
}
