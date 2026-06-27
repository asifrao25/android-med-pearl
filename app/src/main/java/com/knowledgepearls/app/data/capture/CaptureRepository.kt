package com.knowledgepearls.app.data.capture

import com.knowledgepearls.app.data.local.entity.KnowledgePearlContentKind
import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.local.entity.MediaType
import com.knowledgepearls.app.data.local.entity.PearlMediaEntity
import com.knowledgepearls.app.data.local.model.ClinicalCasePayload
import com.knowledgepearls.app.data.local.model.withClinicalCasePayload
import com.knowledgepearls.app.data.media.MediaStorage
import com.knowledgepearls.app.data.repository.KnowledgePearlRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CaptureRepository @Inject constructor(
    private val pearlRepository: KnowledgePearlRepository,
    private val mediaStorage: MediaStorage,
) {
    suspend fun saveQuickPearl(
        title: String,
        notes: String,
        sourceReference: String,
        tags: List<String>,
        mediaItems: List<PickedMedia>,
    ): String = saveStandardPearl(
        title = title,
        notes = notes,
        sourceReference = sourceReference,
        tags = tags,
        contentKind = if (mediaItems.isEmpty()) KnowledgePearlContentKind.QUICK else KnowledgePearlContentKind.STANDARD,
        sourceURL = null,
        linkPreviewImagePath = null,
        linkPreviewDescription = "",
        mediaItems = mediaItems,
    )

    suspend fun saveWebLinkPearl(
        title: String,
        notes: String,
        sourceReference: String,
        tags: List<String>,
        sourceURL: String,
        linkPreviewImagePath: String?,
        linkPreviewDescription: String,
    ): String = saveStandardPearl(
        title = title,
        notes = notes,
        sourceReference = sourceReference,
        tags = tags,
        contentKind = KnowledgePearlContentKind.STANDARD,
        sourceURL = sourceURL,
        linkPreviewImagePath = linkPreviewImagePath,
        linkPreviewDescription = linkPreviewDescription,
        mediaItems = emptyList(),
    )

    suspend fun saveMediaPearl(
        title: String,
        notes: String,
        sourceReference: String,
        tags: List<String>,
        mediaItems: List<PickedMedia>,
    ): String = saveStandardPearl(
        title = title,
        notes = notes,
        sourceReference = sourceReference,
        tags = tags,
        contentKind = KnowledgePearlContentKind.STANDARD,
        sourceURL = null,
        linkPreviewImagePath = null,
        linkPreviewDescription = "",
        mediaItems = mediaItems,
    )

    suspend fun saveClinicalCasePearl(
        title: String,
        payload: ClinicalCasePayload,
        tags: List<String>,
        sectionMedia: Map<String, List<PickedMedia>>,
    ): String {
        val pearlId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val pearl = KnowledgePearlEntity(
            id = pearlId,
            title = title.trim(),
            notes = payload.history.trim(),
            sourceReference = payload.references.trim(),
            createdAt = now,
            updatedAt = now,
            tags = tags,
        ).withClinicalCasePayload(payload)

        pearlRepository.upsertPearl(pearl)

        val mediaEntities = sectionMedia.flatMap { (section, items) ->
            items.map { picked ->
                PearlMediaEntity(
                    pearlId = pearlId,
                    type = picked.type,
                    localPath = mediaStorage.saveBytes(picked.bytes, extensionFor(picked.filename)),
                    filename = picked.filename,
                    sectionTag = section,
                )
            }
        }
        if (mediaEntities.isNotEmpty()) {
            pearlRepository.upsertMediaItems(mediaEntities)
        }
        return pearlId
    }

    private suspend fun saveStandardPearl(
        title: String,
        notes: String,
        sourceReference: String,
        tags: List<String>,
        contentKind: String,
        sourceURL: String?,
        linkPreviewImagePath: String?,
        linkPreviewDescription: String,
        mediaItems: List<PickedMedia>,
    ): String {
        val pearlId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        pearlRepository.upsertPearl(
            KnowledgePearlEntity(
                id = pearlId,
                title = title.trim(),
                notes = notes.trim(),
                sourceURL = sourceURL,
                linkPreviewImagePath = linkPreviewImagePath,
                linkPreviewDescription = linkPreviewDescription.trim(),
                sourceReference = sourceReference.trim(),
                createdAt = now,
                updatedAt = now,
                tags = tags,
                contentKind = contentKind,
            ),
        )

        if (mediaItems.isNotEmpty()) {
            pearlRepository.upsertMediaItems(
                mediaItems.map { picked ->
                    PearlMediaEntity(
                        pearlId = pearlId,
                        type = picked.type,
                        localPath = mediaStorage.saveBytes(picked.bytes, extensionFor(picked.filename)),
                        filename = picked.filename,
                        sectionTag = picked.sectionTag,
                    )
                },
            )
        }
        return pearlId
    }

    private fun extensionFor(filename: String): String =
        filename.substringAfterLast('.', "bin").ifBlank { "bin" }
}
