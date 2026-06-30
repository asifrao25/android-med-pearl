package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.account.AccountUiState
import com.knowledgepearls.app.ui.account.AuthSignInSuccessScreen
import com.knowledgepearls.app.ui.account.InlineAuthPanel
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import kotlinx.coroutines.delay

@Composable
fun PublicFeedAuthGate(
    accountState: AccountUiState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onVerifyCode: (String, String) -> Unit,
    onResendCode: (String) -> Unit,
    onClearSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = TabTheme.PublicFeed
    val darkTheme = isPearlDarkTheme()

    LaunchedEffect(accountState.showSignInSuccess) {
        if (accountState.showSignInSuccess) {
            delay(1400)
            onClearSignInSuccess()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = PearlLayout.screenHorizontalPadding)
                .padding(bottom = PearlLayout.tabBarOverlayInset),
            contentAlignment = Alignment.Center,
        ) {
            if (accountState.showSignInSuccess) {
                AuthSignInSuccessScreen(email = accountState.userEmail)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 620.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(PearlColors.popupSurface(darkTheme))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = if (darkTheme) 0.12f else 0.28f),
                            shape = RoundedCornerShape(24.dp),
                        )
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = theme.primary,
                        modifier = Modifier.size(44.dp),
                    )

                    Text(
                        text = "Public Feed",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PearlColors.heroPrimary(darkTheme),
                    )

                    Text(
                        text = "Discover and share clinical pearls from doctors across the UK.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PearlColors.heroSecondary(darkTheme),
                        textAlign = TextAlign.Center,
                    )

                    InlineAuthPanel(
                        uiState = accountState,
                        onSignIn = onSignIn,
                        onSignUp = onSignUp,
                        onGoogleSignIn = onGoogleSignIn,
                        onVerifyCode = onVerifyCode,
                        onResendCode = onResendCode,
                    )
                }
            }
        }
    }
}

@Composable
fun PublicFeedEmptyState(
    isError: Boolean,
    message: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    val theme = TabTheme.PublicFeed

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PearlLayout.screenHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (isError) {
            Text(
                text = message ?: "Could not load public feed.",
                color = PearlColors.heroSecondary(darkTheme),
                textAlign = TextAlign.Center,
            )
            androidx.compose.material3.Button(onClick = onRetry) {
                Text("Try again")
            }
        } else {
            Icon(
                imageVector = Icons.Default.Inbox,
                contentDescription = null,
                tint = theme.primary.copy(alpha = 0.55f),
                modifier = Modifier.height(40.dp),
            )
            Text(
                text = "No shared public pearls yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = PearlColors.heroPrimary(darkTheme),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Community pearls from other clinicians will appear here when they're approved.",
                color = PearlColors.heroSecondary(darkTheme),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}
