package com.knowledgepearls.app.data.capture

import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LinkPreview(
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
)

@Singleton
class LinkPreviewFetcher @Inject constructor(
    private val mediaStorage: com.knowledgepearls.app.data.media.MediaStorage,
) {
    suspend fun fetch(pageUrl: String): LinkPreview = withContext(Dispatchers.IO) {
        val connection = (URL(pageUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("User-Agent", "MedPearls/1.0")
            instanceFollowRedirects = true
        }
        try {
            val html = connection.inputStream.bufferedReader().use { it.readText().take(250_000) }
            LinkPreview(
                title = metaContent(html, "og:title") ?: metaContent(html, "twitter:title"),
                description = metaContent(html, "og:description") ?: metaContent(html, "description"),
                imageUrl = metaContent(html, "og:image"),
            )
        } finally {
            connection.disconnect()
        }
    }

    suspend fun downloadPreviewImage(imageUrl: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 10_000
            }
            try {
                val bytes = connection.inputStream.use { it.readBytes() }
                if (bytes.isEmpty()) return@runCatching null
                val ext = imageUrl.substringAfterLast('.', "jpg").take(4)
                mediaStorage.saveBytes(bytes, ext)
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }

    private fun metaContent(html: String, property: String): String? {
        val patterns = listOf(
            """property=["']$property["'][^>]*content=["']([^"']+)["']""".toRegex(RegexOption.IGNORE_CASE),
            """content=["']([^"']+)["'][^>]*property=["']$property["']""".toRegex(RegexOption.IGNORE_CASE),
            """name=["']$property["'][^>]*content=["']([^"']+)["']""".toRegex(RegexOption.IGNORE_CASE),
        )
        return patterns.firstNotNullOfOrNull { regex ->
            regex.find(html)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }
        }
    }
}
