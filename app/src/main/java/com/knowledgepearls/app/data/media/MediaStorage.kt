package com.knowledgepearls.app.data.media

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
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

    fun deleteFile(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }
}
