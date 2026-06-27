package com.knowledgepearls.app.data.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InboxCountsRepository @Inject constructor(
    private val messagingRepository: MessagingRepository,
    private val pearlShareRepository: PearlShareRepository,
) {
    suspend fun unreadMessageCount(userId: String): Int =
        runCatching { messagingRepository.unreadMessageCount(userId) }.getOrDefault(0)

    suspend fun pendingShareCount(userId: String): Int =
        runCatching { pearlShareRepository.pendingShareCount(userId) }.getOrDefault(0)

    suspend fun totalInboxBadge(userId: String): Int =
        unreadMessageCount(userId) + pendingShareCount(userId)
}
