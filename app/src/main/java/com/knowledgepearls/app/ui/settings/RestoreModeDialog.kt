package com.knowledgepearls.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMerge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.knowledgepearls.app.data.backup.RestorePreview
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RestoreModeDialog(
    pending: PendingRestoreState,
    isBusy: Boolean,
    onDismiss: () -> Unit,
    onMerge: () -> Unit,
    onReplace: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    val theme = TabTheme.Settings
    var showReplaceConfirm by remember { mutableStateOf(false) }

    if (showReplaceConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showReplaceConfirm = false },
            title = { Text("Replace all local data?") },
            text = {
                Text(
                    "This removes every pearl and folder on this device, then loads only what is in the backup. " +
                        "${pending.replacePreview.pearlsToRemove} pearl(s) will be deleted. This cannot be undone.",
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showReplaceConfirm = false
                        onReplace()
                    },
                ) {
                    Text("Replace all", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showReplaceConfirm = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(PearlLayout.cardCornerRadius))
                .background(PearlColors.popupSurface(darkTheme))
                .padding(18.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Restore backup",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PearlColors.heroPrimary(darkTheme),
                    )
                    IconButton(onClick = onDismiss, enabled = !isBusy) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                BackupInfoSection(
                    createdAt = pending.backupCreatedAt,
                    pearlCount = pending.pearlCount,
                    folderCount = pending.folderCount,
                    mediaCount = pending.mediaCount,
                    darkTheme = darkTheme,
                    theme = theme,
                )

                Text(
                    text = "Choose how to apply this backup to your library on this device.",
                    color = PearlColors.heroSecondary(darkTheme),
                    style = MaterialTheme.typography.bodySmall,
                )

                RestoreModeCard(
                    title = "Merge",
                    subtitle = "Keep existing pearls and add or update from the backup.",
                    icon = Icons.AutoMirrored.Filled.CallMerge,
                    accentColor = theme.primary,
                    darkTheme = darkTheme,
                    stats = listOf(
                        RestoreStat("Add", pending.mergePreview.pearlsToAdd),
                        RestoreStat("Update", pending.mergePreview.pearlsToUpdate),
                        RestoreStat("Keep", pending.mergePreview.pearlsUnchanged),
                        RestoreStat("Media", pending.mergePreview.mediaFilesInBackup),
                    ),
                    footerNote = if (pending.mergePreview.foldersToAdd > 0) {
                        "+ ${pending.mergePreview.foldersToAdd} new folder(s)"
                    } else {
                        null
                    },
                    actionLabel = "Merge backup",
                    actionEnabled = !isBusy,
                    onAction = onMerge,
                )

                RestoreModeCard(
                    title = "Replace",
                    subtitle = "Remove all local pearls and folders, then load the backup only.",
                    icon = Icons.Default.Warning,
                    accentColor = MaterialTheme.colorScheme.error,
                    darkTheme = darkTheme,
                    stats = listOf(
                        RestoreStat("Remove", pending.replacePreview.pearlsToRemove),
                        RestoreStat("Load", pending.replacePreview.pearlsToAdd),
                        RestoreStat("Folders", pending.replacePreview.foldersToAdd),
                        RestoreStat("Media", pending.replacePreview.mediaFilesInBackup),
                    ),
                    footerNote = null,
                    actionLabel = "Replace all data",
                    actionEnabled = !isBusy,
                    destructive = true,
                    onAction = { showReplaceConfirm = true },
                )

                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun BackupInfoSection(
    createdAt: Long,
    pearlCount: Int,
    folderCount: Int,
    mediaCount: Int,
    darkTheme: Boolean,
    theme: TabTheme,
) {
    GlassSurface(cornerRadius = PearlLayout.cardCornerRadius) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Backup contents",
                fontWeight = FontWeight.SemiBold,
                color = PearlColors.heroPrimary(darkTheme),
            )
            Text(
                text = formatBackupDate(createdAt),
                color = PearlColors.heroSecondary(darkTheme),
                style = MaterialTheme.typography.bodySmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                BackupInfoChip("$pearlCount pearls", theme, darkTheme)
                BackupInfoChip("$folderCount folders", theme, darkTheme)
                BackupInfoChip("$mediaCount attachments", theme, darkTheme)
            }
        }
    }
}

@Composable
private fun BackupInfoChip(
    label: String,
    theme: TabTheme,
    darkTheme: Boolean,
) {
    Text(
        text = label,
        color = theme.primary,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(theme.primary.copy(alpha = if (darkTheme) 0.14f else 0.10f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

private data class RestoreStat(val label: String, val value: Int)

@Composable
private fun RestoreModeCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    darkTheme: Boolean,
    stats: List<RestoreStat>,
    footerNote: String? = null,
    actionLabel: String,
    actionEnabled: Boolean,
    destructive: Boolean = false,
    onAction: () -> Unit,
) {
    GlassSurface(cornerRadius = PearlLayout.cardCornerRadius) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = accentColor.copy(alpha = if (destructive) 0.35f else 0.22f),
                    shape = RoundedCornerShape(PearlLayout.cardCornerRadius),
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(icon, contentDescription = null, tint = accentColor)
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        color = PearlColors.heroPrimary(darkTheme),
                    )
                    Text(
                        text = subtitle,
                        color = PearlColors.heroSecondary(darkTheme),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                stats.forEach { stat ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stat.value.toString(),
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = stat.label,
                            color = PearlColors.heroSecondary(darkTheme),
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            if (footerNote != null) {
                Text(
                    text = footerNote,
                    color = PearlColors.heroSecondary(darkTheme),
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (destructive) {
                Button(
                    onClick = onAction,
                    enabled = actionEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                ) {
                    Text(actionLabel)
                }
            } else {
                Button(
                    onClick = onAction,
                    enabled = actionEnabled,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

private fun formatBackupDate(epochMs: Long): String {
    if (epochMs <= 0L) return "Unknown date"
    val formatter = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return formatter.format(Date(epochMs))
}
