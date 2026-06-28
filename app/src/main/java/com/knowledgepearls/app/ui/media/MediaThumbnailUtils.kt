package com.knowledgepearls.app.ui.media

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import java.io.File
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MediaThumbnailUtils {
    suspend fun loadVideoThumbnail(source: String): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            MediaMetadataRetriever().use { retriever ->
                when {
                    source.startsWith("http://", ignoreCase = true) ||
                        source.startsWith("https://", ignoreCase = true) -> {
                        retriever.setDataSource(source, emptyMap())
                    }
                    source.startsWith("file:", ignoreCase = true) -> {
                        retriever.setDataSource(File(URI(source)).absolutePath)
                    }
                    else -> retriever.setDataSource(source)
                }
                retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            }
        }.getOrNull()
    }

    suspend fun loadPdfThumbnail(path: String): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            PdfBitmapUtils.renderFirstPage(File(path))
        }.getOrNull()
    }

    suspend fun loadDocumentThumbnail(cacheDir: File, url: String, filename: String): Bitmap? =
        withContext(Dispatchers.IO) {
            val effectiveName = effectiveMediaFilename(filename, url)
            runCatching {
                val file = DocumentFileResolver.resolveFile(cacheDir, url, effectiveName)
                when {
                    DocumentSupport.isPdf(effectiveName, url) || isPdfFile(file) ->
                        PdfBitmapUtils.renderFirstPage(file)
                    DocumentSupport.isOfficeDocument(effectiveName, url) ->
                        OfficeDocumentPreviewRenderer.renderThumbnail(file, effectiveName)
                    else -> null
                }
            }.getOrNull()
        }

    private fun isPdfFile(file: File): Boolean {
        if (!file.exists() || file.length() < 5) return false
        return runCatching {
            file.inputStream().use { input ->
                val header = ByteArray(5)
                if (input.read(header) == 5) {
                    String(header, Charsets.US_ASCII).startsWith("%PDF-")
                } else {
                    false
                }
            }
        }.getOrDefault(false)
    }
}
