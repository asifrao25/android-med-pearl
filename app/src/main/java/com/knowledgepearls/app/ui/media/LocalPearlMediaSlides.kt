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

fun treatsAsDocument(type: String, filename: String, url: String = ""): Boolean {
    if (treatsAsVideo(type, filename)) return false
    val effectiveName = effectiveMediaFilename(filename, url)
    return DocumentSupport.isDocument(effectiveName, type, url)
}

fun localPearlMediaSlides(items: List<PearlMediaEntity>): List<PublicPearlMediaSlide> =
    items.mapNotNull { item ->
        val path = item.localPath?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val uri = mediaUriForPath(path)
        val filename = effectiveMediaFilename(item.filename, path)
        when {
            treatsAsVideo(item.type, filename) -> PublicPearlMediaSlide.Video(uri, filename)
            treatsAsDocument(item.type, filename, uri) -> PublicPearlMediaSlide.Document(uri, filename)
            else -> PublicPearlMediaSlide.Image(uri)
        }
    }

fun gallerySlides(items: List<PearlMediaEntity>): List<PublicPearlMediaSlide> =
    items.filter { item ->
        val path = item.localPath ?: return@filter false
        val filename = effectiveMediaFilename(item.filename, path)
        val uri = mediaUriForPath(path)
        !treatsAsDocument(item.type, filename, uri)
    }.let(::localPearlMediaSlides)

fun documentSlides(items: List<PearlMediaEntity>): List<PearlMediaEntity> =
    items.filter { item ->
        val path = item.localPath ?: return@filter false
        val filename = effectiveMediaFilename(item.filename, path)
        val uri = mediaUriForPath(path)
        treatsAsDocument(item.type, filename, uri)
    }

fun pearlMediaViewerRequest(
    pearl: PearlWithMedia,
    slide: PublicPearlMediaSlide? = null,
): PublicPearlMediaViewerRequest? {
    val slides = localPearlMediaSlides(pearl.mediaItems)
    if (slide != null) {
        return PublicPearlMediaViewerRequest(
            slides = slides,
            initialIndex = slides.indexOfFirst { it.id == slide.id }.coerceAtLeast(0),
        )
    }
    if (slides.isNotEmpty()) {
        return PublicPearlMediaViewerRequest(slides)
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
        val filename = effectiveMediaFilename(item.filename, cached)
        when {
            treatsAsVideo(item.type, filename) -> PublicPearlMediaSlide.Video(cached, filename)
            treatsAsDocument(item.type, filename, cached) -> PublicPearlMediaSlide.Document(cached, filename)
            else -> PublicPearlMediaSlide.Image(cached)
        }
    }

fun cachePickedMedia(item: PickedMedia, cacheDir: File): String {
    val folder = File(cacheDir, "capture_draft").apply { mkdirs() }
    val extension = effectiveMediaFilename(item.filename)
        .substringAfterLast('.', "bin")
    val target = File(folder, "${item.filename.hashCode()}-${item.bytes.size}.$extension")
    if (!target.exists() || target.length() != item.bytes.size.toLong()) {
        target.writeBytes(item.bytes)
    }
    return target.toURI().toString()
}
