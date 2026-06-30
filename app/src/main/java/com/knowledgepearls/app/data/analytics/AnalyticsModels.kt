package com.knowledgepearls.app.data.analytics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

typealias AnalyticsMetadata = Map<String, JsonElement>

fun analyticsMetaString(key: String, value: String): Pair<String, JsonElement> =
    key to JsonPrimitive(value.truncateAnalyticsLabel())

fun analyticsMetaTags(tags: List<String>): Pair<String, JsonElement> =
    "tags" to JsonArray(
        tags
            .map { it.trim().lowercase().truncateAnalyticsLabel(maxLength = 32) }
            .filter { it.isNotEmpty() }
            .take(12)
            .map(::JsonPrimitive),
    )

fun String.truncateAnalyticsLabel(maxLength: Int = 64): String =
    trim().take(maxLength)

@Serializable
data class AnalyticsEventPayload(
    @SerialName("event_type") val eventType: String,
    @SerialName("pearl_id") val pearlId: String? = null,
    @SerialName("content_type") val contentType: String? = null,
    @SerialName("duration_seconds") val durationSeconds: Double? = null,
    val metadata: AnalyticsMetadata = emptyMap(),
    @SerialName("client_timestamp") val clientTimestamp: String,
)

@Serializable
data class AnalyticsIngestBody(
    @SerialName("device_id") val deviceId: String,
    val platform: String,
    @SerialName("app_version") val appVersion: String?,
    val events: List<AnalyticsEventPayload>,
)
