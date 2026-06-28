package com.knowledgepearls.app.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.knowledgepearls.app.data.push.PushNotificationManager
import com.knowledgepearls.app.navigation.AppNavigationBus
import com.knowledgepearls.app.navigation.AppNavigationEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MedPearlsFirebaseMessagingService : FirebaseMessagingService() {
    @Inject lateinit var pushNotificationManager: PushNotificationManager
    @Inject lateinit var navigationBus: AppNavigationBus

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            pushNotificationManager.onNewToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val payload = PushNotificationPayload.fromRemoteData(data).let { parsed ->
            if (parsed.title.isBlank() && parsed.body.isBlank()) {
                parsed.copy(
                    title = message.notification?.title.orEmpty(),
                    body = message.notification?.body.orEmpty(),
                )
            } else {
                parsed
            }
        }

        if (payload.type == PushNotificationType.PEARL_SHARE && isAppInForeground()) {
            navigationBus.emit(
                AppNavigationEvent.PearlShareReceivedToast(
                    senderName = payload.title.ifBlank { "Someone" },
                    pearlTitle = payload.body,
                ),
            )
        }

        PushNotificationDisplay.show(this, payload)
        navigationBus.emit(AppNavigationEvent.RefreshInboxBadge)
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as android.app.ActivityManager
        return activityManager.runningAppProcesses?.any {
            it.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                it.processName == packageName
        } == true
    }
}
