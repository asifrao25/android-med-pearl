package com.knowledgepearls.app.ui.media

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.media.MediaMetadataRetriever
import android.os.ParcelFileDescriptor
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MediaThumbnailUtils {
    suspend fun loadVideoThumbnail(path: String): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(path)
                retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            }
        }.getOrNull()
    }

    suspend fun loadPdfThumbnail(path: String): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    if (renderer.pageCount == 0) return@withContext null
                    renderer.openPage(0).use { page ->
                        val width = page.width * 2
                        val height = page.height * 2
                        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        }
                    }
                }
            }
        }.getOrNull()
    }

    suspend fun loadDocumentThumbnail(cacheDir: File, url: String, filename: String): Bitmap? =
        withContext(Dispatchers.IO) {
            if (!DocumentSupport.isPdf(filename, url)) return@withContext null
            runCatching {
                val file = DocumentFileResolver.resolveFile(cacheDir, url, filename)
                loadPdfThumbnail(file.absolutePath)
            }.getOrNull()
        }
}
