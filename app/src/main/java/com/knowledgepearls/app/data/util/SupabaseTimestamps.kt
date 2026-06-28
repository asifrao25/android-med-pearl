package com.knowledgepearls.app.data.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

object SupabaseTimestamps {
    fun toEpochMillis(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        val trimmed = raw.trim()

        runCatching { return Instant.parse(trimmed).toEpochMilli() }

        var normalized = trimmed.replace(' ', 'T')
        normalized = normalized.replace(Regex("""([+-]\d{2})$""")) { "${it.value}:00" }
        val hasExplicitZone = normalized.endsWith("Z") ||
            Regex("""[+-]\d{2}:\d{2}$""").containsMatchIn(normalized)
        if (!hasExplicitZone) {
            normalized = "${normalized}Z"
        }

        runCatching { return Instant.parse(normalized).toEpochMilli() }

        val withoutZone = normalized.removeSuffix("Z").substringBefore('+').substringBeforeLast('-')
        runCatching {
            return LocalDateTime.parse(withoutZone)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli()
        }

        return null
    }
}
