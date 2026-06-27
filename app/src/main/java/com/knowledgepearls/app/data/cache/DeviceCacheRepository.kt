package com.knowledgepearls.app.data.cache

import android.content.Context
import com.knowledgepearls.app.data.repository.KnowledgePearlRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class DeviceCacheRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pearlRepository: KnowledgePearlRepository,
) {
    data class Breakdown(
        val pearlMediaBytes: Long,
        val cacheDirBytes: Long,
        val tempBytes: Long,
    ) {
        val totalBytes: Long get() = pearlMediaBytes + cacheDirBytes + tempBytes
    }

    suspend fun measure(): Breakdown {
        val mediaDir = File(context.filesDir, "pearl_media")
        val pearlMediaBytes = directorySize(mediaDir)
        val cacheDirBytes = directorySize(context.cacheDir)
        val tempBytes = directorySize(context.cacheDir) // simplified
        return Breakdown(
            pearlMediaBytes = pearlMediaBytes,
            cacheDirBytes = cacheDirBytes,
            tempBytes = tempBytes,
        )
    }

    fun clearCache(): Long {
        val before = directorySize(context.cacheDir) + directorySize(File(context.filesDir, "pearl_media"))
        clearDirectory(context.cacheDir)
        clearDirectory(File(context.filesDir, "pearl_media"))
        val after = directorySize(context.cacheDir) + directorySize(File(context.filesDir, "pearl_media"))
        return max(0, before - after)
    }

    fun formattedBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format("%.1f MB", mb)
        return String.format("%.1f GB", mb / 1024.0)
    }

    private fun directorySize(dir: File): Long {
        if (!dir.exists()) return 0
        return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }

    private fun clearDirectory(dir: File) {
        if (!dir.exists()) return
        dir.listFiles()?.forEach { runCatching { if (it.isDirectory) it.deleteRecursively() else it.delete() } }
    }
}
