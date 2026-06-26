package com.knowledgepearls.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "knowledge_pearls",
    indices = [
        Index("updatedAt"),
        Index("isFavourite"),
        Index("publicPearlID"),
    ],
)
data class KnowledgePearlEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val notes: String = "",
    val sourceURL: String? = null,
    val linkPreviewImagePath: String? = null,
    val linkPreviewDescription: String = "",
    val sourceReference: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val contentKind: String = KnowledgePearlContentKind.STANDARD,
    val casePayloadJSON: String = "",
    val isSharedPublicly: Boolean = false,
    val publicPearlID: String? = null,
    val publicPearlStatus: String = "",
    val publicFeedSnapshot: String = "",
    val isSharedFromFriend: Boolean = false,
    val sharedByUserID: String? = null,
    val sharedByName: String = "",
    val sharedByAvatarURL: String? = null,
    val friendShareID: String? = null,
    val friendShareContentIdentityAtImport: String = "",
    val isFavourite: Boolean = false,
)

object KnowledgePearlContentKind {
    const val STANDARD = "standard"
    const val QUICK = "quick"
    const val CLINICAL_CASE = "clinical_case"
}
