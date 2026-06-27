package com.knowledgepearls.app.data.model

import com.knowledgepearls.app.data.media.DocumentTypes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PublicPearlMediaItem(
    val type: String = "photo",
    val url: String = "",
    val path: String? = null,
    val filename: String? = null,
    val section: String? = null,
) {
    val loadableUrl: String? get() = PublicPearlMediaUrls.resolve(url, path)

    val resolvedFilename: String
        get() {
            if (!filename.isNullOrBlank()) return filename
            return loadableUrl?.substringAfterLast('/')?.substringBefore('?').orEmpty().ifBlank { "Document" }
        }

    val isVideo: Boolean
        get() {
            if (type == "video") return true
            return type == "document" && PublicPearlMediaUrls.isVideoFilename(resolvedFilename)
        }

    val isPhoto: Boolean get() = type == "photo"

    val isDocument: Boolean
        get() {
            if (isVideo) return false
            return DocumentTypes.isDocument(resolvedFilename, type)
        }
}
