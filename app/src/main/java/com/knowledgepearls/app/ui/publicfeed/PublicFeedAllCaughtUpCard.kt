package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun PublicFeedAllCaughtUpCard(
    theme: TabTheme,
    onSwitchToSeen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    val mint = Color(0xFF61F2B8)
    val transition = rememberInfiniteTransition(label = "caughtUpGlow")
    val glowScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowScale",
    )
    val iconGlow by transition.animateFloat(
        initialValue = 12f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "iconGlow",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(PearlColors.popupSurface(darkTheme))
            .border(1.dp, PearlColors.cardBorder(darkTheme), RoundedCornerShape(22.dp))
            .padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .graphicsLayer {
                        scaleX = glowScale
                        scaleY = glowScale
                    }
                    .clip(CircleShape)
                    .background(theme.primary.copy(alpha = 0.14f)),
            )
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = theme.primary,
                modifier = Modifier
                    .size(46.dp)
                    .graphicsLayer {
                        shadowElevation = iconGlow
                    },
            )
        }

        Text(
            text = "All Caught Up",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PearlColors.heroPrimary(darkTheme),
            modifier = Modifier.padding(top = 14.dp),
        )

        Text(
            text = "No new pearls right now.\nCheck back soon.",
            style = MaterialTheme.typography.bodyMedium,
            color = PearlColors.heroSecondary(darkTheme),
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(theme.primary.copy(alpha = 0.10f))
                .border(1.dp, theme.primary.copy(alpha = 0.28f), RoundedCornerShape(999.dp))
                .clickable(onClick = onSwitchToSeen)
                .semantics {
                    contentDescription = "Switch to Seen tab to revisit previous pearls"
                }
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = theme.primary.copy(alpha = 0.85f),
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "Switch to Seen below to revisit previous pearls",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = theme.primary.copy(alpha = 0.88f),
            )
        }
    }
}

@Composable
fun PublicFeedSectionEmptyState(
    title: String,
    message: String,
    theme: TabTheme,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val darkTheme = isPearlDarkTheme()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PearlColors.popupSurface(darkTheme))
            .border(1.dp, PearlColors.cardBorder(darkTheme), RoundedCornerShape(20.dp))
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = PearlColors.heroPrimary(darkTheme),
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = PearlColors.heroSecondary(darkTheme),
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(listOf(theme.primary, theme.secondary)),
                    )
                    .clickable(onClick = onAction)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
