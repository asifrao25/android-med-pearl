package com.knowledgepearls.app.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.knowledgepearls.app.MainActivity
import com.knowledgepearls.app.R
import com.knowledgepearls.app.data.push.PushNotificationManager
import com.knowledgepearls.app.navigation.AppNavigationBus
import com.knowledgepearls.app.navigation.AppNavigationEvent
import com.knowledgepearls.app.navigation.DeepLinkRouter
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
        val conversationId = data["conversation_id"]
        val pearlShareId = data["pearl_share_id"]
        val title = message.notification?.title ?: data["title"] ?: getString(R.string.app_name)
        val body = message.notification?.body ?: data["body"] ?: ""

        if (pearlShareId != null && isAppInForeground()) {
            navigationBus.emit(
                AppNavigationEvent.PearlShareReceivedToast(
                    senderName = title,
                    pearlTitle = body,
                ),
            )
        }

        showNotification(
            title = title,
            body = body,
            conversationId = conversationId,
            pearlShareId = pearlShareId,
        )
    }

    private fun showNotification(
        title: String,
        body: String,
        conversationId: String?,
        pearlShareId: String?,
    ) {
        ensureChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            conversationId?.let { putExtra(DeepLinkRouter.EXTRA_CONVERSATION_ID, it) }
            pearlShareId?.let { putExtra(DeepLinkRouter.EXTRA_PEARL_SHARE_ID, it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            (conversationId ?: pearlShareId ?: title).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(
            (conversationId ?: pearlShareId ?: System.currentTimeMillis().toString()).hashCode(),
            notification,
        )
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Med Pearls",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Messages and pearl shares"
        }
        manager.createNotificationChannel(channel)
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        return activityManager.runningAppProcesses?.any {
            it.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                it.processName == packageName
        } == true
    }

    private companion object {
        const val CHANNEL_ID = "medpearls_messages"
    }
}
