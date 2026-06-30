package com.knowledgepearls.app.data.security

import android.content.Context
import android.content.SharedPreferences

object LegacyPreferencesMigration {
    fun migrateStringSetPreferences(
        context: Context,
        legacyName: String,
        encrypted: SharedPreferences,
    ) {
        if (encrypted.all.isNotEmpty()) return
        val legacy = context.getSharedPreferences(legacyName, Context.MODE_PRIVATE)
        if (legacy.all.isEmpty()) return

        val editor = encrypted.edit()
        legacy.all.forEach { (key, value) ->
            when (value) {
                is String -> editor.putString(key, value)
                is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
            }
        }
        editor.apply()
        legacy.edit().clear().apply()
    }

    fun readLegacyString(
        context: Context,
        legacyNames: List<String>,
        key: String,
    ): String? {
        legacyNames.forEach { name ->
            val value = context.getSharedPreferences(name, Context.MODE_PRIVATE).getString(key, null)
            if (!value.isNullOrBlank()) return value
        }
        return null
    }

    fun clearLegacyString(
        context: Context,
        legacyNames: List<String>,
        key: String,
    ) {
        legacyNames.forEach { name ->
            context.getSharedPreferences(name, Context.MODE_PRIVATE)
                .edit()
                .remove(key)
                .apply()
        }
    }
}
