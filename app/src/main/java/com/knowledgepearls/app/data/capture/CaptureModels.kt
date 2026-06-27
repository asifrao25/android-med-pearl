package com.knowledgepearls.app.data.capture

data class PickedMedia(
    val bytes: ByteArray,
    val filename: String,
    val type: String,
    val sectionTag: String = "",
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PickedMedia) return false
        return filename == other.filename && type == other.type && sectionTag == other.sectionTag &&
            bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()
}

enum class CaptureSheet {
    QuickText,
    WebLink,
    Camera,
    PhotoLibrary,
    Files,
    ClinicalCase,
}

fun mediaTypeForFilename(filename: String): String {
    val lower = filename.lowercase()
    return when {
        lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") ||
            lower.endsWith(".webp") || lower.endsWith(".heic") -> "image"
        lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".m4v") ||
            lower.endsWith(".webm") -> "video"
        lower.endsWith(".pdf") -> "pdf"
        lower.endsWith(".doc") || lower.endsWith(".docx") ||
            lower.endsWith(".ppt") || lower.endsWith(".pptx") ||
            lower.endsWith(".xls") || lower.endsWith(".xlsx") ||
            lower.endsWith(".odt") || lower.endsWith(".odp") || lower.endsWith(".ods") ||
            lower.endsWith(".rtf") -> "document"
        else -> "document"
    }
}

fun parseTags(raw: String): List<String> =
    raw.split(",", "#")
        .map { it.trim().lowercase() }
        .filter { it.isNotEmpty() }
        .distinct()
