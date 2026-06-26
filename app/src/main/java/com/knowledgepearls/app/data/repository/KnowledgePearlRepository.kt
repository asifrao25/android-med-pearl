package com.knowledgepearls.app.data.repository

import com.knowledgepearls.app.data.local.dao.FolderDao
import com.knowledgepearls.app.data.local.dao.KnowledgePearlDao
import com.knowledgepearls.app.data.local.dao.PearlMediaDao
import com.knowledgepearls.app.data.local.entity.FolderEntity
import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.local.entity.PearlFolderCrossRef
import com.knowledgepearls.app.data.local.entity.PearlMediaEntity
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KnowledgePearlRepository @Inject constructor(
    private val pearlDao: KnowledgePearlDao,
    private val mediaDao: PearlMediaDao,
    private val folderDao: FolderDao,
) {
    fun observeAllPearls(): Flow<List<PearlWithMedia>> = pearlDao.observeAllWithMedia()

    fun observeFavourites(): Flow<List<PearlWithMedia>> = pearlDao.observeFavouritesWithMedia()

    fun observePearl(id: String): Flow<PearlWithMedia?> = pearlDao.observeByIdWithMedia(id)

    fun observeFolders(): Flow<List<FolderEntity>> = folderDao.observeAll()

    suspend fun getPearlsWithPublicPearlId(): List<KnowledgePearlEntity> =
        pearlDao.getAll().filter { it.publicPearlID != null }

    suspend fun getAllPearls(): List<KnowledgePearlEntity> = pearlDao.getAll()

    suspend fun getExistingPublicPearlIds(): Set<String> =
        pearlDao.getExistingPublicPearlIds().toSet()

    suspend fun upsertPearl(pearl: KnowledgePearlEntity) {
        pearlDao.insert(pearl)
    }

    suspend fun updatePearl(pearl: KnowledgePearlEntity) {
        pearlDao.update(pearl)
    }

    suspend fun deletePearl(id: String) {
        mediaDao.deleteForPearl(id)
        pearlDao.deleteById(id)
    }

    suspend fun upsertMedia(media: PearlMediaEntity) {
        mediaDao.insert(media)
    }

    suspend fun upsertMediaItems(items: List<PearlMediaEntity>) {
        mediaDao.insertAll(items)
    }

    suspend fun upsertFolder(folder: FolderEntity) {
        folderDao.insert(folder)
    }

    suspend fun deleteFolder(folder: FolderEntity) {
        folderDao.delete(folder)
    }

    suspend fun addPearlToFolder(pearlId: String, folderId: String) {
        pearlDao.insertPearlFolderCrossRef(PearlFolderCrossRef(pearlId, folderId))
    }

    suspend fun removePearlFromFolder(pearlId: String, folderId: String) {
        pearlDao.removePearlFromFolder(pearlId, folderId)
    }

    suspend fun getPearlsInFolder(folderId: String): List<KnowledgePearlEntity> =
        pearlDao.getPearlsInFolder(folderId)

    suspend fun updatePublicPearlStatus(
        pearlId: String,
        publicPearlId: String?,
        status: String,
        isSharedPublicly: Boolean,
    ) {
        val pearl = pearlDao.getById(pearlId) ?: return
        pearlDao.update(
            pearl.copy(
                publicPearlID = publicPearlId,
                publicPearlStatus = status,
                isSharedPublicly = isSharedPublicly,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }
}
