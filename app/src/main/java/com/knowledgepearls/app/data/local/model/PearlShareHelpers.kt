package com.knowledgepearls.app.data.local.model

import com.knowledgepearls.app.data.capture.PickedMedia
import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.local.entity.PearlMediaEntity
import java.io.File

fun KnowledgePearlEntity.effectiveSourceReference(): String {
    val trimmed = sourceReference.trim()
    if (trimmed.isNotBlank()) return trimmed
    return sourceURL?.trim().orEmpty()
}

fun KnowledgePearlEntity.isSharedToPublicFeed(): Boolean =
    isSharedPublicly && publicPearlStatus in setOf("pending", "approved")

fun KnowledgePearlEntity.belongsInMyFeed(): Boolean {
    if (isSharedFromFriend) return true
    if (publicFeedSnapshot.trim().isNotEmpty()) return true
    if (isSharedPublicly) return false
    if (!publicPearlID.isNullOrBlank() && publicPearlStatus.isNotBlank()) return false
    return true
}

fun PearlMediaEntity.toPickedMedia(): PickedMedia? {
    val path = localPath?.takeIf { it.isNotBlank() } ?: return null
    val file = File(path)
    if (!file.exists()) return null
    return PickedMedia(
        bytes = file.readBytes(),
        filename = filename.ifBlank { file.name },
        type = type,
        sectionTag = sectionTag,
    )
}

fun List<PearlMediaEntity>.toPickedMedia(): List<PickedMedia> = mapNotNull { it.toPickedMedia() }
