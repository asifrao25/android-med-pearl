package com.knowledgepearls.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "folders",
    indices = [Index("name")],
)
data class FolderEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "pearl_folder_cross_ref",
    primaryKeys = ["pearlId", "folderId"],
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = KnowledgePearlEntity::class,
            parentColumns = ["id"],
            childColumns = ["pearlId"],
            onDelete = androidx.room.ForeignKey.CASCADE,
        ),
        androidx.room.ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = androidx.room.ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("folderId")],
)
data class PearlFolderCrossRef(
    val pearlId: String,
    val folderId: String,
)
