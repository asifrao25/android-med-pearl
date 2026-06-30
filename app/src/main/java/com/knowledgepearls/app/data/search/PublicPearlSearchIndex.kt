package com.knowledgepearls.app.data.search

import com.knowledgepearls.app.data.model.PublicPearl

class PublicPearlSearchIndex private constructor(
    private val entries: Map<String, PublicPearlSearchEntry>,
) {
    fun matchesPearl(pearlId: String, rawQuery: String): Boolean {
        val query = rawQuery.trim()
        if (query.isEmpty()) return true
        val entry = entries[pearlId] ?: return false
        val tokens = PearlSearchTokenizer.tokenize(query)
        if (tokens.isEmpty()) {
            return entry.titleNormalized.contains(query.lowercase())
        }
        return tokens.all { token -> entry.matchesToken(token) }
    }

    companion object {
        fun build(pearls: List<PublicPearl>): PublicPearlSearchIndex =
            PublicPearlSearchIndex(
                entries = pearls.associate { pearl ->
                    pearl.id to PublicPearlSearchEntry.from(pearl)
                },
            )

        fun topTags(pearls: List<PublicPearl>, limit: Int = 10): List<String> {
            val counts = linkedMapOf<String, Int>()
            val labels = linkedMapOf<String, String>()
            pearls.forEach { pearl ->
                pearl.tags.forEach { raw ->
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
    }
}

private data class PublicPearlSearchEntry(
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
        fun from(pearl: PublicPearl): PublicPearlSearchEntry {
            val title = pearl.titleDisplay.trim()
            val titleNormalized = title.lowercase()
            val titleTokens = PearlSearchTokenizer.split(titleNormalized).toSet()
            val supporting = buildString {
                append(pearl.notes)
                append(' ')
                append(pearl.scraperTweetText)
                append(' ')
                append(pearl.scraperLearningPoint)
                append(' ')
                append(pearl.sharedBy)
                append(' ')
                append(pearl.tags.joinToString(" "))
            }.lowercase()

            return PublicPearlSearchEntry(
                titleNormalized = titleNormalized,
                titleTokens = titleTokens,
                supportingText = supporting,
            )
        }
    }
}
