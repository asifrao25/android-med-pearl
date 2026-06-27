package com.knowledgepearls.app

import android.app.Application
import com.knowledgepearls.app.data.backup.ScheduledBackupWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MedPearlsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ScheduledBackupWorker.schedule(this)
    }
}
