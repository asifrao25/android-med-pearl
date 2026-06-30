package com.knowledgepearls.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.knowledgepearls.app.data.local.entity.FolderEntity
import com.knowledgepearls.app.data.local.model.FolderWithCount
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAll(): List<FolderEntity>

    @Query("SELECT * FROM folders ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<FolderEntity>>

    @Query(
        """
        SELECT folders.*,
            (SELECT COUNT(*) FROM pearl_folder_cross_ref WHERE folderId = folders.id) AS pearlCount
        FROM folders
        ORDER BY folders.name COLLATE NOCASE ASC
        """,
    )
    fun observeAllWithCounts(): Flow<List<FolderWithCount>>

    @Query("SELECT * FROM folders WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderEntity)

    @Update
    suspend fun update(folder: FolderEntity)

    @Delete
    suspend fun delete(folder: FolderEntity)
}
