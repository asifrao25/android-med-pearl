package com.knowledgepearls.app.ui.feed

import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.local.model.decodedPublicPearl
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.data.model.UserProfile

data class FeedAuthorContext(
    val userId: String?,
    val userEmail: String?,
    val userProfile: UserProfile?,
)

data class FeedPearlAuthorInfo(
    val displayName: String,
    val avatarUrl: String?,
    val userId: String?,
) {
    companion object {
        fun resolve(
            pearl: PearlWithMedia,
            account: FeedAuthorContext,
            publicPearl: PublicPearl? = pearl.pearl.decodedPublicPearl(),
        ): FeedPearlAuthorInfo {
            val entity = pearl.pearl

            // Preserve original public-feed creator across saves and friend shares.
            if (publicPearl != null && publicPearl.safeDisplayName.isNotBlank()) {
                return FeedPearlAuthorInfo(
                    displayName = publicPearl.safeDisplayName,
                    avatarUrl = null,
                    userId = publicPearl.userId,
                )
            }

            if (entity.isSharedFromFriend && entity.sharedByName.isNotBlank()) {
                return FeedPearlAuthorInfo(
                    displayName = entity.sharedByName,
                    avatarUrl = entity.sharedByAvatarURL,
                    userId = entity.sharedByUserID,
                )
            }

            return FeedPearlAuthorInfo(
                displayName = currentUserDisplayName(account),
                avatarUrl = account.userProfile?.avatarUrl,
                userId = account.userId,
            )
        }

        fun fromPublicPearl(pearl: PublicPearl): FeedPearlAuthorInfo =
            FeedPearlAuthorInfo(
                displayName = pearl.safeDisplayName.ifBlank { "Unknown" },
                avatarUrl = null,
                userId = pearl.userId,
            )

        private fun currentUserDisplayName(account: FeedAuthorContext): String {
            account.userProfile?.name?.takeIf { it.isNotBlank() }?.let { return it }
            account.userEmail?.takeIf { it.isNotBlank() }?.let { email ->
                return email.substringBefore("@")
                    .replace('.', ' ')
                    .split(' ')
                    .joinToString(" ") { word ->
                        word.replaceFirstChar { char ->
                            if (char.isLowerCase()) char.titlecase() else char.toString()
                        }
                    }
                    .ifBlank { email }
            }
            return "You"
        }
    }
}
