package com.knowledgepearls.app.ui.media

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File

object PdfBitmapUtils {
    fun pageCount(file: File): Int =
        runCatching {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer -> renderer.pageCount }
            }
        }.getOrDefault(0)

    fun renderPage(page: PdfRenderer.Page, scale: Int = 2): Bitmap {
        val width = page.width * scale
        val height = page.height * scale
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
            page.render(this, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        }
    }

    fun renderPageAt(file: File, pageIndex: Int, scale: Int = 2): Bitmap? =
        runCatching {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    if (pageIndex !in 0 until renderer.pageCount) return null
                    renderer.openPage(pageIndex).use { page -> renderPage(page, scale) }
                }
            }
        }.getOrNull()

    fun renderFirstPage(file: File, scale: Int = 2): Bitmap? = renderPageAt(file, 0, scale)

    fun renderAllPages(file: File, scale: Int = 2): List<Bitmap> {
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                return (0 until renderer.pageCount).map { index ->
                    renderer.openPage(index).use { page -> renderPage(page, scale) }
                }
            }
        }
    }
}
