package com.knowledgepearls.app.ui.media

import java.io.File
import java.net.URI
import java.net.URL
import java.security.MessageDigest

object DocumentFileResolver {
    fun isRemoteUrl(url: String): Boolean = DocumentSupport.isRemoteUrl(url)

    fun resolveLocalFile(url: String): File? {
        val direct = when {
            url.startsWith("file:") -> runCatching { File(URI(url)) }.getOrNull()
            !isRemoteUrl(url) -> File(url)
            else -> null
        }
        return direct?.takeIf { it.exists() && it.isFile }
    }

    fun resolveFile(cacheDir: File, url: String, filename: String): File {
        resolveLocalFile(url)?.let { return it }
        val effectiveName = effectiveMediaFilename(filename, url)
        return cacheRemoteFile(cacheDir, url, effectiveName)
    }

    fun cacheRemoteFile(cacheDir: File, url: String, filename: String): File {
        val cacheRoot = File(cacheDir, "media_viewer").apply { mkdirs() }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(url.toByteArray())
            .joinToString("") { "%02x".format(it) }
        val extension = effectiveMediaFilename(filename, url)
            .substringAfterLast('.', "")
            .takeIf { it.isNotBlank() } ?: "bin"
        val target = File(cacheRoot, "$digest.$extension")
        if (target.exists() && target.length() > 0L) return target

        URL(url).openStream().use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return target
    }
}
