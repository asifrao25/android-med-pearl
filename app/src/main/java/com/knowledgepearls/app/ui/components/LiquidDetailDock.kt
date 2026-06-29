package com.knowledgepearls.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

data class DetailDockAction(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val tint: Color? = null,
    val isActive: Boolean = false,
    val disabled: Boolean = false,
    val showsProgress: Boolean = false,
    val onClick: () -> Unit,
)

@Composable
fun LiquidDetailDock(
    theme: TabTheme,
    onBack: () -> Unit,
    actions: List<DetailDockAction>,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    val capsuleShape = RoundedCornerShape(999.dp)
    val dockFill = PearlColors.detailDockFill(darkTheme)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PearlLayout.screenHorizontalPadding)
            .padding(top = PearlLayout.detailDockTopPadding)
            .padding(bottom = PearlLayout.detailDockBottomPadding)
            .shadow(
                elevation = 8.dp,
                shape = capsuleShape,
                ambientColor = Color.Black.copy(alpha = 0.18f),
                spotColor = theme.primary.copy(alpha = 0.14f),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(PearlLayout.detailDockHeight)
                .clip(capsuleShape)
                .background(dockFill, capsuleShape)
                .background(PearlColors.chromeBarBrush(theme, darkTheme), capsuleShape)
                .border(width = 1.dp, color = PearlColors.chromeBarBorder(theme, darkTheme), shape = capsuleShape)
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DetailDockBackButton(
                theme = theme,
                onBack = onBack,
                modifier = Modifier.width(PearlLayout.detailDockActionSlotWidth),
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(1.dp)
                    .height(32.dp)
                    .background(theme.primary.copy(alpha = if (darkTheme) 0.18f else 0.14f), CircleShape),
            )

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                actions.forEach { action ->
                    DetailDockActionButton(
                        theme = theme,
                        action = action,
                        modifier = Modifier.width(PearlLayout.detailDockActionSlotWidth),
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailDockBackButton(
    theme: TabTheme,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
            .semantics {
                contentDescription = "Back"
                role = Role.Button
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onBack,
            )
            .padding(vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(listOf(theme.primary, theme.secondary)),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
        Text(
            text = "Back",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = PearlColors.heroSecondary(isPearlDarkTheme()),
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DetailDockActionButton(
    theme: TabTheme,
    action: DetailDockAction,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    val activeTint = action.tint ?: theme.primary
    val iconTint = when {
        action.disabled -> PearlColors.heroSecondary(darkTheme).copy(alpha = 0.45f)
        action.tint != null -> action.tint
        action.isActive -> theme.primary
        else -> PearlColors.heroPrimary(darkTheme)
    }
    val labelTint = when {
        action.disabled -> PearlColors.heroSecondary(darkTheme).copy(alpha = 0.45f)
        action.tint != null -> action.tint
        action.isActive -> theme.primary
        else -> PearlColors.heroSecondary(darkTheme)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
            .semantics {
                contentDescription = action.label
                role = Role.Button
            }
            .clickable(
                enabled = !action.disabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = action.onClick,
            )
            .padding(vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (action.isActive) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(activeTint.copy(alpha = 0.18f)),
                )
            }
            if (action.showsProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = activeTint,
                )
            } else {
                Icon(
                    action.icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Text(
            text = action.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = labelTint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
