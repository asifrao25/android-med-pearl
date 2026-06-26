package com.knowledgepearls.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledgepearls.app.data.auth.GoogleSignInHelper
import com.knowledgepearls.app.data.repository.AccountAuthException
import com.knowledgepearls.app.data.repository.AccountRepository
import com.knowledgepearls.app.data.repository.EmailSignUpOutcome
import com.knowledgepearls.app.data.sync.PearlSyncCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val googleSignInHelper: GoogleSignInHelper,
    private val pearlSyncCoordinator: PearlSyncCoordinator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    fun restoreSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val signedIn = accountRepository.restoreSession()
                if (!signedIn) {
                    _uiState.value = AccountUiState(isLoading = false)
                    return@launch
                }
                refreshAuthenticatedState(triggerSync = true)
            }.onFailure {
                _uiState.value = AccountUiState(isLoading = false)
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, pendingVerificationEmail = null) }
            runCatching {
                accountRepository.signIn(email, password)
                refreshAuthenticatedState(triggerSync = true)
                _uiState.update { it.copy(showSignInSuccess = true) }
            }.onFailure { error ->
                when (error) {
                    is AccountAuthException.EmailVerificationRequired -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                pendingVerificationEmail = error.email,
                                errorMessage = error.message,
                            )
                        }
                    }
                    else -> {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = error.message ?: "Sign in failed.")
                        }
                    }
                }
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, pendingVerificationEmail = null) }
            runCatching {
                when (accountRepository.signUp(email, password)) {
                    EmailSignUpOutcome.SignedIn -> {
                        refreshAuthenticatedState(triggerSync = true)
                        _uiState.update { it.copy(showSignInSuccess = true) }
                    }
                    EmailSignUpOutcome.EmailVerificationRequired -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                pendingVerificationEmail = email.trim().lowercase(),
                            )
                        }
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Sign up failed.")
                }
            }
        }
    }

    fun verifySignupCode(email: String, code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                accountRepository.verifySignupEmailCode(email, code)
                refreshAuthenticatedState(triggerSync = true)
                _uiState.update {
                    it.copy(
                        pendingVerificationEmail = null,
                        showSignInSuccess = true,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Verification failed.")
                }
            }
        }
    }

    fun resendVerificationCode(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                accountRepository.resendSignupVerificationCode(email)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "Could not resend code.")
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun signInWithGoogle(useOAuthFallback: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                if (!useOAuthFallback && googleSignInHelper.isConfigured) {
                    val idToken = googleSignInHelper.getGoogleIdToken()
                    if (idToken != null) {
                        accountRepository.signInWithGoogleIdToken(idToken)
                    } else {
                        accountRepository.signInWithGoogleOAuth()
                    }
                } else {
                    accountRepository.signInWithGoogleOAuth()
                }
                refreshAuthenticatedState(triggerSync = true)
                _uiState.update { it.copy(showSignInSuccess = true) }
            }.onFailure { error ->
                val message = error.message.orEmpty()
                if (message.contains("cancel", ignoreCase = true)) {
                    _uiState.update { it.copy(isLoading = false) }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = message.ifBlank { "Google sign-in failed." })
                    }
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { accountRepository.signOut() }
            _uiState.value = AccountUiState(isLoading = false)
        }
    }

    fun clearSignInSuccess() {
        _uiState.update { it.copy(showSignInSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun createProfile(
        name: String,
        bio: String,
        deanery: String,
        specialty: String,
        grade: String,
        allowMessages: Boolean,
        showEmail: Boolean,
        publicEmail: String,
        allowPearlShares: Boolean,
        notifyPearlSharesEmail: Boolean,
        avatarUrl: String?,
    ) {
        val userId = _uiState.value.userId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                accountRepository.createProfile(
                    userId = userId,
                    name = name,
                    bio = bio,
                    deanery = deanery,
                    specialty = specialty,
                    grade = grade,
                    allowMessages = allowMessages,
                    showEmail = showEmail,
                    publicEmail = publicEmail,
                    allowPearlShares = allowPearlShares,
                    notifyPearlSharesEmail = notifyPearlSharesEmail,
                    avatarUrl = avatarUrl,
                )
                refreshAuthenticatedState(triggerSync = false)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Could not save profile.")
                }
            }
        }
    }

    fun updateProfile(
        name: String,
        bio: String,
        deanery: String,
        specialty: String,
        grade: String,
        allowMessages: Boolean,
        showEmail: Boolean,
        publicEmail: String,
        allowPearlShares: Boolean,
        notifyPearlSharesEmail: Boolean,
        avatarUrl: String?,
    ) {
        val userId = _uiState.value.userId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                accountRepository.updateProfile(
                    userId = userId,
                    name = name,
                    bio = bio,
                    deanery = deanery,
                    specialty = specialty,
                    grade = grade,
                    allowMessages = allowMessages,
                    showEmail = showEmail,
                    publicEmail = publicEmail,
                    allowPearlShares = allowPearlShares,
                    notifyPearlSharesEmail = notifyPearlSharesEmail,
                    avatarUrl = avatarUrl,
                )
                refreshAuthenticatedState(triggerSync = false)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Could not update profile.")
                }
            }
        }
    }

    fun uploadAvatar(jpegBytes: ByteArray, onComplete: (String) -> Unit) {
        val userId = _uiState.value.userId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val url = accountRepository.uploadAvatar(userId, jpegBytes)
                if (_uiState.value.userProfile != null) {
                    accountRepository.patchAvatarUrl(userId, url)
                    refreshAuthenticatedState(triggerSync = false)
                }
                onComplete(url)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Avatar upload failed.")
                }
            }
        }
    }

    fun runForegroundSync() {
        viewModelScope.launch {
            pearlSyncCoordinator.runIfAuthenticated(_uiState.value.userId)
        }
    }

    private suspend fun refreshAuthenticatedState(triggerSync: Boolean) {
        val userId = accountRepository.currentUserId()
        var profile: com.knowledgepearls.app.data.model.UserProfile? = null
        var fetchFailed = false
        if (userId != null) {
            val result = runCatching { accountRepository.fetchProfile(userId) }
            profile = result.getOrNull()
            fetchFailed = result.isFailure
        }

        _uiState.update {
            it.copy(
                userId = userId,
                userEmail = accountRepository.currentUserEmail(),
                userProfile = profile,
                isLoading = false,
                hasFetchedProfile = userId != null,
                profileFetchFailed = fetchFailed,
                errorMessage = null,
            )
        }
        if (triggerSync) {
            pearlSyncCoordinator.runIfAuthenticated(userId)
        }
    }
}
