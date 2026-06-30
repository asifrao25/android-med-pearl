package com.knowledgepearls.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.knowledgepearls.app.ui.theme.AppearanceMode
import com.knowledgepearls.app.ui.theme.AppFontChoice
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

    val appFontChoice: Flow<AppFontChoice> =
        context.appearanceDataStore.data.map { prefs ->
            AppFontChoice.fromStorageKey(prefs[KEY_FONT])
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

    suspend fun setAppFontChoice(choice: AppFontChoice) {
        context.appearanceDataStore.edit { prefs ->
            prefs[KEY_FONT] = choice.storageKey
        }
    }

    companion object {
        private val KEY_MODE = stringPreferencesKey("appearance_mode")
        private val KEY_FONT = stringPreferencesKey("app_font_choice")
    }
}
