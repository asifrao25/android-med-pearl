package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

private val outerShape = RoundedCornerShape(999.dp)
private val segmentShape = RoundedCornerShape(999.dp)

@Composable
fun PublicFeedSectionTabs(
    selected: PublicFeedSection,
    newCount: Int,
    seenCount: Int,
    theme: TabTheme,
    onSelected: (PublicFeedSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 10.dp, shape = outerShape, clip = false)
            .clip(outerShape)
            .background(PearlColors.popupSurface(darkTheme))
            .border(1.5.dp, PearlColors.strongBorder(darkTheme), outerShape)
            .padding(4.dp)
            .height(44.dp),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val segmentWidth = maxWidth / 2
            val indicatorOffset by animateDpAsState(
                targetValue = if (selected == PublicFeedSection.NEW) 0.dp else segmentWidth,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
                label = "sectionIndicator",
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .clip(segmentShape)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(theme.primary, theme.secondary.copy(alpha = 0.88f)),
                        ),
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = if (darkTheme) 0.18f else 0.35f),
                        shape = segmentShape,
                    ),
            )

            Row(Modifier.fillMaxSize()) {
                SectionTab(
                    label = PublicFeedSection.NEW.label,
                    count = newCount,
                    selected = selected == PublicFeedSection.NEW,
                    theme = theme,
                    darkTheme = darkTheme,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelected(PublicFeedSection.NEW) },
                )
                SectionTab(
                    label = PublicFeedSection.SEEN.label,
                    count = seenCount,
                    selected = selected == PublicFeedSection.SEEN,
                    theme = theme,
                    darkTheme = darkTheme,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelected(PublicFeedSection.SEEN) },
                )
            }
        }
    }
}

@Composable
private fun SectionTab(
    label: String,
    count: Int,
    selected: Boolean,
    theme: TabTheme,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .semantics(mergeDescendants = true) {
                contentDescription = if (count > 0) "$label, $count" else label
                role = Role.Tab
                this.selected = selected
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (selected) {
                    Color.White
                } else {
                    PearlColors.heroSecondary(darkTheme)
                },
            )
            if (count > 0) {
                Text(
                    text = count.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (selected) {
                                Color.White.copy(alpha = 0.22f)
                            } else {
                                theme.primary.copy(alpha = 0.16f)
                            },
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    color = if (selected) Color.White else theme.primary,
                )
            }
        }
    }
}
