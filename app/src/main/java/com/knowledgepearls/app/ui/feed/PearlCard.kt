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
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

private val cardShape = RoundedCornerShape(PearlLayout.cardCornerRadius)

@Composable
fun PearlCard(
    pearl: PearlWithMedia,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    val theme = pearlCardTheme(pearl)
    val entity = pearl.pearl

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(theme.color.copy(alpha = 0.55f), theme.glowColor.copy(alpha = 0.22f)),
                ),
                shape = cardShape,
            )
            .background(PearlColors.glassOverlay(darkTheme), cardShape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(theme.color.copy(alpha = 0.72f), theme.glowColor.copy(alpha = 0.48f)),
                    ),
                )
                .padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(theme.icon, contentDescription = null, tint = Color.White, modifier = Modifier.height(14.dp))
            Text(
                text = theme.label.uppercase(),
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
                    if (pearl.mediaItems.isNotEmpty()) {
                        Text(
                            text = "${pearl.mediaItems.size} attachment(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = PearlColors.heroSecondary(darkTheme),
                        )
                    }
                }
                entity.notes.isNotBlank() -> {
                    Text(
                        text = entity.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PearlColors.heroSecondary(darkTheme),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (entity.tags.isNotEmpty()) {
                Text(
                    text = entity.tags.joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        pearl.mediaItems.firstOrNull()?.let { media ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(theme.color.copy(alpha = 0.42f), theme.glowColor.copy(alpha = 0.18f)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = mediaTypeIcon(media.type),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.height(36.dp),
                )
            }
        } ?: entity.sourceURL?.takeIf { it.isNotBlank() }?.let {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(theme.color.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
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
    val color: Color,
    val glowColor: Color,
    val icon: ImageVector,
    val label: String,
)

private fun pearlCardTheme(pearl: PearlWithMedia): PearlCardTheme {
    val entity = pearl.pearl
    if (entity.isClinicalCase()) {
        return PearlCardTheme(Color(0xFFF5A623), Color(0xFFC47D0E), Icons.Default.LocalHospital, "Clinical Case")
    }
    if (entity.isQuickPearl()) {
        return PearlCardTheme(Color(0xFF61F2B8), Color(0xFF2EB88A), Icons.Default.AutoAwesome, "Quick")
    }
    pearl.mediaItems.firstOrNull()?.let { media ->
        return when (media.type) {
            "video" -> PearlCardTheme(Color(0xFFFF8C61), Color(0xFFF24A6A), Icons.Default.PlayCircle, "Video")
            "image" -> PearlCardTheme(Color(0xFF38E0F0), Color(0xFF1F8CF2), Icons.Default.Photo, "Photo")
            else -> PearlCardTheme(Color(0xFF9485FF), Color(0xFF6147E0), Icons.Default.Description, "Document")
        }
    }
    if (!entity.sourceURL.isNullOrBlank()) {
        return PearlCardTheme(Color(0xFF52B8FF), Color(0xFF2E7AEB), Icons.Default.Link, "Link")
    }
    return PearlCardTheme(Color(0xFF61F2B8), Color(0xFF2EB88A), Icons.Default.AutoAwesome, "Pearl")
}

private fun mediaTypeIcon(type: String): ImageVector = when (type) {
    "video" -> Icons.Default.PlayCircle
    "image" -> Icons.Default.Photo
    "pdf", "document" -> Icons.Default.Description
    else -> Icons.Default.Description
}
