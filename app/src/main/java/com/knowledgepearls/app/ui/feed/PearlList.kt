package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.local.model.decodedPublicPearl
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
    modifier: Modifier = Modifier,
    theme: TabTheme = TabTheme.Feed,
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
        enableSwipeHintOnFirst = !swipeHintDismissed,
        onSwipeHintDismiss = {
            SwipeRowHintStorage.dismiss(context)
            swipeHintDismissed = true
        },
        modifier = modifier,
        theme = theme,
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
    enableSwipeHintOnFirst: Boolean,
    onSwipeHintDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    theme: TabTheme = TabTheme.Feed,
) {
    val darkTheme = isPearlDarkTheme()

    if (pearls.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No pearls match your filters.",
                style = MaterialTheme.typography.bodyLarge,
                color = PearlColors.heroSecondary(darkTheme),
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = PearlLayout.screenHorizontalPadding,
            end = PearlLayout.screenHorizontalPadding,
            top = 8.dp,
            bottom = 120.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(pearls, key = { it.pearl.id }) { pearl ->
            val publicPearl = pearl.pearl.decodedPublicPearl()
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
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onPearlClick(pearl) },
                        )
                    } else {
                        PearlCard(
                            pearl = pearl,
                            theme = theme,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPearlClick(pearl) },
                        )
                    }
                }
            }
        }
    }
}
