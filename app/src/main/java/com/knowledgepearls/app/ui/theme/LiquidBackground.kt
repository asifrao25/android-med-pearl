package com.knowledgepearls.app.ui.theme

import android.os.Build
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Animated liquid gradient blobs — port of iOS `LiquidBackground.swift`.
 *
 * Platform note: `Modifier.blur` requires API 31+. On API 26–30 blobs render without blur
 * (slightly sharper — acceptable fallback per migration plan).
 */
@Composable
fun LiquidBackground(
    theme: TabTheme,
    modifier: Modifier = Modifier,
    scrollOffset: Float = 0f,
    intensity: Float = 1f,
    darkTheme: Boolean = isPearlDarkTheme(),
) {
    val parallaxDp = (-scrollOffset * 0.3f).dp
    val blobOpacity = PearlColors.liquidBlobOpacity(darkTheme) * intensity

    val infiniteTransition = rememberInfiniteTransition(label = "liquidDrift")
    val driftA by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "driftA",
    )
    val driftB by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 13_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "driftB",
    )
    val driftC by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "driftC",
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(PearlColors.canvas(darkTheme))
        }

        LiquidBlob(
            color = theme.primary,
            size = 240.dp,
            offsetX = (-110).dp + 50.dp * driftA,
            offsetY = (-180).dp + (-30).dp * driftA + parallaxDp,
            opacity = blobOpacity,
        )
        LiquidBlob(
            color = theme.secondary,
            size = 200.dp,
            offsetX = 130.dp + (-40).dp * driftB,
            offsetY = 60.dp + 35.dp * driftB + parallaxDp,
            opacity = blobOpacity,
        )
        LiquidBlob(
            color = theme.primary,
            size = 180.dp,
            offsetX = (-70).dp + 35.dp * driftC,
            offsetY = 320.dp + (-25).dp * driftC + parallaxDp,
            opacity = blobOpacity,
        )
    }
}

@Composable
private fun LiquidBlob(
    color: Color,
    size: Dp,
    offsetX: Dp,
    offsetY: Dp,
    opacity: Float,
) {
    val supportsBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val blurRadius = if (supportsBlur) size * 0.3f else 0.dp

    Box(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .size(size)
            .then(if (supportsBlur) Modifier.blur(blurRadius) else Modifier),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(color = color.copy(alpha = if (supportsBlur) opacity else opacity * 0.85f))
        }
    }
}
