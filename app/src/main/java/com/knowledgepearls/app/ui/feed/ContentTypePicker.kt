package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.model.ContentTypeFilter
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun ContentTypePicker(
    selected: ContentTypeFilter,
    onSelected: (ContentTypeFilter) -> Unit,
    theme: TabTheme,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PearlLayout.screenHorizontalPadding, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ContentTypeFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            Text(
                text = filter.label,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .semantics(mergeDescendants = true) {
                        contentDescription = filter.label
                        role = Role.Tab
                        this.selected = isSelected
                    }
                    .background(
                        if (isSelected) theme.primary.copy(alpha = 0.22f)
                        else PearlColors.controlFill(darkTheme),
                    )
                    .clickable { onSelected(filter) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                color = if (isSelected) theme.primary else PearlColors.segmentedInactive(darkTheme),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )
        }
    }
}
