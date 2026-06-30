package com.knowledgepearls.app.data.backup

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.local.entity.PearlFolderCrossRef
import com.knowledgepearls.app.data.local.entity.PearlMediaEntity
import com.knowledgepearls.app.data.media.MediaStorage
import com.knowledgepearls.app.data.repository.KnowledgePearlRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pearlRepository: KnowledgePearlRepository,
    private val mediaStorage: MediaStorage,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }
    private val backupDir: File
        get() = File(context.filesDir, "Backups").also { it.mkdirs() }

    private val restorer: BackupRestorer
        get() = BackupRestorer(pearlRepository, mediaStorage)

    suspend fun createBackup(): BackupFileInfo {
        val payload = buildPayload()
        val stamp = SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss", Locale.US).format(Date())
        val file = File(backupDir, "${BackupFormat.FILE_PREFIX}$stamp.json")
        file.writeText(json.encodeToString(BackupPayloadV2.serializer(), payload))
        return file.toBackupFileInfo(payload)
    }

    suspend fun listBackups(): List<BackupFileInfo> =
        backupDir.listFiles()
            ?.filter { file ->
                file.isFile &&
                    file.extension.equals("json", ignoreCase = true) &&
                    (file.name.startsWith(BackupFormat.FILE_PREFIX) ||
                        file.name.startsWith(BackupFormat.LEGACY_FILE_PREFIX))
            }
            ?.mapNotNull { file ->
                runCatching {
                    val payload = readPayloadFromText(file.readText())
                    file.toBackupFileInfo(payload)
                }.getOrNull()
            }
            ?.sortedByDescending { it.createdAt }
            .orEmpty()

    suspend fun loadPayloadFromPath(path: String): BackupPayloadV2 =
        readPayloadFromText(File(path).readText())

    suspend fun loadPayloadFromUri(uri: Uri): BackupPayloadV2 {
        val text = context.contentResolver.openInputStream(uri)?.use { input ->
            input.bufferedReader().readText()
        } ?: error("Could not read the selected backup file.")
        return readPayloadFromText(text)
    }

    suspend fun previewMerge(payload: BackupPayloadV2): RestorePreview =
        restorer.previewMerge(payload)

    suspend fun previewReplace(payload: BackupPayloadV2): RestorePreview =
        restorer.previewReplace(payload)

    suspend fun merge(payload: BackupPayloadV2): BackupRestoreSummary =
        restorer.merge(payload)

    suspend fun replace(payload: BackupPayloadV2): BackupRestoreSummary =
        restorer.replace(payload)

    fun copyBackupToUri(sourcePath: String, destinationUri: Uri) {
        val bytes = File(sourcePath).readBytes()
        context.contentResolver.openOutputStream(destinationUri)?.use { output ->
            output.write(bytes)
        } ?: error("Could not save the backup file.")
    }

    fun shareableBackupFile(path: String): File {
        val file = File(path)
        require(file.exists()) { "Backup file not found." }
        return file
    }

    private suspend fun buildPayload(): BackupPayloadV2 {
        val pearls = pearlRepository.getAllPearls()
        val folders = pearlRepository.observeFolders().first()
        val crossRefs = pearlRepository.getAllFolderCrossRefs()
        val folderIdsByPearl = crossRefs.groupBy(PearlFolderCrossRef::pearlId)
            .mapValues { entry -> entry.value.map(PearlFolderCrossRef::folderId) }
        val mediaByPearl = pearlRepository.getAllMedia().groupBy(PearlMediaEntity::pearlId)

        return BackupPayloadV2(
            createdAt = System.currentTimeMillis(),
            folders = folders.map { folder ->
                BackupFolderDto(
                    id = folder.id,
                    name = folder.name,
                    createdAt = folder.createdAt,
                )
            },
            pearls = pearls.map { pearl ->
                val media = mediaByPearl[pearl.id].orEmpty().map { item ->
                    val bytes = readBoundedFileBytes(item.localPath)
                    BackupMediaDto(
                        id = item.id,
                        type = item.type,
                        filename = item.filename,
                        sectionTag = item.sectionTag,
                        createdAt = item.createdAt,
                        dataBase64 = bytes?.let { encoded -> Base64.encodeToString(encoded, Base64.NO_WRAP) },
                    )
                }
                BackupPearlDto(
                    id = pearl.id,
                    title = pearl.title,
                    notes = pearl.notes,
                    sourceURL = pearl.sourceURL,
                    sourceReference = pearl.sourceReference,
                    linkPreviewDescription = pearl.linkPreviewDescription,
                    linkPreviewImageBase64 = readBoundedFileBytes(pearl.linkPreviewImagePath)
                        ?.let { bytes -> Base64.encodeToString(bytes, Base64.NO_WRAP) },
                    createdAt = pearl.createdAt,
                    updatedAt = pearl.updatedAt,
                    tags = pearl.tags,
                    contentKind = pearl.contentKind,
                    casePayloadJSON = pearl.casePayloadJSON,
                    isSharedPublicly = pearl.isSharedPublicly,
                    publicPearlID = pearl.publicPearlID,
                    publicPearlStatus = pearl.publicPearlStatus,
                    publicFeedSnapshot = pearl.publicFeedSnapshot,
                    isSharedFromFriend = pearl.isSharedFromFriend,
                    sharedByUserID = pearl.sharedByUserID,
                    sharedByName = pearl.sharedByName,
                    sharedByAvatarURL = pearl.sharedByAvatarURL,
                    friendShareID = pearl.friendShareID,
                    isFavourite = pearl.isFavourite,
                    folderIds = folderIdsByPearl[pearl.id].orEmpty(),
                    media = media,
                )
            },
        )
    }

    private fun readPayloadFromText(text: String): BackupPayloadV2 {
        val root = json.parseToJsonElement(text).jsonObject
        return when (root.formatVersionOrNull()) {
            BackupFormat.CURRENT_VERSION -> json.decodeFromString(BackupPayloadV2.serializer(), text)
            1, null -> legacyPayloadToV2(json.decodeFromString(LegacyBackupPayloadV1.serializer(), text))
            else -> json.decodeFromString(BackupPayloadV2.serializer(), text)
        }
    }

    private fun JsonObject.formatVersionOrNull(): Int? =
        this["format_version"]?.jsonPrimitive?.int
            ?: this["version"]?.jsonPrimitive?.int

    private fun legacyPayloadToV2(legacy: LegacyBackupPayloadV1): BackupPayloadV2 =
        BackupPayloadV2(
            formatVersion = BackupFormat.CURRENT_VERSION,
            createdAt = legacy.exportedAt,
            folders = legacy.folders.map { folder ->
                BackupFolderDto(
                    id = folder.id,
                    name = folder.name,
                    createdAt = legacy.exportedAt,
                )
            },
            pearls = legacy.pearls.map { pearl ->
                BackupPearlDto(
                    id = pearl.id,
                    title = pearl.title,
                    notes = pearl.notes,
                    sourceURL = pearl.sourceURL,
                    createdAt = legacy.exportedAt,
                    updatedAt = legacy.exportedAt,
                    tags = pearl.tags,
                    contentKind = pearl.contentKind,
                    casePayloadJSON = pearl.casePayloadJSON,
                    isFavourite = pearl.isFavourite,
                )
            },
        )

    private fun readBoundedFileBytes(path: String?): ByteArray? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        if (!file.exists() || !file.isFile) return null
        if (file.length() > MAX_MEDIA_BYTES) return null
        return runCatching { file.readBytes() }.getOrNull()
    }

    private fun File.toBackupFileInfo(payload: BackupPayloadV2): BackupFileInfo =
        BackupFileInfo(
            id = name,
            path = absolutePath,
            createdAt = payload.createdAt,
            pearlCount = payload.pearlCount,
            folderCount = payload.folderCount,
            mediaCount = payload.mediaCount,
            fileSizeBytes = length(),
        )

    data class BackupFileInfo(
        val id: String,
        val path: String,
        val createdAt: Long,
        val pearlCount: Int,
        val folderCount: Int,
        val mediaCount: Int,
        val fileSizeBytes: Long,
    )

    private companion object {
        const val MAX_MEDIA_BYTES = 100L * 1024L * 1024L
    }
}
