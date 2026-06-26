package com.knowledgepearls.app.data.sync

import com.knowledgepearls.app.data.local.entity.KnowledgePearlContentKind
import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.local.model.ClinicalCasePayload
import com.knowledgepearls.app.data.local.model.withClinicalCasePayload
import com.knowledgepearls.app.data.repository.KnowledgePearlRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OwnedPublicPearlSyncRunnerImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val repository: KnowledgePearlRepository,
) : OwnedPublicPearlSyncRunner {
    override suspend fun importMissingOwnedPearls(userId: String) {
        runCatching {
            val rows = supabase.from("public_pearls").select {
                filter {
                    eq("user_id", userId.lowercase())
                    eq("status", "approved")
                }
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                limit(100)
            }.decodeList<OwnedPearlRow>()

            if (rows.isEmpty()) return

            val existingIds = repository.getExistingPublicPearlIds()
            val now = System.currentTimeMillis()

            rows.forEach { row ->
                if (existingIds.contains(row.id)) return@forEach

                var pearl = KnowledgePearlEntity(
                    title = row.title.ifBlank { "Untitled" },
                    notes = row.notes,
                    sourceURL = row.sourceUrl,
                    linkPreviewDescription = row.linkPreviewDescription.orEmpty(),
                    sourceReference = row.sourceReference.orEmpty(),
                    createdAt = now,
                    updatedAt = now,
                    tags = row.tags,
                    publicPearlID = row.id,
                    publicPearlStatus = row.status,
                    isSharedPublicly = true,
                )

                if (row.contentType == KnowledgePearlContentKind.CLINICAL_CASE) {
                    pearl = row.casePayload?.let { pearl.withClinicalCasePayload(it) } ?: pearl.copy(
                        contentKind = KnowledgePearlContentKind.CLINICAL_CASE,
                    )
                }

                repository.upsertPearl(pearl)
            }
        }
    }

    @Serializable
    private data class OwnedPearlRow(
        val id: String,
        val title: String = "",
        val notes: String = "",
        val tags: List<String> = emptyList(),
        @SerialName("content_type") val contentType: String = KnowledgePearlContentKind.STANDARD,
        @SerialName("source_url") val sourceUrl: String? = null,
        @SerialName("link_preview_description") val linkPreviewDescription: String? = null,
        @SerialName("source_reference") val sourceReference: String? = null,
        val status: String = "",
        @SerialName("case_payload") val casePayload: ClinicalCasePayload? = null,
    )
}
