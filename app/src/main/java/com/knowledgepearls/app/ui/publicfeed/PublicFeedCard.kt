package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

private val cardShape = RoundedCornerShape(PearlLayout.cardCornerRadius)

@Composable
fun PublicFeedCard(
    pearl: PublicPearl,
    theme: TabTheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    likeCount: Int = pearl.likeCount,
    commentCount: Int = 0,
    isLiked: Boolean = false,
    onToggleLike: (() -> Unit)? = null,
    onOpenComments: (() -> Unit)? = null,
) {
    val darkTheme = isPearlDarkTheme()
    val cardTheme = publicPearlCardTheme(pearl, theme)
    val showEngagementBar = onToggleLike != null || onOpenComments != null || likeCount > 0 || commentCount > 0

    val cardLabel = pearl.titleDisplay.ifBlank { "Community pearl" }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(PearlColors.glassOverlay(darkTheme))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(cardTheme.color.copy(alpha = 0.55f), cardTheme.glowColor.copy(alpha = 0.22f)),
                ),
                shape = cardShape,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "$cardLabel. Community pearl."
                    role = Role.Button
                }
                .clickable(onClick = onClick),
        ) {
            PublicFeedCardHeader(cardTheme = cardTheme)

            if (pearl.isFromTwitterScraper) {
                TweetFeedCardBody(
                    pearl = pearl,
                    theme = theme,
                    darkTheme = darkTheme,
                )
                PublicPearlTweetCardMediaPreview(
                    pearl = pearl,
                    theme = theme,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                StandardFeedCardBody(
                    pearl = pearl,
                    theme = theme,
                    darkTheme = darkTheme,
                )
            }
        }

        if (showEngagementBar) {
            PublicFeedCardEngagementBar(
                likeCount = likeCount,
                commentCount = commentCount,
                isLiked = isLiked,
                theme = theme,
                onToggleLike = onToggleLike,
                onOpenComments = onOpenComments,
            )
        }
    }
}

@Composable
private fun PublicFeedCardHeader(cardTheme: PublicPearlCardTheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    listOf(cardTheme.color.copy(alpha = 0.72f), cardTheme.glowColor.copy(alpha = 0.48f)),
                ),
            )
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(cardTheme.icon, contentDescription = null, tint = Color.White, modifier = Modifier.height(14.dp))
        Text(
            text = cardTheme.label.uppercase(),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun TweetFeedCardBody(
    pearl: PublicPearl,
    theme: TabTheme,
    darkTheme: Boolean,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val bodyText = pearl.scraperCardBodyText
        if (bodyText.isNotBlank()) {
            Text(
                text = bodyText,
                style = MaterialTheme.typography.bodyLarge,
                color = PearlColors.heroPrimary(darkTheme),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }

        val learningPoint = pearl.scraperLearningPoint
        if (learningPoint.isNotBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "LEARNING POINT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = theme.primary.copy(alpha = 0.85f),
                )
                Text(
                    text = learningPoint,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = PearlColors.heroSecondary(darkTheme),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun StandardFeedCardBody(
    pearl: PublicPearl,
    theme: TabTheme,
    darkTheme: Boolean,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = pearl.titleDisplay,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = PearlColors.heroPrimary(darkTheme),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        if (pearl.isClinicalCase) {
            val history = pearl.casePayload?.history.orEmpty()
            if (history.isNotBlank()) {
                Text(
                    text = history,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PearlColors.heroSecondary(darkTheme),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (pearl.hasGalleryMedia) {
                Text(
                    text = "${pearl.resolvedMediaItems.size} attachment${if (pearl.resolvedMediaItems.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = PearlColors.heroSecondary(darkTheme),
                )
            }
        } else if (pearl.notes.isNotBlank()) {
            Text(
                text = pearl.notes,
                style = MaterialTheme.typography.bodyMedium,
                color = PearlColors.heroSecondary(darkTheme),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }

        PublicPearlCardMediaPreview(
            pearl = pearl,
            theme = theme,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun PublicFeedCardEngagementBar(
    likeCount: Int,
    commentCount: Int,
    isLiked: Boolean,
    theme: TabTheme,
    modifier: Modifier = Modifier,
    onToggleLike: (() -> Unit)? = null,
    onOpenComments: (() -> Unit)? = null,
) {
    val darkTheme = isPearlDarkTheme()

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            color = PearlColors.heroSecondary(darkTheme).copy(alpha = 0.18f),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            if (onToggleLike != null) {
                PublicPearlLikeButton(
                    likeCount = likeCount,
                    isLiked = isLiked,
                    theme = theme,
                    onToggleLike = onToggleLike,
                )
            } else if (likeCount > 0) {
                Text(
                    text = "$likeCount ${if (likeCount == 1) "like" else "likes"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = theme.primary.copy(alpha = 0.85f),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }

            if (onOpenComments != null) {
                PublicPearlCommentButton(
                    commentCount = commentCount,
                    theme = theme,
                    onClick = onOpenComments,
                )
            } else if (commentCount > 0) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = theme.secondary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = commentCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PearlColors.heroPrimary(darkTheme),
                    )
                }
            }
        }
    }
}

private data class PublicPearlCardTheme(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val glowColor: Color,
)

private fun publicPearlCardTheme(pearl: PublicPearl, tabTheme: TabTheme): PublicPearlCardTheme {
    val primary = tabTheme.primary
    val secondary = tabTheme.secondary
    return when {
        pearl.isFromTwitterScraper -> PublicPearlCardTheme("Tweet", Icons.Default.Link, primary, secondary)
        pearl.isClinicalCase -> PublicPearlCardTheme("Clinical case", Icons.Default.LocalHospital, primary, secondary)
        pearl.isLinkPearl -> PublicPearlCardTheme("Link", Icons.Default.Link, primary, secondary)
        pearl.resolvedMediaItems.size > 1 -> PublicPearlCardTheme("Gallery", Icons.Default.Photo, primary, secondary)
        pearl.contentType == "video" -> PublicPearlCardTheme("Video", Icons.Default.PlayCircle, primary, secondary)
        pearl.contentType == "document" -> PublicPearlCardTheme("Document", Icons.Default.Description, primary, secondary)
        pearl.hasGalleryMedia -> PublicPearlCardTheme("Photo", Icons.Default.Photo, primary, secondary)
        pearl.isQuickPearl -> PublicPearlCardTheme("Quick pearl", Icons.Default.AutoAwesome, primary, secondary)
        else -> PublicPearlCardTheme("Pearl", Icons.Default.AutoAwesome, primary, secondary)
    }
}
