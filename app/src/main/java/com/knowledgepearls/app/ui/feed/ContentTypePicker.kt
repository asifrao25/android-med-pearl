package com.knowledgepearls.app.ui.feed

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowledgepearls.app.data.model.ContentTypeFilter
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

private val contentTypePickerHeight = 40.dp

@Composable
fun ContentTypePicker(
    selected: ContentTypeFilter,
    onSelected: (ContentTypeFilter) -> Unit,
    theme: TabTheme,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    val shape = RoundedCornerShape(999.dp)
    val trackFill = PearlColors.popupSurface(darkTheme)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PearlLayout.screenHorizontalPadding, vertical = 8.dp)
            .height(contentTypePickerHeight)
            .clip(shape)
            .background(trackFill)
            .border(2.dp, PearlColors.strongBorder(darkTheme), shape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ContentTypeFilter.entries.forEach { filter ->
            ContentTypeTab(
                label = filter.label,
                selected = filter == selected,
                theme = theme,
                darkTheme = darkTheme,
                inactiveFill = trackFill,
                modifier = Modifier.weight(1f),
                onClick = { onSelected(filter) },
            )
        }
    }
}

@Composable
private fun ContentTypeTab(
    label: String,
    selected: Boolean,
    theme: TabTheme,
    darkTheme: Boolean,
    inactiveFill: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(shape)
            .semantics(mergeDescendants = true) {
                contentDescription = label
                role = Role.Tab
                this.selected = selected
            }
            .background(
                brush = if (selected) {
                    Brush.horizontalGradient(listOf(theme.primary, theme.secondary))
                } else {
                    Brush.linearGradient(listOf(inactiveFill, inactiveFill))
                },
                shape = shape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else PearlColors.segmentedInactive(darkTheme),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}
