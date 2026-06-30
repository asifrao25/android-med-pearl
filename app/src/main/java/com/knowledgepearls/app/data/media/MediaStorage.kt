package com.knowledgepearls.app.data.media

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.net.URL
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val rootDir: File
        get() = File(context.filesDir, "pearl_media").also { it.mkdirs() }

    fun saveBytes(bytes: ByteArray, extension: String): String {
        val file = File(rootDir, "${UUID.randomUUID()}.$extension")
        file.writeBytes(bytes)
        return file.absolutePath
    }

    fun saveFromUrl(
        url: String,
        extension: String,
        maxBytes: Long = DEFAULT_MAX_IMPORT_BYTES,
    ): String {
        val file = File(rootDir, "${UUID.randomUUID()}.$extension")
        var totalBytes = 0L
        try {
            URL(url).openStream().use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(STREAM_BUFFER_BYTES)
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        totalBytes += read
                        if (totalBytes > maxBytes) {
                            throw MediaTooLargeException(maxBytes)
                        }
                        output.write(buffer, 0, read)
                    }
                }
            }
        } catch (error: Throwable) {
            file.delete()
            throw error
        }
        return file.absolutePath
    }

    fun deleteFile(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }

    private companion object {
        const val DEFAULT_MAX_IMPORT_BYTES = 100L * 1024L * 1024L
        const val STREAM_BUFFER_BYTES = 8 * 1024
    }
}
