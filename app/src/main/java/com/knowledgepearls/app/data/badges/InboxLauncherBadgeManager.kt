package com.knowledgepearls.app.data.badges

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import android.Manifest
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.knowledgepearls.app.MainActivity
import com.knowledgepearls.app.R
import com.knowledgepearls.app.navigation.DeepLinkRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InboxLauncherBadgeManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun sync(unreadCount: Int) {
        if (!canPostNotifications()) return
        ensureChannel()
        if (unreadCount <= 0) {
            NotificationManagerCompat.from(context).cancel(BADGE_NOTIFICATION_ID)
            return
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data = android.net.Uri.parse("${DeepLinkRouter.SCHEME}://inbox")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            BADGE_NOTIFICATION_ID,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val body = context.resources.getQuantityString(
            R.plurals.inbox_launcher_badge_body,
            unreadCount,
            unreadCount,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(body)
            .setNumber(unreadCount)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setAutoCancel(false)
            .setOngoing(true)
            .build()

        NotificationManagerCompat.from(context).notify(BADGE_NOTIFICATION_ID, notification)
    }

    fun clear() = sync(0)

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.inbox_launcher_badge_channel),
            NotificationManager.IMPORTANCE_MIN,
        ).apply {
            description = context.getString(R.string.inbox_launcher_badge_channel_desc)
            setShowBadge(true)
            enableLights(false)
            enableVibration(false)
            setSound(null, null)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "medpearls_inbox_badge"
        private const val BADGE_NOTIFICATION_ID = 7401
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface InboxBadgeEntryPoint {
    fun inboxLauncherBadgeManager(): InboxLauncherBadgeManager
}
