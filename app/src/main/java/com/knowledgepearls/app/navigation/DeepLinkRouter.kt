package com.knowledgepearls.app.navigation

import android.content.Intent
import android.net.Uri

sealed interface AppNavigationEvent {
    data object OpenInbox : AppNavigationEvent
    data object OpenSharedPearls : AppNavigationEvent
    data class OpenPearlShare(val shareId: String) : AppNavigationEvent
    data class OpenConversation(val conversationId: String) : AppNavigationEvent
    data class ImportShare(val text: String?, val url: String?) : AppNavigationEvent
    data class PearlShareReceivedToast(val senderName: String, val pearlTitle: String) : AppNavigationEvent
}

data class ShareImportPayload(
    val text: String?,
    val url: String?,
)

object DeepLinkRouter {
    const val SCHEME = "com.knowledgepearls.app"

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
            else -> null
        }
    }

    fun parse(intent: Intent): AppNavigationEvent? {
        intent.data?.let { uri ->
            parse(uri)?.let { return it }
        }

        intent.getStringExtra(EXTRA_PEARL_SHARE_ID)?.let {
            return AppNavigationEvent.OpenPearlShare(it)
        }
        intent.getStringExtra(EXTRA_CONVERSATION_ID)?.let {
            return AppNavigationEvent.OpenConversation(it)
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

    fun buildPearlShareUri(shareId: String): Uri =
        Uri.parse("$SCHEME://inbox/shared-pearls/$shareId")

    fun buildConversationUri(conversationId: String): Uri =
        Uri.parse("$SCHEME://inbox/messages/$conversationId")

    const val EXTRA_PEARL_SHARE_ID = "pearl_share_id"
    const val EXTRA_CONVERSATION_ID = "conversation_id"
    const val EXTRA_SHARE_TEXT = "share_text"
    const val EXTRA_SHARE_URL = "share_url"
}
