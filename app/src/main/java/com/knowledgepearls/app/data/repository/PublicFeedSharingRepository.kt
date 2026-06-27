package com.knowledgepearls.app.data.repository

import com.knowledgepearls.app.data.capture.PickedMedia
import com.knowledgepearls.app.data.local.model.ClinicalCasePayload
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.data.model.PublicPearlMediaItem
import com.knowledgepearls.app.data.remote.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Singleton
class PublicFeedSharingRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    suspend fun fetchPendingSubmissions(userId: String): List<PublicPearl> =
        supabase.from("public_pearls").select {
            filter {
                eq("user_id", userId.lowercase())
                eq("status", "pending")
            }
            order(column = "created_at", order = Order.DESCENDING)
        }.decodeList()

    suspend fun pendingSubmissionCount(userId: String): Int =
        fetchPendingSubmissions(userId).size

    suspend fun withdraw(publicPearlId: String) {
        supabase.from("public_pearls").delete {
            filter { eq("id", publicPearlId.lowercase()) }
        }
    }

    suspend fun shareStandardPearl(
        title: String,
        notes: String,
        tags: List<String>,
        sourceUrl: String?,
        linkPreviewDescription: String,
        sourceReference: String,
        mediaItems: List<PickedMedia>,
    ): String {
        val userId = requireUserId()
        val pearlId = UUID.randomUUID().toString().lowercase()
        val uploaded = uploadMedia(userId, pearlId, mediaItems)
        val contentType = resolveContentType(uploaded, sourceUrl != null)
        val first = uploaded.firstOrNull()
        submitPearl(
            id = pearlId,
            userId = userId,
            title = title,
            notes = notes,
            tags = tags,
            contentType = contentType,
            sourceUrl = sourceUrl,
            linkPreviewDescription = linkPreviewDescription,
            mediaUrl = first?.url,
            mediaPath = first?.path,
            mediaItems = uploaded,
            sourceReference = sourceReference,
            casePayload = ClinicalCasePayload(),
        )
        return pearlId
    }

    suspend fun shareClinicalCase(
        title: String,
        payload: ClinicalCasePayload,
        tags: List<String>,
        sectionMedia: Map<String, List<PickedMedia>>,
    ): String {
        val userId = requireUserId()
        val pearlId = UUID.randomUUID().toString().lowercase()
        val flatMedia = sectionMedia.flatMap { (section, items) ->
            items.map { it.copy(sectionTag = section) }
        }
        val uploaded = uploadMedia(userId, pearlId, flatMedia, includeSection = true)
        val first = uploaded.firstOrNull()
        submitPearl(
            id = pearlId,
            userId = userId,
            title = title,
            notes = payload.history,
            tags = tags,
            contentType = "clinical_case",
            sourceUrl = null,
            linkPreviewDescription = "",
            mediaUrl = first?.url,
            mediaPath = first?.path,
            mediaItems = uploaded,
            sourceReference = payload.references,
            casePayload = payload,
        )
        return pearlId
    }

    private suspend fun submitPearl(
        id: String,
        userId: String,
        title: String,
        notes: String,
        tags: List<String>,
        contentType: String,
        sourceUrl: String?,
        linkPreviewDescription: String,
        mediaUrl: String?,
        mediaPath: String?,
        mediaItems: List<UploadedMediaItem>,
        sourceReference: String,
        casePayload: ClinicalCasePayload,
    ) {
        val params = SubmitPublicPearlParams(
            pId = id,
            pTitle = title,
            pNotes = notes,
            pTags = tags,
            pContentType = contentType,
            pSourceUrl = sourceUrl,
            pLinkPreviewDescription = linkPreviewDescription.ifBlank { null },
            pMediaUrl = mediaUrl,
            pMediaPath = mediaPath,
            pSourceReference = sourceReference,
            pMediaItems = mediaItems,
            pCasePayload = casePayload,
        )
        runCatching {
            supabase.postgrest.rpc("submit_public_pearl", params)
        }.getOrElse { error ->
            if (!shouldFallbackFromRpc(error)) throw error
            supabase.from("public_pearls").insert(
                PublicPearlInsert(
                    id = id,
                    userId = userId,
                    title = title,
                    notes = notes,
                    tags = tags,
                    contentType = contentType,
                    sourceUrl = sourceUrl,
                    linkPreviewDescription = linkPreviewDescription.ifBlank { null },
                    mediaUrl = mediaUrl,
                    mediaPath = mediaPath,
                    mediaItems = mediaItems,
                    sourceReference = sourceReference,
                    casePayload = casePayload,
                ),
            )
        }
    }

    private suspend fun uploadMedia(
        userId: String,
        pearlId: String,
        items: List<PickedMedia>,
        includeSection: Boolean = false,
    ): List<UploadedMediaItem> {
        val bucket = supabase.storage.from(SupabaseConfig.PUBLIC_PEARL_MEDIA_BUCKET)
        return items.mapIndexed { index, item ->
            val ext = item.filename.substringAfterLast('.', "jpg").lowercase()
            val path = "pearls/${userId.lowercase()}/${pearlId.lowercase()}/$index.$ext"
            bucket.upload(path, item.bytes) { upsert = true }
            val publicUrl = bucket.publicUrl(path)
            UploadedMediaItem(
                type = publicContentType(item.type, item.filename),
                url = publicUrl,
                path = path,
                filename = item.filename,
                section = item.sectionTag.takeIf { includeSection && it.isNotBlank() },
            )
        }
    }

    private fun requireUserId(): String =
        supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("Sign in to share to the Public Feed.")

    private fun resolveContentType(items: List<UploadedMediaItem>, hasSourceUrl: Boolean): String {
        val types = items.map { it.type }.toSet()
        if ("video" in types && "photo" !in types) return "video"
        if ("photo" in types) return "photo"
        if ("document" in types) return "document"
        if ("video" in types) return "video"
        if (hasSourceUrl) return "link"
        return "text"
    }

    private fun publicContentType(type: String, filename: String): String = when (type) {
        "video" -> "video"
        "image" -> "photo"
        "pdf", "document" -> {
            val ext = filename.substringAfterLast('.', "").lowercase()
            if (ext in setOf("mp4", "mov", "m4v", "webm")) "video" else "document"
        }
        else -> "photo"
    }

    private fun shouldFallbackFromRpc(error: Throwable): Boolean {
        val message = error.message?.lowercase().orEmpty()
        return message.contains("could not find the function") ||
            message.contains("submit_public_pearl") ||
            message.contains("does not exist") ||
            message.contains("404")
    }

    @Serializable
    private data class UploadedMediaItem(
        val type: String,
        val url: String,
        val path: String,
        val filename: String,
        val section: String? = null,
    )

    @Serializable
    private data class SubmitPublicPearlParams(
        @SerialName("p_id") val pId: String,
        @SerialName("p_title") val pTitle: String,
        @SerialName("p_notes") val pNotes: String,
        @SerialName("p_tags") val pTags: List<String>,
        @SerialName("p_content_type") val pContentType: String,
        @SerialName("p_source_url") val pSourceUrl: String? = null,
        @SerialName("p_link_preview_description") val pLinkPreviewDescription: String? = null,
        @SerialName("p_media_url") val pMediaUrl: String? = null,
        @SerialName("p_media_path") val pMediaPath: String? = null,
        @SerialName("p_source_reference") val pSourceReference: String = "",
        @SerialName("p_media_items") val pMediaItems: List<UploadedMediaItem> = emptyList(),
        @SerialName("p_case_payload") val pCasePayload: ClinicalCasePayload = ClinicalCasePayload(),
    )

    @Serializable
    private data class PublicPearlInsert(
        val id: String,
        @SerialName("user_id") val userId: String,
        val title: String,
        val notes: String,
        val tags: List<String>,
        @SerialName("content_type") val contentType: String,
        val status: String = "pending",
        @SerialName("source_url") val sourceUrl: String? = null,
        @SerialName("link_preview_description") val linkPreviewDescription: String? = null,
        @SerialName("media_url") val mediaUrl: String? = null,
        @SerialName("media_path") val mediaPath: String? = null,
        @SerialName("media_items") val mediaItems: List<UploadedMediaItem> = emptyList(),
        @SerialName("source_reference") val sourceReference: String = "",
        @SerialName("case_payload") val casePayload: ClinicalCasePayload = ClinicalCasePayload(),
    )
}
