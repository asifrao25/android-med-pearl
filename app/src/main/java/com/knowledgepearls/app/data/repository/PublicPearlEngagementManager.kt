package com.knowledgepearls.app.data.repository

import com.knowledgepearls.app.data.model.PublicPearl
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class PublicPearlEngagementManager @Inject constructor(
    private val engagementRepository: PublicFeedEngagementRepository,
    private val accountRepository: AccountRepository,
    private val pearlRepository: KnowledgePearlRepository,
) {
    private val _likedPearlIds = MutableStateFlow<Set<String>>(emptySet())
    val likedPearlIds: StateFlow<Set<String>> = _likedPearlIds.asStateFlow()

    private val _likeCountOverrides = MutableStateFlow<Map<String, Int>>(emptyMap())
    val likeCountOverrides: StateFlow<Map<String, Int>> = _likeCountOverrides.asStateFlow()

    fun isLiked(pearlId: String): Boolean =
        pearlId.lowercase() in _likedPearlIds.value

    fun likeCount(pearl: PublicPearl): Int =
        _likeCountOverrides.value[pearl.id.lowercase()] ?: pearl.likeCount

    fun mergeServerPearls(pearls: List<PublicPearl>) {
        if (pearls.isEmpty()) return
        _likeCountOverrides.update { current ->
            val merged = current.toMutableMap()
            pearls.forEach { pearl ->
                merged[pearl.id.lowercase()] = pearl.likeCount
            }
            merged
        }
    }

    suspend fun syncLikedState(pearlIds: List<String>) {
        val userId = accountRepository.currentUserId()?.takeIf { it.isNotBlank() } ?: return
        val normalized = pearlIds.map { it.lowercase() }.distinct().filter { it.isNotBlank() }
        if (normalized.isEmpty()) return
        runCatching {
            val liked = engagementRepository.fetchLikedPearlIds(userId, normalized)
            _likedPearlIds.update { current -> current + liked }
        }
    }

    suspend fun toggleLike(pearl: PublicPearl): Result<Int> {
        val userId = accountRepository.currentUserId()?.takeIf { it.isNotBlank() }
            ?: return Result.failure(IllegalStateException("Sign in to like pearls."))
        val pearlId = pearl.id.lowercase()
        val currentlyLiked = pearlId in _likedPearlIds.value
        return runCatching {
            if (currentlyLiked) {
                engagementRepository.unlike(pearl.id)
            } else {
                engagementRepository.like(pearl.id)
            }
            val delta = if (currentlyLiked) -1 else 1
            val newCount = (likeCount(pearl) + delta).coerceAtLeast(0)

            _likedPearlIds.update { liked ->
                liked.toMutableSet().apply {
                    if (currentlyLiked) remove(pearlId) else add(pearlId)
                }
            }
            _likeCountOverrides.update { counts ->
                counts + (pearlId to newCount)
            }
            pearlRepository.updatePublicFeedSnapshotEngagement(pearl.id, newCount)
            newCount
        }
    }
}
