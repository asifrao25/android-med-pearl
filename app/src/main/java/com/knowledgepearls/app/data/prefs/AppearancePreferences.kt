package com.knowledgepearls.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.knowledgepearls.app.ui.theme.AppearanceMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appearanceDataStore by preferencesDataStore("appearance_prefs")

@Singleton
class AppearancePreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val appearanceMode: Flow<AppearanceMode> =
        context.appearanceDataStore.data.map { prefs ->
            when (prefs[KEY_MODE]) {
                "light" -> AppearanceMode.Light
                "dark" -> AppearanceMode.Dark
                else -> AppearanceMode.System
            }
        }

    suspend fun setAppearanceMode(mode: AppearanceMode) {
        context.appearanceDataStore.edit { prefs ->
            prefs[KEY_MODE] = when (mode) {
                AppearanceMode.Light -> "light"
                AppearanceMode.Dark -> "dark"
                AppearanceMode.System -> "system"
            }
        }
    }

    companion object {
        private val KEY_MODE = stringPreferencesKey("appearance_mode")
    }
}
