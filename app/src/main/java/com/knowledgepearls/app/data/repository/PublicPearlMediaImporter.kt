package com.knowledgepearls.app.data.repository

import com.knowledgepearls.app.data.local.entity.MediaType
import com.knowledgepearls.app.data.local.entity.PearlMediaEntity
import com.knowledgepearls.app.data.media.MediaStorage
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.data.model.PublicPearlMediaItem

object PublicPearlMediaImporter {
    suspend fun importMediaItems(
        pearlRepository: KnowledgePearlRepository,
        mediaStorage: MediaStorage,
        pearlId: String,
        items: List<PublicPearlMediaItem>,
    ): MediaImportResult {
        if (items.isEmpty()) return MediaImportResult(attempted = 0, imported = 0)
        var imported = 0
        items.forEach { item ->
            val remoteUrl = item.loadableUrl ?: return@forEach
            val success = runCatching {
                val filename = item.resolvedFilename
                val extension = filename.substringAfterLast('.', "jpg")
                val localPath = mediaStorage.saveFromUrl(remoteUrl, extension)
                val type = when {
                    item.isVideo -> MediaType.VIDEO
                    item.isDocument -> MediaType.DOCUMENT
                    else -> MediaType.IMAGE
                }
                pearlRepository.upsertMedia(
                    PearlMediaEntity(
                        pearlId = pearlId,
                        type = type,
                        localPath = localPath,
                        filename = filename,
                        sectionTag = item.section.orEmpty(),
                    ),
                )
            }.isSuccess
            if (success) imported++
        }
        return MediaImportResult(attempted = items.size, imported = imported)
    }

    suspend fun importFromPublicPearl(
        pearlRepository: KnowledgePearlRepository,
        mediaStorage: MediaStorage,
        pearlId: String,
        pearl: PublicPearl,
    ): MediaImportResult {
        val items = pearl.resolvedMediaItems
        if (items.isEmpty()) return MediaImportResult(attempted = 0, imported = 0)
        return importMediaItems(pearlRepository, mediaStorage, pearlId, items)
    }
}
