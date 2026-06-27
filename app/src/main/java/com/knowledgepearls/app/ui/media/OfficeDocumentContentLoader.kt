package com.knowledgepearls.app.ui.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

sealed interface OfficeDocumentContent {
    data class Image(val bitmap: Bitmap) : OfficeDocumentContent
    data class Html(val html: String) : OfficeDocumentContent
    data class Slides(val slides: List<String>) : OfficeDocumentContent
    data object Unsupported : OfficeDocumentContent
}

object OfficeDocumentContentLoader {
    fun load(file: File, filename: String): OfficeDocumentContent {
        if (!file.exists() || !file.canRead()) return OfficeDocumentContent.Unsupported
        val bytes = runCatching { file.readBytes() }.getOrNull() ?: return OfficeDocumentContent.Unsupported
        return load(bytes, filename)
    }

    fun load(bytes: ByteArray, filename: String): OfficeDocumentContent {
        if (bytes.isEmpty()) return OfficeDocumentContent.Unsupported

        if (DocumentSupport.isPdf(filename) || isPdfBytes(bytes)) {
            return OfficeDocumentContent.Unsupported
        }

        val ext = DocumentSupport.extensionOf(effectiveMediaFilename(filename))
        when {
            ext == "docx" || OfficeOpenXmlPreviewSource.isWordProcessingOpenXml(bytes) -> {
                OfficeOpenXmlPreviewSource.docxHtml(bytes)?.let { return OfficeDocumentContent.Html(it) }
                embeddedOrMediaImage(bytes, filename)?.let { return OfficeDocumentContent.Image(it) }
            }
            ext == "pptx" || OfficeOpenXmlPreviewSource.isPresentationOpenXml(bytes) -> {
                val slides = OfficeOpenXmlPreviewSource.pptxSlidesHtml(bytes)
                if (slides.isNotEmpty()) return OfficeDocumentContent.Slides(slides)
                embeddedOrMediaImage(bytes, filename)?.let { return OfficeDocumentContent.Image(it) }
            }
            else -> {
                embeddedOrMediaImage(bytes, filename)?.let { return OfficeDocumentContent.Image(it) }
            }
        }

        return OfficeDocumentContent.Unsupported
    }

    private fun embeddedOrMediaImage(bytes: ByteArray, filename: String): Bitmap? {
        val ext = DocumentSupport.extensionOf(effectiveMediaFilename(filename))
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

    private fun isPdfBytes(bytes: ByteArray): Boolean {
        if (bytes.size < 5) return false
        return String(bytes.copyOfRange(0, 5), Charsets.US_ASCII).startsWith("%PDF-")
    }
}
