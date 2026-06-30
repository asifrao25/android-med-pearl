package com.knowledgepearls.app.data.repository

import com.knowledgepearls.app.data.local.entity.MediaType
import com.knowledgepearls.app.data.local.entity.PearlMediaEntity
import com.knowledgepearls.app.data.media.MediaStorage
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.data.model.PublicPearlMediaItem
import com.knowledgepearls.app.data.remote.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage

object PublicPearlMediaImporter {
    suspend fun importMediaItems(
        pearlRepository: KnowledgePearlRepository,
        mediaStorage: MediaStorage,
        pearlId: String,
        items: List<PublicPearlMediaItem>,
        supabase: SupabaseClient? = null,
    ): MediaImportResult {
        if (items.isEmpty()) return MediaImportResult(attempted = 0, imported = 0)
        var imported = 0
        items.forEach { item ->
            val success = runCatching {
                val filename = item.resolvedFilename
                val extension = filename.substringAfterLast('.', "jpg").ifBlank { "jpg" }
                val localPath = downloadToLocalPath(
                    item = item,
                    mediaStorage = mediaStorage,
                    extension = extension,
                    supabase = supabase,
                )
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
        supabase: SupabaseClient? = null,
    ): MediaImportResult {
        val items = pearl.resolvedMediaItems
        if (items.isEmpty()) return MediaImportResult(attempted = 0, imported = 0)
        return importMediaItems(pearlRepository, mediaStorage, pearlId, items, supabase)
    }

    private suspend fun downloadToLocalPath(
        item: PublicPearlMediaItem,
        mediaStorage: MediaStorage,
        extension: String,
        supabase: SupabaseClient?,
    ): String {
        val remoteUrl = item.loadableUrl
        if (remoteUrl != null) {
            runCatching {
                return mediaStorage.saveFromUrl(remoteUrl, extension)
            }
        }

        val storagePath = item.path?.trim()?.trimStart('/').orEmpty()
        if (storagePath.isNotEmpty() && supabase != null) {
            val bytes = supabase.storage
                .from(SupabaseConfig.PUBLIC_PEARL_MEDIA_BUCKET)
                .downloadAuthenticated(storagePath)
            return mediaStorage.saveBytes(bytes, extension)
        }

        if (remoteUrl != null) {
            return mediaStorage.saveFromUrl(remoteUrl, extension)
        }

        throw IllegalStateException("No downloadable media source for ${item.resolvedFilename}")
    }
}
