package com.knowledgepearls.app.navigation

import android.content.Intent
import android.net.Uri
import com.knowledgepearls.app.push.PushNotificationType

sealed interface AppNavigationEvent {
    data object OpenInbox : AppNavigationEvent
    data object OpenSharedPearls : AppNavigationEvent
    data object OpenPublicFeed : AppNavigationEvent
    data object OpenPendingSubmissions : AppNavigationEvent
    data class OpenPearlShare(val shareId: String) : AppNavigationEvent
    data class OpenConversation(val conversationId: String) : AppNavigationEvent
    data class OpenPublicPearl(val pearlId: String) : AppNavigationEvent
    data class ImportShare(val text: String?, val url: String?) : AppNavigationEvent
    data class PearlShareReceivedToast(val senderName: String, val pearlTitle: String) : AppNavigationEvent
    data object RefreshInboxBadge : AppNavigationEvent
}

data class ShareImportPayload(
    val text: String?,
    val url: String?,
)

object DeepLinkRouter {
    const val SCHEME = "com.knowledgepearls.app"

    const val EXTRA_NOTIFICATION_TYPE = "notification_type"
    const val EXTRA_NOTIFICATION_TITLE = "notification_title"
    const val EXTRA_NOTIFICATION_BODY = "notification_body"
    const val EXTRA_PEARL_ID = "pearl_id"
    const val EXTRA_PEARL_SHARE_ID = "pearl_share_id"
    const val EXTRA_CONVERSATION_ID = "conversation_id"
    const val EXTRA_SHARE_TEXT = "share_text"
    const val EXTRA_SHARE_URL = "share_url"

    fun parse(uri: Uri): AppNavigationEvent? {
        if (uri.scheme != SCHEME) return null
        val host = uri.host.orEmpty()
        val segments = uri.pathSegments.filter { it.isNotBlank() }

        return when {
            host == "login-callback" -> null
            host == "inbox" && segments.isEmpty() -> AppNavigationEvent.OpenInbox
            host == "inbox" && segments == listOf("shared-pearls") -> AppNavigationEvent.OpenSharedPearls
            host == "inbox" && segments.size >= 2 && segments[0] == "shared-pearls" ->
                AppNavigationEvent.OpenPearlShare(segments[1])
            host == "inbox" && segments.size >= 2 && segments[0] == "messages" ->
                AppNavigationEvent.OpenConversation(segments[1])
            host == "public-feed" && segments.isEmpty() -> AppNavigationEvent.OpenPublicFeed
            host == "public-feed" && segments.size >= 2 && segments[0] == "pearl" ->
                AppNavigationEvent.OpenPublicPearl(segments[1])
            host == "settings" && segments == listOf("pending-submissions") ->
                AppNavigationEvent.OpenPendingSubmissions
            else -> null
        }
    }

    fun parse(intent: Intent): AppNavigationEvent? {
        intent.data?.let { uri ->
            parse(uri)?.let { return it }
        }

        parsePushExtras(intent)?.let { return it }

        intent.getStringExtra(EXTRA_PEARL_SHARE_ID)?.let {
            return AppNavigationEvent.OpenPearlShare(it)
        }
        intent.getStringExtra(EXTRA_CONVERSATION_ID)?.let {
            return AppNavigationEvent.OpenConversation(it)
        }
        intent.getStringExtra(EXTRA_PEARL_ID)?.let {
            return AppNavigationEvent.OpenPublicPearl(it)
        }
        intent.getStringExtra(EXTRA_SHARE_URL)?.let { url ->
            return AppNavigationEvent.ImportShare(
                text = intent.getStringExtra(EXTRA_SHARE_TEXT),
                url = url,
            )
        }
        intent.getStringExtra(EXTRA_SHARE_TEXT)?.let { text ->
            return AppNavigationEvent.ImportShare(text = text, url = null)
        }
        return null
    }

    private fun parsePushExtras(intent: Intent): AppNavigationEvent? {
        val type = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE)
            ?: intent.extras?.getString(EXTRA_NOTIFICATION_TYPE)
            ?: return null

        val pearlId = intent.getStringExtra(EXTRA_PEARL_ID)
            ?: intent.extras?.getString(EXTRA_PEARL_ID)
        val conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID)
            ?: intent.extras?.getString(EXTRA_CONVERSATION_ID)
        val shareId = intent.getStringExtra(EXTRA_PEARL_SHARE_ID)
            ?: intent.extras?.getString(EXTRA_PEARL_SHARE_ID)

        return when (type) {
            PushNotificationType.MESSAGE ->
                conversationId?.let { AppNavigationEvent.OpenConversation(it) }
            PushNotificationType.PEARL_SHARE ->
                shareId?.let { AppNavigationEvent.OpenPearlShare(it) }
            PushNotificationType.NEW_PUBLIC_PEARL ->
                pearlId?.let { AppNavigationEvent.OpenPublicPearl(it) }
                    ?: AppNavigationEvent.OpenPublicFeed
            PushNotificationType.PEARL_APPROVED ->
                AppNavigationEvent.OpenPendingSubmissions
            PushNotificationType.PEARL_LIKED ->
                pearlId?.let { AppNavigationEvent.OpenPublicPearl(it) }
            else -> null
        }
    }

    fun buildPearlShareUri(shareId: String): Uri =
        Uri.parse("$SCHEME://inbox/shared-pearls/$shareId")

    fun buildConversationUri(conversationId: String): Uri =
        Uri.parse("$SCHEME://inbox/messages/$conversationId")

    fun buildPublicPearlUri(pearlId: String): Uri =
        Uri.parse("$SCHEME://public-feed/pearl/$pearlId")
}
