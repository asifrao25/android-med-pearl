package com.knowledgepearls.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.data.model.UserProfile
import com.knowledgepearls.app.data.model.normalizeUserId
import com.knowledgepearls.app.data.repository.MessagingRepository
import com.knowledgepearls.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val profile: UserProfile? = null,
    val pearls: List<PublicPearl> = emptyList(),
    val pearlCount: Int = 0,
    val totalLikesReceived: Int = 0,
    val isOpeningMessage: Boolean = false,
    val messageError: String? = null,
    val isBlocked: Boolean = false,
)

data class ProfileMessageTarget(
    val conversationId: String,
    val otherUserId: String,
    val otherDisplayName: String,
    val otherAvatarUrl: String?,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val messagingRepository: MessagingRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            val normalizedId = normalizeUserId(userId)
            _uiState.value = ProfileUiState(isLoading = true, isBlocked = profileRepository.isUserBlocked(normalizedId))
            runCatching {
                val profile = profileRepository.fetchProfile(normalizedId)
                    ?: error("This profile is no longer available.")

                val pearls = runCatching {
                    profileRepository.fetchApprovedPearls(profile.id)
                }.getOrDefault(emptyList())

                val countFromDb = runCatching {
                    profileRepository.fetchApprovedPearlCount(profile.id)
                }.getOrDefault(0)
                val pearlCount = maxOf(pearls.size, countFromDb)

                val likesFromRpc = runCatching {
                    profileRepository.fetchTotalLikesReceived(profile.id)
                }.getOrDefault(0)
                val totalLikes = maxOf(likesFromRpc, pearls.sumOf { it.likeCount })

                _uiState.update {
                    ProfileUiState(
                        isLoading = false,
                        profile = profile,
                        pearls = pearls,
                        pearlCount = pearlCount,
                        totalLikesReceived = totalLikes,
                        isBlocked = profileRepository.isUserBlocked(profile.id),
                    )
                }
            }.onFailure {
                _uiState.update {
                    ProfileUiState(
                        isLoading = false,
                        errorMessage = "Couldn't load profile. Check your connection.",
                        isBlocked = profileRepository.isUserBlocked(normalizedId),
                    )
                }
            }
        }
    }

    fun openMessage(
        profile: UserProfile,
        onSuccess: (ProfileMessageTarget) -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOpeningMessage = true, messageError = null) }
            runCatching {
                val conversationId = messagingRepository.getOrCreateConversation(profile.id)
                onSuccess(
                    ProfileMessageTarget(
                        conversationId = conversationId,
                        otherUserId = profile.id,
                        otherDisplayName = profile.displayName,
                        otherAvatarUrl = profile.avatarUrl,
                    ),
                )
            }.onFailure { error ->
                val message = when {
                    error.message?.contains("accept messages", ignoreCase = true) == true ||
                        error.message?.contains("blocked messages", ignoreCase = true) == true ->
                        "This user has blocked messages."
                    else -> error.message ?: "Couldn't start conversation."
                }
                _uiState.update { it.copy(messageError = message) }
            }
            _uiState.update { it.copy(isOpeningMessage = false) }
        }
    }

    fun blockUser(userId: String) {
        profileRepository.blockUser(userId)
        _uiState.update { it.copy(isBlocked = true) }
    }

    fun clearMessageError() {
        _uiState.update { it.copy(messageError = null) }
    }
}
