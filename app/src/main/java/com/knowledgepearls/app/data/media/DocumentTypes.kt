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

    fun isOfficeDocument(filename: String, url: String = ""): Boolean {
        if (extensionOf(filename) in OFFICE_EXTENSIONS) return true
        return extensionOf(url.substringBefore('?').substringAfterLast('/')) in OFFICE_EXTENSIONS
    }

    fun isDocument(filename: String, mediaType: String? = null, url: String = ""): Boolean {
        if (mediaType == MediaType.PDF) return true
        if (isPdf(filename, url)) return true
        if (isOfficeDocument(filename, url)) return true
        if (mediaType == MediaType.DOCUMENT) return true
        if (mediaType == "document") return true
        return mediaType.equals("application/pdf", ignoreCase = true)
    }

    fun documentLabel(filename: String): String = when (extensionOf(filename)) {
        "pdf" -> "PDF"
        "doc", "docx" -> "Word"
        "ppt", "pptx" -> "PowerPoint"
        "xls", "xlsx" -> "Excel"
        else -> "Document"
    }
}
