package com.knowledgepearls.app.data.backup

import android.content.Context
import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.repository.KnowledgePearlRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pearlRepository: KnowledgePearlRepository,
) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val backupDir: File
        get() = File(context.filesDir, "Backups").also { it.mkdirs() }

    suspend fun createBackup(): BackupFileInfo {
        val pearls = pearlRepository.getAllPearls()
        val folders = pearlRepository.observeFolders().first()
        val payload = BackupPayload(
            version = 1,
            exportedAt = System.currentTimeMillis(),
            pearls = pearls.map { pearl ->
                BackupPearlDto(
                    id = pearl.id,
                    title = pearl.title,
                    notes = pearl.notes,
                    sourceURL = pearl.sourceURL,
                    tags = pearl.tags,
                    contentKind = pearl.contentKind,
                    casePayloadJSON = pearl.casePayloadJSON,
                    isFavourite = pearl.isFavourite,
                )
            },
            folders = folders.map { BackupFolder(it.id, it.name) },
        )
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val file = File(backupDir, "medpearls-backup-$stamp.json")
        file.writeText(json.encodeToString(payload))
        return BackupFileInfo(
            id = file.name,
            path = file.absolutePath,
            createdAt = file.lastModified(),
            pearlCount = pearls.size,
            folderCount = folders.size,
        )
    }

    suspend fun listBackups(): List<BackupFileInfo> =
        backupDir.listFiles()?.filter { it.extension == "json" }?.map { file ->
            BackupFileInfo(
                id = file.name,
                path = file.absolutePath,
                createdAt = file.lastModified(),
                pearlCount = runCatching { json.decodeFromString<BackupPayload>(file.readText()).pearls.size }.getOrDefault(0),
                folderCount = runCatching { json.decodeFromString<BackupPayload>(file.readText()).folders.size }.getOrDefault(0),
            )
        }?.sortedByDescending { it.createdAt }.orEmpty()

    suspend fun restoreBackup(path: String): Int {
        val payload = json.decodeFromString<BackupPayload>(File(path).readText())
        val now = System.currentTimeMillis()
        payload.pearls.forEach { dto ->
            pearlRepository.upsertPearl(
                KnowledgePearlEntity(
                    id = dto.id,
                    title = dto.title,
                    notes = dto.notes,
                    sourceURL = dto.sourceURL,
                    tags = dto.tags,
                    contentKind = dto.contentKind,
                    casePayloadJSON = dto.casePayloadJSON,
                    isFavourite = dto.isFavourite,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }
        payload.folders.forEach { folder ->
            runCatching { pearlRepository.createFolder(folder.name) }
        }
        return payload.pearls.size
    }

    data class BackupFileInfo(
        val id: String,
        val path: String,
        val createdAt: Long,
        val pearlCount: Int,
        val folderCount: Int,
    )

    @Serializable
    data class BackupPayload(
        val version: Int = 1,
        @SerialName("exported_at") val exportedAt: Long,
        val pearls: List<BackupPearlDto>,
        val folders: List<BackupFolder>,
    )

    @Serializable
    data class BackupPearlDto(
        val id: String,
        val title: String = "",
        val notes: String = "",
        @SerialName("source_url") val sourceURL: String? = null,
        val tags: List<String> = emptyList(),
        @SerialName("content_kind") val contentKind: String = "standard",
        @SerialName("case_payload_json") val casePayloadJSON: String = "",
        @SerialName("is_favourite") val isFavourite: Boolean = false,
    )

    @Serializable
    data class BackupFolder(val id: String, val name: String)
}
