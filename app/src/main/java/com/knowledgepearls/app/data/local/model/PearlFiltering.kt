package com.knowledgepearls.app.data.local.model

import com.knowledgepearls.app.data.local.entity.MediaType
import com.knowledgepearls.app.data.local.entity.PearlMediaEntity
import com.knowledgepearls.app.data.model.ContentTypeFilter

private val GALLERY_MEDIA_TYPES = setOf(
    MediaType.IMAGE,
    MediaType.VIDEO,
    MediaType.PDF,
    MediaType.DOCUMENT,
)

fun String.isGalleryMediaType(): Boolean = this in GALLERY_MEDIA_TYPES

fun PearlMediaEntity.isGalleryMedia(): Boolean = type.isGalleryMediaType()

fun PearlWithMedia.hasGalleryMedia(): Boolean = mediaItems.any { it.isGalleryMedia() }

/** Matches PearlsKit: quick kind, or standard text-only pearl with no link and no attachments. */
fun PearlWithMedia.isQuickPearl(): Boolean =
    pearl.isQuickPearl(hasMedia = mediaItems.isNotEmpty())

fun PearlWithMedia.matches(filter: ContentTypeFilter): Boolean {
    pearl.decodedPublicPearl()?.let { return it.matches(filter) }

    val entity = pearl
    return when (filter) {
        ContentTypeFilter.ALL -> true
        ContentTypeFilter.CASES -> entity.isClinicalCase()
        ContentTypeFilter.QUICK -> isQuickPearl()
        ContentTypeFilter.PHOTOS ->
            !entity.isClinicalCase() &&
                !isQuickPearl() &&
                hasGalleryMedia()
        ContentTypeFilter.LINKS ->
            !entity.isClinicalCase() && !entity.sourceURL.isNullOrBlank()
    }
}
