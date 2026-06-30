package com.knowledgepearls.app

import android.app.Application
import com.knowledgepearls.app.data.analytics.AnalyticsMediaReporter
import com.knowledgepearls.app.data.analytics.AnalyticsService
import com.knowledgepearls.app.data.backup.ScheduledBackupWorker
import com.knowledgepearls.app.push.PushNotificationDisplay
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

@HiltAndroidApp
class MedPearlsApplication : Application() {
    @Inject lateinit var analyticsService: AnalyticsService
    @Inject lateinit var analyticsMediaReporter: AnalyticsMediaReporter

    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("sqlcipher")
        PushNotificationDisplay.ensureChannels(this)
        ScheduledBackupWorker.schedule(this)
        analyticsService.startLifecycleMonitoring()
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    appScope.launch { analyticsMediaReporter.reportSnapshot() }
                }

                override fun onStop(owner: LifecycleOwner) {
                    appScope.launch { analyticsMediaReporter.reportSnapshot() }
                }
            },
        )
    }
}
