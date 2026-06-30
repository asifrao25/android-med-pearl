package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUpOffAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun PublicPearlLikeButton(
    likeCount: Int,
    isLiked: Boolean,
    theme: TabTheme,
    onToggleLike: () -> Unit,
    modifier: Modifier = Modifier,
    showCount: Boolean = true,
) {
    val darkTheme = isPearlDarkTheme()
    val likeDescription = when {
        isLiked && likeCount == 1 -> "Unlike. 1 like"
        isLiked -> "Unlike. $likeCount likes"
        likeCount == 1 -> "Like. 1 like"
        else -> "Like. $likeCount likes"
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        IconButton(
            onClick = onToggleLike,
            modifier = Modifier
                .size(36.dp)
                .semantics { contentDescription = likeDescription },
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUpOffAlt,
                contentDescription = null,
                tint = if (isLiked) theme.primary else PearlColors.heroSecondary(darkTheme),
                modifier = Modifier.size(20.dp),
            )
        }
        if (showCount) {
            Text(
                text = likeCount.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isLiked) theme.primary else PearlColors.heroSecondary(darkTheme),
            )
        }
    }
}

@Composable
fun PublicPearlEngagementBar(
    likeCount: Int,
    commentCount: Int,
    isLiked: Boolean,
    theme: TabTheme,
    modifier: Modifier = Modifier,
    onToggleLike: () -> Unit,
    onOpenComments: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()

    GlassSurface(
        modifier = modifier,
        cornerRadius = PearlLayout.cardCornerRadius,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            PublicPearlLikeButton(
                likeCount = likeCount,
                isLiked = isLiked,
                theme = theme,
                onToggleLike = onToggleLike,
            )

            IconButton(onClick = onOpenComments) {
                Icon(
                    imageVector = Icons.Filled.ChatBubbleOutline,
                    contentDescription = "Comments",
                    tint = theme.secondary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = commentCount.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PearlColors.heroPrimary(darkTheme),
            )
        }
    }
}
