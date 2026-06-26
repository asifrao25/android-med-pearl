package com.knowledgepearls.app.data.sync

import javax.inject.Inject
import javax.inject.Singleton

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
