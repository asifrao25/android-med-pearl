package com.knowledgepearls.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.knowledgepearls.app.data.remote.SupabaseConfig
import com.knowledgepearls.app.ui.account.AccountUiState
import com.knowledgepearls.app.ui.account.profileDisplayName
import com.knowledgepearls.app.ui.account.profileSubtitle
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.components.SheetDragHandle
import com.knowledgepearls.app.ui.components.TabScreenHeader
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.knowledgepearls.app.data.backup.BackupFormat
import com.knowledgepearls.app.data.backup.BackupRepository
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.knowledgepearls.app.ui.profile.SettingsProfileAvatar
import com.knowledgepearls.app.ui.publicfeed.PendingSubmissionsScreen
import com.knowledgepearls.app.ui.theme.AppearanceMode
import com.knowledgepearls.app.ui.theme.AppFontChoice
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

enum class SettingsRoute {
    Main,
    PendingSubmissions,
    BackupRestore,
    DeviceCache,
    Privacy,
    AboutCreator,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    visible: Boolean,
    route: SettingsRoute,
    accountState: AccountUiState,
    settingsState: SettingsUiState,
    appearanceMode: AppearanceMode,
    appFontChoice: AppFontChoice,
    onDismiss: () -> Unit,
    onNavigate: (SettingsRoute) -> Unit,
    onSignIn: () -> Unit,
    onOpenProfile: () -> Unit,
    onSignOut: () -> Unit,
    onLoadPending: () -> Unit,
    onWithdrawSubmission: (com.knowledgepearls.app.data.model.PublicPearl) -> Unit,
    onSetAppearance: (AppearanceMode) -> Unit,
    onSetAppFontChoice: (AppFontChoice) -> Unit,
    onLoadBackups: () -> Unit,
    onCreateBackup: (onCreated: ((BackupRepository.BackupFileInfo) -> Unit)?) -> Unit,
    onPrepareRestore: (String) -> Unit,
    onPrepareImport: (Uri) -> Unit,
    onConfirmRestoreMerge: () -> Unit,
    onConfirmRestoreReplace: () -> Unit,
    onCancelPendingRestore: () -> Unit,
    onSaveBackupToUri: (String, Uri) -> Unit,
    onMeasureCache: () -> Unit,
    onClearCache: () -> Unit,
    onDeleteAccount: () -> Unit,
    onOpenUserProfile: (String) -> Unit = {},
) {
    if (!visible) return

    val darkTheme = isPearlDarkTheme()
    val theme = TabTheme.Settings
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        sheetState.show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PearlColors.popupSurface(darkTheme),
        scrimColor = PearlColors.scrim(darkTheme, 0.42f),
        dragHandle = { SheetDragHandle() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f),
        ) {
            LiquidBackground(theme = theme, intensity = 0.72f)

            when (route) {
                SettingsRoute.Main -> SettingsMainScreen(
                    accountState = accountState,
                    settingsState = settingsState,
                    appearanceMode = appearanceMode,
                    appFontChoice = appFontChoice,
                    onDismiss = onDismiss,
                    onNavigate = onNavigate,
                    onSignIn = onSignIn,
                    onOpenProfile = onOpenProfile,
                    onSignOut = onSignOut,
                    onSetAppearance = onSetAppearance,
                    onSetAppFontChoice = onSetAppFontChoice,
                )
                SettingsRoute.PendingSubmissions -> {
                    LaunchedEffect(Unit) { onLoadPending() }
                    PendingSubmissionsScreen(
                        submissions = settingsState.pendingSubmissions,
                        isLoading = settingsState.isLoadingPending,
                        withdrawingId = settingsState.withdrawingId,
                        errorMessage = settingsState.errorMessage,
                        onBack = { onNavigate(SettingsRoute.Main) },
                        onRefresh = onLoadPending,
                        onWithdraw = onWithdrawSubmission,
                        embeddedInSheet = true,
                    )
                }
                SettingsRoute.BackupRestore -> BackupRestoreScreen(
                    settingsState = settingsState,
                    onBack = { onNavigate(SettingsRoute.Main) },
                    onLoad = onLoadBackups,
                    onCreate = onCreateBackup,
                    onPrepareRestore = onPrepareRestore,
                    onPrepareImport = onPrepareImport,
                    onConfirmRestoreMerge = onConfirmRestoreMerge,
                    onConfirmRestoreReplace = onConfirmRestoreReplace,
                    onCancelPendingRestore = onCancelPendingRestore,
                    onSaveToUri = onSaveBackupToUri,
                )
                SettingsRoute.DeviceCache -> DeviceCacheScreen(
                    settingsState = settingsState,
                    onBack = { onNavigate(SettingsRoute.Main) },
                    onMeasure = onMeasureCache,
                    onClear = onClearCache,
                )
                SettingsRoute.Privacy -> PrivacySettingsScreen(
                    settingsState = settingsState,
                    onBack = { onNavigate(SettingsRoute.Main) },
                    onDeleteAccount = onDeleteAccount,
                )
                SettingsRoute.AboutCreator -> AboutCreatorScreen(
                    onBack = { onNavigate(SettingsRoute.Main) },
                    onOpenProfile = { userId ->
                        onOpenUserProfile(userId)
                    },
                    embeddedInSheet = true,
                )
            }
        }
    }
}

