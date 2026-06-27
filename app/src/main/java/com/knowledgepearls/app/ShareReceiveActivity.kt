package com.knowledgepearls.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.knowledgepearls.app.navigation.DeepLinkRouter

/**
 * Receives shares from other apps (ACTION_SEND) and .pearl / backup file opens,
 * then forwards into [MainActivity] capture/import flows.
 */
class ShareReceiveActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val forward = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            action = Intent.ACTION_VIEW
        }

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim().orEmpty()
                val sharedUrl = intent.getStringExtra(Intent.EXTRA_SUBJECT)?.trim()
                    ?.takeIf { it.startsWith("http") }
                    ?: extractUrl(sharedText)
                if (!sharedUrl.isNullOrBlank()) {
                    forward.putExtra(DeepLinkRouter.EXTRA_SHARE_URL, sharedUrl)
                    forward.putExtra(DeepLinkRouter.EXTRA_SHARE_TEXT, sharedText.takeIf { it != sharedUrl })
                } else if (sharedText.isNotBlank()) {
                    forward.putExtra(DeepLinkRouter.EXTRA_SHARE_TEXT, sharedText)
                }
            }
            Intent.ACTION_VIEW -> {
                intent.data?.let { uri ->
                    when {
                        uri.scheme == DeepLinkRouter.SCHEME -> forward.data = uri
                        uri.lastPathSegment?.endsWith(".pearl", ignoreCase = true) == true ||
                            uri.lastPathSegment?.endsWith(".json", ignoreCase = true) == true -> {
                            forward.data = uri
                            forward.putExtra(DeepLinkRouter.EXTRA_SHARE_URL, uri.toString())
                        }
                        else -> {
                            val url = uri.toString()
                            if (url.startsWith("http")) {
                                forward.putExtra(DeepLinkRouter.EXTRA_SHARE_URL, url)
                            }
                        }
                    }
                }
            }
        }

        startActivity(forward)
        finish()
    }

    private fun extractUrl(text: String): String? {
        val match = URL_REGEX.find(text)?.value ?: return null
        return if (match.startsWith("http")) match else "https://$match"
    }

    private companion object {
        private val URL_REGEX = Regex("""https?://\S+|www\.\S+""")
    }
}
