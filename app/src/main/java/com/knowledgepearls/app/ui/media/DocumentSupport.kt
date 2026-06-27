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

    fun mimeType(filename: String): String {
        val ext = extensionOf(filename)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"
    }
}
