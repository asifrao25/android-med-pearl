package com.knowledgepearls.app.ui.feed

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.components.AvatarView
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

private val avatarSize = 42.dp
private val columnSpacing = 12.dp
private val headerContentSpacing = 8.dp

@Composable
fun PearlFeedAuthorLayout(
    displayName: String,
    avatarUrl: String?,
    createdAtMillis: Long,
    userId: String?,
    onResolveAvatarUrl: suspend (String) -> String?,
    modifier: Modifier = Modifier,
    onAuthorClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    var resolvedAvatarUrl by remember(userId, avatarUrl) { mutableStateOf(avatarUrl) }

    LaunchedEffect(userId, avatarUrl) {
        resolvedAvatarUrl = avatarUrl
        if (avatarUrl.isNullOrBlank() && !userId.isNullOrBlank()) {
            resolvedAvatarUrl = runCatching { onResolveAvatarUrl(userId) }.getOrNull()
        }
    }

    val resolvedName = displayName.trim().ifBlank { "Unknown" }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(columnSpacing),
        verticalAlignment = Alignment.Top,
    ) {
        val avatarModifier = if (onAuthorClick != null) {
            Modifier.clickable(onClick = onAuthorClick)
        } else {
            Modifier
        }
        AvatarView(
            url = resolvedAvatarUrl,
            displayName = resolvedName,
            size = avatarSize,
            modifier = avatarModifier,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(headerContentSpacing),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val nameModifier = if (onAuthorClick != null) {
                    Modifier
                        .weight(1f)
                        .clickable(onClick = onAuthorClick)
                } else {
                    Modifier.weight(1f)
                }
                Text(
                    text = resolvedName,
                    modifier = nameModifier,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = PearlColors.heroPrimary(darkTheme),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = formatRelativeCreatedAt(createdAtMillis),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.72f),
                    maxLines = 1,
                )
            }

            content()
        }
    }
}

private fun formatRelativeCreatedAt(createdAtMillis: Long): String {
    return DateUtils.getRelativeTimeSpanString(
        createdAtMillis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE,
    ).toString()
}
