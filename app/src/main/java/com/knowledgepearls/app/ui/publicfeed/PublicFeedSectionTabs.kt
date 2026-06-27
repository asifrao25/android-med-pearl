package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

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
    val shape = RoundedCornerShape(999.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(shape)
            .background(PearlColors.glassOverlay(darkTheme))
            .border(1.dp, PearlColors.cardBorder(darkTheme), shape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PublicFeedSection.entries.forEach { section ->
            val count = when (section) {
                PublicFeedSection.NEW -> newCount
                PublicFeedSection.SEEN -> seenCount
            }
            SectionTab(
                label = section.label,
                count = count,
                selected = selected == section,
                theme = theme,
                darkTheme = darkTheme,
                modifier = Modifier.weight(1f),
                onClick = { onSelected(section) },
            )
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
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(shape)
            .then(
                if (selected) {
                    Modifier.background(
                        brush = Brush.horizontalGradient(
                            listOf(theme.primary.copy(alpha = 0.92f), theme.secondary.copy(alpha = 0.72f)),
                        ),
                        shape = shape,
                    )
                } else {
                    Modifier
                },
            )
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
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else PearlColors.segmentedInactive(darkTheme),
            )
            if (count > 0) {
                Text(
                    text = count.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (selected) PearlColors.selectedPillFill(darkTheme)
                            else theme.primary.copy(alpha = 0.22f),
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    color = if (selected) Color.White else theme.primary,
                )
            }
        }
    }
}
