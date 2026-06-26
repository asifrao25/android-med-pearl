package com.knowledgepearls.app.data.sync

/**
 * Imports pearls the signed-in user authored on the server into local Room
 * when they are not already present in My Feed. Implemented in Stage 4.
 */
interface OwnedPublicPearlSyncRunner {
    suspend fun importMissingOwnedPearls(userId: String)
}

/**
 * Keeps local [publicPearlStatus] in sync with Supabase after moderation.
 * Implemented in Stage 4.
 */
interface PublicFeedStatusSyncRunner {
    suspend fun syncLocalPearlStatuses(userId: String)
}
