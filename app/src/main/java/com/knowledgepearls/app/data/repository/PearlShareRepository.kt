package com.knowledgepearls.app.data.repository

import com.knowledgepearls.app.data.capture.PickedMedia
import com.knowledgepearls.app.data.local.entity.KnowledgePearlContentKind
import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.local.entity.MediaType
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.local.model.clinicalCasePayload
import com.knowledgepearls.app.data.local.model.effectiveSourceReference
import com.knowledgepearls.app.data.share.PearlContentIdentity
import com.knowledgepearls.app.data.local.model.isClinicalCase
import com.knowledgepearls.app.data.local.model.toPickedMedia
import com.knowledgepearls.app.data.local.model.withClinicalCasePayload
import com.knowledgepearls.app.data.media.MediaStorage
import com.knowledgepearls.app.data.model.PearlShareInboxRow
import com.knowledgepearls.app.data.model.PearlSharePayload
import com.knowledgepearls.app.data.model.PearlShareRecord
import com.knowledgepearls.app.data.model.PublicPearlMediaItem
import com.knowledgepearls.app.data.model.ShareProfileResult
import com.knowledgepearls.app.data.remote.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Singleton
class PearlShareRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val pearlRepository: KnowledgePearlRepository,
    private val mediaStorage: MediaStorage,
) {
    suspend fun searchProfiles(query: String): List<ShareProfileResult> {
        if (query.trim().length < 2) return emptyList()
        return supabase.postgrest.rpc(
            "search_profiles_for_share",
            ShareSearchParams(query = query.trim()),
        ).decodeList()
    }

    suspend fun sharePearlWithFriends(
        pearl: PearlWithMedia,
        recipientIds: List<String>,
    ): List<String> {
        val entity = pearl.pearl
        val userId = requireUserId()
        val batchShareId = UUID.randomUUID().toString().lowercase()
        val pickedMedia = pearl.mediaItems.toPickedMedia()
        val uploaded = uploadShareMedia(
            userId = userId,
            batchShareId = batchShareId,
            items = pickedMedia,
            includeSection = entity.isClinicalCase(),
        )
        val payload = buildSharePayload(entity, uploaded)
        val fingerprint = buildFingerprint(entity)
        val contentIdentity = PearlContentIdentity.forPearl(entity, pickedMedia)
        return sendShare(recipientIds, payload, fingerprint, contentIdentity)
    }

    suspend fun checkShareDuplicates(
        pearl: PearlWithMedia,
        recipientIds: List<String>,
    ): List<PearlShareDuplicate> {
        if (recipientIds.isEmpty()) return emptyList()
        val contentIdentity = PearlContentIdentity.forPearl(pearl.pearl, pearl.mediaItems.toPickedMedia())
        return runCatching {
            supabase.postgrest.rpc(
                "check_pearl_share_duplicates",
                CheckPearlShareDuplicatesParams(
                    recipientIds = recipientIds.map { it.lowercase() },
                    contentIdentity = contentIdentity,
                ),
            ).decodeList<PearlShareDuplicateRow>()
                .map {
                    PearlShareDuplicate(
                        recipientId = it.recipientId,
                        recipientName = it.recipientName,
                        reason = it.reason,
                    )
                }
        }.getOrDefault(emptyList())
    }

    suspend fun sendShare(
        recipientIds: List<String>,
        payload: PearlSharePayload,
        fingerprint: String,
        contentIdentity: String,
    ): List<String> {
        runCatching {
            supabase.postgrest.rpc(
                "send_pearl_shares",
                SendPearlSharesParams(
                    recipientIds = recipientIds.map { it.lowercase() },
                    pearlPayload = payload,
                    pearlFingerprint = fingerprint,
                    contentIdentity = contentIdentity,
                ),
            ).decodeList<String>()
        }.getOrElse {
            supabase.postgrest.rpc(
                "send_pearl_shares",
                SendPearlSharesLegacyParams(
                    recipientIds = recipientIds.map { it.lowercase() },
                    pearlPayload = payload,
                    pearlFingerprint = fingerprint,
                ),
            ).decodeList<String>()
        }.let { shareIds ->
            return shareIds.ifEmpty { recipientIds }
        }
    }

    suspend fun fetchShareById(shareId: String): PearlShareRecord? =
        runCatching {
            supabase.from("pearl_shares").select {
                filter { eq("id", shareId.lowercase()) }
            }.decodeSingle<PearlShareRecord>()
        }.getOrNull()

    suspend fun fetchPendingShares(recipientId: String): List<PearlShareInboxRow> {
        val shares = supabase.from("pearl_shares").select {
            filter {
                eq("recipient_id", recipientId.lowercase())
                eq("status", "pending")
            }
            order(column = "created_at", order = Order.DESCENDING)
        }.decodeList<PearlShareRecord>()

        return shares.map { share ->
            val sender = fetchSenderName(share.senderId)
            PearlShareInboxRow(
                share = share,
                senderName = sender.first,
                senderAvatarUrl = sender.second,
            )
        }
    }

    suspend fun pendingShareCount(recipientId: String): Int =
        runCatching {
            supabase.from("pearl_shares").select(columns = Columns.list("id")) {
                filter {
                    eq("recipient_id", recipientId.lowercase())
                    eq("status", "pending")
                }
            }.decodeList<ShareIdRow>().size
        }.getOrDefault(0)

    suspend fun respondToShare(shareId: String, accept: Boolean) {
        supabase.postgrest.rpc(
            "respond_pearl_share",
            RespondPearlShareParams(
                shareId = shareId.lowercase(),
                action = if (accept) "accept" else "decline",
            ),
        )
    }

    suspend fun importAcceptedShare(share: PearlShareRecord): KnowledgePearlEntity {
        val payload = share.pearlPayload
        val sender = fetchSenderName(share.senderId)
        val now = System.currentTimeMillis()
        var pearl = KnowledgePearlEntity(
            title = payload.title.ifBlank { "Shared pearl" },
            notes = payload.notes,
            sourceURL = payload.sourceUrl,
            linkPreviewDescription = payload.linkPreviewDescription,
            sourceReference = payload.sourceReference,
            createdAt = now,
            updatedAt = now,
            tags = payload.tags,
            isSharedFromFriend = true,
            sharedByUserID = share.senderId,
            sharedByName = sender.first,
            sharedByAvatarURL = sender.second,
            friendShareID = share.id,
            friendShareContentIdentityAtImport = PearlContentIdentity.forPayload(payload),
            publicFeedSnapshot = payload.publicFeedSnapshot.orEmpty(),
            contentKind = payload.contentKind.ifBlank { KnowledgePearlContentKind.STANDARD },
        )
        if (payload.contentType == "clinical_case" || payload.contentKind == KnowledgePearlContentKind.CLINICAL_CASE) {
            pearl = payload.casePayload.let { pearl.withClinicalCasePayload(it) }
                ?: pearl.copy(contentKind = KnowledgePearlContentKind.CLINICAL_CASE)
        }
        pearlRepository.upsertPearl(pearl)
        PublicPearlMediaImporter.importMediaItems(
            pearlRepository = pearlRepository,
            mediaStorage = mediaStorage,
            pearlId = pearl.id,
            items = payload.mediaItems,
        )
        return pearl
    }

    private fun buildSharePayload(
        pearl: KnowledgePearlEntity,
        uploadedMedia: List<PublicPearlMediaItem>,
    ): PearlSharePayload {
        if (pearl.isClinicalCase()) {
            val payload = pearl.clinicalCasePayload()
            return PearlSharePayload(
                title = pearl.title,
                notes = payload.history,
                tags = pearl.tags,
                contentType = "clinical_case",
                contentKind = KnowledgePearlContentKind.CLINICAL_CASE,
                sourceReference = pearl.effectiveSourceReference(),
                casePayload = payload,
                mediaItems = uploadedMedia,
                publicFeedSnapshot = pearl.publicFeedSnapshot.takeIf { it.isNotBlank() },
            )
        }

        return PearlSharePayload(
            title = pearl.title,
            notes = pearl.notes,
            tags = pearl.tags,
            contentType = resolveShareContentType(pearl, uploadedMedia),
            contentKind = pearl.contentKind,
            sourceUrl = pearl.sourceURL,
            linkPreviewDescription = pearl.linkPreviewDescription,
            sourceReference = pearl.effectiveSourceReference(),
            mediaItems = uploadedMedia,
            publicFeedSnapshot = pearl.publicFeedSnapshot.takeIf { it.isNotBlank() },
        )
    }

    private fun buildFingerprint(pearl: KnowledgePearlEntity): String {
        val basis = listOf(
            pearl.id,
            pearl.title,
            pearl.notes,
            pearl.contentKind,
            pearl.updatedAt.toString(),
        ).joinToString("|")
        return android.util.Base64.encodeToString(
            basis.toByteArray(Charsets.UTF_8),
            android.util.Base64.NO_WRAP,
        )
    }

    private suspend fun uploadShareMedia(
        userId: String,
        batchShareId: String,
        items: List<PickedMedia>,
        includeSection: Boolean,
    ): List<PublicPearlMediaItem> {
        if (items.isEmpty()) return emptyList()
        val bucket = supabase.storage.from(SupabaseConfig.PUBLIC_PEARL_MEDIA_BUCKET)
        return items.mapIndexed { index, item ->
            val ext = item.filename.substringAfterLast('.', "jpg").lowercase()
            val path = "pearls/${userId.lowercase()}/shares/${batchShareId.lowercase()}/$index.$ext"
            bucket.upload(path, item.bytes) { upsert = true }
            val publicUrl = bucket.publicUrl(path)
            PublicPearlMediaItem(
                type = publicShareContentType(item.type, item.filename),
                url = publicUrl,
                path = path,
                filename = item.filename.ifBlank { "attachment.$ext" },
                section = item.sectionTag.takeIf { includeSection && it.isNotBlank() },
            )
        }
    }

    private fun resolveShareContentType(
        pearl: KnowledgePearlEntity,
        uploadedMedia: List<PublicPearlMediaItem>,
    ): String {
        val types = uploadedMedia.map { it.type }.toSet()
        if ("video" in types && "photo" !in types) return "video"
        if ("photo" in types) return "photo"
        if ("document" in types) return "document"
        if ("video" in types) return "video"
        if (!pearl.sourceURL.isNullOrBlank()) return "link"
        return "text"
    }

    private fun publicShareContentType(type: String, filename: String): String = when (type) {
        MediaType.VIDEO -> "video"
        MediaType.IMAGE -> "photo"
        MediaType.PDF, MediaType.DOCUMENT -> {
            val ext = filename.substringAfterLast('.', "").lowercase()
            if (ext in setOf("mp4", "mov", "m4v", "webm")) "video" else "document"
        }
        else -> "photo"
    }

    private suspend fun fetchSenderName(senderId: String): Pair<String, String?> {
        return runCatching {
            val row = supabase.from("profiles").select {
                filter { eq("id", senderId.lowercase()) }
            }.decodeSingle<SenderProfile>()
            row.name.orEmpty().ifBlank { "Unknown" } to row.avatarUrl
        }.getOrDefault("Unknown" to null)
    }

    private fun requireUserId(): String =
        supabase.auth.currentUserOrNull()?.id?.lowercase()
            ?: throw IllegalStateException("Sign in to share pearls with friends.")

    @Serializable
    private data class ShareIdRow(val id: String)

    @Serializable
    private data class ShareSearchParams(@SerialName("p_query") val query: String)

    @Serializable
    private data class SendPearlSharesParams(
        @SerialName("p_recipient_ids") val recipientIds: List<String>,
        @SerialName("p_pearl_payload") val pearlPayload: PearlSharePayload,
        @SerialName("p_pearl_fingerprint") val pearlFingerprint: String,
        @SerialName("p_content_identity") val contentIdentity: String,
    )

    @Serializable
    private data class SendPearlSharesLegacyParams(
        @SerialName("p_recipient_ids") val recipientIds: List<String>,
        @SerialName("p_pearl_payload") val pearlPayload: PearlSharePayload,
        @SerialName("p_pearl_fingerprint") val pearlFingerprint: String,
    )

    @Serializable
    private data class RespondPearlShareParams(
        @SerialName("p_share_id") val shareId: String,
        @SerialName("p_action") val action: String,
    )

    @Serializable
    private data class SenderProfile(
        val name: String? = null,
        @SerialName("avatar_url") val avatarUrl: String? = null,
    )

    @Serializable
    private data class CheckPearlShareDuplicatesParams(
        @SerialName("p_recipient_ids") val recipientIds: List<String>,
        @SerialName("p_content_identity") val contentIdentity: String,
    )

    @Serializable
    private data class PearlShareDuplicateRow(
        @SerialName("recipient_id") val recipientId: String,
        @SerialName("recipient_name") val recipientName: String,
        val reason: String,
    )
}

data class PearlShareDuplicate(
    val recipientId: String,
    val recipientName: String,
    val reason: String,
)
