package com.knowledgepearls.app.data.repository

import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.data.model.UserProfile
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
class ProfileRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val accountRepository: AccountRepository,
) {
    suspend fun fetchProfile(userId: String): UserProfile? = accountRepository.fetchProfile(userId)

    suspend fun fetchApprovedPearls(userId: String): List<PublicPearl> =
        supabase.from("public_pearls").select {
            filter {
                eq("user_id", userId.lowercase())
                eq("status", "approved")
            }
            order(column = "created_at", order = Order.DESCENDING)
        }.decodeList()

    suspend fun fetchTotalLikesReceived(userId: String): Int =
        runCatching {
            supabase.postgrest.rpc(
                "user_total_likes_received",
                UserIdParam(targetUserId = userId.lowercase()),
            ).decodeAs<Int>()
        }.getOrDefault(0)

    @Serializable
    private data class UserIdParam(@SerialName("target_user_id") val targetUserId: String)
}
