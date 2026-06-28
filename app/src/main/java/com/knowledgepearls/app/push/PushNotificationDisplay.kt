package com.knowledgepearls.app.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.knowledgepearls.app.MainActivity
import com.knowledgepearls.app.R
import com.knowledgepearls.app.navigation.DeepLinkRouter

object PushNotificationDisplay {
    fun ensureChannels(context: Context) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        listOf(
            Triple(PushNotificationChannels.DEFAULT, "Med Pearls", "General alerts"),
            Triple(PushNotificationChannels.MESSAGES, "Messages", "Direct messages and chats"),
            Triple(PushNotificationChannels.SHARES, "Pearl shares", "Pearls shared with you"),
            Triple(PushNotificationChannels.FEED, "Public feed", "New and approved public pearls"),
            Triple(PushNotificationChannels.SOCIAL, "Social", "Likes and community activity"),
        ).forEach { (id, name, description) ->
            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT).apply {
                this.description = description
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun show(context: Context, payload: PushNotificationPayload) {
        ensureChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(DeepLinkRouter.EXTRA_NOTIFICATION_TYPE, payload.type)
            payload.title.takeIf { it.isNotBlank() }?.let {
                putExtra(DeepLinkRouter.EXTRA_NOTIFICATION_TITLE, it)
            }
            payload.body.takeIf { it.isNotBlank() }?.let {
                putExtra(DeepLinkRouter.EXTRA_NOTIFICATION_BODY, it)
            }
            payload.conversationId?.let { putExtra(DeepLinkRouter.EXTRA_CONVERSATION_ID, it) }
            payload.pearlShareId?.let { putExtra(DeepLinkRouter.EXTRA_PEARL_SHARE_ID, it) }
            payload.pearlId?.let { putExtra(DeepLinkRouter.EXTRA_PEARL_ID, it) }
        }

        val requestCode = (payload.conversationId ?: payload.pearlShareId ?: payload.pearlId ?: payload.type).hashCode()
        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val title = payload.title.ifBlank { context.getString(R.string.app_name) }
        val body = payload.body

        val notification = NotificationCompat.Builder(context, payload.channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(requestCode, notification)
    }
}
