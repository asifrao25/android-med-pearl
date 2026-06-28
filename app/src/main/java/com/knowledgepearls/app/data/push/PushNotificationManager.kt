package com.knowledgepearls.app.data.push

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class PushNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PushNotificationRepository,
) {
    private var registeredUserId: String? = null
    private var currentToken: String? = null

    val isFirebaseConfigured: Boolean
        get() = runCatching {
            Class.forName("com.google.firebase.FirebaseApp")
            com.google.firebase.FirebaseApp.getApps(context).isNotEmpty()
        }.getOrDefault(false)

    suspend fun syncAuthState(userId: String?) {
        if (userId.isNullOrBlank()) {
            val previousUser = registeredUserId
            val token = currentToken
            if (!previousUser.isNullOrBlank() && !token.isNullOrBlank()) {
                runCatching { repository.removeToken(previousUser, token) }
            }
            registeredUserId = null
            currentToken = null
            return
        }

        registeredUserId = userId
        if (!isFirebaseConfigured) {
            Log.w(TAG, "Firebase not configured — skip FCM token upload")
            return
        }
        if (!hasNotificationPermission()) {
            Log.d(TAG, "Notification permission not granted yet")
            return
        }

        runCatching {
            val token = fetchFcmToken()
            currentToken = token
            repository.uploadToken(userId, token)
            Log.d(TAG, "FCM token uploaded for user")
        }.onFailure { error ->
            Log.e(TAG, "FCM token upload failed", error)
        }
    }

    suspend fun onNewToken(token: String) {
        currentToken = token
        val userId = registeredUserId ?: return
        runCatching { repository.uploadToken(userId, token) }
    }

    fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun fetchFcmToken(): String = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                if (continuation.isActive) continuation.resume(token)
            }
            .addOnFailureListener { error ->
                if (continuation.isActive) continuation.resumeWithException(error)
            }
    }

    companion object {
        private const val TAG = "PushNotificationManager"
    }
}
