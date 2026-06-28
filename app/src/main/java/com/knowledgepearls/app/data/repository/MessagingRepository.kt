package com.knowledgepearls.app.data.repository

import com.knowledgepearls.app.data.model.Conversation
import com.knowledgepearls.app.data.model.ConversationRow
import com.knowledgepearls.app.data.model.DirectMessage
import com.knowledgepearls.app.data.model.isFrom
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Singleton
class MessagingRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    suspend fun getOrCreateConversation(otherUserId: String): String {
        val data = supabase.postgrest.rpc(
            "get_or_create_conversation",
            OtherUserParam(otherUserId = otherUserId.lowercase()),
        ).decodeAs<String>()
        return data.trim('"', ' ', '\n')
    }

    suspend fun fetchConversations(userId: String): List<Conversation> =
        supabase.from("conversations").select {
            filter {
                or {
                    eq("participant_a", userId.lowercase())
                    eq("participant_b", userId.lowercase())
                }
            }
            order(column = "updated_at", order = Order.DESCENDING)
        }.decodeList()

    suspend fun fetchMessages(conversationId: String): List<DirectMessage> =
        supabase.from("messages").select {
            filter { eq("conversation_id", conversationId.lowercase()) }
            order(column = "created_at", order = Order.ASCENDING)
        }.decodeList()

    suspend fun sendMessage(conversationId: String, senderId: String, body: String): DirectMessage {
        val trimmed = body.trim()
        require(trimmed.isNotEmpty())
        return supabase.from("messages").insert(
            MessageInsert(
                conversationId = conversationId.lowercase(),
                senderId = senderId.lowercase(),
                body = trimmed,
            ),
        ) {
            select()
        }.decodeList<DirectMessage>().first()
    }

    suspend fun markConversationRead(conversationId: String) {
        supabase.postgrest.rpc(
            "mark_conversation_read",
            ConversationReadParam(conversationId = conversationId.lowercase()),
        )
    }

    suspend fun unreadMessageCount(userId: String): Int {
        val normalizedUserId = userId.lowercase()
        val conversations = fetchConversations(normalizedUserId)
        var total = 0
        for (conversation in conversations) {
            val unread = supabase.from("messages").select(columns = Columns.list("id")) {
                filter {
                    eq("conversation_id", conversation.id.lowercase())
                    neq("sender_id", normalizedUserId)
                    exact("read_at", null)
                }
            }.decodeList<IdRow>()
            total += unread.size
        }
        return total
    }

    suspend fun buildConversationRows(userId: String): List<ConversationRow> {
        val conversations = fetchConversations(userId)
        return conversations.map { conversation ->
            val otherId = conversation.otherParticipant(userId)
            val profile = fetchProfileSummary(otherId)
            val messages = fetchMessages(conversation.id)
            val last = messages.lastOrNull()
            val unread = messages.count { !it.isFrom(userId) && it.readAt.isNullOrBlank() }
            ConversationRow(
                id = conversation.id,
                otherUserId = otherId,
                otherDisplayName = profile.name.orEmpty().ifBlank { "Unknown" },
                otherAvatarUrl = profile.avatarUrl,
                lastMessageBody = last?.body,
                unreadCount = unread,
            )
        }
    }

    private suspend fun fetchProfileSummary(userId: String): ProfileSummary {
        return runCatching {
            supabase.from("profiles").select {
                filter { eq("id", userId.lowercase()) }
            }.decodeSingle<ProfileSummary>()
        }.getOrDefault(ProfileSummary(name = "Unknown"))
    }

    @Serializable
    private data class OtherUserParam(@SerialName("other_user_id") val otherUserId: String)

    @Serializable
    private data class ConversationReadParam(@SerialName("p_conversation_id") val conversationId: String)

    @Serializable
    private data class MessageInsert(
        @SerialName("conversation_id") val conversationId: String,
        @SerialName("sender_id") val senderId: String,
        val body: String,
    )

    @Serializable
    private data class IdRow(val id: String)

    @Serializable
    private data class ProfileSummary(
        val name: String? = null,
        @SerialName("avatar_url") val avatarUrl: String? = null,
    )
}
