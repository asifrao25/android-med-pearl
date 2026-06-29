package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import com.knowledgepearls.app.data.local.model.FolderWithCount
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.ui.components.PearlSwipeAction
import com.knowledgepearls.app.ui.components.PearlSwipeRow
import com.knowledgepearls.app.ui.feed.PearlDeleteConfirmationDialog
import androidx.compose.ui.zIndex
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.components.PearlActionOutcome
import com.knowledgepearls.app.ui.components.PearlAlreadyInFeedAlert
import com.knowledgepearls.app.ui.components.PublicFeedOfflineState
import com.knowledgepearls.app.ui.feed.FeedEmptyFilterAlert
import com.knowledgepearls.app.ui.feed.FeedPearlAuthorInfo
import com.knowledgepearls.app.ui.feed.PearlFeedAuthorLayout
import com.knowledgepearls.app.ui.components.HeaderIconButton
import com.knowledgepearls.app.ui.components.TabScreenHeader
import com.knowledgepearls.app.ui.feed.ContentTypePicker
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import com.knowledgepearls.app.ui.capture.CaptureOptionsOverlay
import com.knowledgepearls.app.ui.capture.GlowingAddButton
import com.knowledgepearls.app.data.capture.CaptureSheet
import com.knowledgepearls.app.ui.components.SharedPearlIntroAlert
import com.knowledgepearls.app.ui.account.AccountUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicFeedScreen(
    uiState: PublicFeedUiState,
    isSignedIn: Boolean,
    inboxBadgeCount: Int = 0,
    onOpenSettings: () -> Unit,
    onOpenInbox: () -> Unit,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onVerifyCode: (String, String) -> Unit,
    onResendCode: (String) -> Unit,
    onClearSignInSuccess: () -> Unit,
    accountState: AccountUiState,
    onPearlClick: (String) -> Unit,
    onResolveAvatarUrl: suspend (String) -> String? = { null },
    onOpenUserProfile: (String) -> Unit = {},
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
    onOpenSavePicker: (PublicPearl) -> Unit = {},
    onHidePearl: (PublicPearl) -> Unit = {},
    onSaveToMyFeed: (PublicPearl) -> Unit = {},
    onSaveToFolder: (PublicPearl, FolderWithCount) -> Unit = { _, _ -> },
    onCreateFolderAndSave: (PublicPearl, String) -> Unit = { _, _ -> },
    isNetworkAvailable: Boolean = true,
    isOfflineMode: Boolean = false,
    onRetryConnection: () -> Unit = {},
    captureMenuOpen: Boolean = false,
    onCaptureMenuOpenChange: (Boolean) -> Unit = {},
    onCaptureSheetSelected: (CaptureSheet) -> Unit = {},
    showSharedPearlIntro: Boolean = false,
    onRequestSharedPearlIntro: () -> Unit = {},
    onSharedPearlIntroContinue: () -> Unit = {},
    onSharedPearlIntroCancel: () -> Unit = {},
    showsSectionTabs: Boolean = false,
) {
    val theme = TabTheme.PublicFeed
    val darkTheme = isPearlDarkTheme()
    val floatingAddButtonBottomPadding = if (showsSectionTabs) {
        PearlLayout.publicFeedAddButtonBottomPadding
    } else {
        PearlLayout.addButtonBottomPadding
    }
    val listState = rememberLazyListState()
    var removeTarget by remember { mutableStateOf<PublicPearl?>(null) }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= uiState.filteredPearls.lastIndex - 2
        }
    }

    val seenToastFocus = isSignedIn && isNetworkAvailable && uiState.showSeenToast
    val feedBlurRadius by animateDpAsState(
        targetValue = if (seenToastFocus) 22.dp else 0.dp,
        animationSpec = tween(durationMillis = if (seenToastFocus) 340 else 460),
        label = "seenFocusBlur",
    )

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
                trailing = { },
            )

            ContentTypePicker(
                selected = uiState.contentTypeFilter,
                onSelected = onContentTypeSelected,
                theme = theme,
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(feedBlurRadius),
                ) {
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
                                bottom = PearlLayout.publicFeedAddButtonBottomPadding +
                                    PearlLayout.addButtonSize + 24.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                        if (uiState.filteredPearls.isEmpty()) {
                            item {
                                when {
                                    uiState.section == PublicFeedSection.NEW &&
                                        uiState.unseenPearls.isEmpty() &&
                                        uiState.seenPearls.isNotEmpty() &&
                                        !uiState.isLoading -> {
                                        PublicFeedAllCaughtUpCard(
                                            theme = theme,
                                            onSwitchToSeen = { onSectionSelected(PublicFeedSection.SEEN) },
                                        )
                                    }
                                    uiState.section == PublicFeedSection.NEW &&
                                        uiState.unseenPearls.isEmpty() &&
                                        uiState.seenPearls.isEmpty() -> {
                                        PublicFeedSectionEmptyState(
                                            title = "No New Pearls",
                                            message = "You're all caught up. Approved pearls will appear here first.",
                                            theme = theme,
                                        )
                                    }
                                    uiState.section == PublicFeedSection.NEW &&
                                        uiState.unseenPearls.isNotEmpty() &&
                                        uiState.contentTypeFilter != com.knowledgepearls.app.data.model.ContentTypeFilter.ALL -> {
                                        PublicFeedSectionEmptyState(
                                            title = "No ${uiState.contentTypeFilter.label} Pearls",
                                            message = "No new pearls match this filter. Try another filter or show all.",
                                            theme = theme,
                                            actionLabel = "Show all",
                                            onAction = onResetContentTypeFilter,
                                        )
                                    }
                                    uiState.section == PublicFeedSection.SEEN &&
                                        uiState.seenPearls.isEmpty() -> {
                                        PublicFeedSectionEmptyState(
                                            title = "No Seen Pearls Yet",
                                            message = "Pearls you've opened will collect here for easy revisiting.",
                                            theme = theme,
                                        )
                                    }
                                    uiState.section == PublicFeedSection.SEEN &&
                                        uiState.seenPearls.isNotEmpty() &&
                                        uiState.contentTypeFilter != com.knowledgepearls.app.data.model.ContentTypeFilter.ALL -> {
                                        PublicFeedSectionEmptyState(
                                            title = "No ${uiState.contentTypeFilter.label} Pearls",
                                            message = "No seen pearls match this filter. Try another filter or show all.",
                                            theme = theme,
                                            actionLabel = "Show all",
                                            onAction = onResetContentTypeFilter,
                                        )
                                    }
                                    else -> {
                                        PublicFeedSectionEmptyState(
                                            title = if (uiState.section == PublicFeedSection.NEW) {
                                                "All Caught Up"
                                            } else {
                                                "No Seen Pearls Yet"
                                            },
                                            message = if (uiState.section == PublicFeedSection.NEW) {
                                                "Switch to Seen below to revisit previous pearls."
                                            } else {
                                                "Pearls you've opened will collect here for easy revisiting."
                                            },
                                            theme = theme,
                                            actionLabel = if (uiState.section == PublicFeedSection.NEW && uiState.seenCount > 0) {
                                                "Go to Seen"
                                            } else {
                                                null
                                            },
                                            onAction = if (uiState.section == PublicFeedSection.NEW && uiState.seenCount > 0) {
                                                { onSectionSelected(PublicFeedSection.SEEN) }
                                            } else {
                                                null
                                            },
                                        )
                                    }
                                }
                            }
                        } else {
                            items(uiState.filteredPearls, key = { it.id }) { pearl ->
                                val author = FeedPearlAuthorInfo.fromPublicPearl(pearl)
                                PearlFeedAuthorLayout(
                                    displayName = author.displayName,
                                    avatarUrl = author.avatarUrl,
                                    createdAtMillis = pearl.feedSortMillis,
                                    userId = author.userId,
                                    onResolveAvatarUrl = onResolveAvatarUrl,
                                    onAuthorClick = author.userId
                                        ?.takeIf { it.isNotBlank() }
                                        ?.let { userId -> { onOpenUserProfile(userId) } },
                                ) {
                                    PearlSwipeRow(
                                        leadingAction = PearlSwipeAction(
                                            icon = Icons.Default.CreateNewFolder,
                                            title = "Save",
                                            color = theme.primary,
                                            onClick = { onOpenSavePicker(pearl) },
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

                PublicFeedSeenFocusScrim(
                    visible = seenToastFocus,
                    theme = theme,
                )
            }
        }

        if (isSignedIn && isNetworkAvailable) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(1f)
                    .padding(
                        start = PearlLayout.screenHorizontalPadding,
                        end = PearlLayout.screenHorizontalPadding,
                        bottom = PearlLayout.publicFeedSectionTabsBottomPadding,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MovedToSeenTabToast(
                    visible = uiState.showSeenToast,
                    theme = theme,
                    onDismiss = onDismissSeenToast,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                PublicFeedSectionTabs(
                    selected = uiState.section,
                    newCount = uiState.newCount,
                    seenCount = uiState.seenCount,
                    theme = theme,
                    onSelected = onSectionSelected,
                )
            }
        }

        if (isSignedIn && isNetworkAvailable) {
            CaptureOptionsOverlay(
                visible = captureMenuOpen,
                onDismiss = { onCaptureMenuOpenChange(false) },
                onSelect = onCaptureSheetSelected,
                isSharedCapture = true,
                fabBottomPadding = floatingAddButtonBottomPadding,
            )

            GlowingAddButton(
                isMenuOpen = captureMenuOpen,
                onClick = { onCaptureMenuOpenChange(!captureMenuOpen) },
                onBeforeOpen = {
                    onRequestSharedPearlIntro()
                    false
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = floatingAddButtonBottomPadding),
            )
        }

        if (showSharedPearlIntro) {
            SharedPearlIntroAlert(
                theme = theme,
                onContinue = onSharedPearlIntroContinue,
                onCancel = onSharedPearlIntroCancel,
            )
        }

        if (!isSignedIn) {
            PublicFeedAuthGate(
                accountState = accountState,
                onSignIn = onSignIn,
                onSignUp = onSignUp,
                onGoogleSignIn = onGoogleSignIn,
                onVerifyCode = onVerifyCode,
                onResendCode = onResendCode,
                onClearSignInSuccess = onClearSignInSuccess,
            )
        }

        when (uiState.actionOutcome) {
            PearlActionOutcome.AlreadyInMyFeed -> {
                PearlAlreadyInFeedAlert(
                    pearlTitle = uiState.actionSuccessMessage.orEmpty(),
                    theme = theme,
                    onDismiss = onDismissActionSuccess,
                )
            }
            else -> Unit
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
