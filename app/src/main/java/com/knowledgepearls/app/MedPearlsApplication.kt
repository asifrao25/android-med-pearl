package com.knowledgepearls.app

import android.app.Application
import com.knowledgepearls.app.data.backup.ScheduledBackupWorker
import com.knowledgepearls.app.push.PushNotificationDisplay
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MedPearlsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PushNotificationDisplay.ensureChannels(this)
        ScheduledBackupWorker.schedule(this)
    }
}
