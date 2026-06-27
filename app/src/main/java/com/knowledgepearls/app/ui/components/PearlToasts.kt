package com.knowledgepearls.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import kotlinx.coroutines.delay

private val restoredAccent = Color(0xFF61F2B8)

@Composable
fun BackendRestoredToast(
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(visible) {
        if (visible) {
            delay(3_500)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = Modifier.statusBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    .border(1.dp, restoredAccent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Column {
                    Text("Connection Restored", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Knowledge Pearls is back online.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.72f),
                    )
                }
            }
        }
    }
}

@Composable
fun SeenToastView(
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(visible) {
        if (visible) {
            delay(2_400)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PearlLayout.screenHorizontalPadding, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .background(TabTheme.PublicFeed.primary.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = TabTheme.PublicFeed.primary,
                    )
                    Text(
                        text = "Marked as seen",
                        color = TabTheme.PublicFeed.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
fun InboxUnreadReminderChip(
    unreadCount: Int,
    theme: TabTheme,
    onOpenInbox: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (unreadCount <= 0) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PearlLayout.screenHorizontalPadding)
            .background(theme.primary.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenInbox)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Column {
            Icon(Icons.Default.Inbox, contentDescription = null, tint = theme.primary)
            Text(
                text = if (unreadCount == 1) "1 unread item in your inbox" else "$unreadCount unread items in your inbox",
                fontWeight = FontWeight.SemiBold,
                color = theme.primary,
            )
            Text(
                text = "Tap to open · Dismiss",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.clickable(onClick = onDismiss),
            )
        }
    }
}

@Composable
fun PearlShareReceivedToast(
    visible: Boolean,
    senderName: String,
    onOpenInbox: () -> Unit,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(visible) {
        if (visible) {
            delay(4_000)
            onDismiss()
        }
    }

    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PearlLayout.screenHorizontalPadding, vertical = 12.dp)
                .background(TabTheme.PublicFeed.primary.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
                .clickable(onClick = onOpenInbox)
                .padding(16.dp),
        ) {
            Column {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TabTheme.PublicFeed.primary)
                Text("Pearl shared with you", fontWeight = FontWeight.SemiBold)
                Text("From $senderName", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
