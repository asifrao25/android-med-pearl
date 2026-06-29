package com.knowledgepearls.app.ui.media

import android.webkit.MimeTypeMap
import com.knowledgepearls.app.data.media.DocumentTypes
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object DocumentSupport {
    fun extensionOf(filename: String): String = DocumentTypes.extensionOf(filename)

    fun isPdf(filename: String, url: String = ""): Boolean = DocumentTypes.isPdf(filename, url)

    fun isOfficeDocument(filename: String, url: String = ""): Boolean =
        DocumentTypes.isOfficeDocument(filename, url)

    fun isDocument(filename: String, mediaType: String? = null, url: String = ""): Boolean =
        DocumentTypes.isDocument(filename, mediaType, url)

    fun isRemoteUrl(url: String): Boolean =
        url.startsWith("http://", ignoreCase = true) || url.startsWith("https://", ignoreCase = true)

    fun officeEmbedUrl(remoteUrl: String): String {
        val encoded = URLEncoder.encode(remoteUrl, StandardCharsets.UTF_8.name())
        return "https://view.officeapps.live.com/op/embed.aspx?src=$encoded"
    }

    fun googleViewerUrl(remoteUrl: String): String {
        val encoded = URLEncoder.encode(remoteUrl, StandardCharsets.UTF_8.name())
        return "https://docs.google.com/gviewer?embedded=true&url=$encoded"
    }

    fun documentLabel(filename: String): String = DocumentTypes.documentLabel(filename)

    fun openActionTitle(filename: String): String = when (extensionOf(filename)) {
        "pdf" -> "Open PDF"
        "ppt", "pptx" -> "Open Slides"
        "doc", "docx", "rtf", "txt", "odt" -> "Open Document"
        "xls", "xlsx", "csv", "ods" -> "Open Spreadsheet"
        else -> "Open File"
    }

    fun openActionHint(filename: String): String = when (extensionOf(filename)) {
        "pdf" -> "View pages inside Med Pearls"
        "ppt", "pptx" -> "Opens in PowerPoint or a slides app"
        "xls", "xlsx", "csv", "ods" -> "Opens in Excel or a spreadsheet app"
        else -> "Opens in Word, Google Docs, or a similar app"
    }

    fun mimeType(filename: String): String {
        val ext = extensionOf(filename)
        val explicit = when (ext) {
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "csv" -> "text/csv"
            "txt" -> "text/plain"
            "rtf" -> "application/rtf"
            "odt" -> "application/vnd.oasis.opendocument.text"
            "odp" -> "application/vnd.oasis.opendocument.presentation"
            "ods" -> "application/vnd.oasis.opendocument.spreadsheet"
            else -> null
        }
        if (explicit != null) return explicit
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
    }
}
