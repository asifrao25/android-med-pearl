package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import kotlinx.coroutines.delay

private val bubbleShape = RoundedCornerShape(16.dp)

@Composable
fun PublicFeedSeenFocusScrim(
    visible: Boolean,
    theme: TabTheme,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    val scrimAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = if (visible) 340 else 460),
        label = "seenFocusScrimAlpha",
    )

    if (scrimAlpha <= 0.001f) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = scrimAlpha }
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to theme.primary.copy(alpha = if (darkTheme) 0.24f else 0.18f),
                        0.22f to Color.Black.copy(alpha = if (darkTheme) 0.72f else 0.56f),
                        0.55f to Color.Black.copy(alpha = if (darkTheme) 0.68f else 0.52f),
                        1f to theme.primary.copy(alpha = if (darkTheme) 0.30f else 0.22f),
                    ),
                ),
            )
            .background(Color.Black.copy(alpha = if (darkTheme) 0.28f else 0.18f)),
    )
}

@Composable
fun MovedToSeenTabToast(
    visible: Boolean,
    theme: TabTheme,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    val surface = PearlColors.popupSurface(darkTheme)
    val borderColor = theme.primary.copy(alpha = 0.38f)

    LaunchedEffect(visible) {
        if (visible) {
            delay(2_600)
            onDismiss()
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.86f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "seenToastScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "seenToastAlpha",
    )

    if (alpha <= 0.01f && !visible) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PearlLayout.screenHorizontalPadding)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            },
        contentAlignment = Alignment.BottomEnd,
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .fillMaxWidth(0.74f)
                .semantics { contentDescription = "Moved to Seen tab" },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(bubbleShape)
                    .background(surface)
                    .border(1.5.dp, borderColor, bubbleShape)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = theme.primary,
                    modifier = Modifier.size(22.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Moved to Seen tab",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = PearlColors.heroPrimary(darkTheme),
                    )
                    Text(
                        text = "Find it in Seen below",
                        style = MaterialTheme.typography.bodySmall,
                        color = PearlColors.heroSecondary(darkTheme),
                    )
                }
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = theme.primary.copy(alpha = 0.75f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
