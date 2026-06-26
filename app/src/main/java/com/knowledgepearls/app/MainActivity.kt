package com.knowledgepearls.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.knowledgepearls.app.ui.shell.MainScaffold
import com.knowledgepearls.app.ui.theme.AppearanceMode
import com.knowledgepearls.app.ui.theme.MedPearlsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
}
