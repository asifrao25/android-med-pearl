package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.components.TabScreenHeader
import com.knowledgepearls.app.ui.feed.ContentTypePicker
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun PublicFeedScreen(
    uiState: PublicFeedUiState,
    isSignedIn: Boolean,
    onOpenSettings: () -> Unit,
    onSignIn: () -> Unit,
    onPearlClick: (String) -> Unit,
    onLoadInitial: () -> Unit,
    onLoadNextPage: () -> Unit,
    onSectionSelected: (PublicFeedSection) -> Unit,
    onContentTypeSelected: (com.knowledgepearls.app.data.model.ContentTypeFilter) -> Unit,
    onResetContentTypeFilter: () -> Unit,
    onDismissEmptyFilterAlert: () -> Unit,
    onDismissActionSuccess: () -> Unit,
    onDismissError: () -> Unit,
) {
    val theme = TabTheme.PublicFeed
    val darkTheme = isPearlDarkTheme()
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= uiState.filteredPearls.lastIndex - 2
        }
    }

    LaunchedEffect(isSignedIn) {
        if (isSignedIn && uiState.pearls.isEmpty() && !uiState.isLoading) {
            onLoadInitial()
        }
    }

    LaunchedEffect(shouldLoadMore, uiState.hasMore, uiState.isLoading) {
        if (isSignedIn && shouldLoadMore && uiState.hasMore && !uiState.isLoading && uiState.pearls.isNotEmpty()) {
            onLoadNextPage()
        }
    }

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = theme)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            TabScreenHeader(
                title = "Public Feed",
                subtitle = "Community pearls",
                theme = theme,
                onSettingsClick = onOpenSettings,
            )

            ContentTypePicker(
                selected = uiState.contentTypeFilter,
                onSelected = onContentTypeSelected,
                theme = theme,
            )

            when {
                !isSignedIn -> Unit
                uiState.pearls.isEmpty() && uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = PearlLayout.tabBarOverlayInset),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = theme.primary)
                    }
                }
                uiState.pearls.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = PearlLayout.tabBarOverlayInset),
                        contentAlignment = Alignment.Center,
                    ) {
                        PublicFeedEmptyState(
                            isError = uiState.errorMessage != null,
                            message = uiState.errorMessage,
                            onRetry = onLoadInitial,
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = PearlLayout.screenHorizontalPadding,
                            end = PearlLayout.screenHorizontalPadding,
                            top = 8.dp,
                            bottom = PearlLayout.tabBarOverlayInset + 52.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        if (uiState.filteredPearls.isEmpty()) {
                            item {
                                Text(
                                    text = if (uiState.section == PublicFeedSection.NEW) {
                                        "You're all caught up."
                                    } else {
                                        "No seen pearls yet."
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color = PearlColors.heroSecondary(darkTheme),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        } else {
                            items(uiState.filteredPearls, key = { it.id }) { pearl ->
                                PublicFeedCard(
                                    pearl = pearl,
                                    theme = theme,
                                    onClick = { onPearlClick(pearl.id) },
                                )
                            }
                        }

                        if (uiState.isLoading && uiState.pearls.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(color = theme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isSignedIn && uiState.pearls.isNotEmpty()) {
            PublicFeedSectionTabs(
                selected = uiState.section,
                newCount = uiState.newCount,
                seenCount = uiState.seenCount,
                theme = theme,
                onSelected = onSectionSelected,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(
                        start = PearlLayout.screenHorizontalPadding,
                        end = PearlLayout.screenHorizontalPadding,
                        bottom = PearlLayout.tabBarOverlayInset - 8.dp,
                    ),
            )
        }

        if (!isSignedIn) {
            PublicFeedAuthGate(onSignIn = onSignIn)
        }

        uiState.actionSuccessMessage?.let { message ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 72.dp)
                    .padding(horizontal = PearlLayout.screenHorizontalPadding),
            ) {
                Text(
                    text = message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = PearlColors.heroPrimary(darkTheme),
                    textAlign = TextAlign.Center,
                )
            }
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(2200)
                onDismissActionSuccess()
            }
        }

        if (uiState.showEmptyFilterAlert) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = PearlLayout.screenHorizontalPadding),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "No pearls match this filter.",
                        color = PearlColors.heroPrimary(darkTheme),
                        textAlign = TextAlign.Center,
                    )
                    TextButton(onClick = onResetContentTypeFilter) {
                        Text("Show all")
                    }
                    TextButton(onClick = onDismissEmptyFilterAlert) {
                        Text("Dismiss")
                    }
                }
            }
        }

        uiState.errorMessage?.takeIf { uiState.pearls.isNotEmpty() }?.let {
            TextButton(
                onClick = onDismissError,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 64.dp),
            ) {
                Text(it, color = PearlColors.heroSecondary(darkTheme))
            }
        }
    }
}
