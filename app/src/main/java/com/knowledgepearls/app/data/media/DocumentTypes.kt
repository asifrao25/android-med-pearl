package com.knowledgepearls.app.data.media

import com.knowledgepearls.app.data.local.entity.MediaType

object DocumentTypes {
    private val OFFICE_EXTENSIONS = setOf(
        "doc", "docx", "ppt", "pptx", "xls", "xlsx", "odt", "odp", "ods", "rtf",
    )

    fun extensionOf(filename: String): String =
        filename.substringAfterLast('.', "").lowercase()

    fun isPdf(filename: String, url: String = ""): Boolean {
        if (extensionOf(filename) == "pdf") return true
        return url.lowercase().contains(".pdf")
    }

    fun isOfficeDocument(filename: String): Boolean =
        extensionOf(filename) in OFFICE_EXTENSIONS

    fun isDocument(filename: String, mediaType: String? = null): Boolean {
        if (mediaType == MediaType.PDF) return true
        if (isPdf(filename)) return true
        if (isOfficeDocument(filename)) return true
        if (mediaType == MediaType.DOCUMENT) return true
        return mediaType == "document"
    }

    fun documentLabel(filename: String): String = when (extensionOf(filename)) {
        "pdf" -> "PDF"
        "doc", "docx" -> "Word"
        "ppt", "pptx" -> "PowerPoint"
        "xls", "xlsx" -> "Excel"
        else -> "Document"
    }
}
