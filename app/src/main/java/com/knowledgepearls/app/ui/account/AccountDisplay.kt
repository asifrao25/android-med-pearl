package com.knowledgepearls.app.ui.account

fun AccountUiState.profileDisplayName(): String {
    userProfile?.name?.takeIf { it.isNotBlank() }?.let { return it }
    userEmail?.substringBefore("@")?.let { local ->
        return local.split(".").joinToString(" ") { part ->
            part.replaceFirstChar { char -> char.uppercaseChar() }
        }
    }
    return "Your Account"
}

fun AccountUiState.profileSubtitle(): String? {
    val profile = userProfile ?: return null
    return listOfNotNull(
        profile.grade?.takeIf { it.isNotBlank() },
        profile.specialty?.takeIf { it.isNotBlank() },
    ).joinToString(" · ").takeIf { it.isNotBlank() }
}
