package com.knowledgepearls.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "pearl_media",
    foreignKeys = [
        ForeignKey(
            entity = KnowledgePearlEntity::class,
            parentColumns = ["id"],
            childColumns = ["pearlId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("pearlId")],
)
data class PearlMediaEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val pearlId: String,
    val type: String,
    val localPath: String? = null,
    val filename: String = "",
    val sectionTag: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

object MediaType {
    const val IMAGE = "image"
    const val VIDEO = "video"
    const val PDF = "pdf"
    const val DOCUMENT = "document"
}
