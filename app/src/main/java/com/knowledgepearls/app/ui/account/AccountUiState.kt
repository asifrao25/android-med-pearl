package com.knowledgepearls.app.ui.account

import com.knowledgepearls.app.data.model.UserProfile

data class AccountUiState(
    val userId: String? = null,
    val userEmail: String? = null,
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasFetchedProfile: Boolean = false,
    val profileFetchFailed: Boolean = false,
    val pendingVerificationEmail: String? = null,
    val showSignInSuccess: Boolean = false,
) {
    val isSignedIn: Boolean get() = userId != null

    val needsProfileSetup: Boolean
        get() = isSignedIn &&
            hasFetchedProfile &&
            !profileFetchFailed &&
            (userProfile == null || userProfile.name.isNullOrBlank())
}
