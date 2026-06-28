package com.knowledgepearls.app.push

object PushNotificationType {
    const val MESSAGE = "message"
    const val PEARL_SHARE = "pearl_share"
    const val NEW_PUBLIC_PEARL = "new_public_pearl"
    const val PEARL_APPROVED = "pearl_approved"
    const val PEARL_LIKED = "pearl_liked"
}

data class PushNotificationPayload(
    val type: String,
    val title: String,
    val body: String,
    val conversationId: String? = null,
    val pearlShareId: String? = null,
    val pearlId: String? = null,
) {
    val channelId: String
        get() = when (type) {
            PushNotificationType.MESSAGE -> PushNotificationChannels.MESSAGES
            PushNotificationType.PEARL_SHARE -> PushNotificationChannels.SHARES
            PushNotificationType.NEW_PUBLIC_PEARL,
            PushNotificationType.PEARL_APPROVED,
            -> PushNotificationChannels.FEED
            PushNotificationType.PEARL_LIKED -> PushNotificationChannels.SOCIAL
            else -> PushNotificationChannels.DEFAULT
        }

    companion object {
        fun fromRemoteData(data: Map<String, String>): PushNotificationPayload {
            val type = data["notification_type"].orEmpty()
            return PushNotificationPayload(
                type = type,
                title = data["title"].orEmpty(),
                body = data["body"].orEmpty(),
                conversationId = data["conversation_id"],
                pearlShareId = data["pearl_share_id"],
                pearlId = data["pearl_id"],
            )
        }
    }
}

object PushNotificationChannels {
    const val DEFAULT = "medpearls_default"
    const val MESSAGES = "medpearls_messages"
    const val SHARES = "medpearls_shares"
    const val FEED = "medpearls_feed"
    const val SOCIAL = "medpearls_social"
}
