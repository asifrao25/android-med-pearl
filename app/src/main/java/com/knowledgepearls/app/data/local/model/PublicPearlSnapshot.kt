package com.knowledgepearls.app.data.local.model

import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.model.PublicPearl
import kotlinx.serialization.json.Json

private val publicPearlSnapshotJson = Json { ignoreUnknownKeys = true }

fun KnowledgePearlEntity.decodedPublicPearl(): PublicPearl? {
    val raw = publicFeedSnapshot.trim()
    if (raw.isEmpty()) return null
    return runCatching { publicPearlSnapshotJson.decodeFromString<PublicPearl>(raw) }.getOrNull()
}

fun KnowledgePearlEntity.isSavedFromPublicFeed(): Boolean = decodedPublicPearl() != null
