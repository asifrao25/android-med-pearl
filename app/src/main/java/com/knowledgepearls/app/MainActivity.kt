package com.knowledgepearls.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.knowledgepearls.app.data.connectivity.BackendHealthMonitor
import com.knowledgepearls.app.data.connectivity.ConnectivityMonitor
import com.knowledgepearls.app.data.push.PushNotificationManager
import com.knowledgepearls.app.navigation.AppNavigationBus
import com.knowledgepearls.app.navigation.DeepLinkRouter
import com.knowledgepearls.app.navigation.ShareImportPayload
import com.knowledgepearls.app.ui.account.AccountViewModel
import com.knowledgepearls.app.ui.settings.SettingsViewModel
import com.knowledgepearls.app.ui.shell.MainScaffold
import com.knowledgepearls.app.ui.theme.MedPearlsTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import javax.inject.Inject
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var supabase: SupabaseClient
    @Inject lateinit var connectivityMonitor: ConnectivityMonitor
    @Inject lateinit var backendHealthMonitor: BackendHealthMonitor
    @Inject lateinit var pushNotificationManager: PushNotificationManager
    @Inject lateinit var navigationBus: AppNavigationBus

    private val accountViewModel: AccountViewModel by viewModels()

    private var pendingShareImport: ShareImportPayload? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            lifecycleScope.launch {
                pushNotificationManager.syncAuthState(accountViewModel.uiState.value.userId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { false }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingIntent(intent)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                accountViewModel.uiState
                    .map { it.isSignedIn && !it.isLoading }
                    .distinctUntilChanged()
                    .collect { ready ->
                        if (ready) ensurePushNotificationsReady()
                    }
            }
        }

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val appearanceMode by settingsViewModel.appearanceMode.collectAsStateWithLifecycle()

            MedPearlsTheme(appearanceMode = appearanceMode) {
                MainScaffold(
                    accountViewModel = accountViewModel,
                    settingsViewModel = settingsViewModel,
                    connectivityMonitor = connectivityMonitor,
                    backendHealthMonitor = backendHealthMonitor,
                    navigationBus = navigationBus,
                    initialShareImport = pendingShareImport,
                    onShareImportConsumed = { pendingShareImport = null },
                    onRequestPushNotifications = ::ensurePushNotificationsReady,
                )
            }
        }
    }

    fun ensurePushNotificationsReady() {
        requestPushPermissionIfNeeded()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent) {
        if (handleAuthDeepLink(intent)) return

        DeepLinkRouter.parse(intent)?.let { event ->
            navigationBus.emit(event)
            if (event is com.knowledgepearls.app.navigation.AppNavigationEvent.ImportShare) {
                pendingShareImport = ShareImportPayload(event.text, event.url)
            }
            return
        }
    }

    private fun handleAuthDeepLink(intent: Intent): Boolean {
        var handled = false
        supabase.handleDeeplinks(intent) {
            handled = true
            lifecycleScope.launch {
                accountViewModel.onOAuthSessionEstablished()
                requestPushPermissionIfNeeded()
            }
        }
        return handled
    }

    override fun onStart() {
        super.onStart()
        connectivityMonitor.start()
        backendHealthMonitor.start()
        lifecycleScope.launch {
            accountViewModel.runForegroundSync()
            requestPushPermissionIfNeeded()
        }
    }

    override fun onStop() {
        connectivityMonitor.stop()
        backendHealthMonitor.stop()
        super.onStop()
    }

    private fun requestPushPermissionIfNeeded() {
        if (!accountViewModel.uiState.value.isSignedIn) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !pushNotificationManager.hasNotificationPermission()
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            lifecycleScope.launch {
                pushNotificationManager.syncAuthState(accountViewModel.uiState.value.userId)
            }
        }
    }
}
