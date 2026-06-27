package com.knowledgepearls.app.data.repository

import com.knowledgepearls.app.data.local.entity.KnowledgePearlContentKind
import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.local.entity.MediaType
import com.knowledgepearls.app.data.local.entity.PearlMediaEntity
import com.knowledgepearls.app.data.local.model.decodedPublicPearl
import com.knowledgepearls.app.data.local.model.withClinicalCasePayload
import com.knowledgepearls.app.data.media.MediaStorage
import com.knowledgepearls.app.data.model.PearlShareInboxRow
import com.knowledgepearls.app.data.model.PearlSharePayload
import com.knowledgepearls.app.data.model.PearlShareRecord
import com.knowledgepearls.app.data.model.PublicPearlMediaItem
import com.knowledgepearls.app.data.model.ShareProfileResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
            )
        }.getOrElse {
            supabase.postgrest.rpc(
                "send_pearl_shares",
                SendPearlSharesLegacyParams(
                    recipientIds = recipientIds.map { it.lowercase() },
                    pearlPayload = payload,
                    pearlFingerprint = fingerprint,
                ),
            )
        }
        return recipientIds
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
        fetchPendingShares(recipientId).size

    suspend fun respondToShare(shareId: String, accept: Boolean) {
        supabase.postgrest.rpc(
            "respond_pearl_share",
            RespondPearlShareParams(shareId = shareId.lowercase(), accept = accept),
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
            publicFeedSnapshot = payload.publicFeedSnapshot.orEmpty(),
            contentKind = payload.contentKind.ifBlank { KnowledgePearlContentKind.STANDARD },
        )
        if (payload.contentType == "clinical_case" || payload.contentKind == KnowledgePearlContentKind.CLINICAL_CASE) {
            pearl = payload.casePayload.let { pearl.withClinicalCasePayload(it) }
                ?: pearl.copy(contentKind = KnowledgePearlContentKind.CLINICAL_CASE)
        }
        pearlRepository.upsertPearl(pearl)
        importMediaItems(pearl.id, payload.mediaItems)
        return pearl
    }

    fun buildPayloadFromPearl(
        pearl: KnowledgePearlEntity,
        mediaItems: List<PublicPearlMediaItem>,
    ): PearlSharePayload {
        val snapshot = pearl.publicFeedSnapshot.takeIf { it.isNotBlank() }
        val resolvedMedia = mediaItems.ifEmpty {
            pearl.decodedPublicPearl()?.resolvedMediaItems.orEmpty()
        }
        return PearlSharePayload(
            title = pearl.title,
            notes = pearl.notes,
            tags = pearl.tags,
            contentType = when (pearl.contentKind) {
                KnowledgePearlContentKind.CLINICAL_CASE -> "clinical_case"
                KnowledgePearlContentKind.QUICK -> "text"
                else -> "text"
            },
            contentKind = pearl.contentKind,
            sourceUrl = pearl.sourceURL,
            linkPreviewDescription = pearl.linkPreviewDescription,
            sourceReference = pearl.sourceReference,
            mediaItems = resolvedMedia,
            publicFeedSnapshot = snapshot,
        )
    }

    private suspend fun importMediaItems(pearlId: String, items: List<PublicPearlMediaItem>) {
        items.forEach { item ->
            val remoteUrl = item.loadableUrl ?: return@forEach
            runCatching {
                val bytes = withContext(Dispatchers.IO) { URL(remoteUrl).readBytes() }
                val filename = item.resolvedFilename
                val extension = filename.substringAfterLast('.', "jpg")
                val localPath = mediaStorage.saveBytes(bytes, extension)
                val type = when {
                    item.isVideo -> MediaType.VIDEO
                    item.isDocument -> MediaType.DOCUMENT
                    else -> MediaType.IMAGE
                }
                pearlRepository.upsertMedia(
                    PearlMediaEntity(
                        pearlId = pearlId,
                        type = type,
                        localPath = localPath,
                        filename = filename,
                        sectionTag = item.section.orEmpty(),
                    ),
                )
            }
        }
    }

    private suspend fun fetchSenderName(senderId: String): Pair<String, String?> {
        return runCatching {
            val row = supabase.from("profiles").select {
                filter { eq("id", senderId.lowercase()) }
            }.decodeSingle<SenderProfile>()
            row.name.orEmpty().ifBlank { "Unknown" } to row.avatarUrl
        }.getOrDefault("Unknown" to null)
    }

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
        @SerialName("p_accept") val accept: Boolean,
    )

    @Serializable
    private data class SenderProfile(
        val name: String? = null,
        @SerialName("avatar_url") val avatarUrl: String? = null,
    )
}
