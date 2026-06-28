package com.knowledgepearls.app.ui.components

import android.content.Context

/** Persists whether the user has performed a horizontal swipe on a pearl card. */
object SwipeRowHintStorage {
    private const val PREFS_NAME = "swipe_row_hint_prefs"
    private const val KEY_DISMISSED = "cardSwipeHintDismissed"

    fun isDismissed(context: Context): Boolean =
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DISMISSED, false)

    fun dismiss(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DISMISSED, true)
            .apply()
    }
}
