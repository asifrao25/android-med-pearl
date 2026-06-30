package com.knowledgepearls.app.data.auth

import android.content.Context
import com.knowledgepearls.app.data.security.LegacyPreferencesMigration
import com.knowledgepearls.app.data.security.SecurePreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Singleton
class EncryptedSupabaseSessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : SessionManager {
    private val prefs = SecurePreferences.create(context, PREFS_NAME)
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @OptIn(SupabaseInternal::class)
    override suspend fun saveSession(session: UserSession) {
        prefs.edit()
            .putString(KEY_SESSION, json.encodeToString(UserSession.serializer(), session))
            .apply()
    }

    @OptIn(SupabaseInternal::class)
    override suspend fun loadSession(): UserSession? {
        prefs.getString(KEY_SESSION, null)?.let { raw ->
            return runCatching {
                json.decodeFromString(UserSession.serializer(), raw)
            }.getOrNull()
        }

        val legacyRaw = LegacyPreferencesMigration.readLegacyString(
            context = context,
            legacyNames = LEGACY_AUTH_PREF_NAMES,
            key = KEY_SESSION,
        ) ?: return null

        val session = runCatching {
            json.decodeFromString(UserSession.serializer(), legacyRaw)
        }.getOrNull() ?: return null

        saveSession(session)
        LegacyPreferencesMigration.clearLegacyString(context, LEGACY_AUTH_PREF_NAMES, KEY_SESSION)
        return session
    }

    override suspend fun deleteSession() {
        prefs.edit().remove(KEY_SESSION).apply()
    }

    private companion object {
        const val PREFS_NAME = "supabase_auth_secure_prefs"
        const val KEY_SESSION = "session"
        val LEGACY_AUTH_PREF_NAMES = listOf(
            "supabase",
            "supabase.supabase-kt",
            "io.github.jan.supabase",
        )
    }
}
