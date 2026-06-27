package com.knowledgepearls.app.ui.media

import com.knowledgepearls.app.data.capture.PickedMedia
import com.knowledgepearls.app.data.local.entity.MediaType
import com.knowledgepearls.app.data.local.entity.PearlMediaEntity
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaSlide
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerRequest
import java.io.File

fun treatsAsVideo(type: String, filename: String): Boolean {
    if (type == MediaType.VIDEO) return true
    val lower = filename.lowercase()
    return type == MediaType.DOCUMENT &&
        (lower.endsWith(".mp4") || lower.endsWith(".mov") || lower.endsWith(".m4v"))
}

fun treatsAsDocument(type: String, filename: String): Boolean {
    if (treatsAsVideo(type, filename)) return false
    return DocumentSupport.isDocument(filename, type)
}

fun mediaUriForPath(path: String): String = File(path).toURI().toString()

fun localPearlMediaSlides(items: List<PearlMediaEntity>): List<PublicPearlMediaSlide> =
    items.mapNotNull { item ->
        val path = item.localPath?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val uri = mediaUriForPath(path)
        val filename = item.filename.ifBlank { File(path).name }
        when {
            treatsAsVideo(item.type, filename) -> PublicPearlMediaSlide.Video(uri, filename)
            treatsAsDocument(item.type, filename) -> PublicPearlMediaSlide.Document(uri, filename)
            else -> PublicPearlMediaSlide.Image(uri)
        }
    }

fun gallerySlides(items: List<PearlMediaEntity>): List<PublicPearlMediaSlide> =
    items.filter { item ->
        val path = item.localPath ?: return@filter false
        !treatsAsDocument(item.type, item.filename.ifBlank { File(path).name })
    }.let(::localPearlMediaSlides)

fun documentSlides(items: List<PearlMediaEntity>): List<PearlMediaEntity> =
    items.filter { item ->
        val path = item.localPath ?: return@filter false
        treatsAsDocument(item.type, item.filename.ifBlank { File(path).name })
    }

fun pearlMediaViewerRequest(
    pearl: PearlWithMedia,
    slide: PublicPearlMediaSlide? = null,
): PublicPearlMediaViewerRequest? {
    val gallery = gallerySlides(pearl.mediaItems)
    if (slide != null) {
        return PublicPearlMediaViewerRequest(
            slides = if (slide is PublicPearlMediaSlide.Document) listOf(slide) else gallery,
            initialIndex = if (slide is PublicPearlMediaSlide.Document) {
                0
            } else {
                gallery.indexOfFirst { it.id == slide.id }.coerceAtLeast(0)
            },
        )
    }
    if (gallery.isNotEmpty()) {
        return PublicPearlMediaViewerRequest(gallery)
    }
    pearl.pearl.linkPreviewImagePath?.takeIf { it.isNotBlank() }?.let { path ->
        return PublicPearlMediaViewerRequest(
            slides = listOf(PublicPearlMediaSlide.Image(mediaUriForPath(path))),
        )
    }
    return null
}

fun pickedMediaSlides(items: List<PickedMedia>, cacheDir: File): List<PublicPearlMediaSlide> =
    items.mapNotNull { item ->
        val cached = cachePickedMedia(item, cacheDir)
        when {
            treatsAsVideo(item.type, item.filename) -> PublicPearlMediaSlide.Video(cached, item.filename)
            treatsAsDocument(item.type, item.filename) -> PublicPearlMediaSlide.Document(cached, item.filename)
            else -> PublicPearlMediaSlide.Image(cached)
        }
    }

fun cachePickedMedia(item: PickedMedia, cacheDir: File): String {
    val folder = File(cacheDir, "capture_draft").apply { mkdirs() }
    val extension = item.filename.substringAfterLast('.', "bin")
    val target = File(folder, "${item.filename.hashCode()}-${item.bytes.size}.$extension")
    if (!target.exists() || target.length() != item.bytes.size.toLong()) {
        target.writeBytes(item.bytes)
    }
    return target.toURI().toString()
}
