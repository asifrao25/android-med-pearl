package com.knowledgepearls.app.data.repository

import android.content.Context
import com.knowledgepearls.app.data.local.entity.KnowledgePearlContentKind
import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.local.model.isClinicalCase
import com.knowledgepearls.app.data.local.model.withClinicalCasePayload
import com.knowledgepearls.app.data.media.MediaStorage
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.data.model.normalizeUserId
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class PublicFeedRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val supabase: SupabaseClient,
    private val pearlRepository: KnowledgePearlRepository,
    private val mediaStorage: MediaStorage,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }
    private val addToMyFeedMutex = Mutex()

    suspend fun fetchPearlById(pearlId: String): PublicPearl? = runCatching {
        supabase.postgrest.rpc(
            "fetch_public_pearl",
            FetchPublicPearlParams(pPearlId = pearlId.lowercase()),
        ).decodeSingle<PublicPearl>()
    }.getOrNull()

    suspend fun fetchPage(offset: Int, limit: Int = PAGE_SIZE): List<PublicPearl> {
        val rpcPage = runCatching {
            supabase.postgrest.rpc(
                "list_approved_public_pearls",
                ListApprovedPublicPearlsParams(limit = limit, offset = offset),
            ).decodeList<PublicPearl>()
        }

        rpcPage.onSuccess { page ->
            if (page.isNotEmpty() || offset > 0) return page
        }

        val directPage = runCatching { fetchApprovedPageDirect(offset, limit) }
        directPage.onSuccess { page ->
            if (page.isNotEmpty()) return page
        }

        return rpcPage.getOrElse { rpcError ->
            directPage.getOrElse { throw rpcError }
        }
    }

    private suspend fun fetchApprovedPageDirect(offset: Int, limit: Int): List<PublicPearl> =
        supabase.from("public_pearls").select {
            filter {
                eq("status", "approved")
            }
            order(column = "created_at", order = Order.DESCENDING)
            range(offset.toLong(), (offset + limit - 1).toLong())
        }.decodeList()

    fun getSeenIds(): Set<String> = prefs.getStringSet(KEY_SEEN, emptySet()).orEmpty()

    fun getHiddenIds(): Set<String> = prefs.getStringSet(KEY_HIDDEN, emptySet()).orEmpty()

    fun getBlockedUserIds(): Set<String> = prefs.getStringSet(KEY_BLOCKED_USERS, emptySet()).orEmpty()

    fun isUserBlocked(userId: String): Boolean =
        normalizeUserId(userId) in getBlockedUserIds()

    fun blockUser(userId: String) {
        val ids = getBlockedUserIds().toMutableSet()
        ids.add(normalizeUserId(userId))
        prefs.edit().putStringSet(KEY_BLOCKED_USERS, ids).apply()
    }

    fun markSeen(id: String) {
        val ids = getSeenIds().toMutableSet()
        ids.add(id)
        prefs.edit().putStringSet(KEY_SEEN, ids).apply()
    }

    fun markUnseen(id: String) {
        val ids = getSeenIds().toMutableSet()
        ids.remove(id)
        prefs.edit().putStringSet(KEY_SEEN, ids).apply()
    }

    fun hide(id: String) {
        val ids = getHiddenIds().toMutableSet()
        ids.add(id)
        prefs.edit().putStringSet(KEY_HIDDEN, ids).apply()
    }

    suspend fun addToMyFeed(pearl: PublicPearl, currentUserId: String?): AddToMyFeedResult =
        addToMyFeedMutex.withLock {
            val existing = pearlRepository.findByPublicPearlId(pearl.id)
            if (existing != null) {
                val mediaImport = backfillMediaIfNeeded(existing.id, pearl)
                syncLocalCopy(
                    existing = existing,
                    pearl = pearl,
                    ownedByCurrentUser = isOwnPublicPearl(pearl, currentUserId),
                    bumpFeedPosition = false,
                )
                return@withLock AddToMyFeedResult.AlreadyInFeed(existing, mediaImport)
            }

            val now = System.currentTimeMillis()
            val ownedByUser = isOwnPublicPearl(pearl, currentUserId)
            val entity = buildLocalPearlEntity(
                pearl = pearl,
                now = now,
                ownedByUser = ownedByUser,
            )

            pearlRepository.upsertPearl(entity)
            val mediaImport = PublicPearlMediaImporter.importFromPublicPearl(
                pearlRepository = pearlRepository,
                mediaStorage = mediaStorage,
                pearlId = entity.id,
                pearl = pearl,
            )
            AddToMyFeedResult.Saved(entity, mediaImport)
        }

    suspend fun saveToFolder(
        pearl: PublicPearl,
        folderId: String,
        currentUserId: String?,
    ): AddToMyFeedResult {
        return when (val result = addToMyFeed(pearl, currentUserId)) {
            is AddToMyFeedResult.Saved -> {
                pearlRepository.addPearlToFolder(result.pearl.id, folderId)
                result
            }
            is AddToMyFeedResult.AlreadyInFeed -> {
                pearlRepository.addPearlToFolder(result.pearl.id, folderId)
                result
            }
        }
    }

    suspend fun createFolder(name: String) = pearlRepository.createFolder(name.trim())

    private suspend fun backfillMediaIfNeeded(pearlId: String, pearl: PublicPearl): MediaImportResult? {
        if (pearl.resolvedMediaItems.isEmpty() || pearlRepository.mediaCountForPearl(pearlId) > 0) {
            return null
        }
        return PublicPearlMediaImporter.importFromPublicPearl(
            pearlRepository = pearlRepository,
            mediaStorage = mediaStorage,
            pearlId = pearlId,
            pearl = pearl,
        )
    }

    private suspend fun syncLocalCopy(
        existing: KnowledgePearlEntity,
        pearl: PublicPearl,
        ownedByCurrentUser: Boolean,
        bumpFeedPosition: Boolean,
    ) {
        val now = System.currentTimeMillis()
        val updated = existing.copy(
            title = pearl.titleDisplay,
            notes = if (existing.isClinicalCase()) existing.notes else pearl.notes,
            tags = pearl.tags,
            sourceReference = pearl.effectiveSourceReference,
            sourceURL = pearl.sourceUrl ?: existing.sourceURL,
            linkPreviewDescription = pearl.linkPreviewDescription ?: existing.linkPreviewDescription,
            publicPearlStatus = pearl.status,
            updatedAt = if (bumpFeedPosition) now else existing.updatedAt,
            publicFeedSnapshot = if (ownedByCurrentUser) {
                ""
            } else {
                runCatching { json.encodeToString(pearl) }.getOrDefault(existing.publicFeedSnapshot)
            },
        )
        val synced = when {
            pearl.isClinicalCase && pearl.casePayload != null ->
                updated.withClinicalCasePayload(pearl.casePayload)
            pearl.isClinicalCase ->
                updated.copy(contentKind = KnowledgePearlContentKind.CLINICAL_CASE)
            else -> updated
        }
        pearlRepository.updatePearl(synced)
    }

    private fun buildLocalPearlEntity(
        pearl: PublicPearl,
        now: Long,
        ownedByUser: Boolean,
    ): KnowledgePearlEntity {
        var entity = KnowledgePearlEntity(
            title = pearl.titleDisplay,
            notes = pearl.notes,
            sourceURL = pearl.sourceUrl,
            linkPreviewDescription = pearl.linkPreviewDescription.orEmpty(),
            sourceReference = pearl.effectiveSourceReference,
            createdAt = now,
            updatedAt = now,
            tags = pearl.tags,
            publicPearlID = pearl.id,
            publicPearlStatus = pearl.status,
            isSharedPublicly = ownedByUser,
            publicFeedSnapshot = if (ownedByUser) {
                ""
            } else {
                runCatching { json.encodeToString(pearl) }.getOrDefault("")
            },
        )

        entity = when {
            pearl.isClinicalCase -> {
                pearl.casePayload?.let { entity.withClinicalCasePayload(it) }
                    ?: entity.copy(contentKind = KnowledgePearlContentKind.CLINICAL_CASE)
            }
            pearl.isQuickPearl -> entity.copy(contentKind = KnowledgePearlContentKind.QUICK)
            else -> entity.copy(contentKind = KnowledgePearlContentKind.STANDARD)
        }
        return entity
    }

    private fun isOwnPublicPearl(pearl: PublicPearl, currentUserId: String?): Boolean {
        val userId = currentUserId?.trim().orEmpty()
        if (userId.isEmpty()) return false
        return normalizeUserId(pearl.userId) == normalizeUserId(userId)
    }

    companion object {
        const val PAGE_SIZE = 20
        private const val PREFS_NAME = "public_feed_prefs"
        private const val KEY_SEEN = "publicFeedSeenIDs"
        private const val KEY_HIDDEN = "publicFeedHiddenIDs"
        private const val KEY_BLOCKED_USERS = "publicFeedBlockedUserIDs"
    }
}

@Serializable
private data class ListApprovedPublicPearlsParams(
    @SerialName("p_limit") val limit: Int,
    @SerialName("p_offset") val offset: Int,
)

@Serializable
private data class FetchPublicPearlParams(
    @SerialName("p_pearl_id") val pPearlId: String,
)
