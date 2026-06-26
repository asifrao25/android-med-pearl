package com.knowledgepearls.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.knowledgepearls.app.data.local.dao.FolderDao
import com.knowledgepearls.app.data.local.dao.KnowledgePearlDao
import com.knowledgepearls.app.data.local.dao.PearlMediaDao
import com.knowledgepearls.app.data.local.entity.FolderEntity
import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.local.entity.PearlFolderCrossRef
import com.knowledgepearls.app.data.local.entity.PearlMediaEntity

@Database(
    entities = [
        KnowledgePearlEntity::class,
        PearlMediaEntity::class,
        FolderEntity::class,
        PearlFolderCrossRef::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class MedPearlsDatabase : RoomDatabase() {
    abstract fun knowledgePearlDao(): KnowledgePearlDao
    abstract fun pearlMediaDao(): PearlMediaDao
    abstract fun folderDao(): FolderDao

    companion object {
        const val NAME = "med_pearls.db"
    }
}
