package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun FeedSearchBar(
    query: String,
    theme: TabTheme,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search pearl titles and notes",
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var fieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(query, TextRange(query.length)))
    }
    var lastEmittedText by rememberSaveable { mutableStateOf(query) }

    LaunchedEffect(query) {
        if (query != lastEmittedText) {
            fieldValue = TextFieldValue(
                text = query,
                selection = TextRange(query.length),
            )
            lastEmittedText = query
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    OutlinedTextField(
        value = fieldValue,
        onValueChange = { updated ->
            fieldValue = updated
            lastEmittedText = updated.text
            if (updated.text != query) {
                onQueryChange(updated.text)
            }
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .fillMaxWidth()
            .padding(horizontal = PearlLayout.screenHorizontalPadding),
        placeholder = { Text(placeholder) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { keyboardController?.hide() },
        ),
        trailingIcon = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun FeedSearchOverlay(
    theme: TabTheme,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val darkTheme = isPearlDarkTheme()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PearlColors.canvas(darkTheme)),
    ) {
        LiquidBackground(theme = theme, intensity = 0.45f)
        Column(
            modifier = Modifier.fillMaxSize(),
            content = content,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedSearchTagSuggestions(
    topTags: List<String>,
    theme: TabTheme,
    onTagSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (topTags.isEmpty()) return

    val darkTheme = isPearlDarkTheme()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PearlLayout.screenHorizontalPadding, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "POPULAR TAGS",
            style = MaterialTheme.typography.labelSmall,
            color = theme.primary.copy(alpha = 0.88f),
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            topTags.forEach { tag ->
                FilterChip(
                    selected = false,
                    onClick = { onTagSelected(tag) },
                    label = { Text(tag) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = PearlColors.controlFill(darkTheme),
                        labelColor = PearlColors.heroSecondary(darkTheme),
                    ),
                )
            }
        }
    }
}
