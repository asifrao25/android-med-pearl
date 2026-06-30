package com.knowledgepearls.app.data.analytics

import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.local.entity.MediaType
import com.knowledgepearls.app.data.local.entity.PearlMediaEntity
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.local.model.isClinicalCase
import com.knowledgepearls.app.data.model.PublicPearl

object AnalyticsContentType {
    fun forPearl(pearl: KnowledgePearlEntity, mediaItems: List<PearlMediaEntity>): String {
        if (pearl.isClinicalCase()) return "clinical_case"
        if (!pearl.sourceURL.isNullOrBlank() && mediaItems.isEmpty()) return "link"
        if (mediaItems.isEmpty()) return "text"
        return when (mediaItems.firstOrNull()?.type) {
            MediaType.IMAGE -> "photo"
            MediaType.VIDEO -> "video"
            MediaType.PDF, MediaType.DOCUMENT -> "document"
            else -> "text"
        }
    }

    fun forPearl(pearlWithMedia: PearlWithMedia): String =
        forPearl(pearlWithMedia.pearl, pearlWithMedia.mediaItems)

    fun forPublicPearl(pearl: PublicPearl): String =
        pearl.contentType.ifBlank { "text" }
}
