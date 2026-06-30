package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.local.model.rememberPublicPearlForCard
import com.knowledgepearls.app.ui.components.TrackFeedChromeScroll
import com.knowledgepearls.app.ui.components.feedChromeBottomPadding
import com.knowledgepearls.app.ui.components.PearlSwipeAction
import com.knowledgepearls.app.ui.components.PearlSwipeRow
import com.knowledgepearls.app.ui.components.SwipeRowHintStorage
import com.knowledgepearls.app.ui.publicfeed.PublicFeedCard
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun PearlList(
    pearls: List<PearlWithMedia>,
    feedAuthorContext: FeedAuthorContext,
    onResolveAvatarUrl: suspend (String) -> String?,
    onPearlClick: (PearlWithMedia) -> Unit,
    onDeleteRequest: (PearlWithMedia) -> Unit,
    onFoldersRequest: (PearlWithMedia) -> Unit,
    onFetchPublicPearl: suspend (String) -> com.knowledgepearls.app.data.model.PublicPearl? = { null },
    isPublicPearlLiked: (String) -> Boolean = { false },
    publicPearlLikeCount: (com.knowledgepearls.app.data.model.PublicPearl) -> Int = { it.likeCount },
    onTogglePublicPearlLike: (com.knowledgepearls.app.data.model.PublicPearl) -> Unit = {},
    hasAnyPearlsInFeed: Boolean = true,
    emptySearchQuery: String? = null,
    onCreateFirstPearl: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    theme: TabTheme = TabTheme.Feed,
    listState: LazyListState = rememberLazyListState(),
    chromeScrollEnabled: Boolean = true,
) {
    val context = LocalContext.current
    var swipeHintDismissed by remember {
        mutableStateOf(SwipeRowHintStorage.isDismissed(context))
    }

    PearlListContent(
        pearls = pearls,
        feedAuthorContext = feedAuthorContext,
        onResolveAvatarUrl = onResolveAvatarUrl,
        onPearlClick = onPearlClick,
        onDeleteRequest = onDeleteRequest,
        onFoldersRequest = onFoldersRequest,
        onFetchPublicPearl = onFetchPublicPearl,
        isPublicPearlLiked = isPublicPearlLiked,
        publicPearlLikeCount = publicPearlLikeCount,
        onTogglePublicPearlLike = onTogglePublicPearlLike,
        hasAnyPearlsInFeed = hasAnyPearlsInFeed,
        emptySearchQuery = emptySearchQuery,
        onCreateFirstPearl = onCreateFirstPearl,
        enableSwipeHintOnFirst = !swipeHintDismissed,
        onSwipeHintDismiss = {
            SwipeRowHintStorage.dismiss(context)
            swipeHintDismissed = true
        },
        modifier = modifier,
        theme = theme,
        listState = listState,
        chromeScrollEnabled = chromeScrollEnabled,
    )
}

@Composable
private fun PearlListContent(
    pearls: List<PearlWithMedia>,
    feedAuthorContext: FeedAuthorContext,
    onResolveAvatarUrl: suspend (String) -> String?,
    onPearlClick: (PearlWithMedia) -> Unit,
    onDeleteRequest: (PearlWithMedia) -> Unit,
    onFoldersRequest: (PearlWithMedia) -> Unit,
    onFetchPublicPearl: suspend (String) -> com.knowledgepearls.app.data.model.PublicPearl?,
    isPublicPearlLiked: (String) -> Boolean,
    publicPearlLikeCount: (com.knowledgepearls.app.data.model.PublicPearl) -> Int,
    onTogglePublicPearlLike: (com.knowledgepearls.app.data.model.PublicPearl) -> Unit,
    hasAnyPearlsInFeed: Boolean,
    emptySearchQuery: String?,
    onCreateFirstPearl: (() -> Unit)?,
    enableSwipeHintOnFirst: Boolean,
    onSwipeHintDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    theme: TabTheme = TabTheme.Feed,
    listState: LazyListState,
    chromeScrollEnabled: Boolean,
) {
    val darkTheme = isPearlDarkTheme()
    TrackFeedChromeScroll(listState = listState, enabled = chromeScrollEnabled)
    val bottomPadding = feedChromeBottomPadding(fullPadding = 120.dp)

    if (pearls.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            when {
                !hasAnyPearlsInFeed && onCreateFirstPearl != null -> {
                    MyFeedFirstRunEmptyState(
                        theme = theme,
                        onCreatePearl = onCreateFirstPearl,
                    )
                }
                !emptySearchQuery.isNullOrBlank() -> {
                    Text(
                        text = "No pearls match \"$emptySearchQuery\".",
                        style = MaterialTheme.typography.bodyLarge,
                        color = PearlColors.heroSecondary(darkTheme),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                }
                else -> {
                    Text(
                        text = "No pearls match your filters.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = PearlColors.heroSecondary(darkTheme),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                }
            }
        }
        return
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = PearlLayout.screenHorizontalPadding,
            end = PearlLayout.screenHorizontalPadding,
            top = 8.dp,
            bottom = bottomPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(pearls, key = { it.pearl.id }) { pearl ->
            val publicPearl = rememberPublicPearlForCard(pearl.pearl, onFetchPublicPearl)
            val author = FeedPearlAuthorInfo.resolve(pearl, feedAuthorContext, publicPearl)
            val showHint = enableSwipeHintOnFirst && pearl == pearls.first()

            PearlFeedAuthorLayout(
                displayName = author.displayName,
                avatarUrl = author.avatarUrl,
                createdAtMillis = pearl.pearl.createdAt,
                userId = author.userId,
                onResolveAvatarUrl = onResolveAvatarUrl,
            ) {
                PearlSwipeRow(
                    leadingAction = PearlSwipeAction(
                        icon = Icons.Default.CreateNewFolder,
                        title = "Folders",
                        color = theme.primary,
                        onClick = { onFoldersRequest(pearl) },
                    ),
                    trailingAction = PearlSwipeAction(
                        icon = Icons.Default.Delete,
                        title = "Delete",
                        color = Color(0xFFFF3B30),
                        onClick = { onDeleteRequest(pearl) },
                    ),
                    enableSwipeHint = showHint,
                    onSwipeHintDismiss = onSwipeHintDismiss,
                ) {
                    if (publicPearl != null) {
                        PublicFeedCard(
                            pearl = publicPearl,
                            theme = theme,
                            likeCount = publicPearlLikeCount(publicPearl),
                            isLiked = isPublicPearlLiked(publicPearl.id),
                            onToggleLike = { onTogglePublicPearlLike(publicPearl) },
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onPearlClick(pearl) },
                        )
                    } else {
                        val cardLabel = pearl.pearl.title.ifBlank { "Untitled pearl" }
                        PearlCard(
                            pearl = pearl,
                            theme = theme,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "$cardLabel. Pearl card."
                                    role = Role.Button
                                }
                                .clickable { onPearlClick(pearl) },
                        )
                    }
                }
            }
        }
    }
}
