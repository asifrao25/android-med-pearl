package com.knowledgepearls.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
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
                    .semantics { contentDescription = "Connection restored. Knowledge Pearls is back online." }
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
                    .semantics { contentDescription = "Marked as seen" }
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PearlLayout.screenHorizontalPadding)
            .background(theme.primary.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .semantics(mergeDescendants = true) {
                    contentDescription = if (unreadCount == 1) {
                        "1 unread item in your inbox. Tap to open."
                    } else {
                        "$unreadCount unread items in your inbox. Tap to open."
                    }
                }
                .clickable(onClick = onOpenInbox),
        ) {
            Icon(Icons.Default.Inbox, contentDescription = null, tint = theme.primary)
            Text(
                text = if (unreadCount == 1) "1 unread item in your inbox" else "$unreadCount unread items in your inbox",
                fontWeight = FontWeight.SemiBold,
                color = theme.primary,
            )
        }
        Text(
            text = "Dismiss",
            style = MaterialTheme.typography.labelSmall,
            color = theme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clickable(onClick = onDismiss)
                .semantics { contentDescription = "Dismiss inbox reminder" },
        )
    }
}

@Composable
fun PearlActionSuccessToast(
    outcome: PearlActionOutcome,
    theme: TabTheme,
    onDismiss: () -> Unit,
    folderName: String? = null,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    val surface = PearlColors.popupSurface(darkTheme)
    val borderColor = theme.primary.copy(alpha = 0.42f)

    val (icon, title, message) = when (outcome) {
        PearlActionOutcome.SavedToMyFeed -> Triple(
            Icons.Default.CheckCircle,
            "Saved to My Feed",
            "This pearl is now in your personal feed.",
        )
        PearlActionOutcome.SavedToFolder -> Triple(
            Icons.Default.Folder,
            "Saved to Folder",
            "Added to \"${folderName.orEmpty()}\".",
        )
        PearlActionOutcome.RemovedFromFeed -> Triple(
            Icons.Default.Delete,
            "Removed from Feed",
            "This pearl is hidden from your public feed.",
        )
        else -> return
    }

    LaunchedEffect(outcome) {
        delay(2_000)
        onDismiss()
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn(initialScale = 0.88f),
        exit = fadeOut() + scaleOut(targetScale = 0.92f),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PearlLayout.screenHorizontalPadding)
                .semantics { contentDescription = "$title. $message" },
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(surface)
                .border(1.5.dp, borderColor, RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                theme.primary.copy(alpha = 0.95f),
                                theme.primary.copy(alpha = 0.55f),
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PearlColors.heroPrimary(darkTheme),
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = PearlColors.heroSecondary(darkTheme),
                )
            }
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = theme.primary,
                modifier = Modifier.size(22.dp),
            )
        }
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
                .semantics(mergeDescendants = true) {
                    contentDescription = "Pearl shared with you from $senderName. Tap to open inbox."
                }
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
