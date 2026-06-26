package com.knowledgepearls.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.knowledgepearls.app.ui.account.AccountViewModel
import com.knowledgepearls.app.ui.shell.MainScaffold
import com.knowledgepearls.app.ui.theme.AppearanceMode
import com.knowledgepearls.app.ui.theme.MedPearlsTheme
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var supabase: SupabaseClient
    private val accountViewModel: AccountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supabase.handleDeeplinks(intent)

        setContent {
            MedPearlsTheme(appearanceMode = AppearanceMode.Dark) {
                MainScaffold(accountViewModel = accountViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        supabase.handleDeeplinks(intent)
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            accountViewModel.runForegroundSync()
        }
    }
}
