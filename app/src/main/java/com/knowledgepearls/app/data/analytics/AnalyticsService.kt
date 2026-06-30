package com.knowledgepearls.app.data.analytics

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.knowledgepearls.app.AnalyticsPlatform
import com.knowledgepearls.app.BuildConfig
import com.knowledgepearls.app.data.local.entity.KnowledgePearlEntity
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.data.remote.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class AnalyticsService @Inject constructor(
    private val store: AnalyticsStore,
    private val supabase: SupabaseClient,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val queueMutex = Mutex()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val httpClient = HttpClient(Android)

    private var pending = listOf<AnalyticsEventPayload>()
    private var sessionStartMs: Long? = null
    private var flushJob: Job? = null
    private var heartbeatJob: Job? = null
    private var lifecycleRegistered = false

    fun startLifecycleMonitoring() {
        if (lifecycleRegistered) return
        lifecycleRegistered = true

        scope.launch {
            pending = store.loadPending()
            track(AnalyticsEventKind.AppOpen)
            sessionStartMs = System.currentTimeMillis()
            startFlushLoop()
            startHeartbeat()
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    scope.launch {
                        track(AnalyticsEventKind.AppOpen)
                        if (sessionStartMs == null) {
                            sessionStartMs = System.currentTimeMillis()
                        }
                        startHeartbeat()
                    }
                }

                override fun onStop(owner: LifecycleOwner) {
                    scope.launch {
                        endSession()
                        track(AnalyticsEventKind.AppBackground)
                        flushNow()
                        stopHeartbeat()
                    }
                }
            },
        )
    }

    fun track(
        kind: AnalyticsEventKind,
        pearlId: String? = null,
        contentType: String? = null,
        durationSeconds: Double? = null,
        metadata: AnalyticsMetadata = emptyMap(),
    ) {
        scope.launch {
            queueMutex.withLock {
                pending = pending + AnalyticsEventPayload(
                    eventType = kind.wireName,
                    pearlId = pearlId?.lowercase(),
                    contentType = contentType?.take(32),
                    durationSeconds = durationSeconds,
                    metadata = metadata,
                    clientTimestamp = Instant.now().toString(),
                )
                store.savePending(pending)
                if (pending.size >= MAX_BATCH_SIZE) {
                    scope.launch { flushNowInternal() }
                }
            }
        }
    }

    fun trackPearlCreated(
        pearl: KnowledgePearlEntity,
        mediaItems: List<com.knowledgepearls.app.data.local.entity.PearlMediaEntity>,
        captureKind: AnalyticsEventKind? = null,
    ) {
        if (captureKind != null) {
            track(
                kind = captureKind,
                contentType = AnalyticsContentType.forPearl(pearl, mediaItems),
            )
        }
        track(
            kind = AnalyticsEventKind.PearlCreated,
            pearlId = pearl.id,
            contentType = AnalyticsContentType.forPearl(pearl, mediaItems),
            metadata = pearlCreatedMetadata(pearl),
        )
    }

    fun trackPearlDeleted(pearl: KnowledgePearlEntity, mediaItems: List<com.knowledgepearls.app.data.local.entity.PearlMediaEntity>) {
        track(
            kind = AnalyticsEventKind.PearlDeleted,
            pearlId = pearl.id,
            contentType = AnalyticsContentType.forPearl(pearl, mediaItems),
        )
    }

    fun trackCardOpened(
        pearl: PearlWithMedia,
        isPublic: Boolean = false,
        ownerLabel: String? = null,
    ) {
        val title = pearl.pearl.title.ifBlank { "Untitled" }
        track(
            kind = AnalyticsEventKind.CardOpened,
            pearlId = pearl.pearl.id,
            contentType = AnalyticsContentType.forPearl(pearl),
            metadata = cardOpenedMetadata(title, ownerLabel, if (isPublic) "public" else "local"),
        )
        if (isPublic) {
            pearl.pearl.publicPearlID?.let { publicId ->
                track(
                    kind = AnalyticsEventKind.Viewed,
                    pearlId = publicId,
                    contentType = AnalyticsContentType.forPearl(pearl),
                )
            }
        }
    }

    fun trackPublicCardOpened(pearl: PublicPearl) {
        track(
            kind = AnalyticsEventKind.CardOpened,
            pearlId = pearl.id,
            contentType = AnalyticsContentType.forPublicPearl(pearl),
            metadata = cardOpenedMetadata(
                title = pearl.titleDisplay.ifBlank { "Untitled" },
                ownerLabel = pearl.safeDisplayName,
                scope = "public",
            ),
        )
        track(
            kind = AnalyticsEventKind.Viewed,
            pearlId = pearl.id,
            contentType = AnalyticsContentType.forPublicPearl(pearl),
        )
    }

    fun trackLinkOpened(url: String, pearlId: String?) {
        val host = runCatching { java.net.URI(url).host }.getOrNull() ?: "unknown"
        track(
            kind = AnalyticsEventKind.LinkOpened,
            pearlId = pearlId,
            metadata = mapOf(analyticsMetaString("host", host)),
        )
    }

    fun trackPearlSharedFriend(pearl: PearlWithMedia, recipientCount: Int) {
        track(
            kind = AnalyticsEventKind.PearlSharedFriend,
            pearlId = pearl.pearl.id,
            contentType = AnalyticsContentType.forPearl(pearl),
            metadata = shareMetadata(pearl.pearl.title, "friend", recipientCount),
        )
    }

    fun trackPearlShareResponse(accepted: Boolean, pearlTitle: String, contentType: String?) {
        track(
            kind = if (accepted) AnalyticsEventKind.PearlShareAccepted else AnalyticsEventKind.PearlShareDeclined,
            contentType = contentType,
            metadata = mapOf(
                analyticsMetaString("share_channel", "friend"),
                analyticsMetaString("pearl_title", pearlTitle.ifBlank { "Untitled" }),
            ),
        )
    }

    fun trackPearlSharedPublic(pearlId: String, contentType: String) {
        track(
            kind = AnalyticsEventKind.PearlSharedPublic,
            pearlId = pearlId,
            contentType = contentType,
        )
    }

    fun trackAddedToFeed(publicPearlId: String, contentType: String) {
        track(
            kind = AnalyticsEventKind.AddedToFeed,
            pearlId = publicPearlId,
            contentType = contentType,
        )
    }

    suspend fun flushNow() {
        flushNowInternal()
    }

    private suspend fun endSession() {
        val start = sessionStartMs ?: return
        val seconds = ((System.currentTimeMillis() - start) / 1000.0).coerceAtLeast(1.0)
        track(AnalyticsEventKind.SessionEnd, durationSeconds = seconds)
        sessionStartMs = null
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (true) {
                delay(HEARTBEAT_INTERVAL_MS)
                track(AnalyticsEventKind.Heartbeat)
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    private fun startFlushLoop() {
        flushJob?.cancel()
        flushJob = scope.launch {
            while (true) {
                delay(FLUSH_INTERVAL_MS)
                flushNowInternal()
            }
        }
    }

    private suspend fun flushNowInternal() {
        val accessToken = supabase.auth.currentSessionOrNull()?.accessToken ?: return

        val batch = queueMutex.withLock {
            if (pending.isEmpty()) return
            val slice = pending.take(MAX_BATCH_SIZE)
            pending = pending.drop(slice.size)
            store.savePending(pending)
            slice
        }

        val body = AnalyticsIngestBody(
            deviceId = store.deviceId(),
            platform = AnalyticsPlatform.VALUE,
            appVersion = BuildConfig.VERSION_NAME,
            events = batch,
        )

        try {
            val response: HttpResponse = httpClient.post(SupabaseConfig.ANALYTICS_INGEST_URL) {
                contentType(ContentType.Application.Json)
                header("apikey", SupabaseConfig.ANON_KEY)
                header("Authorization", "Bearer $accessToken")
                setBody(json.encodeToString(body))
            }
            if (!response.status.isSuccess()) {
                requeue(batch)
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "Analytics flush failed: HTTP ${response.status.value}; re-queued ${batch.size}")
                }
            }
        } catch (error: Exception) {
            requeue(batch)
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Analytics flush error: ${error.message}; re-queued ${batch.size}")
            }
        }
    }

    private suspend fun requeue(batch: List<AnalyticsEventPayload>) {
        queueMutex.withLock {
            pending = batch + pending
            store.savePending(pending)
        }
    }

    private fun pearlCreatedMetadata(pearl: KnowledgePearlEntity): AnalyticsMetadata {
        val title = pearl.title.ifBlank { "Untitled" }
        val metadata = mutableMapOf(analyticsMetaString("pearl_title", title))
        if (pearl.tags.isNotEmpty()) {
            metadata += analyticsMetaTags(pearl.tags)
        }
        return metadata
    }

    private fun shareMetadata(title: String, channel: String, recipientCount: Int? = null): AnalyticsMetadata {
        val metadata = mutableMapOf(
            analyticsMetaString("share_channel", channel),
            analyticsMetaString("pearl_title", title.ifBlank { "Untitled" }),
        )
        recipientCount?.let { metadata += analyticsMetaString("recipient_count", it.toString()) }
        return metadata
    }

    private fun cardOpenedMetadata(title: String, ownerLabel: String?, scope: String): AnalyticsMetadata {
        val metadata = mutableMapOf(
            analyticsMetaString("scope", scope),
            analyticsMetaString("pearl_title", title),
        )
        ownerLabel?.trim()?.takeIf { it.isNotEmpty() }?.let {
            metadata += analyticsMetaString("owner_label", it)
        }
        return metadata
    }

    companion object {
        private const val TAG = "Analytics"
        private const val FLUSH_INTERVAL_MS = 30_000L
        private const val HEARTBEAT_INTERVAL_MS = 30_000L
        private const val MAX_BATCH_SIZE = 40
    }
}
