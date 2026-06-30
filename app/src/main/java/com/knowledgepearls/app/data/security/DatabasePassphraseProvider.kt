package com.knowledgepearls.app.data.security

import android.content.Context
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabasePassphraseProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun getOrCreate(): ByteArray {
        val prefs = SecurePreferences.create(context, PREFS_NAME)
        val encoded = prefs.getString(KEY_PASSPHRASE, null)
        if (encoded != null) {
            return Base64.decode(encoded, Base64.NO_WRAP)
        }
        val passphrase = ByteArray(PASSPHRASE_BYTES).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(KEY_PASSPHRASE, Base64.encodeToString(passphrase, Base64.NO_WRAP))
            .apply()
        return passphrase
    }

    private companion object {
        const val PREFS_NAME = "db_security_prefs"
        const val KEY_PASSPHRASE = "room_passphrase"
        const val PASSPHRASE_BYTES = 32
    }
}
