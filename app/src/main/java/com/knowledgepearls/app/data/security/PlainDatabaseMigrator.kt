package com.knowledgepearls.app.data.security

import android.content.Context
import androidx.room.Room
import com.knowledgepearls.app.data.local.MedPearlsDatabase
import com.knowledgepearls.app.data.local.entity.PearlFolderCrossRef
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Singleton
class PlainDatabaseMigrator @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun migrateToEncryptedIfNeeded(passphrase: ByteArray) {
        if (!hasUnencryptedDatabase()) return

        val snapshot = runBlocking { readPlainSnapshot() }
        deleteDatabaseFiles()

        val encryptedDb = Room.databaseBuilder(
            context,
            MedPearlsDatabase::class.java,
            MedPearlsDatabase.NAME,
        )
            .openHelperFactory(SupportOpenHelperFactory(passphrase))
            .build()

        runBlocking {
            val pearlDao = encryptedDb.knowledgePearlDao()
            val folderDao = encryptedDb.folderDao()
            val mediaDao = encryptedDb.pearlMediaDao()

            snapshot.pearls.forEach { pearlDao.insert(it) }
            snapshot.folders.forEach { folderDao.insert(it) }
            snapshot.media.forEach { mediaDao.insert(it) }
            snapshot.crossRefs.forEach { pearlDao.insertPearlFolderCrossRef(it) }
        }
        encryptedDb.close()
    }

    private fun hasUnencryptedDatabase(): Boolean {
        val dbFile = context.getDatabasePath(MedPearlsDatabase.NAME)
        if (!dbFile.exists()) return false
        return runCatching {
            android.database.sqlite.SQLiteDatabase.openDatabase(
                dbFile.path,
                null,
                android.database.sqlite.SQLiteDatabase.OPEN_READONLY,
            ).use { database ->
                database.rawQuery("SELECT 1", null).use { cursor ->
                    cursor.moveToFirst()
                }
            }
            true
        }.getOrDefault(false)
    }

    private suspend fun readPlainSnapshot(): PlainDatabaseSnapshot {
        val plainDb = Room.databaseBuilder(
            context,
            MedPearlsDatabase::class.java,
            MedPearlsDatabase.NAME,
        ).build()
        return try {
            PlainDatabaseSnapshot(
                pearls = plainDb.knowledgePearlDao().getAll(),
                folders = plainDb.folderDao().getAll(),
                media = plainDb.pearlMediaDao().getAll(),
                crossRefs = plainDb.knowledgePearlDao().getAllFolderCrossRefs(),
            )
        } finally {
            plainDb.close()
        }
    }

    private fun deleteDatabaseFiles() {
        val dbFile = context.getDatabasePath(MedPearlsDatabase.NAME)
        listOf(
            dbFile,
            File(dbFile.path + "-wal"),
            File(dbFile.path + "-shm"),
            File(dbFile.path + "-journal"),
        ).forEach { file ->
            if (file.exists()) file.delete()
        }
    }

    private data class PlainDatabaseSnapshot(
        val pearls: List<com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity>,
        val folders: List<com.knowledgepearls.app.data.local.entity.FolderEntity>,
        val media: List<com.knowledgepearls.app.data.local.entity.PearlMediaEntity>,
        val crossRefs: List<PearlFolderCrossRef>,
    )
}
