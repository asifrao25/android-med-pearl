package com.knowledgepearls.app.ui.shell

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.AppBrand
import com.knowledgepearls.app.R
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import kotlinx.coroutines.delay

@Composable
fun LaunchSplashScreen(onFinished: () -> Unit) {
    val darkTheme = isPearlDarkTheme()
    var progress by remember { mutableFloatStateOf(0f) }
    var contentAlpha by remember { mutableFloatStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 2000),
        label = "splashProgress",
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = contentAlpha,
        animationSpec = tween(durationMillis = 550),
        label = "splashAlpha",
    )

    LaunchedEffect(Unit) {
        delay(200)
        contentAlpha = 1f
        progress = 1f
        delay(2300)
        onFinished()
    }

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = TabTheme.Feed, intensity = 1.15f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .alpha(animatedAlpha)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFF6C5CE7),
                                Color(0xFF14B8A6),
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "MP",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineLarge,
                )
            }

            Spacer(Modifier.height(32.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(6.dp)
                    .alpha(animatedAlpha)
                    .clip(RoundedCornerShape(999.dp)),
                color = Color(0xFF22D3EE),
                trackColor = PearlColors.mutedTileBackground(darkTheme),
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = AppBrand.NAME,
                modifier = Modifier.alpha(animatedAlpha),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PearlColors.heroPrimary(darkTheme),
            )
            Text(
                text = stringResource(R.string.splash_tagline),
                modifier = Modifier
                    .alpha(animatedAlpha)
                    .padding(top = 8.dp),
                color = PearlColors.heroSecondary(darkTheme),
                textAlign = TextAlign.Center,
            )
        }
    }
}
