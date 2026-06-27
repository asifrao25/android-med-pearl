package com.knowledgepearls.app.ui.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.io.File

object OfficeDocumentPreviewRenderer {
    fun renderThumbnail(file: File, filename: String, targetWidth: Int = 900): Bitmap? {
        val bytes = runCatching { file.readBytes() }.getOrNull() ?: return null
        return renderThumbnail(bytes, filename, targetWidth)
    }

    fun renderThumbnail(bytes: ByteArray, filename: String, targetWidth: Int = 900): Bitmap? {
        val ext = DocumentSupport.extensionOf(effectiveMediaFilename(filename))

        embeddedOrMediaImage(bytes, ext)?.let { return scalePreview(it, targetWidth) }

        when {
            ext == "docx" || OfficeOpenXmlPreviewSource.isWordProcessingOpenXml(bytes) -> {
                OfficeOpenXmlPreviewSource.docxParagraphs(bytes)
                    .joinToString(" ")
                    .takeIf { it.isNotBlank() }
                    ?.let { return renderTextPreview(it, targetWidth) }
            }
            ext == "pptx" || OfficeOpenXmlPreviewSource.isPresentationOpenXml(bytes) -> {
                OfficeOpenXmlPreviewSource.pptxSlideParagraphs(bytes)
                    .firstOrNull()
                    ?.joinToString("\n")
                    ?.takeIf { it.isNotBlank() }
                    ?.let { return renderTextPreview(it, targetWidth) }
            }
        }
        return null
    }

    private fun embeddedOrMediaImage(bytes: ByteArray, ext: String): Bitmap? {
        val imageBytes = when {
            ext == "docx" || OfficeOpenXmlPreviewSource.isWordProcessingOpenXml(bytes) ->
                OfficeOpenXmlPreviewSource.docxEmbeddedThumbnailData(bytes)
                    ?: OfficeOpenXmlPreviewSource.docxFirstMediaImageData(bytes)
            ext == "pptx" || OfficeOpenXmlPreviewSource.isPresentationOpenXml(bytes) ->
                OfficeOpenXmlPreviewSource.pptxEmbeddedThumbnailData(bytes)
                    ?: OfficeOpenXmlPreviewSource.pptxFirstMediaImageData(bytes)
            else -> null
        } ?: return null
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun scalePreview(source: Bitmap, targetWidth: Int): Bitmap {
        val height = (targetWidth * 1.32f).toInt().coerceAtLeast(1)
        return Bitmap.createBitmap(targetWidth, height, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
            val canvas = Canvas(this)
            val scale = minOf(
                targetWidth.toFloat() / source.width.coerceAtLeast(1),
                height.toFloat() / source.height.coerceAtLeast(1),
            )
            val drawWidth = source.width * scale
            val drawHeight = source.height * scale
            val left = (targetWidth - drawWidth) / 2f
            val top = (height - drawHeight) / 2f
            canvas.drawBitmap(
                source,
                null,
                android.graphics.RectF(left, top, left + drawWidth, top + drawHeight),
                Paint(Paint.FILTER_BITMAP_FLAG),
            )
        }
    }

    private fun renderTextPreview(text: String, targetWidth: Int): Bitmap {
        val width = targetWidth.coerceAtLeast(320)
        val height = (width * 1.32f).toInt().coerceAtLeast(1)
        val previewText = text.take(900)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 34f
        }
        val padding = 42
        val layout = StaticLayout.Builder
            .obtain(previewText, 0, previewText.length, textPaint, width - padding * 2)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.15f)
            .build()

        canvas.save()
        canvas.translate(padding.toFloat(), padding.toFloat())
        layout.draw(canvas)
        canvas.restore()
        return bitmap
    }
}