@Composable
private fun SettingsMainScreen(
    accountState: AccountUiState,
    settingsState: SettingsUiState,
    appearanceMode: AppearanceMode,
    appFontChoice: AppFontChoice,
    onDismiss: () -> Unit,
    onNavigate: (SettingsRoute) -> Unit,
    onSignIn: () -> Unit,
    onOpenProfile: () -> Unit,
    onSignOut: () -> Unit,
    onSetAppearance: (AppearanceMode) -> Unit,
    onSetAppFontChoice: (AppFontChoice) -> Unit,
) {
    val theme = TabTheme.Settings
    val darkTheme = isPearlDarkTheme()
    val pendingSubtitle = if (settingsState.pendingCount > 0) {
        "${settingsState.pendingCount} awaiting approval"
    } else {
        "Track shared pearls sent for moderation"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        TabScreenHeader(
            title = "Settings",
            subtitle = "Account & app",
            theme = theme,
            showsSettingsButton = false,
            trailing = {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.primary)
                }
            },
        )

        LazyColumn(
            contentPadding = PaddingValues(
                horizontal = PearlLayout.screenHorizontalPadding,
                vertical = 12.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                SettingsAccountSection(
                    accountState = accountState,
                    theme = theme,
                    onOpenProfile = onOpenProfile,
                    onSignIn = {
                        onDismiss()
                        onSignIn()
                    },
                    onSignOut = onSignOut,
                )
            }

            if (accountState.isSignedIn) {
                item {
                    SettingsSectionHeader(title = "Community")
                    SettingsMenuCard(theme = theme) {
                        SettingsMenuRow(
                            icon = Icons.Default.Schedule,
                            accent = SettingsSectionAccent.Community,
                            title = "Pending submissions",
                            subtitle = pendingSubtitle,
                            theme = theme,
                            onClick = { onNavigate(SettingsRoute.PendingSubmissions) },
                            trailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (settingsState.pendingCount > 0) {
                                        Text(
                                            text = "${settingsState.pendingCount} pending",
                                            color = SettingsSectionAccent.Community.colors(theme, darkTheme).first,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(end = 4.dp),
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = PearlColors.heroSecondary(darkTheme),
                                    )
                                }
                            },
                        )
                    }
                }
            }

            item {
                SettingsSectionHeader(title = "Appearance")
                SettingsMenuCard(theme = theme) {
                    Column {
                        AppearanceMode.entries.forEachIndexed { index, mode ->
                            if (index > 0) {
                                SettingsMenuDivider()
                            }
                            AppearanceModeRow(
                                mode = mode,
                                isSelected = appearanceMode == mode,
                                theme = theme,
                                onClick = { onSetAppearance(mode) },
                            )
                        }
                    }
                }
            }

            item {
                SettingsSectionHeader(title = "Text font")
                SettingsMenuCard(theme = theme) {
                    Column {
                        AppFontChoice.entries.forEachIndexed { index, choice ->
                            if (index > 0) {
                                SettingsMenuDivider()
                            }
                            AppFontChoiceRow(
                                choice = choice,
                                isSelected = appFontChoice == choice,
                                theme = theme,
                                onClick = { onSetAppFontChoice(choice) },
                            )
                        }
                    }
                }
            }

            item {
                SettingsSectionHeader(title = "Data & Sync")
                SettingsMenuCard(theme = theme) {
                    Column {
                        SettingsMenuRow(
                            icon = Icons.Default.CloudUpload,
                            accent = SettingsSectionAccent.Backup,
                            title = "Backup & restore",
                            subtitle = "Move pearls and attachments to another phone",
                            theme = theme,
                            onClick = { onNavigate(SettingsRoute.BackupRestore) },
                            trailing = {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = PearlColors.heroSecondary(darkTheme),
                                )
                            },
                        )
                        SettingsMenuDivider()
                        SettingsMenuRow(
                            icon = Icons.Default.Storage,
                            accent = SettingsSectionAccent.Cache,
                            title = "Device cache",
                            subtitle = "Review storage used on this device",
                            theme = theme,
                            onClick = { onNavigate(SettingsRoute.DeviceCache) },
                            trailing = {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = PearlColors.heroSecondary(darkTheme),
                                )
                            },
                        )
                    }
                }
            }

            item {
                SettingsSectionHeader(title = "Privacy")
                SettingsMenuCard(theme = theme) {
                    SettingsMenuRow(
                        icon = Icons.Default.Lock,
                        accent = SettingsSectionAccent.Privacy,
                        title = "Privacy & account",
                        subtitle = "Data, permissions, and your choices",
                        theme = theme,
                        onClick = { onNavigate(SettingsRoute.Privacy) },
                        trailing = {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = PearlColors.heroSecondary(darkTheme),
                            )
                        },
                    )
                }
            }

            item {
                SettingsSectionHeader(title = "About")
                SettingsMenuCard(theme = theme) {
                    Column {
                        AboutCreatorSettingsRow(onClick = { onNavigate(SettingsRoute.AboutCreator) })
                        SettingsMenuDivider()
                        SettingsMenuRow(
                            icon = Icons.Default.Info,
                            accent = SettingsSectionAccent.About,
                            title = "Version",
                            subtitle = "Med Pearls",
                            theme = theme,
                            trailing = {
                                Text(
                                    text = "1.0.0",
                                    color = PearlColors.heroSecondary(darkTheme),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            },
                        )
                        Text(
                            text = "Support: ${SupabaseConfig.SUPPORT_EMAIL}",
                            color = PearlColors.heroSecondary(darkTheme),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
            }

            item {
                SettingsMenuCard(theme = theme) {
                    SettingsMenuRow(
                        icon = Icons.Default.Policy,
                        accent = SettingsSectionAccent.Governance,
                        title = "Information governance",
                        subtitle = "Avoid patient-identifiable details in pearls, notes, or attachments.",
                        theme = theme,
                    )
                }
            }

            settingsState.statusMessage?.let { message ->
                item {
                    Text(text = message, color = theme.primary)
                }
            }
            settingsState.errorMessage?.let { message ->
                item {
                    Text(text = message, color = MaterialTheme.colorScheme.error)
                }
            }

            item {
                Spacer(Modifier.height(PearlLayout.tabBarOverlayInset))
            }
        }
    }
}

@Composable
private fun BackupRestoreScreen(
    settingsState: SettingsUiState,
    onBack: () -> Unit,
    onLoad: () -> Unit,
    onCreate: (onCreated: ((BackupRepository.BackupFileInfo) -> Unit)?) -> Unit,
    onPrepareRestore: (String) -> Unit,
    onPrepareImport: (Uri) -> Unit,
    onConfirmRestoreMerge: () -> Unit,
    onConfirmRestoreReplace: () -> Unit,
    onCancelPendingRestore: () -> Unit,
    onSaveToUri: (String, Uri) -> Unit,
) {
    val theme = TabTheme.Settings
    val darkTheme = isPearlDarkTheme()
    val context = LocalContext.current
    var pendingSavePath by remember { mutableStateOf<String?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) onPrepareImport(uri)
    }

    val saveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(BackupFormat.MIME_TYPE),
    ) { uri ->
        val sourcePath = pendingSavePath
        if (uri != null && sourcePath != null) {
            onSaveToUri(sourcePath, uri)
        }
        pendingSavePath = null
    }

    LaunchedEffect(Unit) { onLoad() }

    fun shareBackup(path: String, fileName: String) {
        val file = java.io.File(path)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = BackupFormat.MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export backup"))
    }

    settingsState.pendingRestore?.let { pending ->
        RestoreModeDialog(
            pending = pending,
            isBusy = settingsState.isBackupBusy,
            onDismiss = onCancelPendingRestore,
            onMerge = onConfirmRestoreMerge,
            onReplace = onConfirmRestoreReplace,
        )
    }

    SettingsSubScreenShell(
        title = "Backup & restore",
        subtitle = "Move your library to another phone",
        onBack = onBack,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            BackupInstructionCard(
                title = "Why use this?",
                body = "Automatic phone backup is turned off to protect clinical content. " +
                    "Use a backup file when you change phones or want a copy you control.",
                darkTheme = darkTheme,
            )

            BackupInstructionCard(
                title = "Move to another phone",
                body = "1. On this phone: tap Back Up Now.\n" +
                    "2. Tap Export / Share and send the file to Drive, email, or Files.\n" +
                    "3. On the new phone: install Med Pearls, open Settings → Backup & restore.\n" +
                    "4. Tap Import backup file and choose the file you saved.",
                darkTheme = darkTheme,
            )

            BackupInstructionCard(
                title = "What is included",
                body = "Pearls, folders, favourites, clinical case text, attachments (photos, videos, documents), " +
                    "and folder organisation. Sign in on the new phone to get public feed and messages back from the server.",
                darkTheme = darkTheme,
            )

            Text(
                text = "Back up",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = PearlColors.heroPrimary(darkTheme),
            )

            Button(
                onClick = { onCreate(null) },
                enabled = !settingsState.isBackupBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (settingsState.isBackupBusy) {
                    CircularProgressIndicator(strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Back Up Now")
                }
            }

            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf(BackupFormat.MIME_TYPE, "application/*")) },
                enabled = !settingsState.isBackupBusy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Import backup file…")
            }

            settingsState.statusMessage?.let { message ->
                Text(text = message, color = theme.primary, style = MaterialTheme.typography.bodySmall)
            }
            settingsState.errorMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            HorizontalDivider(color = PearlColors.heroSecondary(darkTheme).copy(alpha = 0.2f))

            Text(
                text = "Backups on this device",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = PearlColors.heroPrimary(darkTheme),
            )
            Text(
                text = if (settingsState.backups.isEmpty()) {
                    "No backups on this phone yet. Create one, then export it before switching devices."
                } else {
                    "${settingsState.backups.size} backup(s) saved on this device"
                },
                color = PearlColors.heroSecondary(darkTheme),
                style = MaterialTheme.typography.bodySmall,
            )

            if (settingsState.backups.isEmpty()) {
                GlassSurface(cornerRadius = PearlLayout.cardCornerRadius) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = theme.primary)
                        Text(
                            "Backups stay inside the app until you export them.",
                            color = PearlColors.heroSecondary(darkTheme),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            } else {
                settingsState.backups.forEach { backup ->
                    GlassSurface(cornerRadius = PearlLayout.cardCornerRadius) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                backup.id,
                                fontWeight = FontWeight.SemiBold,
                                color = PearlColors.heroPrimary(darkTheme),
                            )
                            Text(
                                "${backup.pearlCount} pearls · ${backup.folderCount} folders · ${backup.mediaCount} attachments · ${formatBytes(backup.fileSizeBytes)}",
                                color = PearlColors.heroSecondary(darkTheme),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { shareBackup(backup.path, backup.id) },
                                    enabled = !settingsState.isBackupBusy,
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text("Export", modifier = Modifier.padding(start = 6.dp))
                                }
                                OutlinedButton(
                                    onClick = {
                                        pendingSavePath = backup.path
                                        saveLauncher.launch(backup.id)
                                    },
                                    enabled = !settingsState.isBackupBusy,
                                ) {
                                    Text("Save to Files")
                                }
                            }
                            OutlinedButton(
                                onClick = { onPrepareRestore(backup.path) },
                                enabled = !settingsState.isBackupBusy,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Restore on this device")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BackupInstructionCard(
    title: String,
    body: String,
    darkTheme: Boolean,
) {
    GlassSurface(cornerRadius = PearlLayout.cardCornerRadius) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = PearlColors.heroPrimary(darkTheme),
            )
            Text(
                text = body,
                color = PearlColors.heroSecondary(darkTheme),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun DeviceCacheScreen(
    settingsState: SettingsUiState,
    onBack: () -> Unit,
    onMeasure: () -> Unit,
    onClear: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()

    LaunchedEffect(Unit) { onMeasure() }

    SettingsSubScreenShell(title = "Device cache", subtitle = "Storage on this device", onBack = onBack) {
        settingsState.cacheBreakdown?.let { breakdown ->
            CacheRow("Pearl media", breakdown.pearlMediaBytes, darkTheme)
            CacheRow("App cache", breakdown.cacheDirBytes, darkTheme)
            CacheRow("Total", breakdown.totalBytes, darkTheme)
        } ?: run {
            if (settingsState.isCacheBusy) {
                CircularProgressIndicator()
            }
        }

        Button(onClick = onClear, enabled = !settingsState.isCacheBusy, modifier = Modifier.fillMaxWidth()) {
            Text("Clear cache")
        }
    }
}

@Composable
private fun CacheRow(label: String, bytes: Long, darkTheme: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = PearlColors.heroPrimary(darkTheme))
        Text(
            text = formatBytes(bytes),
            color = PearlColors.heroSecondary(darkTheme),
        )
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format("%.1f MB", mb)
    return String.format("%.1f GB", mb / 1024.0)
}

