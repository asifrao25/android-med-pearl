package com.knowledgepearls.app.data.search

import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.local.model.clinicalCasePayload
import com.knowledgepearls.app.data.local.model.isClinicalCase

/**
 * In-memory title-focused search index rebuilt when the pearl list changes.
 * Tokenizes headings for prefix/word matching without a full DB migration.
 */
class PearlSearchIndex private constructor(
    private val entries: Map<String, PearlSearchEntry>,
) {
    fun matchesPearl(pearlId: String, rawQuery: String): Boolean {
        val query = rawQuery.trim()
        if (query.isEmpty()) return true
        val entry = entries[pearlId] ?: return false
        val tokens = tokenize(query)
        if (tokens.isEmpty()) {
            return entry.titleNormalized.contains(query.lowercase())
        }
        return tokens.all { token -> entry.matchesToken(token) }
    }

    companion object {
        fun build(pearls: List<PearlWithMedia>): PearlSearchIndex =
            PearlSearchIndex(
                entries = pearls.associate { pearl ->
                    pearl.pearl.id to PearlSearchEntry.from(pearl)
                },
            )

        fun topTags(pearls: List<PearlWithMedia>, limit: Int = 10): List<String> {
            val counts = linkedMapOf<String, Int>()
            val labels = linkedMapOf<String, String>()
            pearls.forEach { pearl ->
                pearl.pearl.tags.forEach { raw ->
                    val label = raw.trim()
                    if (label.isEmpty()) return@forEach
                    val key = label.lowercase()
                    counts[key] = (counts[key] ?: 0) + 1
                    labels.putIfAbsent(key, label)
                }
            }
            return counts.entries
                .sortedWith(
                    compareByDescending<Map.Entry<String, Int>> { it.value }
                        .thenBy { it.key },
                )
                .take(limit)
                .mapNotNull { labels[it.key] }
        }

        private fun tokenize(text: String): List<String> =
            TOKEN_SPLIT.split(text.lowercase())
                .filter { it.length >= 2 }
                .distinct()
    }
}

private data class PearlSearchEntry(
    val titleNormalized: String,
    val titleTokens: Set<String>,
    val supportingText: String,
) {
    fun matchesToken(token: String): Boolean {
        if (token.length < 2) return false
        if (titleNormalized.contains(token)) return true
        if (titleTokens.any { word -> word.startsWith(token) || token.startsWith(word) }) return true
        return supportingText.contains(token)
    }

    companion object {
        fun from(pearl: PearlWithMedia): PearlSearchEntry {
            val entity = pearl.pearl
            val title = entity.title.trim()
            val titleNormalized = title.lowercase()
            val titleTokens = TOKEN_SPLIT.split(titleNormalized).filter { it.isNotEmpty() }.toSet()

            val supporting = buildString {
                append(entity.notes)
                append(' ')
                append(entity.sourceReference)
                append(' ')
                append(entity.linkPreviewDescription)
                append(' ')
                append(entity.tags.joinToString(" "))
                if (entity.isClinicalCase()) {
                    val payload = entity.clinicalCasePayload()
                    append(' ')
                    append(payload.history)
                    append(' ')
                    append(payload.examination)
                    append(' ')
                    append(payload.investigation)
                    append(' ')
                    append(payload.diagnosis)
                    append(' ')
                    append(payload.discussion)
                }
            }.lowercase()

            return PearlSearchEntry(
                titleNormalized = titleNormalized,
                titleTokens = titleTokens,
                supportingText = supporting,
            )
        }
    }
}

private val TOKEN_SPLIT = Regex("[^a-z0-9]+")
