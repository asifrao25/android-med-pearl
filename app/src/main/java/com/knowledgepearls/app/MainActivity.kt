package com.knowledgepearls.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.knowledgepearls.app.data.sync.PearlSyncCoordinator
import com.knowledgepearls.app.ui.shell.MainScaffold
import com.knowledgepearls.app.ui.theme.AppearanceMode
import com.knowledgepearls.app.ui.theme.MedPearlsTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var pearlSyncCoordinator: PearlSyncCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Default dark — matches iOS Info.plist UIUserInterfaceStyle
            MedPearlsTheme(appearanceMode = AppearanceMode.Dark) {
                MainScaffold()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            // Stage 4 will pass the signed-in Supabase user id.
            pearlSyncCoordinator.runIfAuthenticated(userId = null)
        }
    }
}
