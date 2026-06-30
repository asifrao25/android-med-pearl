package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.ui.components.PearlSwipeAction
import com.knowledgepearls.app.ui.components.PearlSwipeRow
import com.knowledgepearls.app.ui.feed.FeedPearlAuthorInfo
import com.knowledgepearls.app.ui.feed.PearlFeedAuthorLayout
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme

@Composable
fun PublicFeedSearchResultsList(
    pearls: List<PublicPearl>,
    theme: TabTheme,
    listState: LazyListState,
    bottomPadding: Dp,
    onResolveAvatarUrl: suspend (String) -> String?,
    onOpenUserProfile: (String) -> Unit,
    onPearlClick: (String) -> Unit,
    onOpenSavePicker: (PublicPearl) -> Unit,
    onHidePearl: (PublicPearl) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = PearlLayout.screenHorizontalPadding,
            end = PearlLayout.screenHorizontalPadding,
            top = 8.dp,
            bottom = bottomPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(pearls, key = { it.id }) { pearl ->
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
                        onClick = { onHidePearl(pearl) },
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
}
