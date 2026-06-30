package com.knowledgepearls.app.data.backup

import android.util.Base64
import com.knowledgepearls.app.data.local.entity.FolderEntity
import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.local.entity.PearlMediaEntity
import com.knowledgepearls.app.data.media.MediaStorage
import com.knowledgepearls.app.data.repository.KnowledgePearlRepository

internal class BackupRestorer(
    private val pearlRepository: KnowledgePearlRepository,
    private val mediaStorage: MediaStorage,
) {
    suspend fun previewMerge(payload: BackupPayloadV2): RestorePreview {
        val foldersById = pearlRepository.getAllFolders().associateBy { it.id }
        val foldersToAdd = payload.folders.count { it.id !in foldersById }

        val pearlsById = pearlRepository.getAllPearls().associateBy { it.id }
        var toAdd = 0
        var toUpdate = 0
        var unchanged = 0
        for (dto in payload.pearls) {
            val existing = pearlsById[dto.id]
            when {
                existing == null -> toAdd++
                dto.updatedAt > existing.updatedAt -> toUpdate++
                else -> unchanged++
            }
        }

        return RestorePreview(
            pearlsToAdd = toAdd,
            pearlsToUpdate = toUpdate,
            pearlsUnchanged = unchanged,
            pearlsToRemove = 0,
            foldersToAdd = foldersToAdd,
            mediaFilesInBackup = payload.mediaCount,
            backupCreatedAt = payload.createdAt,
        )
    }

    suspend fun previewReplace(payload: BackupPayloadV2): RestorePreview {
        val backupIds = payload.pearls.map { it.id }.toSet()
        val removed = pearlRepository.getAllPearls().count { it.id !in backupIds }
        return RestorePreview(
            pearlsToAdd = payload.pearls.size,
            pearlsToUpdate = 0,
            pearlsUnchanged = 0,
            pearlsToRemove = removed,
            foldersToAdd = payload.folders.size,
            mediaFilesInBackup = payload.mediaCount,
            backupCreatedAt = payload.createdAt,
        )
    }

    suspend fun merge(payload: BackupPayloadV2): BackupRestoreSummary {
        var pearlsAdded = 0
        var pearlsUpdated = 0
        var foldersAdded = 0
        var mediaRestored = 0
        var mediaSkipped = 0

        val foldersById = pearlRepository.getAllFolders().associateBy { it.id }.toMutableMap()
        for (folderDto in payload.folders) {
            if (folderDto.id !in foldersById) {
                val folder = FolderEntity(
                    id = folderDto.id,
                    name = folderDto.name,
                    createdAt = folderDto.createdAt,
                )
                pearlRepository.upsertFolder(folder)
                foldersById[folderDto.id] = folder
                foldersAdded++
            }
        }

        val pearlsById = pearlRepository.getAllPearls().associateBy { it.id }
        for (dto in payload.pearls) {
            val existing = pearlsById[dto.id]
            when {
                existing == null -> {
                    val counts = importPearl(dto, foldersById.keys)
                    mediaRestored += counts.first
                    mediaSkipped += counts.second
                    pearlsAdded++
                }
                dto.updatedAt > existing.updatedAt -> {
                    deletePearlMediaFiles(existing)
                    val counts = importPearl(dto, foldersById.keys)
                    mediaRestored += counts.first
                    mediaSkipped += counts.second
                    pearlsUpdated++
                }
            }
        }

        return BackupRestoreSummary(
            pearlsAdded = pearlsAdded,
            pearlsUpdated = pearlsUpdated,
            foldersAdded = foldersAdded,
            mediaRestored = mediaRestored,
            mediaSkipped = mediaSkipped,
        )
    }

    suspend fun replace(payload: BackupPayloadV2): BackupRestoreSummary {
        deleteAllLocalMediaFiles()
        pearlRepository.clearAllLocalLibrary()

        val folderIds = mutableSetOf<String>()
        for (folderDto in payload.folders) {
            pearlRepository.upsertFolder(
                FolderEntity(
                    id = folderDto.id,
                    name = folderDto.name,
                    createdAt = folderDto.createdAt,
                ),
            )
            folderIds.add(folderDto.id)
        }

        var mediaRestored = 0
        var mediaSkipped = 0
        for (dto in payload.pearls) {
            val counts = importPearl(dto, folderIds)
            mediaRestored += counts.first
            mediaSkipped += counts.second
        }

        return BackupRestoreSummary(
            pearlsAdded = payload.pearls.size,
            pearlsUpdated = 0,
            foldersAdded = payload.folders.size,
            mediaRestored = mediaRestored,
            mediaSkipped = mediaSkipped,
        )
    }

    private suspend fun importPearl(
        dto: BackupPearlDto,
        knownFolderIds: Set<String>,
    ): Pair<Int, Int> {
        var mediaRestored = 0
        var mediaSkipped = 0

        val linkPreviewPath = dto.linkPreviewImageBase64?.let { encoded ->
            runCatching {
                val bytes = Base64.decode(encoded, Base64.NO_WRAP)
                mediaStorage.saveBytes(bytes, "jpg")
            }.getOrNull()
        }

        pearlRepository.upsertPearl(
            KnowledgePearlEntity(
                id = dto.id,
                title = dto.title,
                notes = dto.notes,
                sourceURL = dto.sourceURL,
                linkPreviewImagePath = linkPreviewPath,
                linkPreviewDescription = dto.linkPreviewDescription,
                sourceReference = dto.sourceReference,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt,
                tags = dto.tags,
                contentKind = dto.contentKind,
                casePayloadJSON = dto.casePayloadJSON,
                isSharedPublicly = dto.isSharedPublicly,
                publicPearlID = dto.publicPearlID,
                publicPearlStatus = dto.publicPearlStatus,
                publicFeedSnapshot = dto.publicFeedSnapshot,
                isSharedFromFriend = dto.isSharedFromFriend,
                sharedByUserID = dto.sharedByUserID,
                sharedByName = dto.sharedByName,
                sharedByAvatarURL = dto.sharedByAvatarURL,
                friendShareID = dto.friendShareID,
                isFavourite = dto.isFavourite,
            ),
        )

        pearlRepository.deleteMediaForPearl(dto.id)
        val restoredMedia = dto.media.mapNotNull { mediaDto ->
            val bytes = mediaDto.dataBase64?.let { encoded ->
                runCatching { Base64.decode(encoded, Base64.NO_WRAP) }.getOrNull()
            }
            if (bytes == null) {
                mediaSkipped++
                return@mapNotNull null
            }
            val extension = extensionFor(mediaDto.filename, fallbackForType(mediaDto.type))
            val localPath = runCatching {
                mediaStorage.saveBytes(bytes, extension)
            }.getOrNull()
            if (localPath == null) {
                mediaSkipped++
                return@mapNotNull null
            }
            mediaRestored++
            PearlMediaEntity(
                id = mediaDto.id,
                pearlId = dto.id,
                type = mediaDto.type,
                localPath = localPath,
                filename = mediaDto.filename,
                sectionTag = mediaDto.sectionTag,
                createdAt = mediaDto.createdAt,
            )
        }
        if (restoredMedia.isNotEmpty()) {
            pearlRepository.upsertMediaItems(restoredMedia)
        }

        pearlRepository.clearFolderMemberships(dto.id)
        dto.folderIds.filter { it in knownFolderIds }.forEach { folderId ->
            runCatching { pearlRepository.addPearlToFolder(dto.id, folderId) }
        }

        return mediaRestored to mediaSkipped
    }

    private suspend fun deletePearlMediaFiles(pearl: KnowledgePearlEntity) {
        mediaStorage.deleteFile(pearl.linkPreviewImagePath)
        pearlRepository.getAllMedia()
            .filter { it.pearlId == pearl.id }
            .forEach { mediaStorage.deleteFile(it.localPath) }
    }

    private suspend fun deleteAllLocalMediaFiles() {
        pearlRepository.getAllPearls().forEach { pearl ->
            mediaStorage.deleteFile(pearl.linkPreviewImagePath)
        }
        pearlRepository.getAllMedia().forEach { media ->
            mediaStorage.deleteFile(media.localPath)
        }
    }

    private fun extensionFor(filename: String, fallback: String): String {
        val ext = filename.substringAfterLast('.', "").lowercase()
        return ext.ifBlank { fallback }
    }

    private fun fallbackForType(type: String): String = when (type.lowercase()) {
        "video" -> "mp4"
        "pdf" -> "pdf"
        "document" -> "pdf"
        else -> "jpg"
    }
}
