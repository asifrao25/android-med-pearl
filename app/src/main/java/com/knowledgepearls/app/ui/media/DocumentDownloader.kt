package com.knowledgepearls.app.ui.media

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DocumentDownloader {
    suspend fun download(
        context: Context,
        cacheDir: File,
        url: String,
        filename: String,
    ): Boolean = withContext(Dispatchers.IO) {
        val effectiveName = effectiveMediaFilename(filename, url)
        runCatching {
            val source = DocumentFileResolver.resolveFile(cacheDir, url, effectiveName)
            if (!source.exists() || source.length() <= 0L) {
                notify(context, "File not found")
                return@withContext false
            }
            val saved = saveToDownloads(context, source, effectiveName)
            if (saved) {
                notify(context, "Saved to Downloads")
            } else {
                shareFile(context, source, effectiveName)
            }
            saved
        }.getOrElse {
            notify(context, "Couldn't save file")
            false
        }
    }

    fun shareFile(context: Context, source: File, displayName: String): Boolean {
        return runCatching {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                source,
            )
            val mime = DocumentSupport.mimeType(displayName)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mime
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TITLE, displayName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Save document"))
            true
        }.getOrElse {
            notify(context, "Couldn't share file")
            false
        }
    }

    private fun saveToDownloads(context: Context, source: File, displayName: String): Boolean {
        val mime = DocumentSupport.mimeType(displayName)
        val uniqueName = uniqueDownloadName(context, displayName)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, uniqueName)
                put(MediaStore.Downloads.MIME_TYPE, mime)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return false
            val written = resolver.openOutputStream(uri)?.use { output ->
                FileInputStream(source).use { input -> input.copyTo(output) }
            } != null
            if (!written) {
                resolver.delete(uri, null, null)
                return false
            }
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return true
        }

        @Suppress("DEPRECATION")
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists() && !downloadsDir.mkdirs()) return false
        val target = File(downloadsDir, uniqueName)
        return runCatching {
            source.inputStream().use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            android.media.MediaScannerConnection.scanFile(
                context,
                arrayOf(target.absolutePath),
                arrayOf(mime),
                null,
            )
            target.exists() && target.length() > 0L
        }.getOrDefault(false)
    }

    private fun uniqueDownloadName(context: Context, displayName: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val projection = arrayOf(MediaStore.Downloads.DISPLAY_NAME)
            var candidate = displayName
            var counter = 1
            while (true) {
                val exists = resolver.query(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    projection,
                    "${MediaStore.Downloads.DISPLAY_NAME} = ?",
                    arrayOf(candidate),
                    null,
                )?.use { it.moveToFirst() } == true
                if (!exists) return candidate
                val base = displayName.substringBeforeLast('.', displayName)
                val ext = displayName.substringAfterLast('.', "")
                candidate = if (ext.isNotBlank()) "$base ($counter).$ext" else "$base ($counter)"
                counter++
            }
        }

        @Suppress("DEPRECATION")
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        var candidate = displayName
        var counter = 1
        while (File(downloadsDir, candidate).exists()) {
            val base = displayName.substringBeforeLast('.', displayName)
            val ext = displayName.substringAfterLast('.', "")
            candidate = if (ext.isNotBlank()) "$base ($counter).$ext" else "$base ($counter)"
            counter++
        }
        return candidate
    }

    private fun notify(context: Context, message: String) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
