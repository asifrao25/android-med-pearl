package com.knowledgepearls.app.ui.feed

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.components.AvatarView
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import java.text.DateFormat
import java.util.Date

fun parseOpenableUrl(text: String): String? {
    val trimmed = text.trim()
    if (trimmed.isBlank()) return null
    val candidate = when {
        trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
        trimmed.startsWith("www.") -> "https://$trimmed"
        trimmed.contains('.') && !trimmed.contains(' ') -> "https://$trimmed"
        else -> null
    } ?: return null
    return runCatching { Uri.parse(candidate) }.getOrNull()?.toString()
}

fun openExternalUrl(context: android.content.Context, url: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

@Composable
fun PearlDetailAuthorBar(
    displayName: String,
    avatarUrl: String?,
    createdAtMillis: Long,
    theme: TabTheme,
    modifier: Modifier = Modifier,
    caption: String? = null,
    userId: String? = null,
    onResolveAvatarUrl: (suspend (String) -> String?)? = null,
    onOpenProfile: (() -> Unit)? = null,
) {
    val darkTheme = isPearlDarkTheme()
    var resolvedAvatarUrl by remember(userId, avatarUrl) { mutableStateOf(avatarUrl) }

    LaunchedEffect(userId, avatarUrl) {
        resolvedAvatarUrl = avatarUrl
        if (avatarUrl.isNullOrBlank() && !userId.isNullOrBlank() && onResolveAvatarUrl != null) {
            resolvedAvatarUrl = onResolveAvatarUrl(userId)
        }
    }

    val resolvedName = displayName.trim().ifBlank { "Unknown" }
    val profileClickable = onOpenProfile != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PearlColors.glassOverlay(darkTheme))
            .then(
                if (profileClickable) {
                    Modifier.clickable(onClick = onOpenProfile!!)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarView(
            url = resolvedAvatarUrl,
            displayName = resolvedName,
            size = 40.dp,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            caption?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = theme.primary,
                )
            }
            Text(
                text = resolvedName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PearlColors.heroPrimary(darkTheme),
            )
            Text(
                text = formatDetailCreatedAt(createdAtMillis),
                style = MaterialTheme.typography.labelSmall,
                color = PearlColors.heroSecondary(darkTheme),
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = if (profileClickable) "View profile" else null,
            tint = PearlColors.heroSecondary(darkTheme),
        )
    }
}

@Composable
fun PearlDetailTitleBar(
    title: String,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    Text(
        text = title.ifBlank { "Untitled" },
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = if (darkTheme) 0.04f else 0.35f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = PearlColors.heroPrimary(darkTheme),
    )
}

@Composable
fun PearlDetailSectionHeaderBar(
    title: String,
    theme: TabTheme,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    Text(
        text = title,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PearlColors.sectionHeaderGradient(theme, darkTheme))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = Color.White,
    )
}

@Composable
fun PearlDetailSection(
    title: String,
    body: String,
    theme: TabTheme,
    modifier: Modifier = Modifier,
    linkifyBody: Boolean = title.equals("Source", ignoreCase = true) ||
        title.equals("References", ignoreCase = true) ||
        title.equals("Link", ignoreCase = true),
) {
    if (body.isBlank()) return
    val darkTheme = isPearlDarkTheme()
    val context = LocalContext.current
    val openableUrl = if (linkifyBody) parseOpenableUrl(body) else null

    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 14.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PearlDetailSectionHeaderBar(title = title, theme = theme)
            if (openableUrl != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { openExternalUrl(context, openableUrl) }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = theme.primary)
                    Text(
                        text = body.trim(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = theme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        Icons.Default.OpenInNew,
                        contentDescription = "Open link",
                        tint = PearlColors.heroSecondary(darkTheme),
                    )
                }
            } else {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PearlColors.heroPrimary(darkTheme),
                )
            }
        }
    }
}

private fun formatDetailCreatedAt(createdAtMillis: Long): String {
    if (createdAtMillis <= 0L) return ""
    val flags = DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_SHOW_YEAR or
        DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE
    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
        .format(Date(createdAtMillis))
}
