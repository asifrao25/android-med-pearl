package com.knowledgepearls.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.knowledgepearls.app.data.backup.BackupRepository
import com.knowledgepearls.app.data.backup.BackupRestoreSummary
import com.knowledgepearls.app.data.backup.RestoreMode
import com.knowledgepearls.app.data.cache.DeviceCacheRepository
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.data.prefs.AppearancePreferences
import com.knowledgepearls.app.data.repository.AccountRepository
import com.knowledgepearls.app.data.repository.PublicFeedSharingRepository
import com.knowledgepearls.app.data.repository.PublicFeedWithdrawal
import com.knowledgepearls.app.ui.theme.AppearanceMode
import com.knowledgepearls.app.ui.theme.AppFontChoice
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val pendingSubmissions: List<PublicPearl> = emptyList(),
    val pendingCount: Int = 0,
    val isLoadingPending: Boolean = false,
    val withdrawingId: String? = null,
    val backups: List<BackupRepository.BackupFileInfo> = emptyList(),
    val isBackupBusy: Boolean = false,
    val cacheBreakdown: DeviceCacheRepository.Breakdown? = null,
    val isCacheBusy: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null,
    val isDeletingAccount: Boolean = false,
    val cacheClearedAlert: CacheClearedAlert? = null,
    val pendingRestore: PendingRestoreState? = null,
)

data class CacheClearedAlert(
    val bytesFreedLabel: String,
    val effectSummary: String,
)

