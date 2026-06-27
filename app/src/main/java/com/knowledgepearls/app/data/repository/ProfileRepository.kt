package com.knowledgepearls.app.data.repository

import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.data.model.UserProfile
import com.knowledgepearls.app.data.model.normalizeUserId
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Count
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
    private val publicFeedRepository: PublicFeedRepository,
) {
    suspend fun fetchProfile(userId: String): UserProfile? {
        val normalized = normalizeUserId(userId)
        return accountRepository.fetchProfile(normalized)
            ?: accountRepository.fetchProfile(userId.trim())
    }

    suspend fun fetchApprovedPearls(userId: String): List<PublicPearl> {
        userIdCandidates(userId).forEach { candidate ->
            val pearls = runCatching { queryApprovedPearls(candidate) }.getOrNull()
            if (!pearls.isNullOrEmpty()) return pearls
        }
        return queryApprovedPearls(normalizeUserId(userId))
    }

    suspend fun fetchApprovedPearlCount(userId: String): Int {
        userIdCandidates(userId).forEach { candidate ->
            val count = runCatching { queryApprovedPearlCount(candidate) }.getOrNull()
            if (count != null && count > 0) return count
        }
        return queryApprovedPearlCount(normalizeUserId(userId))
    }

    private suspend fun queryApprovedPearls(userId: String): List<PublicPearl> =
        supabase.from("public_pearls").select {
            filter {
                eq("user_id", userId)
                eq("status", "approved")
            }
            order(column = "created_at", order = Order.DESCENDING)
        }.decodeList<PublicPearl>()

    private suspend fun queryApprovedPearlCount(userId: String): Int =
        supabase.from("public_pearls").select {
            count(Count.EXACT)
            filter {
                eq("user_id", userId)
                eq("status", "approved")
            }
        }.countOrNull()?.toInt() ?: 0

    private fun userIdCandidates(userId: String): List<String> {
        val trimmed = userId.trim()
        return listOf(trimmed, normalizeUserId(trimmed)).distinct()
    }

    suspend fun fetchTotalLikesReceived(userId: String): Int {
        val normalized = normalizeUserId(userId)
        val fromRpc = runCatching {
            supabase.postgrest.rpc(
                "user_total_likes_received",
                UserIdParam(targetUserId = normalized),
            ).decodeAs<Int>()
        }.getOrNull()
        if (fromRpc != null && fromRpc > 0) return fromRpc

        return runCatching {
            supabase.postgrest.rpc(
                "user_total_likes_received",
                UserIdParam(targetUserId = normalized),
            ).decodeAs<Long>().toInt()
        }.getOrDefault(0)
    }

    fun blockUser(userId: String) {
        publicFeedRepository.blockUser(userId)
    }

    fun isUserBlocked(userId: String): Boolean =
        publicFeedRepository.isUserBlocked(userId)

    @Serializable
    private data class UserIdParam(@SerialName("target_user_id") val targetUserId: String)
}
