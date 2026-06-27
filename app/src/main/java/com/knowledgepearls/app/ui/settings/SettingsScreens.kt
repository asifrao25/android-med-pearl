package com.knowledgepearls.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.remote.SupabaseConfig
import com.knowledgepearls.app.ui.account.AccountUiState
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.components.TabScreenHeader
import com.knowledgepearls.app.ui.publicfeed.PendingSubmissionsScreen
import com.knowledgepearls.app.ui.theme.AppearanceMode
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

@Composable
fun SettingsScreen(
    visible: Boolean,
    route: SettingsRoute,
    accountState: AccountUiState,
    settingsState: SettingsUiState,
    appearanceMode: AppearanceMode,
    onDismiss: () -> Unit,
    onNavigate: (SettingsRoute) -> Unit,
    onSignIn: () -> Unit,
    onEditProfile: () -> Unit,
    onSignOut: () -> Unit,
    onLoadPending: () -> Unit,
    onWithdrawSubmission: (com.knowledgepearls.app.data.model.PublicPearl) -> Unit,
    onSetAppearance: (AppearanceMode) -> Unit,
    onLoadBackups: () -> Unit,
    onCreateBackup: () -> Unit,
    onRestoreBackup: (String) -> Unit,
    onMeasureCache: () -> Unit,
    onClearCache: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    if (!visible) return

    when (route) {
        SettingsRoute.Main -> SettingsMainScreen(
            accountState = accountState,
            settingsState = settingsState,
            appearanceMode = appearanceMode,
            onDismiss = onDismiss,
            onNavigate = onNavigate,
            onSignIn = onSignIn,
            onEditProfile = onEditProfile,
            onSignOut = onSignOut,
            onSetAppearance = onSetAppearance,
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
            )
        }
        SettingsRoute.BackupRestore -> BackupRestoreScreen(
            settingsState = settingsState,
            onBack = { onNavigate(SettingsRoute.Main) },
            onLoad = onLoadBackups,
            onCreate = onCreateBackup,
            onRestore = onRestoreBackup,
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
        )
    }
}

