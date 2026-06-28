package com.knowledgepearls.app.data.model

import com.knowledgepearls.app.data.local.model.ClinicalCasePayload
import com.knowledgepearls.app.data.util.SupabaseTimestamps
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PublicPearl(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String = "",
    val notes: String = "",
    val tags: List<String> = emptyList(),
    @SerialName("content_type") val contentType: String = "text",
    @SerialName("source_url") val sourceUrl: String? = null,
    @SerialName("link_preview_title") val linkPreviewTitle: String? = null,
    @SerialName("link_preview_description") val linkPreviewDescription: String? = null,
    @SerialName("link_preview_image_url") val linkPreviewImageUrl: String? = null,
    @SerialName("media_url") val mediaUrl: String? = null,
    @SerialName("media_path") val mediaPath: String? = null,
    @SerialName("media_items") val mediaItems: List<PublicPearlMediaItem>? = null,
    @SerialName("shared_by") val sharedBy: String = "",
    @SerialName("source_reference") val sourceReference: String = "",
    val status: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("like_count") val likeCount: Int = 0,
    @SerialName("ingestion_source") val ingestionSource: String = "user",
    @SerialName("external_id") val externalId: String? = null,
    @SerialName("ingestion_meta") val ingestionMeta: ScraperIngestionMeta? = null,
    @SerialName("moderated_at") val moderatedAt: String? = null,
    @SerialName("case_payload") val casePayload: ClinicalCasePayload? = null,
) {
    val titleDisplay: String get() = title.ifBlank { "Untitled" }

    val isClinicalCase: Boolean get() = contentType == "clinical_case"

    val isQuickPearl: Boolean get() = contentType == "text"

    val isLinkPearl: Boolean get() = contentType == "link"

    val isFromTwitterScraper: Boolean
        get() = ingestionSource == "twitter_scraper" || !ingestionMeta?.tweetUrl.isNullOrBlank()

    val preferredPreviewUrl: String?
        get() = ingestionMeta?.tweetUrl?.trim()?.takeIf { it.isNotEmpty() }
            ?: sourceUrl?.trim()?.takeIf { it.isNotEmpty() }

    val canLinkToTwitterOriginalAuthor: Boolean
        get() = isFromTwitterScraper && preferredPreviewUrl != null

    val twitterOriginalAuthorLinkLabel: String
        get() {
            ingestionMeta?.authorName?.trim()?.takeIf { it.isNotEmpty() && !it.startsWith("@") }?.let { return it }
            return "Original poster on X"
        }

    val originalTweetAuthorLabel: String
        get() {
            ingestionMeta?.authorName?.trim()?.takeIf { it.isNotEmpty() && !it.startsWith("@") }?.let { return it }
            ingestionMeta?.authorHandle?.trim()?.takeIf { it.isNotEmpty() }?.let { handle ->
                return if (handle.startsWith("@")) handle else "@$handle"
            }
            return twitterOriginalAuthorLinkLabel
        }

    val scraperTweetText: String
        get() {
            ingestionMeta?.tweetText?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
            val fromTitle = title.trim()
            if (fromTitle.isNotEmpty() && fromTitle != "Tweet") return fromTitle
            return ""
        }

    val scraperAISummary: String
        get() {
            ingestionMeta?.aiSummary?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
            val noteText = notes.trim()
            if (noteText.isNotEmpty() && noteText != scraperTweetText) return noteText
            return ""
        }

    val scraperLearningPoint: String
        get() {
            ingestionMeta?.teachingPoint?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
            ingestionMeta?.answer?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
            return ""
        }

    val scraperExternalLinks: List<ScraperSourceLink>
        get() {
            val links = ingestionMeta?.sourceLinks.orEmpty()
            return links.filter { link ->
                val host = runCatching { android.net.Uri.parse(link.url).host?.lowercase() }.getOrNull().orEmpty()
                if (host.contains("x.com") || host.contains("twitter.com")) return@filter false
                val preferred = preferredPreviewUrl
                if (preferred != null && link.url == preferred) return@filter false
                true
            }
        }

    val caseSectionMediaItems: List<PublicPearlMediaItem>
        get() = resolvedMediaItems.filter { !it.section.isNullOrBlank() }

    val resolvedMediaItems: List<PublicPearlMediaItem>
        get() {
            val items = if (!mediaItems.isNullOrEmpty()) {
                mediaItems
            } else {
                val raw = mediaUrl?.trim().orEmpty()
                if (raw.isEmpty()) {
                    emptyList()
                } else {
                    val legacyType = when (contentType) {
                        "video" -> "video"
                        "document" -> "document"
                        else -> "photo"
                    }
                    listOf(PublicPearlMediaItem(type = legacyType, url = raw, path = mediaPath))
                }
            }
            return items.filter { it.loadableUrl != null }
        }

    val parsedMediaUrl: String? get() = resolvedMediaItems.firstOrNull()?.loadableUrl

    val resolvedLinkPreviewImageUrl: String?
        get() {
            linkPreviewImageUrl?.trim()?.takeIf { it.isNotEmpty() }?.let { raw ->
                return PublicPearlMediaUrls.fixPublicMediaUrl(raw) ?: raw
            }
            if (ingestionSource == "twitter_scraper") {
                parsedMediaUrl?.takeIf { PublicPearlMediaUrls.isImageUrl(it) }?.let { return it }
            }
            if (isLinkPearl) {
                parsedMediaUrl?.takeIf { PublicPearlMediaUrls.isImageUrl(it) }?.let { return it }
            }
            return null
        }

    val hasGalleryMedia: Boolean get() = resolvedMediaItems.isNotEmpty()

    val safeDisplayName: String
        get() {
            if (sharedBy.isBlank()) return ""
            if (!sharedBy.contains("@")) return sharedBy
            val local = sharedBy.substringBefore("@").trim()
            if (local.isEmpty()) return ""
            return local
                .replace('.', ' ')
                .replace('_', ' ')
                .replace('-', ' ')
                .split(' ')
                .joinToString(" ") { word ->
                    word.replaceFirstChar { ch -> ch.uppercaseChar() }
                }
        }

    val effectiveSourceReference: String
        get() {
            val trimmed = sourceReference.trim()
            if (trimmed.isNotEmpty()) return trimmed
            return sourceUrl?.trim().orEmpty()
        }

    val createdAtMillis: Long?
        get() = SupabaseTimestamps.toEpochMillis(createdAt)

    val moderatedAtMillis: Long?
        get() = SupabaseTimestamps.toEpochMillis(moderatedAt)

    /** Newest-first ordering for the public feed (approval time wins when present). */
    val feedSortMillis: Long
        get() = listOfNotNull(createdAtMillis, moderatedAtMillis).maxOrNull() ?: 0L

    fun matches(filter: ContentTypeFilter): Boolean = when (filter) {
        ContentTypeFilter.ALL -> true
        ContentTypeFilter.QUICK -> isQuickPearl
        ContentTypeFilter.PHOTOS -> contentType in setOf("photo", "video", "document") || hasGalleryMedia
        ContentTypeFilter.LINKS -> isLinkPearl
        ContentTypeFilter.CASES -> isClinicalCase
    }

    fun replacing(likeCount: Int? = null): PublicPearl = copy(likeCount = likeCount ?: this.likeCount)
}
