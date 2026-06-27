package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayCircle
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
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
) {
    val darkTheme = isPearlDarkTheme()
    val cardTheme = publicPearlCardTheme(pearl, theme)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(cardTheme.color.copy(alpha = 0.55f), cardTheme.glowColor.copy(alpha = 0.22f)),
                ),
                shape = cardShape,
            )
            .background(PearlColors.glassOverlay(darkTheme))
            .clickable(onClick = onClick),
    ) {
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
            if (pearl.safeDisplayName.isNotBlank()) {
                Box(modifier = Modifier.weight(1f))
                Text(
                    text = pearl.safeDisplayName,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

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
            } else if (pearl.notes.isNotBlank()) {
                Text(
                    text = pearl.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PearlColors.heroSecondary(darkTheme),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            pearl.linkPreviewImageUrl?.takeIf { it.isNotBlank() }?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(158.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
            }

            if (pearl.tags.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    pearl.tags.take(4).forEach { tag ->
                        Text(
                            text = tag,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(PearlColors.controlFill(darkTheme))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = PearlColors.heroSecondary(darkTheme),
                        )
                    }
                }
            }

            if (pearl.likeCount > 0) {
                Text(
                    text = "${pearl.likeCount} likes",
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.primary.copy(alpha = 0.85f),
                )
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
