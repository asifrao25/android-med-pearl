package com.knowledgepearls.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.recentShareRecipientsDataStore by preferencesDataStore("recent_share_recipients")

@Serializable
data class RecentShareRecipient(
    val id: String,
    val name: String,
)

@Singleton
class RecentShareRecipientsStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    val recentRecipients: Flow<List<RecentShareRecipient>> =
        context.recentShareRecipientsDataStore.data.map { prefs ->
            decodeRecipients(prefs[KEY_RECENT])
        }

    suspend fun getRecent(): List<RecentShareRecipient> = recentRecipients.first()

    suspend fun recordShares(recipients: List<RecentShareRecipient>) {
        if (recipients.isEmpty()) return
        val normalized = recipients.map { recipient ->
            recipient.copy(
                id = recipient.id.lowercase(),
                name = recipient.name.ifBlank { "Unknown" },
            )
        }
        context.recentShareRecipientsDataStore.edit { prefs ->
            val existing = decodeRecipients(prefs[KEY_RECENT])
            val newIds = normalized.map { it.id }.toSet()
            val merged = (normalized + existing.filter { it.id !in newIds }).take(MAX_RECENT)
            prefs[KEY_RECENT] = json.encodeToString(merged)
        }
    }

    private fun decodeRecipients(raw: String?): List<RecentShareRecipient> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            json.decodeFromString<List<RecentShareRecipient>>(raw)
        }.getOrDefault(emptyList())
    }

    private companion object {
        const val MAX_RECENT = 5
        val KEY_RECENT = stringPreferencesKey("recent_share_recipients_json")
    }
}
