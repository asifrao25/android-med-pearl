package com.knowledgepearls.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun TabScreenHeader(
    title: String,
    subtitle: String?,
    theme: TabTheme,
    modifier: Modifier = Modifier,
    showsSettingsButton: Boolean = true,
    onSettingsClick: () -> Unit = {},
    trailing: @Composable () -> Unit = {},
) {
    val darkTheme = isPearlDarkTheme()
    val onPrimary = MaterialTheme.colorScheme.onBackground

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(PearlLayout.headerContentHeight)
                .padding(horizontal = PearlLayout.screenHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(if (subtitle == null) 24.dp else 30.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(theme.primary, theme.secondary),
                        ),
                    ),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.1.sp,
                        color = theme.primary.copy(alpha = 0.88f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                trailing()
                if (showsSettingsButton) {
                    HeaderIconButton(
                        theme = theme,
                        onClick = onSettingsClick,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = theme.primary,
                            modifier = Modifier.size(PearlLayout.headerIconSize),
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .padding(horizontal = PearlLayout.screenHorizontalPadding)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            theme.primary.copy(alpha = 0.35f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

@Composable
fun HeaderIconButton(
    theme: TabTheme,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    Box(
        modifier = Modifier
            .size(PearlLayout.headerActionSize)
            .clip(RoundedCornerShape(12.dp))
            .background(PearlColors.controlFill(darkTheme))
            .semantics(mergeDescendants = true) {}
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