@Composable
private fun SettingsMainScreen(
    accountState: AccountUiState,
    settingsState: SettingsUiState,
    appearanceMode: AppearanceMode,
    onDismiss: () -> Unit,
    onNavigate: (SettingsRoute) -> Unit,
    onSignIn: () -> Unit,
    onEditProfile: () -> Unit,
    onSignOut: () -> Unit,
    onSetAppearance: (AppearanceMode) -> Unit,
) {
    val theme = TabTheme.Settings
    val darkTheme = isPearlDarkTheme()

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = theme)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    SettingsSection(title = "Account") {
                        if (accountState.isSignedIn) {
                            Text(
                                text = accountState.userProfile?.name?.ifBlank { accountState.userEmail }
                                    ?: accountState.userEmail.orEmpty(),
                                color = PearlColors.heroSecondary(darkTheme),
                            )
                            SettingsActionButton("Edit profile", onEditProfile)
                            SettingsActionButton("Sign out", onSignOut, outlined = true)
                        } else {
                            Text(
                                text = "Sign in to sync public pearls and use community features.",
                                color = PearlColors.heroSecondary(darkTheme),
                            )
                            SettingsActionButton("Sign in", onClick = {
                                onDismiss()
                                onSignIn()
                            })
                        }
                    }
                }

                item {
                    SettingsSection(title = "Community") {
                        SettingsNavRow(
                            label = "Pending submissions",
                            detail = if (settingsState.pendingCount > 0) "${settingsState.pendingCount} pending" else null,
                            onClick = { onNavigate(SettingsRoute.PendingSubmissions) },
                        )
                    }
                }

                item {
                    SettingsSection(title = "Appearance") {
                        AppearanceMode.entries.forEach { mode ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSetAppearance(mode) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = appearanceMode == mode,
                                    onClick = { onSetAppearance(mode) },
                                )
                                Text(
                                    text = when (mode) {
                                        AppearanceMode.System -> "System"
                                        AppearanceMode.Light -> "Light"
                                        AppearanceMode.Dark -> "Dark"
                                    },
                                    color = PearlColors.heroPrimary(darkTheme),
                                )
                            }
                        }
                    }
                }

                item {
                    SettingsSection(title = "Data & Sync") {
                        SettingsNavRow("Backup & restore", onClick = { onNavigate(SettingsRoute.BackupRestore) })
                        SettingsNavRow("Device cache", onClick = { onNavigate(SettingsRoute.DeviceCache) })
                    }
                }

                item {
                    SettingsSection(title = "Privacy") {
                        SettingsNavRow("Privacy & account", onClick = { onNavigate(SettingsRoute.Privacy) })
                    }
                }

                item {
                    SettingsSection(title = "About") {
                        SettingsNavRow("About the creator", onClick = { onNavigate(SettingsRoute.AboutCreator) })
                        Text(
                            text = "Med Pearls v1.0.0",
                            color = PearlColors.heroSecondary(darkTheme),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            text = "Support: ${SupabaseConfig.SUPPORT_EMAIL}",
                            color = PearlColors.heroSecondary(darkTheme),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                item {
                    SettingsSection(title = "Governance notice") {
                        Text(
                            text = "Community pearls are moderated before publication. " +
                                "Do not share identifiable patient information. " +
                                "Content must comply with your local professional guidance.",
                            color = PearlColors.heroSecondary(darkTheme),
                            style = MaterialTheme.typography.bodyMedium,
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
            }
        }
    }
}

@Composable
private fun BackupRestoreScreen(
    settingsState: SettingsUiState,
    onBack: () -> Unit,
    onLoad: () -> Unit,
    onCreate: () -> Unit,
    onRestore: (String) -> Unit,
) {
    val theme = TabTheme.Settings
    val darkTheme = isPearlDarkTheme()

    LaunchedEffect(Unit) { onLoad() }

    SettingsSubScreenShell(title = "Backup & restore", subtitle = "Local JSON export", onBack = onBack) {
        Button(onClick = onCreate, enabled = !settingsState.isBackupBusy, modifier = Modifier.fillMaxWidth()) {
            if (settingsState.isBackupBusy) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            } else {
                Text("Create backup")
            }
        }

        if (settingsState.backups.isEmpty()) {
            Text("No backups yet.", color = PearlColors.heroSecondary(darkTheme))
        } else {
            settingsState.backups.forEach { backup ->
                GlassSurface(cornerRadius = PearlLayout.cardCornerRadius) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(backup.id, fontWeight = FontWeight.SemiBold, color = PearlColors.heroPrimary(darkTheme))
                        Text(
                            "${backup.pearlCount} pearls · ${backup.folderCount} folders",
                            color = PearlColors.heroSecondary(darkTheme),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        OutlinedButton(onClick = { onRestore(backup.path) }, enabled = !settingsState.isBackupBusy) {
                            Text("Restore")
                        }
                    }
                }
            }
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
private fun AboutCreatorScreen(onBack: () -> Unit) {
    val darkTheme = isPearlDarkTheme()

    SettingsSubScreenShell(title = "About the creator", subtitle = "Med Pearls", onBack = onBack) {
        Text(
            text = "Med Pearls helps clinicians capture, organise, and share learning pearls — " +
                "from quick notes and links to structured clinical cases.",
            color = PearlColors.heroPrimary(darkTheme),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "Built for cross-platform use with a shared Supabase backend so your community " +
                "pearls and friend shares work across iOS and Android.",
            color = PearlColors.heroSecondary(darkTheme),
            style = MaterialTheme.typography.bodyMedium,
        )
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

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = theme, intensity = 0.55f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
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
                    .verticalScroll(rememberScrollState())
                    .padding(PearlLayout.screenHorizontalPadding)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    GlassSurface(cornerRadius = PearlLayout.cardCornerRadius) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = PearlColors.heroPrimary(darkTheme),
                style = MaterialTheme.typography.titleMedium,
            )
            content()
        }
    }
}

@Composable
private fun SettingsNavRow(
    label: String,
    detail: String? = null,
    onClick: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = PearlColors.heroPrimary(darkTheme))
        Row(verticalAlignment = Alignment.CenterVertically) {
            detail?.let {
                Text(it, color = PearlColors.heroSecondary(darkTheme), style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = PearlColors.heroSecondary(darkTheme))
        }
    }
}

@Composable
private fun SettingsActionButton(
    label: String,
    onClick: () -> Unit,
    outlined: Boolean = false,
) {
    if (outlined) {
        OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(label)
        }
    } else {
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(label)
        }
    }
}
