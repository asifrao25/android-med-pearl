package com.knowledgepearls.app.data.repository

import android.content.Context
import com.knowledgepearls.app.data.local.entity.KnowledgePearlContentKind
import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.local.model.withClinicalCasePayload
import com.knowledgepearls.app.data.model.PublicPearl
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class PublicFeedRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val supabase: SupabaseClient,
    private val pearlRepository: KnowledgePearlRepository,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchPage(offset: Int, limit: Int = PAGE_SIZE): List<PublicPearl> {
        val end = offset + limit - 1
        return supabase.from("public_pearls").select {
            filter {
                eq("status", "approved")
            }
            order(column = "created_at", order = Order.DESCENDING)
            range(offset.toLong(), end.toLong())
        }.decodeList<PublicPearl>()
    }

    fun getSeenIds(): Set<String> = prefs.getStringSet(KEY_SEEN, emptySet()).orEmpty()

    fun getHiddenIds(): Set<String> = prefs.getStringSet(KEY_HIDDEN, emptySet()).orEmpty()

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

    suspend fun addToMyFeed(pearl: PublicPearl): KnowledgePearlEntity {
        val existing = pearlRepository.getAllPearls().firstOrNull { it.publicPearlID == pearl.id }
        if (existing != null) return existing

        val now = System.currentTimeMillis()
        var entity = KnowledgePearlEntity(
            title = pearl.titleDisplay,
            notes = pearl.notes,
            sourceURL = pearl.sourceUrl,
            linkPreviewDescription = pearl.linkPreviewDescription.orEmpty(),
            sourceReference = pearl.effectiveSourceReference,
            createdAt = pearl.createdAtMillis ?: now,
            updatedAt = now,
            tags = pearl.tags,
            publicPearlID = pearl.id,
            publicPearlStatus = pearl.status,
            isSharedPublicly = true,
            publicFeedSnapshot = runCatching { json.encodeToString(pearl) }.getOrDefault(""),
        )

        if (pearl.isClinicalCase) {
            entity = pearl.casePayload?.let { entity.withClinicalCasePayload(it) }
                ?: entity.copy(contentKind = KnowledgePearlContentKind.CLINICAL_CASE)
        }

        pearlRepository.upsertPearl(entity)
        return entity
    }

    companion object {
        const val PAGE_SIZE = 20
        private const val PREFS_NAME = "public_feed_prefs"
        private const val KEY_SEEN = "publicFeedSeenIDs"
        private const val KEY_HIDDEN = "publicFeedHiddenIDs"
    }
}
