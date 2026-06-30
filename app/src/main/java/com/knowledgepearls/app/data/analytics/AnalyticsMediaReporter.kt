package com.knowledgepearls.app.data.analytics

import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.repository.KnowledgePearlRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsMediaReporter @Inject constructor(
    private val pearlRepository: KnowledgePearlRepository,
    private val analyticsService: AnalyticsService,
) {
    suspend fun reportSnapshot() {
        val pearls = pearlRepository.getAllPearlsWithMedia()
        var totalBytes = 0L
        var totalCount = 0
        val byType = mutableMapOf<String, Int>()
        val pearlTypes = mutableMapOf<String, Int>()

        for (pearl in pearls) {
            val pearlType = AnalyticsContentType.forPearl(pearl)
            pearlTypes[pearlType] = pearlTypes.getOrDefault(pearlType, 0) + 1
            for (media in pearl.mediaItems) {
                totalCount += 1
                val size = media.localPath?.let { path ->
                    runCatching { java.io.File(path).length() }.getOrDefault(0L)
                } ?: 0L
                totalBytes += size
                val key = media.type
                byType[key] = byType.getOrDefault(key, 0) + 1
            }
        }

        val metadata = mutableMapOf<String, kotlinx.serialization.json.JsonElement>()
        metadata += analyticsMetaString("total_bytes", totalBytes.toString())
        metadata += analyticsMetaString("total_count", totalCount.toString())
        metadata += analyticsMetaString("pearl_count", pearls.size.toString())
        byType.forEach { (key, value) -> metadata += analyticsMetaString("type_$key", value.toString()) }
        pearlTypes.forEach { (key, value) -> metadata += analyticsMetaString("pearl_type_$key", value.toString()) }

        analyticsService.track(
            kind = AnalyticsEventKind.MediaSnapshot,
            metadata = metadata,
        )
    }
}
