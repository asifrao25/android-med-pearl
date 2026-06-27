package com.knowledgepearls.app.data.local.model

import com.knowledgepearls.app.data.local.entity.KnowledgePearlContentKind
import com.knowledgepearls.app.data.model.ContentTypeFilter

fun PearlWithMedia.matches(filter: ContentTypeFilter): Boolean {
    val pearl = pearl
    return when (filter) {
        ContentTypeFilter.ALL -> true
        ContentTypeFilter.CASES -> pearl.isClinicalCase()
        ContentTypeFilter.QUICK -> pearl.isQuickPearl()
        ContentTypeFilter.PHOTOS ->
            !pearl.isClinicalCase() &&
                !pearl.isQuickPearl() &&
                mediaItems.any { it.type in GALLERY_MEDIA_TYPES }
        ContentTypeFilter.LINKS ->
            !pearl.isClinicalCase() && !pearl.sourceURL.isNullOrBlank()
    }
}

private val GALLERY_MEDIA_TYPES = setOf("image", "video", "pdf", "document")
