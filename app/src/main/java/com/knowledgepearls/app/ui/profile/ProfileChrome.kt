package com.knowledgepearls.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import coil3.compose.SubcomposeAsyncImage
import com.knowledgepearls.app.ui.components.AvatarView
import com.knowledgepearls.app.ui.theme.TabTheme
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ProfileTeal = Color(0xFF14B8A6)
private val ProfileCyan = Color(0xFF22D3EE)
private val ProfileGold = Color(0xFFFFC747)

fun formatMemberSince(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return "—"
    val instant = runCatching { Instant.parse(createdAt) }.getOrNull()
        ?: runCatching {
            Instant.parse(
                createdAt.replace(" ", "T").let { value ->
                    if (value.endsWith("Z")) value else "${value}Z"
                },
            )
        }.getOrNull()
    if (instant == null) return "—"
    return DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(instant)
}

@Composable
fun ProfileHeroPhoto(
    url: String?,
    displayName: String,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val diameter = minOf(maxWidth - 32.dp, maxHeight * 0.92f)
        Box(
            modifier = Modifier
                .size(diameter)
                .clip(CircleShape)
                .background(Color(0xFF06060A)),
            contentAlignment = Alignment.Center,
        ) {
            if (!url.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = url,
                    contentDescription = displayName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        ProfileHeroInitials(displayName = displayName)
                    },
                    error = {
                        ProfileHeroInitials(displayName = displayName)
                    },
                )
            } else {
                ProfileHeroInitials(displayName = displayName)
            }
        }
    }
}

@Composable
private fun ProfileHeroInitials(displayName: String) {
    val initials = displayName.trim().split(Regex("\\s+")).take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .joinToString("")
        .ifBlank { "?" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    listOf(ProfileTeal.copy(alpha = 0.85f), ProfileCyan.copy(alpha = 0.65f), Color.Black.copy(alpha = 0.9f)),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = Color.White.copy(alpha = 0.18f),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.displayLarge,
        )
    }
}

@Composable
fun ProfileLikesHonourBadge(
    totalLikes: Int,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    val formatted = NumberFormat.getIntegerInstance().format(totalLikes)

    if (compact) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LikesBadgeIcon(diameter = 34.dp, iconSize = 14.dp)
            Column {
                Text(
                    text = formatted,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = if (totalLikes == 1) "LIKE" else "LIKES",
                    color = Color.White.copy(alpha = 0.72f),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    } else {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            LikesBadgeIcon(diameter = 44.dp, iconSize = 18.dp)
            Text(
                text = formatted,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = if (totalLikes == 1) "LIKE RECEIVED" else "LIKES RECEIVED",
                color = Color.White.copy(alpha = 0.78f),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun LikesBadgeIcon(
    diameter: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
) {
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(diameter + 8.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        listOf(ProfileGold.copy(alpha = 0.35f), ProfileTeal.copy(alpha = 0.22f)),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(diameter)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        listOf(ProfileGold, ProfileTeal.copy(alpha = 0.88f)),
                    ),
                )
                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}

@Composable
fun ProfileStatsBar(
    pearlCount: Int,
    totalLikes: Int,
    memberSince: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.28f))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
            .padding(horizontal = 28.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileStatColumn(value = pearlCount.toString(), label = "pearls")
        ProfileStatDivider()
        ProfileLikesHonourBadge(totalLikes = totalLikes, compact = true)
        ProfileStatDivider()
        ProfileStatColumn(value = memberSince, label = "member since")
    }
}

@Composable
private fun ProfileStatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.72f),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun ProfileStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(44.dp)
            .background(Color.White.copy(alpha = 0.28f)),
    )
}

@Composable
fun SettingsProfileAvatar(
    url: String?,
    displayName: String,
    theme: TabTheme,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(shape)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(listOf(theme.primary, theme.secondary)),
                shape = shape,
            ),
    ) {
        AvatarView(
            url = url,
            displayName = displayName,
            size = 48.dp,
        )
    }
}

@Composable
fun ProfileMetaLine(
    grade: String?,
    deanery: String?,
    specialty: String?,
    showEmail: Boolean,
    publicEmail: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        val gradeDeanery = buildList {
            grade?.takeIf { it.isNotBlank() }?.let { add(it) }
            deanery?.takeIf { it.isNotBlank() }?.let { add(it) }
        }.joinToString(" · ")

        if (gradeDeanery.isNotBlank()) {
            Text(
                text = gradeDeanery,
                color = Color.White.copy(alpha = 0.88f),
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }

        specialty?.takeIf { it.isNotBlank() }?.let {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.82f),
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = it,
                    color = Color.White.copy(alpha = 0.82f),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }

        if (showEmail && !publicEmail.isNullOrBlank()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = ProfileTeal.copy(alpha = 0.95f),
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = publicEmail,
                    color = ProfileTeal.copy(alpha = 0.95f),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun ProfileLoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = ProfileTeal)
    }
}

@Composable
fun ProfileErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Retry",
            color = ProfileTeal,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null,
                    onClick = onRetry,
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
fun ProfileSimplePullToDismiss(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var dismissDrag by remember { mutableFloatStateOf(0f) }
    val animatedDrag by animateFloatAsState(dismissDrag, label = "profileSimpleDismissDrag")

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(0, animatedDrag.roundToInt()) }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dismissDrag > 140f) {
                            onDismiss()
                        } else {
                            dismissDrag = 0f
                        }
                    },
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount > 0f || dismissDrag > 0f) {
                            dismissDrag = (dismissDrag + dragAmount).coerceAtLeast(0f)
                        }
                    },
                )
            },
    ) {
        content()
    }
}

@Composable
fun ProfileFloatingCloseButton(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f)),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White.copy(alpha = 0.9f),
            )
        }
    }
}

@Composable
fun ProfilePullToDismissContainer(
    listState: LazyListState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var dismissDrag by remember { mutableFloatStateOf(0f) }
    val animatedDrag by animateFloatAsState(dismissDrag, label = "profileDismissDrag")
    val canPullDismiss = !listState.canScrollBackward

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(0, animatedDrag.roundToInt()) }
            .pointerInput(canPullDismiss) {
                if (canPullDismiss) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (dismissDrag > 140f) {
                                onDismiss()
                            } else {
                                dismissDrag = 0f
                            }
                        },
                        onVerticalDrag = { _, dragAmount ->
                            if (dragAmount > 0f || dismissDrag > 0f) {
                                dismissDrag = (dismissDrag + dragAmount).coerceAtLeast(0f)
                            }
                        },
                    )
                }
            },
    ) {
        content()
    }
}