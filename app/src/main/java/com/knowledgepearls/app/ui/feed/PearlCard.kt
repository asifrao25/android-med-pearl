package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.local.model.clinicalCasePayload
import com.knowledgepearls.app.data.local.model.isClinicalCase
import com.knowledgepearls.app.data.local.model.isQuickPearl
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerRequest
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

private val cardShape = RoundedCornerShape(PearlLayout.cardCornerRadius)

@Composable
fun PearlCard(
    pearl: PearlWithMedia,
    theme: TabTheme = TabTheme.Feed,
    modifier: Modifier = Modifier,
    onOpenMedia: ((PublicPearlMediaViewerRequest) -> Unit)? = null,
) {
    val darkTheme = isPearlDarkTheme()
    val cardTheme = pearlCardTheme(pearl, theme)
    val entity = pearl.pearl

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
            .background(PearlColors.glassOverlay(darkTheme)),
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
            if (entity.isSharedPublicly) {
                Box(modifier = Modifier.weight(1f))
                PublicStatusBadge(entity.publicPearlStatus)
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = entity.title.ifBlank { "Untitled pearl" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = PearlColors.heroPrimary(darkTheme),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            when {
                entity.isClinicalCase() -> {
                    val history = entity.clinicalCasePayload().history
                    if (history.isNotBlank()) {
                        Text(
                            text = history,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PearlColors.heroSecondary(darkTheme),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                entity.notes.isNotBlank() -> {
                    Text(
                        text = entity.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PearlColors.heroSecondary(darkTheme),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (
                entity.linkPreviewDescription.isNotBlank() &&
                !entity.sourceURL.isNullOrBlank() &&
                pearl.mediaItems.isEmpty()
            ) {
                Text(
                    text = entity.linkPreviewDescription,
                    style = MaterialTheme.typography.labelSmall,
                    color = PearlColors.heroSecondary(darkTheme),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            PearlCardMediaPreview(
                pearl = pearl,
                theme = theme,
                modifier = Modifier.fillMaxWidth(),
                interactive = onOpenMedia != null,
                onOpenMedia = onOpenMedia,
            )
        }
    }
}

@Composable
private fun PublicStatusBadge(status: String) {
    val (label, color) = when (status) {
        "pending" -> "Pending" to Color(0xFFFF9500)
        "approved" -> "Shared" to Color(0xFF14B8A6)
        "rejected" -> "Rejected" to Color.Gray
        else -> return
    }
    Text(
        text = label,
        modifier = Modifier
            .background(color.copy(alpha = 0.85f), RoundedCornerShape(999.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Color.White,
    )
}

private data class PearlCardTheme(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val glowColor: Color,
)

private fun pearlCardTheme(pearl: PearlWithMedia, tabTheme: TabTheme): PearlCardTheme {
    val primary = tabTheme.primary
    val secondary = tabTheme.secondary
    val entity = pearl.pearl

    return when {
        entity.isClinicalCase() -> PearlCardTheme("Clinical case", Icons.Default.LocalHospital, primary, secondary)
        !entity.sourceURL.isNullOrBlank() && pearl.mediaItems.isEmpty() ->
            PearlCardTheme("Link", Icons.Default.Link, primary, secondary)
        pearl.mediaItems.size > 1 -> PearlCardTheme("Gallery", Icons.Default.Photo, primary, secondary)
        pearl.mediaItems.firstOrNull()?.type == "video" ->
            PearlCardTheme("Video", Icons.Default.PlayCircle, primary, secondary)
        pearl.mediaItems.firstOrNull()?.type in listOf("pdf", "document") ->
            PearlCardTheme("Document", Icons.Default.Description, primary, secondary)
        pearl.mediaItems.isNotEmpty() -> PearlCardTheme("Photo", Icons.Default.Photo, primary, secondary)
        pearl.isQuickPearl() -> PearlCardTheme("Quick pearl", Icons.Default.AutoAwesome, primary, secondary)
        else -> PearlCardTheme("Pearl", Icons.Default.AutoAwesome, primary, secondary)
    }
}
