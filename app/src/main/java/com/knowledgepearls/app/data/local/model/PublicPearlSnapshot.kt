package com.knowledgepearls.app.data.local.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

/** Locally captured pearls the user can edit — not friend shares or public-feed saves. */
fun KnowledgePearlEntity.isUserEditable(): Boolean =
    !isSharedFromFriend && !isSavedFromPublicFeed()

@Composable
fun rememberDecodedPublicPearl(pearl: KnowledgePearlEntity): PublicPearl? =
    remember(pearl.id, pearl.publicFeedSnapshot) {
        pearl.decodedPublicPearl()
    }

@Composable
fun rememberPublicPearlForCard(
    pearl: KnowledgePearlEntity,
    fetchById: suspend (String) -> PublicPearl?,
): PublicPearl? {
    val snapshot = rememberDecodedPublicPearl(pearl)
    var fetched by remember(pearl.id) { mutableStateOf<PublicPearl?>(null) }
    val publicId = pearl.publicPearlID

    LaunchedEffect(pearl.id, publicId, snapshot) {
        if (snapshot != null) {
            fetched = null
            return@LaunchedEffect
        }
        fetched = publicId?.takeIf { it.isNotBlank() }?.let { fetchById(it) }
    }

    return snapshot ?: fetched
}
