package com.knowledgepearls.app.data.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object BackupFormat {
    const val CURRENT_VERSION = 2
    const val FILE_PREFIX = "MedPearls-Backup-"
    const val LEGACY_FILE_PREFIX = "medpearls-backup-"
    const val MIME_TYPE = "application/json"
}

@Serializable
data class BackupPayloadV2(
    @SerialName("format_version") val formatVersion: Int = BackupFormat.CURRENT_VERSION,
    @SerialName("created_at") val createdAt: Long,
    val folders: List<BackupFolderDto> = emptyList(),
    val pearls: List<BackupPearlDto> = emptyList(),
) {
    val pearlCount: Int get() = pearls.size
    val folderCount: Int get() = folders.size
    val mediaCount: Int get() = pearls.sumOf { it.media.size }
}

@Serializable
data class BackupFolderDto(
    val id: String,
    val name: String,
    @SerialName("created_at") val createdAt: Long,
)

@Serializable
data class BackupPearlDto(
    val id: String,
    val title: String = "",
    val notes: String = "",
    @SerialName("source_url") val sourceURL: String? = null,
    @SerialName("source_reference") val sourceReference: String = "",
    @SerialName("link_preview_description") val linkPreviewDescription: String = "",
    @SerialName("link_preview_image_base64") val linkPreviewImageBase64: String? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long,
    val tags: List<String> = emptyList(),
    @SerialName("content_kind") val contentKind: String = "standard",
    @SerialName("case_payload_json") val casePayloadJSON: String = "",
    @SerialName("is_shared_publicly") val isSharedPublicly: Boolean = false,
    @SerialName("public_pearl_id") val publicPearlID: String? = null,
    @SerialName("public_pearl_status") val publicPearlStatus: String = "",
    @SerialName("public_feed_snapshot") val publicFeedSnapshot: String = "",
    @SerialName("is_shared_from_friend") val isSharedFromFriend: Boolean = false,
    @SerialName("shared_by_user_id") val sharedByUserID: String? = null,
    @SerialName("shared_by_name") val sharedByName: String = "",
    @SerialName("shared_by_avatar_url") val sharedByAvatarURL: String? = null,
    @SerialName("friend_share_id") val friendShareID: String? = null,
    @SerialName("is_favourite") val isFavourite: Boolean = false,
    @SerialName("folder_ids") val folderIds: List<String> = emptyList(),
    val media: List<BackupMediaDto> = emptyList(),
)

@Serializable
data class BackupMediaDto(
    val id: String,
    val type: String,
    val filename: String = "",
    @SerialName("section_tag") val sectionTag: String = "",
    @SerialName("created_at") val createdAt: Long,
    @SerialName("data_base64") val dataBase64: String? = null,
)

/** Android v1 export (metadata only) — still supported on import. */
@Serializable
data class LegacyBackupPayloadV1(
    val version: Int = 1,
    @SerialName("exported_at") val exportedAt: Long,
    val pearls: List<LegacyBackupPearlV1>,
    val folders: List<LegacyBackupFolderV1>,
)

@Serializable
data class LegacyBackupPearlV1(
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
data class LegacyBackupFolderV1(
    val id: String,
    val name: String,
)

data class BackupRestoreSummary(
    val pearlsAdded: Int = 0,
    val pearlsUpdated: Int = 0,
    val foldersAdded: Int = 0,
    val mediaRestored: Int = 0,
    val mediaSkipped: Int = 0,
) {
    /** @deprecated use explicit fields; kept for call-site compatibility */
    val pearlsRestored: Int get() = pearlsAdded + pearlsUpdated
    val foldersRestored: Int get() = foldersAdded
}

/** Counts shown before the user confirms merge or replace (iOS-aligned). */
data class RestorePreview(
    val pearlsToAdd: Int = 0,
    val pearlsToUpdate: Int = 0,
    val pearlsUnchanged: Int = 0,
    val pearlsToRemove: Int = 0,
    val foldersToAdd: Int = 0,
    val mediaFilesInBackup: Int = 0,
    val backupCreatedAt: Long = 0L,
)

enum class RestoreMode {
    Merge,
    Replace,
}
