package com.knowledgepearls.app.ui.feed

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

enum class PearlShareDestination {
    PublicFeed,
    Friends,
}

@Composable
fun PearlShareOptionsOverlay(
    visible: Boolean,
    isSharedToPublicFeed: Boolean,
    theme: TabTheme,
    onDismiss: () -> Unit,
    onSelect: (PearlShareDestination) -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 3 },
        exit = fadeOut() + slideOutVertically { it / 3 },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.42f))
                .clickable(onClick = onDismiss),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = PearlLayout.screenHorizontalPadding)
                    .padding(bottom = PearlLayout.detailScrollBottomPadding)
                    .clip(RoundedCornerShape(22.dp))
                    .background(PearlColors.glassOverlay(isPearlDarkTheme()))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(22.dp),
                    )
                    .clickable(enabled = false) {}
                    .padding(bottom = 12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Share Pearl",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PearlColors.heroPrimary(isPearlDarkTheme()),
                        )
                        Text(
                            text = "Choose where to share",
                            style = MaterialTheme.typography.bodySmall,
                            color = PearlColors.heroSecondary(isPearlDarkTheme()),
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                ShareOptionRow(
                    title = "Public Feed",
                    subtitle = if (isSharedToPublicFeed) {
                        "Already shared — tap to withdraw"
                    } else {
                        "Submit for community approval"
                    },
                    icon = Icons.Default.Public,
                    primary = TabTheme.PublicFeed.primary,
                    secondary = TabTheme.PublicFeed.secondary,
                    onClick = {
                        onDismiss()
                        onSelect(PearlShareDestination.PublicFeed)
                    },
                )

                ShareOptionRow(
                    title = "Share with Friends",
                    subtitle = "Send privately to selected users",
                    icon = Icons.Default.Groups,
                    primary = Color(0xFF7359F2),
                    secondary = Color(0xFFB894FA),
                    onClick = {
                        onDismiss()
                        onSelect(PearlShareDestination.Friends)
                    },
                )
            }
        }
    }
}

@Composable
private fun ShareOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    primary: Color,
    secondary: Color,
    onClick: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.linearGradient(
                        listOf(primary.copy(alpha = 0.55f), secondary.copy(alpha = 0.25f)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PearlColors.heroPrimary(darkTheme),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = PearlColors.heroSecondary(darkTheme),
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = PearlColors.heroSecondary(darkTheme),
        )
    }
}