@Composable
private fun PrivacySettingsScreen(
    settingsState: SettingsUiState,
    onBack: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()

    SettingsSubScreenShell(title = "Privacy", subtitle = "Your data", onBack = onBack) {
        Text(
            text = "Your pearls are stored locally on this device and optionally synced via your account. " +
                "Community submissions are reviewed before appearing in the Public Feed.",
            color = PearlColors.heroSecondary(darkTheme),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Deleting your account permanently removes your profile and community data from the server.",
            color = PearlColors.heroSecondary(darkTheme),
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedButton(
            onClick = onDeleteAccount,
            enabled = !settingsState.isDeletingAccount,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (settingsState.isDeletingAccount) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            } else {
                Text("Delete account")
            }
        }
    }
}

@Composable
private fun SettingsSubScreenShell(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    val theme = TabTheme.Settings
    val darkTheme = isPearlDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PearlColors.heroPrimary(darkTheme))
            }
        }

        TabScreenHeader(
            title = title,
            subtitle = subtitle,
            theme = theme,
            showsSettingsButton = false,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(PearlLayout.screenHorizontalPadding)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }

        Spacer(Modifier.height(PearlLayout.tabBarOverlayInset))
    }
}

@Composable
private fun SettingsAccountSection(
    accountState: AccountUiState,
    theme: TabTheme,
    onOpenProfile: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    Column {
        SettingsSectionHeader(title = "Account")
        SettingsMenuCard(theme = theme) {
            if (accountState.isSignedIn) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onOpenProfile)
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SettingsProfileAvatar(
                            url = accountState.userProfile?.avatarUrl,
                            displayName = accountState.profileDisplayName(),
                            theme = theme,
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = accountState.profileDisplayName(),
                                color = PearlColors.heroPrimary(darkTheme),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 2,
                            )
                            accountState.profileSubtitle()?.let { subtitle ->
                                Text(
                                    text = subtitle,
                                    color = PearlColors.heroSecondary(darkTheme),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                )
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = theme.primary,
                                    modifier = Modifier.height(14.dp),
                                )
                                Text(
                                    text = "Signed in as ${accountState.userEmail.orEmpty()}",
                                    color = PearlColors.heroSecondary(darkTheme),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = PearlColors.heroSecondary(darkTheme),
                        )
                    }
                    HorizontalDivider(color = PearlColors.divider(darkTheme))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onSignOut)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Sign Out",
                            color = Color(0xFFFF7373),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(theme.primary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = theme.primary)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Not signed in",
                                color = PearlColors.heroPrimary(darkTheme),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                text = "Sign in to share pearls, message colleagues, and sync your public profile.",
                                color = PearlColors.heroSecondary(darkTheme),
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.linearGradient(listOf(theme.primary, theme.secondary)),
                            )
                            .clickable(onClick = onSignIn)
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Sign In / Create Account",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}
