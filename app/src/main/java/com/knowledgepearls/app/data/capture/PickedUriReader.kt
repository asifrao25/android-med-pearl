package com.knowledgepearls.app.data.capture

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

sealed interface PickResult {
    data class Success(val media: PickedMedia) : PickResult
    data class Failure(val message: String) : PickResult
}

object PickedUriReader {
    private const val MAX_BYTES = 50L * 1024L * 1024L

    private val googleWorkspaceMimeTypes = setOf(
        "application/vnd.google-apps.document",
        "application/vnd.google-apps.presentation",
        "application/vnd.google-apps.spreadsheet",
        "application/vnd.google-apps.form",
        "application/vnd.google-apps.drawing",
    )

    fun openDocumentMimeTypes(): Array<String> = arrayOf("*/*")

    fun read(context: Context, uri: Uri): PickResult {
        val mimeType = context.contentResolver.getType(uri)?.lowercase()
        if (mimeType in googleWorkspaceMimeTypes) {
            return PickResult.Failure(
                "Google Docs/Slides files must be downloaded first. In Drive, choose Download or Export as .docx/.pdf, then attach that file.",
            )
        }

        val filename = resolveFilename(context, uri, mimeType)
        val extension = filename.substringAfterLast('.', "bin").take(16)
        val tempFile = File.createTempFile("picked_", ".$extension", context.cacheDir)
        return try {
            if (!copyUriToFile(context, uri, tempFile)) {
                PickResult.Failure("Couldn't open the selected file. Try downloading it locally first.")
            } else if (tempFile.length() == 0L) {
                PickResult.Failure("The selected file is empty.")
            } else if (tempFile.length() > MAX_BYTES) {
                PickResult.Failure("File is too large. Maximum size is 50 MB.")
            } else {
                PickResult.Success(
                    PickedMedia(
                        bytes = tempFile.readBytes(),
                        filename = filename,
                        type = mediaTypeForPickedFile(filename, mimeType),
                    ),
                )
            }
        } catch (_: OutOfMemoryError) {
            PickResult.Failure("File is too large to attach.")
        } catch (_: SecurityException) {
            PickResult.Failure("Permission denied while reading the selected file.")
        } catch (_: Exception) {
            PickResult.Failure("Couldn't read the selected file.")
        } finally {
            tempFile.delete()
        }
    }

    private fun resolveFilename(context: Context, uri: Uri, mimeType: String?): String {
        val displayName = queryDisplayName(context, uri)?.takeIf { it.isNotBlank() }
        val raw = displayName
            ?: DocumentFile.fromSingleUri(context, uri)?.name
            ?: uri.lastPathSegment?.substringAfterLast('/')
            ?: "attachment"
        val cleaned = raw.substringAfterLast('/')
        if (cleaned.contains('.') && !cleaned.endsWith('.')) return cleaned
        val extension = extensionForMime(mimeType) ?: guessExtensionFromUri(uri)
        return if (extension != null) "$cleaned.$extension" else cleaned
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        val projection = arrayOf(
            OpenableColumns.DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        )
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            for (column in projection) {
                val index = cursor.getColumnIndex(column)
                if (index >= 0 && cursor.moveToFirst()) {
                    cursor.getString(index)?.takeIf { it.isNotBlank() }?.let { return it }
                }
            }
        }
        return null
    }

    private fun guessExtensionFromUri(uri: Uri): String? {
        val path = uri.toString().lowercase()
        return listOf("pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "rtf", "odt", "odp", "ods")
            .firstOrNull { ext -> path.contains(".$ext") }
    }

    private fun copyUriToFile(context: Context, uri: Uri, dest: File): Boolean {
        if (copyWithInputStream(context, uri, dest)) return true
        if (copyWithAssetFileDescriptor(context, uri, dest)) return true
        if (copyWithFileDescriptor(context, uri, dest)) return true
        return DocumentFile.fromSingleUri(context, uri)?.uri?.let { documentUri ->
            if (documentUri == uri) false else copyWithInputStream(context, documentUri, dest)
        } == true
    }

    private fun copyWithInputStream(context: Context, uri: Uri, dest: File): Boolean =
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { input ->
                writeStreamToFile(input, dest)
            } == true
        }.getOrDefault(false)

    private fun copyWithAssetFileDescriptor(context: Context, uri: Uri, dest: File): Boolean =
        runCatching {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
                descriptor.createInputStream().use { input ->
                    writeStreamToFile(input, dest)
                }
            } == true
        }.getOrDefault(false)

    private fun copyWithFileDescriptor(context: Context, uri: Uri, dest: File): Boolean =
        runCatching {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                FileInputStream(descriptor.fileDescriptor).use { input ->
                    writeStreamToFile(input, dest)
                }
            } == true
        }.getOrDefault(false)

    private fun writeStreamToFile(input: java.io.InputStream, dest: File): Boolean {
        FileOutputStream(dest, false).use { output ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                output.write(buffer, 0, read)
            }
            output.flush()
        }
        return dest.exists() && dest.length() > 0L
    }
}

fun extensionForMime(mimeType: String?): String? {
    if (mimeType.isNullOrBlank()) return null
    val normalized = mimeType.lowercase()
    return when (normalized) {
        "application/pdf" -> "pdf"
        "application/msword" -> "doc"
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
        "application/vnd.ms-powerpoint" -> "ppt"
        "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> "pptx"
        "application/vnd.ms-excel" -> "xls"
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx"
        "application/vnd.oasis.opendocument.text" -> "odt"
        "application/vnd.oasis.opendocument.presentation" -> "odp"
        "application/vnd.oasis.opendocument.spreadsheet" -> "ods"
        "application/rtf", "text/rtf" -> "rtf"
        else -> MimeTypeMap.getSingleton().getExtensionFromMimeType(normalized)
    }
}

fun mediaTypeForPickedFile(filename: String, mimeType: String?): String {
    val fromName = mediaTypeForFilename(filename)
    if (fromName != "document" || mimeType.isNullOrBlank()) return fromName
    return mediaTypeForMime(mimeType) ?: fromName
}

fun mediaTypeForMime(mimeType: String): String? = when {
    mimeType == "application/pdf" -> "pdf"
    mimeType.startsWith("image/") -> "image"
    mimeType.startsWith("video/") -> "video"
    mimeType.startsWith("audio/") -> "document"
    else -> "document"
}
