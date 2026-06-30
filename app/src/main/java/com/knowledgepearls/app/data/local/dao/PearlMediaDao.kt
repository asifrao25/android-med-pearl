package com.knowledgepearls.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.knowledgepearls.app.data.local.entity.PearlMediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PearlMediaDao {
    @Query("SELECT * FROM pearl_media")
    suspend fun getAll(): List<PearlMediaEntity>

    @Query("SELECT * FROM pearl_media WHERE pearlId = :pearlId ORDER BY createdAt ASC")
    fun observeForPearl(pearlId: String): Flow<List<PearlMediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: PearlMediaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(media: List<PearlMediaEntity>)

    @Update
    suspend fun update(media: PearlMediaEntity)

    @Delete
    suspend fun delete(media: PearlMediaEntity)

    @Query("SELECT COUNT(*) FROM pearl_media WHERE pearlId = :pearlId")
    suspend fun countForPearl(pearlId: String): Int

    @Query("DELETE FROM pearl_media")
    suspend fun deleteAll()

    @Query("DELETE FROM pearl_media WHERE pearlId = :pearlId")
    suspend fun deleteForPearl(pearlId: String)
}
