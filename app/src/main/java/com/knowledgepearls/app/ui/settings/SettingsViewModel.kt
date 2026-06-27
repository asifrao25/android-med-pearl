package com.knowledgepearls.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledgepearls.app.data.backup.BackupRepository
import com.knowledgepearls.app.data.cache.DeviceCacheRepository
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.data.prefs.AppearancePreferences
import com.knowledgepearls.app.data.repository.AccountRepository
import com.knowledgepearls.app.data.repository.PublicFeedSharingRepository
import com.knowledgepearls.app.ui.theme.AppearanceMode
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
)

data class CacheClearedAlert(
    val bytesFreedLabel: String,
    val effectSummary: String,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val publicFeedSharingRepository: PublicFeedSharingRepository,
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
                publicFeedSharingRepository.withdraw(pearl.id)
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

    fun createBackup() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupBusy = true, errorMessage = null) }
            runCatching {
                backupRepository.createBackup()
                loadBackups()
                _uiState.update { it.copy(statusMessage = "Backup created.") }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Backup failed.") }
            }
            _uiState.update { it.copy(isBackupBusy = false) }
        }
    }

    fun restoreBackup(path: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupBusy = true, errorMessage = null) }
            runCatching {
                val count = backupRepository.restoreBackup(path)
                _uiState.update { it.copy(statusMessage = "Restored $count pearls.") }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message ?: "Restore failed.") }
            }
            _uiState.update { it.copy(isBackupBusy = false) }
        }
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
