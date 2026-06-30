package com.knowledgepearls.app.data.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PublicFeedWithdrawal @Inject constructor(
    private val sharingRepository: PublicFeedSharingRepository,
    private val pearlRepository: KnowledgePearlRepository,
) {
    suspend fun withdrawSubmission(publicPearlId: String) {
        sharingRepository.withdrawWithStorageCleanup(publicPearlId)
        pearlRepository.clearPublicPearlLink(publicPearlId)
    }
}
