package com.knowledgepearls.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScraperSourceLink(
    val url: String = "",
    val title: String = "",
    val site: String = "",
) {
    val displayTitle: String
        get() {
            title.trim().takeIf { it.isNotEmpty() }?.let { return it }
            site.trim().takeIf { it.isNotEmpty() }?.let { return it }
            return url
        }

    val displaySite: String
        get() {
            site.trim().takeIf { it.isNotEmpty() }?.let { return it }
            return runCatching { android.net.Uri.parse(url).host }.getOrNull().orEmpty().ifBlank { url }
        }
}

@Serializable
data class ScraperIngestionMeta(
    @SerialName("tweet_url") val tweetUrl: String? = null,
    @SerialName("tweet_text") val tweetText: String? = null,
    @SerialName("author_handle") val authorHandle: String? = null,
    @SerialName("author_name") val authorName: String? = null,
    @SerialName("ai_summary") val aiSummary: String? = null,
    val answer: String? = null,
    val discussion: List<String>? = null,
    @SerialName("key_points") val keyPoints: List<String>? = null,
    @SerialName("teaching_point") val teachingPoint: String? = null,
    @SerialName("thread_summary") val threadSummary: String? = null,
    @SerialName("source_links") val sourceLinks: List<ScraperSourceLink>? = null,
)
