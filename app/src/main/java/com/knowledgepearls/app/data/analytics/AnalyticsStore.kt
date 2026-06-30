package com.knowledgepearls.app.data.analytics

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.analyticsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "analytics_store",
)

@Singleton
class AnalyticsStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val mutex = Mutex()

    private val deviceIdKey = stringPreferencesKey("analytics_device_id")
    private val pendingKey = stringPreferencesKey("analytics_pending_events")

    suspend fun deviceId(): String = mutex.withLock {
        val prefs = context.analyticsDataStore.data.first()
        prefs[deviceIdKey]?.let { existing ->
            runCatching { UUID.fromString(existing) }.getOrNull()?.toString()
        } ?: UUID.randomUUID().toString().also { created ->
            context.analyticsDataStore.edit { it[deviceIdKey] = created }
        }
    }

    suspend fun loadPending(): List<AnalyticsEventPayload> = mutex.withLock {
        val raw = context.analyticsDataStore.data.first()[pendingKey] ?: return emptyList()
        runCatching { json.decodeFromString<List<AnalyticsEventPayload>>(raw) }.getOrDefault(emptyList())
    }

    suspend fun savePending(events: List<AnalyticsEventPayload>) = mutex.withLock {
        context.analyticsDataStore.edit { prefs ->
            if (events.isEmpty()) {
                prefs.remove(pendingKey)
            } else {
                prefs[pendingKey] = json.encodeToString(events)
            }
        }
    }
}
