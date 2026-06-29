package com.knowledgepearls.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.local.entity.PearlFolderCrossRef
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import kotlinx.coroutines.flow.Flow

@Dao
interface KnowledgePearlDao {
    @Transaction
    @Query("SELECT * FROM knowledge_pearls ORDER BY updatedAt DESC")
    fun observeAllWithMedia(): Flow<List<PearlWithMedia>>

    @Transaction
    @Query("SELECT * FROM knowledge_pearls WHERE isFavourite = 1 ORDER BY updatedAt DESC")
    fun observeFavouritesWithMedia(): Flow<List<PearlWithMedia>>

    @Transaction
    @Query("SELECT * FROM knowledge_pearls WHERE id = :id LIMIT 1")
    fun observeByIdWithMedia(id: String): Flow<PearlWithMedia?>

    @Transaction
    @Query(
        """
        SELECT * FROM knowledge_pearls
        WHERE id IN (
            SELECT pearlId FROM pearl_folder_cross_ref WHERE folderId = :folderId
        )
        ORDER BY updatedAt DESC
        """,
    )
    fun observePearlsInFolderWithMedia(folderId: String): Flow<List<PearlWithMedia>>

    @Query("SELECT folderId FROM pearl_folder_cross_ref WHERE pearlId = :pearlId")
    fun observeFolderIdsForPearl(pearlId: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM pearl_folder_cross_ref WHERE folderId = :folderId")
    suspend fun countPearlsInFolder(folderId: String): Int

    @Query("SELECT * FROM knowledge_pearls WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): KnowledgePearlEntity?

    @Query("SELECT * FROM knowledge_pearls WHERE publicPearlID = :publicPearlId LIMIT 1")
    suspend fun getByPublicPearlId(publicPearlId: String): KnowledgePearlEntity?

    @Query("SELECT * FROM knowledge_pearls")
    suspend fun getAll(): List<KnowledgePearlEntity>

    @Query("SELECT publicPearlID FROM knowledge_pearls WHERE publicPearlID IS NOT NULL")
    suspend fun getExistingPublicPearlIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pearl: KnowledgePearlEntity)

    @Update
    suspend fun update(pearl: KnowledgePearlEntity)

    @Delete
    suspend fun delete(pearl: KnowledgePearlEntity)

    @Query("DELETE FROM knowledge_pearls WHERE id = :id")
    suspend fun deleteById(id: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPearlFolderCrossRef(crossRef: PearlFolderCrossRef)

    @Query("DELETE FROM pearl_folder_cross_ref WHERE pearlId = :pearlId AND folderId = :folderId")
    suspend fun removePearlFromFolder(pearlId: String, folderId: String)

    @Query(
        """
        SELECT p.* FROM knowledge_pearls p
        INNER JOIN pearl_folder_cross_ref ref ON ref.pearlId = p.id
        WHERE ref.folderId = :folderId
        ORDER BY p.updatedAt DESC
        """,
    )
    suspend fun getPearlsInFolder(folderId: String): List<KnowledgePearlEntity>
}
