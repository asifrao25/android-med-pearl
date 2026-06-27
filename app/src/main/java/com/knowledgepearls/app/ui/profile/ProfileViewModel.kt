package com.knowledgepearls.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.data.model.UserProfile
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
    val totalLikesReceived: Int = 0,
) {
    val pearlCount: Int get() = pearls.size
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val profile = profileRepository.fetchProfile(userId)
                    ?: error("This profile is no longer available.")
                val pearls = profileRepository.fetchApprovedPearls(userId)
                val likes = profileRepository.fetchTotalLikesReceived(userId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        profile = profile,
                        pearls = pearls,
                        totalLikesReceived = likes,
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Couldn't load profile. Check your connection.",
                    )
                }
            }
        }
    }
}
