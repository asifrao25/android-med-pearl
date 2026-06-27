package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme

@Composable
fun TagFilterRow(
    tags: List<String>,
    selectedTag: String?,
    onTagSelected: (String?) -> Unit,
    theme: TabTheme,
    modifier: Modifier = Modifier,
) {
    if (tags.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = PearlLayout.screenHorizontalPadding, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedTag == null,
            onClick = { onTagSelected(null) },
            label = { Text("All tags") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = theme.primary.copy(alpha = 0.22f),
                selectedLabelColor = theme.primary,
            ),
        )
        tags.forEach { tag ->
            FilterChip(
                selected = selectedTag == tag,
                onClick = { onTagSelected(if (selectedTag == tag) null else tag) },
                label = { Text(tag) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = theme.primary.copy(alpha = 0.22f),
                    selectedLabelColor = theme.primary,
                ),
            )
        }
    }
}
