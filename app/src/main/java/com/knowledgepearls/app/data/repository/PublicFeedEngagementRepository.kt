package com.knowledgepearls.app.data.repository

import com.knowledgepearls.app.data.model.PearlComment
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Singleton
class PublicFeedEngagementRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    suspend fun fetchLikedPearlIds(userId: String, pearlIds: List<String>): Set<String> {
        if (pearlIds.isEmpty()) return emptySet()
        val rows = supabase.from("public_pearl_likes").select {
            filter {
                eq("user_id", userId.lowercase())
                isIn("pearl_id", pearlIds)
            }
        }.decodeList<LikeRow>()
        return rows.map { it.pearlId.lowercase() }.toSet()
    }

    suspend fun like(pearlId: String) {
        supabase.postgrest.rpc("like_public_pearl", PearlIdParam(targetPearlId = pearlId.lowercase()))
    }

    suspend fun unlike(pearlId: String) {
        supabase.postgrest.rpc("unlike_public_pearl", PearlIdParam(targetPearlId = pearlId.lowercase()))
    }

    suspend fun fetchComments(pearlId: String): List<PearlComment> =
        supabase.from("public_pearl_comments").select {
            filter { eq("pearl_id", pearlId.lowercase()) }
            order(column = "created_at", order = Order.ASCENDING)
        }.decodeList()

    suspend fun fetchCommentCount(pearlId: String): Int =
        supabase.from("public_pearl_comments").select {
            filter { eq("pearl_id", pearlId.lowercase()) }
        }.decodeList<CommentIdRow>().size

    suspend fun postComment(pearlId: String, userId: String, body: String): PearlComment {
        val rows = supabase.from("public_pearl_comments").insert(
            CommentInsert(
                pearlId = pearlId.lowercase(),
                userId = userId.lowercase(),
                body = body.trim(),
            ),
        ) {
            select()
        }.decodeList<PearlComment>()
        return rows.first()
    }

    @Serializable
    private data class PearlIdParam(@SerialName("target_pearl_id") val targetPearlId: String)

    @Serializable
    private data class LikeRow(@SerialName("pearl_id") val pearlId: String)

    @Serializable
    private data class CommentIdRow(val id: String)

    @Serializable
    private data class CommentInsert(
        @SerialName("pearl_id") val pearlId: String,
        @SerialName("user_id") val userId: String,
        @SerialName("author_name") val authorName: String = "",
        val body: String,
    )
}
