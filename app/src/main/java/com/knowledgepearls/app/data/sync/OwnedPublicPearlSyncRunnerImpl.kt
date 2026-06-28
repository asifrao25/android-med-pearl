package com.knowledgepearls.app.data.sync

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OwnedPublicPearlSyncRunnerImpl @Inject constructor() : OwnedPublicPearlSyncRunner {
    override suspend fun importMissingOwnedPearls(userId: String) {
        // Approved community pearls belong in the Public Feed tab only.
    }
}
