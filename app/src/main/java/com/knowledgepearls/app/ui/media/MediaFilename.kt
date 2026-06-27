package com.knowledgepearls.app.ui.media

import java.io.File

fun effectiveMediaFilename(filename: String, pathOrUrl: String = ""): String {
    val trimmed = filename.trim()
    if (trimmed.contains('.') && !trimmed.endsWith('.')) return trimmed

    val fromPath = when {
        pathOrUrl.startsWith("file:") -> runCatching { File(java.net.URI(pathOrUrl)).name }.getOrNull()
        pathOrUrl.isNotBlank() && !DocumentSupport.isRemoteUrl(pathOrUrl) ->
            File(pathOrUrl.removePrefix("file://")).name
        else -> null
    }
    if (!fromPath.isNullOrBlank() && fromPath.contains('.')) return fromPath

    return trimmed.ifBlank { "Document" }
}

fun mediaUriForPath(path: String): String =
    if (path.startsWith("file:")) path else File(path).toURI().toString()
