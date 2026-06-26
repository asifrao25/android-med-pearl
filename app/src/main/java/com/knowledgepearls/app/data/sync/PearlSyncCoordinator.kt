package com.knowledgepearls.app.data.sync

import com.knowledgepearls.app.data.repository.KnowledgePearlRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StubOwnedPublicPearlSyncRunner @Inject constructor(
    private val repository: KnowledgePearlRepository,
) : OwnedPublicPearlSyncRunner {
    override suspend fun importMissingOwnedPearls(userId: String) {
        // Stage 4: query Supabase public_pearls and insert missing rows locally.
        repository.getPearlsWithPublicPearlId()
    }
}

@Singleton
class StubPublicFeedStatusSyncRunner @Inject constructor(
    private val repository: KnowledgePearlRepository,
) : PublicFeedStatusSyncRunner {
    override suspend fun syncLocalPearlStatuses(userId: String) {
        // Stage 4: refresh publicPearlStatus from Supabase for local pearls.
        repository.getPearlsWithPublicPearlId()
    }
}

@Singleton
class PearlSyncCoordinator @Inject constructor(
    private val ownedPublicPearlSync: OwnedPublicPearlSyncRunner,
    private val publicFeedStatusSync: PublicFeedStatusSyncRunner,
) {
    suspend fun runIfAuthenticated(userId: String?) {
        val id = userId?.trim().orEmpty()
        if (id.isEmpty()) return
        ownedPublicPearlSync.importMissingOwnedPearls(id)
        publicFeedStatusSync.syncLocalPearlStatuses(id)
    }
}
