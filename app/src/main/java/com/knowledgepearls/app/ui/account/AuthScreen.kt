package com.knowledgepearls.app.ui.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.AppBrand
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import kotlinx.coroutines.delay

private val Teal = Color(0xFF14B8A6)
private val Cyan = Color(0xFF22D3EE)

@Composable
fun AuthScreen(
    uiState: AccountUiState,
    onDismiss: () -> Unit,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onVerifyCode: (String, String) -> Unit,
    onResendCode: (String) -> Unit,
    onClearSignInSuccess: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()

    LaunchedEffect(uiState.showSignInSuccess) {
        if (uiState.showSignInSuccess) {
            delay(1400)
            onClearSignInSuccess()
            onDismiss()
        }
    }

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = TabTheme.PublicFeed)

        if (uiState.showSignInSuccess) {
            AuthSignInSuccessScreen(email = uiState.userEmail)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = PearlColors.heroPrimary(darkTheme))
                    }
                }

                AuthHeroSection()

                Spacer(Modifier.height(20.dp))

                Text(
                    text = AppBrand.NAME,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PearlColors.heroPrimary(darkTheme),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(20.dp))

                InlineAuthPanel(
                    uiState = uiState,
                    onSignIn = onSignIn,
                    onSignUp = onSignUp,
                    onGoogleSignIn = onGoogleSignIn,
                    onVerifyCode = onVerifyCode,
                    onResendCode = onResendCode,
                )

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun AuthHeroSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(
                    brush = Brush.linearGradient(listOf(Teal, Cyan)),
                    shape = RoundedCornerShape(28.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "MP",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

@Composable
fun AuthSignInSuccessScreen(email: String?) {
    val darkTheme = isPearlDarkTheme()
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(Teal, Cyan)),
                        shape = RoundedCornerShape(999.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = "You're signed in",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PearlColors.heroPrimary(darkTheme),
            )
            email?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = PearlColors.heroSecondary(darkTheme))
            }
        }
    }
}
