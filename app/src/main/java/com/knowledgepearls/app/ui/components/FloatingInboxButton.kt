package com.knowledgepearls.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme

@Composable
fun FloatingInboxButton(
    badgeCount: Int,
    theme: TabTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasUnread = badgeCount > 0
    val badgeLabel = when {
        badgeCount <= 0 -> null
        badgeCount > 99 -> "99+"
        else -> badgeCount.toString()
    }
    val description = if (badgeLabel != null) {
        "Inbox, $badgeCount unread"
    } else {
        "Inbox"
    }

    val pulseTransition = rememberInfiniteTransition(label = "inboxFabPulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "inboxFabPulseScale",
    )
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "inboxFabPulseAlpha",
    )

    Box(
        modifier = modifier.size(PearlLayout.inboxButtonSize + 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (hasUnread) {
            Box(
                modifier = Modifier
                    .size(PearlLayout.inboxButtonSize + 12.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .border(2.5.dp, theme.primary.copy(alpha = pulseAlpha), CircleShape),
            )
        }

        Box(
            modifier = Modifier
                .size(PearlLayout.inboxButtonSize)
                .semantics { contentDescription = description }
                .shadow(10.dp, CircleShape, clip = false)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        listOf(theme.primary, theme.secondary),
                    ),
                )
                .border(2.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }

        badgeLabel?.let { label ->
            Text(
                text = label,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .zIndex(1f)
                    .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                    .background(Color(0xFFFF3B30), CircleShape)
                    .border(1.5.dp, Color.White, CircleShape)
                    .padding(horizontal = 5.dp, vertical = 2.dp),
            )
        }
    }
}
