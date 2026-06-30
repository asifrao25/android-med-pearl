package com.knowledgepearls.app.ui.favourites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.ui.components.LocalFeedChromeVisibility
import com.knowledgepearls.app.ui.components.TabHeaderIconRow
import com.knowledgepearls.app.ui.components.TabScreenHeader
import com.knowledgepearls.app.ui.feed.FeedAuthorContext
import com.knowledgepearls.app.ui.feed.FeedSearchBar
import com.knowledgepearls.app.ui.feed.FeedSearchOverlay
import com.knowledgepearls.app.ui.feed.FeedSearchTagSuggestions
import com.knowledgepearls.app.ui.feed.PearlDeleteConfirmationDialog
import com.knowledgepearls.app.ui.feed.PearlList
import com.knowledgepearls.app.ui.folders.FolderPickerOverlay
import com.knowledgepearls.app.ui.folders.FoldersViewModel
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun FavouritesScreen(
    uiState: FavouritesUiState,
    feedAuthorContext: FeedAuthorContext,
    onResolveAvatarUrl: suspend (String) -> String?,
    onOpenSettings: () -> Unit,
    onPearlClick: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchActiveChange: (Boolean) -> Unit,
    onDeletePearl: (String) -> Unit,
    foldersViewModel: FoldersViewModel = hiltViewModel(),
) {
    val theme = TabTheme.Favourites
    val darkTheme = isPearlDarkTheme()
    val feedChrome = LocalFeedChromeVisibility.current
    val feedListState = rememberLazyListState()
    val searchListState = rememberLazyListState()
    val folders by foldersViewModel.foldersWithCounts.collectAsStateWithLifecycle()
    var deleteTarget by remember { mutableStateOf<PearlWithMedia?>(null) }
    var folderPickerPearl by remember { mutableStateOf<PearlWithMedia?>(null) }
    var memberFolderIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val folderPearlId = folderPickerPearl?.pearl?.id

    LaunchedEffect(folderPearlId) {
        if (folderPearlId == null) {
            memberFolderIds = emptySet()
        } else {
            foldersViewModel.observePearlFolderIds(folderPearlId).collect { memberFolderIds = it }
        }
    }

    LaunchedEffect(uiState.isSearchActive) {
        if (uiState.isSearchActive) {
            feedChrome?.suppress()
        } else {
            feedChrome?.releaseSuppress()
        }
    }

    val searchResults = if (uiState.searchQuery.isBlank()) {
        emptyList()
    } else {
        uiState.filteredFavourites
    }

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = theme)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            TabScreenHeader(
                title = "Favourites",
                subtitle = "Saved pearls",
                theme = theme,
                showsSettingsButton = false,
                onSettingsClick = onOpenSettings,
                trailing = {
                    if (!uiState.isSearchActive) {
                        TabHeaderIconRow(
                            theme = theme,
                            onSettingsClick = onOpenSettings,
                            onSearchClick = { onSearchActiveChange(true) },
                        )
                    }
                },
            )

            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                if (!uiState.isSearchActive) {
                    if (uiState.favourites.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(horizontal = 32.dp),
                            ) {
                                Icon(
                                    Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = theme.primary,
                                    modifier = Modifier.padding(bottom = 4.dp),
                                )
                                Text(
                                    "No Favourites Yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PearlColors.heroPrimary(darkTheme),
                                )
                                Text(
                                    "Tap the heart on any pearl\nto save it here.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PearlColors.heroSecondary(darkTheme),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    } else {
                        PearlList(
                            pearls = uiState.favourites,
                            feedAuthorContext = feedAuthorContext,
                            onResolveAvatarUrl = onResolveAvatarUrl,
                            onPearlClick = { onPearlClick(it.pearl.id) },
                            onDeleteRequest = { deleteTarget = it },
                            onFoldersRequest = { folderPickerPearl = it },
                            modifier = Modifier.fillMaxSize(),
                            listState = feedListState,
                            theme = theme,
                            chromeScrollEnabled = true,
                        )
                    }
                }

                if (uiState.isSearchActive) {
                    FeedSearchOverlay(theme = theme) {
                        FeedSearchBar(
                            query = uiState.searchQuery,
                            theme = theme,
                            onQueryChange = onSearchQueryChange,
                            onDismiss = {
                                onSearchActiveChange(false)
                                onSearchQueryChange("")
                            },
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                            placeholder = "Search saved pearls",
                        )

                        PearlList(
                            pearls = searchResults,
                            feedAuthorContext = feedAuthorContext,
                            onResolveAvatarUrl = onResolveAvatarUrl,
                            onPearlClick = { onPearlClick(it.pearl.id) },
                            onDeleteRequest = { deleteTarget = it },
                            onFoldersRequest = { folderPickerPearl = it },
                            modifier = Modifier.weight(1f),
                            listState = searchListState,
                            theme = theme,
                            chromeScrollEnabled = false,
                        )

                        if (uiState.searchQuery.isBlank()) {
                            FeedSearchTagSuggestions(
                                topTags = uiState.topSearchTags,
                                theme = theme,
                                onTagSelected = onSearchQueryChange,
                            )
                        }
                    }
                }
            }
        }

        deleteTarget?.let { target ->
            PearlDeleteConfirmationDialog(
                pearlTitle = target.pearl.title,
                onConfirm = {
                    onDeletePearl(target.pearl.id)
                    deleteTarget = null
                },
                onDismiss = { deleteTarget = null },
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
    }
}
