package com.knowledgepearls.app.data.model

import com.knowledgepearls.app.data.local.model.ClinicalCasePayload
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShareProfileResult(
    val id: String,
    val name: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("allow_pearl_shares") val allowPearlShares: Boolean = true,
)

@Serializable
data class PearlSharePayload(
    val title: String = "",
    val notes: String = "",
    val tags: List<String> = emptyList(),
    @SerialName("content_type") val contentType: String = "text",
    @SerialName("content_kind") val contentKind: String = "standard",
    @SerialName("source_url") val sourceUrl: String? = null,
    @SerialName("link_preview_description") val linkPreviewDescription: String = "",
    @SerialName("source_reference") val sourceReference: String = "",
    @SerialName("case_payload") val casePayload: ClinicalCasePayload = ClinicalCasePayload(),
    @SerialName("media_items") val mediaItems: List<PublicPearlMediaItem> = emptyList(),
)

@Serializable
data class PearlShareRecord(
    val id: String,
    @SerialName("batch_id") val batchId: String,
    @SerialName("sender_id") val senderId: String,
    @SerialName("recipient_id") val recipientId: String,
    val status: String = "pending",
    @SerialName("pearl_payload") val pearlPayload: PearlSharePayload,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("responded_at") val respondedAt: String? = null,
)

data class PearlShareInboxRow(
    val share: PearlShareRecord,
    val senderName: String,
    val senderAvatarUrl: String?,
)
