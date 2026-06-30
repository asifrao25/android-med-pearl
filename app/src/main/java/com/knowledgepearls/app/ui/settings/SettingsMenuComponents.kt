package com.knowledgepearls.app.ui.settings

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.WbSunny
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.theme.AppearanceMode
import com.knowledgepearls.app.ui.theme.AppFontChoice
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.fontFamilyFor
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

enum class SettingsSectionAccent {
    Primary,
    Community,
    Backup,
    Cache,
    Privacy,
    Creator,
    About,
    Governance,
    ;

    fun colors(theme: TabTheme, darkTheme: Boolean): Pair<Color, Color> {
        val tint = when (this) {
            Primary, Backup -> theme.primary
            Community -> Color(0xFFFF9F0A)
            Cache -> Color(0xFFB794F6)
            Privacy -> Color(0xFF8C9EFF)
            Creator -> theme.secondary
            About -> PearlColors.heroSecondary(darkTheme)
            Governance -> theme.primary.copy(alpha = 0.88f)
        }
        val background = when (this) {
            About -> PearlColors.mutedTileBackground(darkTheme)
            else -> tint.copy(alpha = if (darkTheme) 0.18f else 0.14f)
        }
        return tint to background
    }
}

@Composable
fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier.padding(start = 4.dp, bottom = 2.dp),
        fontWeight = FontWeight.SemiBold,
        color = PearlColors.heroSecondary(isPearlDarkTheme()),
        style = MaterialTheme.typography.labelLarge,
    )
}

@Composable
fun SettingsMenuCard(
    theme: TabTheme,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    val shape = RoundedCornerShape(PearlLayout.cardCornerRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(PearlColors.glassOverlay(darkTheme), shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        theme.primary.copy(alpha = if (darkTheme) 0.12f else 0.10f),
                        theme.secondary.copy(alpha = if (darkTheme) 0.05f else 0.04f),
                    ),
                ),
                shape = shape,
            )
            .border(width = 1.dp, color = PearlColors.cardBorder(darkTheme), shape = shape),
    ) {
        content()
    }
}

@Composable
fun SettingsMenuDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(start = 80.dp),
        color = PearlColors.divider(isPearlDarkTheme()),
        thickness = 0.5.dp,
    )
}

@Composable
fun SettingsMenuRow(
    icon: ImageVector,
    accent: SettingsSectionAccent,
    title: String,
    subtitle: String,
    theme: TabTheme,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val darkTheme = isPearlDarkTheme()
    val (tint, background) = accent.colors(theme, darkTheme)
    val rowModifier = Modifier
        .fillMaxWidth()
        .then(
            if (onClick != null) {
                Modifier
                    .semantics(mergeDescendants = true) { contentDescription = title }
                    .clickable(onClick = onClick)
            } else {
                Modifier
            },
        )
        .padding(horizontal = 16.dp, vertical = 14.dp)

    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(background),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                color = PearlColors.heroPrimary(darkTheme),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = subtitle,
                color = PearlColors.heroSecondary(darkTheme),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
            )
        }

        trailing?.invoke()
    }
}

@Composable
fun AppearanceModeRow(
    mode: AppearanceMode,
    isSelected: Boolean,
    theme: TabTheme,
    onClick: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    val icon = when (mode) {
        AppearanceMode.System -> Icons.Default.Circle
        AppearanceMode.Light -> Icons.Default.WbSunny
        AppearanceMode.Dark -> Icons.Default.Bedtime
    }
    val subtitle = when (mode) {
        AppearanceMode.System -> "Match your device setting"
        AppearanceMode.Light -> "Bright backgrounds, dark text"
        AppearanceMode.Dark -> "Deep backgrounds, light text"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(theme.primary.copy(alpha = if (isSelected) 0.22f else 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) theme.primary else PearlColors.heroSecondary(darkTheme),
                modifier = Modifier.size(22.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = when (mode) {
                    AppearanceMode.System -> "System"
                    AppearanceMode.Light -> "Light"
                    AppearanceMode.Dark -> "Dark"
                },
                color = PearlColors.heroPrimary(darkTheme),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = subtitle,
                color = PearlColors.heroSecondary(darkTheme),
                style = MaterialTheme.typography.labelSmall,
            )
        }

        Icon(
            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Circle,
            contentDescription = null,
            tint = if (isSelected) theme.primary else PearlColors.heroSecondary(darkTheme).copy(alpha = 0.55f),
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun AppFontChoiceRow(
    choice: AppFontChoice,
    isSelected: Boolean,
    theme: TabTheme,
    onClick: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    val previewFamily = fontFamilyFor(choice)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(theme.primary.copy(alpha = if (isSelected) 0.22f else 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Aa",
                fontFamily = previewFamily,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) theme.primary else PearlColors.heroSecondary(darkTheme),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = choice.label,
                color = PearlColors.heroPrimary(darkTheme),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = choice.subtitle,
                color = PearlColors.heroSecondary(darkTheme),
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = choice.previewLine,
                color = PearlColors.heroPrimary(darkTheme),
                fontFamily = previewFamily,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
            )
        }

        Icon(
            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Circle,
            contentDescription = null,
            tint = if (isSelected) theme.primary else PearlColors.heroSecondary(darkTheme).copy(alpha = 0.55f),
            modifier = Modifier.size(20.dp),
        )
    }
}
