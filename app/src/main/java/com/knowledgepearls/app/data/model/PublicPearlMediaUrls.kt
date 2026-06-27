package com.knowledgepearls.app.data.model

import com.knowledgepearls.app.data.remote.SupabaseConfig

object PublicPearlMediaUrls {
    fun publicStorageUrl(path: String): String {
        val clean = path.trim().trimStart('/')
        return "${SupabaseConfig.URL}/storage/v1/object/public/${SupabaseConfig.PUBLIC_PEARL_MEDIA_BUCKET}/$clean"
    }

    fun resolve(url: String?, path: String?): String? {
        val trimmedUrl = url?.trim().orEmpty()
        if (trimmedUrl.isNotEmpty()) {
            return fixPublicMediaUrl(trimmedUrl) ?: trimmedUrl
        }
        val trimmedPath = path?.trim().orEmpty()
        if (trimmedPath.isNotEmpty()) {
            return publicStorageUrl(trimmedPath)
        }
        return null
    }

    fun fixPublicMediaUrl(url: String): String? {
        if (
            !url.contains("supabase-kong") &&
            !url.contains("localhost") &&
            !url.contains("127.0.0.1")
        ) {
            return url
        }
        val match = Regex("public-pearl-media/(.+)$").find(url) ?: return url
        return publicStorageUrl(match.groupValues[1])
    }

    fun isImageUrl(url: String): Boolean {
        val path = url.substringBefore('?').lowercase()
        return listOf(".jpg", ".jpeg", ".png", ".gif", ".webp", ".heic")
            .any { path.endsWith(it) }
    }

    private val videoExtensions = setOf("mp4", "mov", "m4v", "webm", "3gp", "3g2", "avi", "mkv")

    fun isVideoFilename(filename: String): Boolean {
        val ext = filename.substringAfterLast('.', "").lowercase()
        return ext in videoExtensions
    }
}