data class PendingRestoreState(
    val path: String? = null,
    val uri: Uri? = null,
    val backupCreatedAt: Long,
    val pearlCount: Int,
    val folderCount: Int,
    val mediaCount: Int,
    val mergePreview: com.knowledgepearls.app.data.backup.RestorePreview,
    val replacePreview: com.knowledgepearls.app.data.backup.RestorePreview,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val publicFeedSharingRepository: PublicFeedSharingRepository,
    private val publicFeedWithdrawal: PublicFeedWithdrawal,
    private val backupRepository: BackupRepository,
    private val deviceCacheRepository: DeviceCacheRepository,
    private val appearancePreferences: AppearancePreferences,
) : ViewModel() {
    val appearanceMode: StateFlow<AppearanceMode> =
        appearancePreferences.appearanceMode.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppearanceMode.System,
        )

    val appFontChoice: StateFlow<AppFontChoice> =
        appearancePreferences.appFontChoice.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppFontChoice.Inter,
        )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun loadPendingSubmissions() {
        viewModelScope.launch {
            val userId = accountRepository.currentUserId() ?: return@launch
            _uiState.update { it.copy(isLoadingPending = true, errorMessage = null) }
            runCatching {
                val pending = publicFeedSharingRepository.fetchPendingSubmissions(userId)
                _uiState.update {
                    it.copy(
                        pendingSubmissions = pending,
                        pendingCount = pending.size,
                        isLoadingPending = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoadingPending = false,
                        errorMessage = error.message ?: "Could not load pending submissions.",
                    )
                }
            }
        }
    }

    fun withdrawSubmission(pearl: PublicPearl) {
        viewModelScope.launch {
            _uiState.update { it.copy(withdrawingId = pearl.id, errorMessage = null) }
            runCatching {
                publicFeedWithdrawal.withdrawSubmission(pearl.id)
                loadPendingSubmissions()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.message ?: "Could not withdraw submission.",
                    )
                }
            }
            _uiState.update { it.copy(withdrawingId = null) }
        }
    }

    fun setAppearanceMode(mode: AppearanceMode) {
        viewModelScope.launch {
            appearancePreferences.setAppearanceMode(mode)
        }
    }

    fun setAppFontChoice(choice: AppFontChoice) {
        viewModelScope.launch {
            appearancePreferences.setAppFontChoice(choice)
        }
    }

    fun loadBackups() {
        viewModelScope.launch {
            runCatching {
                val backups = backupRepository.listBackups()
                _uiState.update { it.copy(backups = backups) }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Could not list backups.") }
            }
        }
    }

    fun createBackup(onCreated: ((BackupRepository.BackupFileInfo) -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupBusy = true, errorMessage = null) }
            runCatching {
                val backup = backupRepository.createBackup()
                loadBackups()
                _uiState.update {
                    it.copy(
                        statusMessage = "Backup created with ${backup.pearlCount} pearls and ${backup.mediaCount} attachments. Export it to move to another phone.",
                    )
                }
                onCreated?.invoke(backup)
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Backup failed.") }
            }
            _uiState.update { it.copy(isBackupBusy = false) }
        }
    }

    fun prepareRestoreFromPath(path: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupBusy = true, errorMessage = null) }
            runCatching {
                val payload = backupRepository.loadPayloadFromPath(path)
                presentRestoreChoice(path = path, uri = null, payload = payload)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "Could not read this backup.")
                }
            }
            _uiState.update { it.copy(isBackupBusy = false) }
        }
    }

    fun prepareRestoreFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupBusy = true, errorMessage = null) }
            runCatching {
                val payload = backupRepository.loadPayloadFromUri(uri)
                presentRestoreChoice(path = null, uri = uri, payload = payload)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        errorMessage = error.message ?: "This file isn't a valid Med Pearls backup.",
                    )
                }
            }
            _uiState.update { it.copy(isBackupBusy = false) }
        }
    }

    fun confirmRestoreMerge() {
        val pending = _uiState.value.pendingRestore ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupBusy = true, errorMessage = null) }
            runCatching {
                val payload = loadPendingPayload(pending)
                val summary = backupRepository.merge(payload)
                loadBackups()
                _uiState.update {
                    it.copy(
                        pendingRestore = null,
                        statusMessage = summary.toUserMessage(mode = RestoreMode.Merge),
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Restore failed.") }
            }
            _uiState.update { it.copy(isBackupBusy = false) }
        }
    }

    fun confirmRestoreReplace() {
        val pending = _uiState.value.pendingRestore ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupBusy = true, errorMessage = null) }
            runCatching {
                val payload = loadPendingPayload(pending)
                val summary = backupRepository.replace(payload)
                loadBackups()
                _uiState.update {
                    it.copy(
                        pendingRestore = null,
                        statusMessage = summary.toUserMessage(
                            prefix = "Replace complete.",
                            mode = RestoreMode.Replace,
                        ),
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Replace failed.") }
            }
            _uiState.update { it.copy(isBackupBusy = false) }
        }
    }

    fun cancelPendingRestore() {
        _uiState.update { it.copy(pendingRestore = null) }
    }

    private suspend fun presentRestoreChoice(
        path: String?,
        uri: Uri?,
        payload: com.knowledgepearls.app.data.backup.BackupPayloadV2,
    ) {
        val mergePreview = backupRepository.previewMerge(payload)
        val replacePreview = backupRepository.previewReplace(payload)
        _uiState.update {
            it.copy(
                pendingRestore = PendingRestoreState(
                    path = path,
                    uri = uri,
                    backupCreatedAt = payload.createdAt,
                    pearlCount = payload.pearlCount,
                    folderCount = payload.folderCount,
                    mediaCount = payload.mediaCount,
                    mergePreview = mergePreview,
                    replacePreview = replacePreview,
                ),
            )
        }
    }

    private suspend fun loadPendingPayload(
        pending: PendingRestoreState,
    ): com.knowledgepearls.app.data.backup.BackupPayloadV2 =
        when {
            pending.path != null -> backupRepository.loadPayloadFromPath(pending.path)
            pending.uri != null -> backupRepository.loadPayloadFromUri(pending.uri)
            else -> error("No backup source selected.")
        }

    fun saveBackupToUri(sourcePath: String, destinationUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupBusy = true, errorMessage = null) }
            runCatching {
                backupRepository.copyBackupToUri(sourcePath, destinationUri)
                _uiState.update { it.copy(statusMessage = "Backup saved to your chosen location.") }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Could not save backup.") }
            }
            _uiState.update { it.copy(isBackupBusy = false) }
        }
    }

    private fun BackupRestoreSummary.toUserMessage(
        prefix: String = "Restore complete.",
        mode: RestoreMode? = null,
    ): String {
        val pearlPart = when (mode) {
            RestoreMode.Merge -> when {
                pearlsAdded > 0 && pearlsUpdated > 0 ->
                    "$pearlsAdded pearl(s) added, $pearlsUpdated updated"
                pearlsAdded > 0 -> "$pearlsAdded pearl(s) added"
                pearlsUpdated > 0 -> "$pearlsUpdated pearl(s) updated"
                else -> "No pearl changes"
            }
            RestoreMode.Replace -> "$pearlsAdded pearl(s) loaded"
            null -> "$pearlsRestored pearl(s)"
        }
        val folderPart = when {
            foldersAdded > 0 -> "$foldersAdded folder(s)"
            else -> "0 folders"
        }
        val mediaNote = when {
            mediaSkipped > 0 -> " $mediaSkipped attachment(s) could not be restored."
            else -> ""
        }
        return "$prefix $pearlPart, $folderPart, and $mediaRestored attachment(s).$mediaNote"
    }

    fun measureCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCacheBusy = true) }
            runCatching {
                val breakdown = deviceCacheRepository.measure()
                _uiState.update { it.copy(cacheBreakdown = breakdown) }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Could not measure cache.") }
            }
            _uiState.update { it.copy(isCacheBusy = false) }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCacheBusy = true, errorMessage = null) }
            runCatching {
                val cleared = deviceCacheRepository.clearCache()
                measureCache()
                _uiState.update {
                    it.copy(
                        cacheClearedAlert = CacheClearedAlert(
                            bytesFreedLabel = deviceCacheRepository.formattedBytes(cleared),
                            effectSummary = "Thumbnails, temporary files, and downloaded previews were removed.",
                        ),
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Could not clear cache.") }
            }
            _uiState.update { it.copy(isCacheBusy = false) }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingAccount = true, errorMessage = null) }
            runCatching {
                accountRepository.deleteAccount()
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isDeletingAccount = false,
                        errorMessage = error.message ?: "Account deletion failed.",
                    )
                }
            }
        }
    }

    fun dismissMessages() {
        _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
    }

    fun dismissCacheClearedAlert() {
        _uiState.update { it.copy(cacheClearedAlert = null) }
    }
}
