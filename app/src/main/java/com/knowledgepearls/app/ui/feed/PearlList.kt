package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PearlList(
    pearls: List<PearlWithMedia>,
    feedAuthorContext: FeedAuthorContext,
    onResolveAvatarUrl: suspend (String) -> String?,
    onPearlClick: (PearlWithMedia) -> Unit,
    onDeleteRequest: (PearlWithMedia) -> Unit,
    modifier: Modifier = Modifier,
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
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
    ) {
        items(pearls, key = { it.pearl.id }) { pearl ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value == SwipeToDismissBoxValue.EndToStart) {
                        onDeleteRequest(pearl)
                        false
                    } else {
                        false
                    }
                },
            )

            LaunchedEffect(dismissState.currentValue) {
                if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                    dismissState.reset()
                }
            }

            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = false,
                backgroundContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.padding(end = 24.dp),
                        )
                    }
                },
                content = {
                    val author = FeedPearlAuthorInfo.resolve(pearl, feedAuthorContext)
                    PearlFeedAuthorLayout(
                        displayName = author.displayName,
                        avatarUrl = author.avatarUrl,
                        createdAtMillis = pearl.pearl.createdAt,
                        userId = author.userId,
                        onResolveAvatarUrl = onResolveAvatarUrl,
                    ) {
                        PearlCard(
                            pearl = pearl,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPearlClick(pearl) },
                        )
                    }
                },
            )
        }
    }
}
