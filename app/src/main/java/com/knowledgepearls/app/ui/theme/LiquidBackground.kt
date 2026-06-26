package com.knowledgepearls.app.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.max

/**
 * Per-tab ambient background for Android — soft radial colour washes on a dark
 * canvas. Uses native Canvas gradients (no blur) so glows stay smooth on API 26+.
 */
@Composable
fun LiquidBackground(
    theme: TabTheme,
    modifier: Modifier = Modifier,
    scrollOffset: Float = 0f,
    intensity: Float = 1f,
    darkTheme: Boolean = isPearlDarkTheme(),
) {
    val parallaxY = scrollOffset * 0.12f

    val infiniteTransition = rememberInfiniteTransition(label = "tabBgDrift")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "drift",
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val maxDim = max(width, height)
            val driftX = (drift - 0.5f) * maxDim * 0.04f
            val driftY = (drift - 0.5f) * maxDim * 0.03f

            val base = if (darkTheme) theme.canvasTintDark else theme.canvasTintLight
            drawRect(base)

            val glowStrength = if (darkTheme) 0.26f * intensity else 0.16f * intensity

            drawAmbientGlow(
                color = theme.primary,
                centerX = width * 0.18f + driftX,
                centerY = height * 0.10f - parallaxY + driftY,
                radius = maxDim * 0.72f,
                alpha = glowStrength,
            )
            drawAmbientGlow(
                color = theme.secondary,
                centerX = width * 0.88f - driftX * 0.6f,
                centerY = height * 0.38f - parallaxY * 0.5f,
                radius = maxDim * 0.58f,
                alpha = glowStrength * 0.85f,
            )
            drawAmbientGlow(
                color = theme.primary,
                centerX = width * 0.42f + driftX * 0.4f,
                centerY = height * 0.78f - parallaxY + driftY * 0.5f,
                radius = maxDim * 0.50f,
                alpha = glowStrength * 0.55f,
            )

            if (darkTheme) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.28f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.42f),
                        ),
                    ),
                )
            }
        }
    }
}

private fun DrawScope.drawAmbientGlow(
    color: Color,
    centerX: Float,
    centerY: Float,
    radius: Float,
    alpha: Float,
) {
    drawCircle(
        brush = Brush.radialGradient(
            0f to color.copy(alpha = alpha),
            0.42f to color.copy(alpha = alpha * 0.32f),
            1f to Color.Transparent,
            center = Offset(centerX, centerY),
            radius = radius,
        ),
        radius = radius,
        center = Offset(centerX, centerY),
    )
}
