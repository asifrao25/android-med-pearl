package com.knowledgepearls.app.data.sync

import com.knowledgepearls.app.data.repository.KnowledgePearlRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PublicFeedStatusSyncRunnerImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val repository: KnowledgePearlRepository,
) : PublicFeedStatusSyncRunner {
    override suspend fun syncLocalPearlStatuses(userId: String) {
        runCatching {
            val rows = supabase.from("public_pearls").select {
                filter { eq("user_id", userId) }
            }.decodeList<StatusRow>()

            val statusById = rows.associate { it.id to it.status }
            val pearls = repository.getAllPearls()
            val now = System.currentTimeMillis()

            pearls.forEach { pearl ->
                val updated = applyRemoteStatus(pearl, statusById, now)
                if (updated != pearl) {
                    repository.updatePearl(updated)
                }
            }
        }
    }

    private fun applyRemoteStatus(
        pearl: com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity,
        statusById: Map<String, String>,
        now: Long,
    ): com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity {
        val publicId = pearl.publicPearlID
        if (publicId == null) {
            return if (pearl.publicPearlStatus.isEmpty()) {
                pearl
            } else {
                pearl.copy(publicPearlStatus = "", updatedAt = now)
            }
        }

        val remoteStatus = statusById[publicId]
        if (remoteStatus == null) {
            return pearl.copy(
                isSharedPublicly = false,
                publicPearlID = null,
                publicPearlStatus = "",
                updatedAt = now,
            )
        }

        val isShared = when (remoteStatus) {
            "approved", "pending" -> true
            "rejected" -> false
            else -> pearl.isSharedPublicly
        }

        return pearl.copy(
            publicPearlStatus = remoteStatus,
            isSharedPublicly = isShared,
            publicPearlID = if (remoteStatus == "rejected") null else publicId,
            updatedAt = now,
        )
    }

    @Serializable
    private data class StatusRow(
        val id: String,
        val status: String,
    )
}
