package com.knowledgepearls.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.connectivity.BackendHealthState
import com.knowledgepearls.app.data.connectivity.ConnectivityState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ConnectivityOverlays(
    connectivity: ConnectivityState,
    backendHealth: BackendHealthState,
    isActive: Boolean = true,
    onContinueOffline: () -> Unit,
    onRetryConnection: () -> Unit,
    onDismissBackendAlert: () -> Unit,
    onRetryBackend: suspend () -> Unit,
    onDismissRestoredToast: () -> Unit,
    scope: CoroutineScope,
) {
    val showsOfflineBorder = isActive &&
        connectivity.hasResolvedPath &&
        !connectivity.isConnected

    val showsBackendBorder = isActive &&
        connectivity.isConnected &&
        !connectivity.showOfflinePrompt &&
        backendHealth.hasCompletedHealthCheck &&
        !backendHealth.isBackendReachable

    AnimatedVisibility(
        visible = showsOfflineBorder,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        PulsingScreenBorder(
            colors = listOf(Color(0xFFFF6B57), Color(0xFFFF3B30), Color(0xFFD1141F)),
        )
    }

    AnimatedVisibility(
        visible = showsBackendBorder,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        PulsingScreenBorder(
            colors = listOf(Color(0xFFFFBD57), Color(0xFFFF9F38), Color(0xFFD16B14)),
        )
    }

    if (isActive && connectivity.showOfflinePrompt) {
        NoInternetConnectionAlert(
            onContinueOffline = onContinueOffline,
            onTryAgain = onRetryConnection,
        )
    }

    if (isActive &&
        connectivity.isConnected &&
        !connectivity.showOfflinePrompt &&
        backendHealth.showUnavailableAlert
    ) {
        BackendUnavailableAlert(
            onDismiss = onDismissBackendAlert,
            onTryAgain = { scope.launch { onRetryBackend() } },
        )
    }

    Box(Modifier.fillMaxSize()) {
        BackendRestoredToast(
            visible = isActive && backendHealth.showRestoredNotice,
            onDismiss = onDismissRestoredToast,
        )
    }
}

@Composable
private fun PulsingScreenBorder(colors: List<Color>) {
    val transition = rememberInfiniteTransition(label = "borderPulse")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(880),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    val cornerRadius = 42.dp
    val strokeWidth = 5.dp + (4.dp * pulse)
    val glowAlpha = 0.35f + (0.45f * pulse)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val strokePx = strokeWidth.toPx()
                val radiusPx = cornerRadius.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(colors.map { it.copy(alpha = glowAlpha) }),
                    topLeft = Offset(strokePx / 2, strokePx / 2),
                    size = Size(size.width - strokePx, size.height - strokePx),
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokePx),
                )
            }
            .border(
                width = 1.5.dp,
                color = Color.White.copy(alpha = 0.12f + (0.16f * pulse)),
                shape = RoundedCornerShape(cornerRadius),
            ),
    )
}
