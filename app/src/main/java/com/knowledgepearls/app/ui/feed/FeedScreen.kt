package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import com.knowledgepearls.app.ui.components.FeedChromeAnchor
import com.knowledgepearls.app.ui.components.FeedChromeMetrics
import com.knowledgepearls.app.ui.components.LocalFeedChromeVisibility
import com.knowledgepearls.app.ui.components.feedChromeSlide
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowledgepearls.app.ui.folders.FolderPickerOverlay
import com.knowledgepearls.app.ui.folders.FoldersViewModel
import com.knowledgepearls.app.ui.components.HeaderIconButton
import com.knowledgepearls.app.ui.components.TabScreenHeader
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.capture.CaptureOptionsOverlay
import com.knowledgepearls.app.ui.capture.GlowingAddButton
import com.knowledgepearls.app.data.capture.CaptureSheet

@Composable
fun FeedScreen(
    uiState: FeedUiState,
    feedAuthorContext: FeedAuthorContext,
    onResolveAvatarUrl: suspend (String) -> String?,
    onOpenSettings: () -> Unit,
    isSignedIn: Boolean = false,
    inboxBadgeCount: Int = 0,
    onOpenInbox: () -> Unit = {},
    onPearlClick: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onTagSelected: (String?) -> Unit,
    onContentTypeSelected: (com.knowledgepearls.app.data.model.ContentTypeFilter) -> Unit,
    onDeleteRequest: (com.knowledgepearls.app.data.local.model.PearlWithMedia) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    onEmptyFilterShowAll: () -> Unit,
    onEmptyFilterDismiss: () -> Unit,
    onActionSuccessDismiss: () -> Unit,
    captureMenuOpen: Boolean,
    onCaptureMenuOpenChange: (Boolean) -> Unit,
    onCaptureSheetSelected: (CaptureSheet) -> Unit,
) {
    val theme = TabTheme.Feed
    val feedChrome = LocalFeedChromeVisibility.current
    val listState = rememberLazyListState()
    val foldersViewModel: FoldersViewModel = hiltViewModel()
    val folders by foldersViewModel.foldersWithCounts.collectAsStateWithLifecycle()
    var folderPickerPearl by remember { mutableStateOf<com.knowledgepearls.app.data.local.model.PearlWithMedia?>(null) }
    var memberFolderIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val folderPearlId = folderPickerPearl?.pearl?.id

    LaunchedEffect(folderPearlId) {
        if (folderPearlId == null) {
            memberFolderIds = emptySet()
        } else {
            foldersViewModel.observePearlFolderIds(folderPearlId).collect { memberFolderIds = it }
        }
    }

    LaunchedEffect(captureMenuOpen, uiState.isSearchActive) {
        if (captureMenuOpen || uiState.isSearchActive) {
            feedChrome?.suppress()
        } else {
            feedChrome?.releaseSuppress()
        }
    }

    LaunchedEffect(uiState.contentTypeFilter) {
        feedChrome?.forceShow()
    }

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = theme)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            TabScreenHeader(
                title = "My Feed",
                subtitle = "Your pearls",
                theme = theme,
                onSettingsClick = onOpenSettings,
                trailing = {
                    if (!uiState.isSearchActive) {
                        HeaderIconButton(theme = theme, onClick = { onSearchActiveChange(true) }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = theme.primary,
                                modifier = Modifier.size(PearlLayout.headerIconSize),
                            )
                        }
                    }
                },
            )

            ContentTypePicker(
                selected = uiState.contentTypeFilter,
                onSelected = onContentTypeSelected,
                theme = theme,
                modifier = Modifier.feedChromeSlide(
                    anchor = FeedChromeAnchor.Top,
                    hideDistance = FeedChromeMetrics.topChromeHideDistance,
                ),
            )

            if (uiState.isSearchActive) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PearlLayout.screenHorizontalPadding),
                    placeholder = { Text("Search pearls") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchActiveChange(false) }),
                    trailingIcon = {
                        TextButton(onClick = {
                            onSearchActiveChange(false)
                            onSearchQueryChange("")
                        }) {
                            Text("Cancel")
                        }
                    },
                )
            }

            uiState.actionSuccessMessage?.let { message ->
                PearlActionSuccessBanner(
                    message = message,
                    theme = theme,
                    onDismiss = onActionSuccessDismiss,
                )
            }

            PearlList(
                pearls = uiState.filteredPearls,
                feedAuthorContext = feedAuthorContext,
                onResolveAvatarUrl = onResolveAvatarUrl,
                onPearlClick = { onPearlClick(it.pearl.id) },
                onDeleteRequest = onDeleteRequest,
                onFoldersRequest = { folderPickerPearl = it },
                modifier = Modifier.weight(1f),
                listState = listState,
                chromeScrollEnabled = !captureMenuOpen && !uiState.isSearchActive,
            )
        }

        uiState.deleteTarget?.let { target ->
            PearlDeleteConfirmationDialog(
                pearlTitle = target.pearl.title,
                onConfirm = onDeleteConfirm,
                onDismiss = onDeleteCancel,
            )
        }

        folderPickerPearl?.let { pearl ->
            FolderPickerOverlay(
                pearl = pearl,
                folders = folders,
                memberFolderIds = memberFolderIds,
                onDismiss = { folderPickerPearl = null },
                onToggleFolder = { folderId ->
                    foldersViewModel.togglePearlFolderMembership(pearl.pearl.id, folderId)
                },
                onCreateFolder = { name ->
                    foldersViewModel.createFolderAndAddPearl(name, pearl.pearl.id)
                },
            )
        }

        if (uiState.showEmptyFilterAlert) {
            FeedEmptyFilterAlert(
                filter = uiState.contentTypeFilter,
                theme = theme,
                onShowAll = onEmptyFilterShowAll,
                onDismiss = onEmptyFilterDismiss,
            )
        }

        CaptureOptionsOverlay(
            visible = captureMenuOpen,
            onDismiss = { onCaptureMenuOpenChange(false) },
            onSelect = onCaptureSheetSelected,
            fabBottomPadding = PearlLayout.addButtonBottomPadding,
        )

        GlowingAddButton(
            isMenuOpen = captureMenuOpen,
            onClick = { onCaptureMenuOpenChange(!captureMenuOpen) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .feedChromeSlide(
                    anchor = FeedChromeAnchor.Bottom,
                    hideDistance = FeedChromeMetrics.fabHideDistance,
                )
                .padding(end = 20.dp, bottom = PearlLayout.addButtonBottomPadding),
        )
    }
}
