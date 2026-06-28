package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.Color
import com.knowledgepearls.app.data.local.model.FolderWithCount
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.ui.components.PearlSwipeAction
import com.knowledgepearls.app.ui.components.PearlSwipeRow
import com.knowledgepearls.app.ui.feed.PearlDeleteConfirmationDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.components.PearlActionOutcome
import com.knowledgepearls.app.ui.components.PearlActionSuccessAlert
import com.knowledgepearls.app.ui.components.PublicFeedOfflineState
import com.knowledgepearls.app.ui.components.SeenToastView
import com.knowledgepearls.app.ui.feed.FeedEmptyFilterAlert
import com.knowledgepearls.app.ui.components.HeaderIconButton
import com.knowledgepearls.app.ui.components.InboxHeaderButton
import com.knowledgepearls.app.ui.components.TabScreenHeader
import com.knowledgepearls.app.ui.feed.ContentTypePicker
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicFeedScreen(
    uiState: PublicFeedUiState,
    isSignedIn: Boolean,
    inboxBadgeCount: Int = 0,
    onOpenSettings: () -> Unit,
    onOpenInbox: () -> Unit,
    onSignIn: () -> Unit,
    onPearlClick: (String) -> Unit,
    onLoadInitial: () -> Unit,
    onRefreshFeed: () -> Unit = onLoadInitial,
    onLoadNextPage: () -> Unit,
    onSectionSelected: (PublicFeedSection) -> Unit,
    onContentTypeSelected: (com.knowledgepearls.app.data.model.ContentTypeFilter) -> Unit,
    onResetContentTypeFilter: () -> Unit,
    onDismissEmptyFilterAlert: () -> Unit,
    onDismissActionSuccess: () -> Unit,
    onDismissError: () -> Unit,
    onDismissSeenToast: () -> Unit,
    folders: List<FolderWithCount> = emptyList(),
    onHidePearl: (PublicPearl) -> Unit = {},
    onSaveToMyFeed: (PublicPearl) -> Unit = {},
    onSaveToFolder: (PublicPearl, FolderWithCount) -> Unit = { _, _ -> },
    onCreateFolderAndSave: (PublicPearl, String) -> Unit = { _, _ -> },
    isNetworkAvailable: Boolean = true,
    isOfflineMode: Boolean = false,
    onRetryConnection: () -> Unit = {},
) {
    val theme = TabTheme.PublicFeed
    val darkTheme = isPearlDarkTheme()
    val listState = rememberLazyListState()
    var saveTarget by remember { mutableStateOf<PublicPearl?>(null) }
    var removeTarget by remember { mutableStateOf<PublicPearl?>(null) }

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

    LaunchedEffect(uiState.feedRefreshGeneration, uiState.isLoading) {
        if (uiState.feedRefreshGeneration > 0 && !uiState.isLoading && uiState.filteredPearls.isNotEmpty()) {
            listState.scrollToItem(0)
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
                trailing = {
                    if (isSignedIn) {
                        InboxHeaderButton(
                            theme = theme,
                            inboxBadgeCount = inboxBadgeCount,
                            onClick = onOpenInbox,
                        )
                    }
                },
            )

            ContentTypePicker(
                selected = uiState.contentTypeFilter,
                onSelected = onContentTypeSelected,
                theme = theme,
            )

            when {
                !isSignedIn -> Unit
                !isNetworkAvailable -> {
                    PublicFeedOfflineState(
                        isOfflineMode = isOfflineMode,
                        onTryAgain = onRetryConnection,
                    )
                }
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
                    val isRefreshing = uiState.isLoading && uiState.pearls.isNotEmpty()
                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = onRefreshFeed,
                        modifier = Modifier.fillMaxSize(),
                    ) {
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
                                PearlSwipeRow(
                                    leadingAction = PearlSwipeAction(
                                        icon = Icons.Default.CreateNewFolder,
                                        title = "Save",
                                        color = theme.primary,
                                        onClick = { saveTarget = pearl },
                                    ),
                                    trailingAction = PearlSwipeAction(
                                        icon = Icons.Default.Delete,
                                        title = "Remove",
                                        color = Color(0xFFFF3B30),
                                        onClick = { removeTarget = pearl },
                                    ),
                                ) {
                                    PublicFeedCard(
                                        pearl = pearl,
                                        theme = theme,
                                        onClick = { onPearlClick(pearl.id) },
                                    )
                                }
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

        uiState.actionOutcome?.let { outcome ->
            PearlActionSuccessAlert(
                outcome = outcome,
                theme = theme,
                folderName = uiState.actionSuccessMessage,
                onDismiss = onDismissActionSuccess,
            )
        }

        removeTarget?.let { pearl ->
            PearlDeleteConfirmationDialog(
                pearlTitle = pearl.titleDisplay,
                onConfirm = {
                    removeTarget = null
                    onHidePearl(pearl)
                },
                onDismiss = { removeTarget = null },
                headline = "Remove pearl?",
                confirmLabel = "Remove",
                message = "This pearl will be hidden from your public feed.",
            )
        }

        saveTarget?.let { pearl ->
            PublicPearlSaveOverlay(
                folders = folders,
                theme = theme,
                onSaveToMyFeed = {
                    saveTarget = null
                    onSaveToMyFeed(pearl)
                },
                onSaveToFolder = { folder ->
                    saveTarget = null
                    onSaveToFolder(pearl, folder)
                },
                onCreateFolder = { name ->
                    saveTarget = null
                    onCreateFolderAndSave(pearl, name)
                },
                onDismiss = { saveTarget = null },
            )
        }

        SeenToastView(
            visible = uiState.showSeenToast,
            onDismiss = onDismissSeenToast,
        )

        if (uiState.showEmptyFilterAlert) {
            FeedEmptyFilterAlert(
                filter = uiState.contentTypeFilter,
                theme = theme,
                onShowAll = onResetContentTypeFilter,
                onDismiss = onDismissEmptyFilterAlert,
            )
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
