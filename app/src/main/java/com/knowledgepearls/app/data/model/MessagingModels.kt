package com.knowledgepearls.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    val id: String,
    @SerialName("participant_a") val participantA: String,
    @SerialName("participant_b") val participantB: String,
    @SerialName("updated_at") val updatedAt: String = "",
) {
    fun otherParticipant(currentUserId: String): String =
        if (participantA.equals(currentUserId, ignoreCase = true)) participantB else participantA
}

@Serializable
data class DirectMessage(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    @SerialName("sender_id") val senderId: String,
    val body: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("read_at") val readAt: String? = null,
)

data class ConversationRow(
    val id: String,
    val otherUserId: String,
    val otherDisplayName: String,
    val otherAvatarUrl: String?,
    val lastMessageBody: String?,
    val unreadCount: Int,
)

fun DirectMessage.isFrom(userId: String): Boolean =
    userId.isNotBlank() && senderId.equals(userId, ignoreCase = true)
