package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.ui.components.DetailDockAction
import com.knowledgepearls.app.ui.components.LiquidDetailDock
import com.knowledgepearls.app.ui.feed.PearlDetailAuthorBar
import com.knowledgepearls.app.ui.feed.PearlDetailTitleBar
import com.knowledgepearls.app.ui.feed.PublicPearlDetailBody
import com.knowledgepearls.app.ui.feed.openExternalUrl
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import kotlinx.coroutines.delay

@Composable
fun PublicPearlDetailScreen(
    pearl: PublicPearl,
    likeCount: Int,
    commentCount: Int,
    isLiked: Boolean,
    commentsVisible: Boolean,
    comments: List<com.knowledgepearls.app.data.model.PearlComment>,
    isCommentsLoading: Boolean,
    isPostingComment: Boolean,
    commentsError: String?,
    onResolveAvatarUrl: suspend (String) -> String?,
    onBack: () -> Unit,
    onOpenUserProfile: (String) -> Unit = {},
    onAddToMyFeed: () -> Unit,
    onHide: () -> Unit,
    onToggleLike: () -> Unit,
    onOpenComments: () -> Unit,
    onCloseComments: () -> Unit,
    onPostComment: (String) -> Unit,
) {
    val theme = TabTheme.PublicFeed
    val context = LocalContext.current
    var mediaViewerRequest by remember(pearl.id) { mutableStateOf<PublicPearlMediaViewerRequest?>(null) }
    var commentsSheetOpen by remember(commentsVisible) { mutableStateOf(commentsVisible) }
    var savedFeedback by remember(pearl.id) { mutableStateOf(false) }

    LaunchedEffect(savedFeedback) {
        if (savedFeedback) {
            delay(1500)
            savedFeedback = false
        }
    }

    PublicPearlMediaViewerOverlay(
        request = mediaViewerRequest,
        theme = theme,
        onDismiss = { mediaViewerRequest = null },
    )

    if (commentsVisible) {
        commentsSheetOpen = true
    }

    PearlCommentsSheet(
        visible = commentsSheetOpen,
        comments = comments,
        isLoading = isCommentsLoading,
        isPosting = isPostingComment,
        errorMessage = commentsError,
        theme = theme,
        onDismiss = {
            commentsSheetOpen = false
            onCloseComments()
        },
        onPostComment = onPostComment,
    )

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = theme, intensity = 0.5f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding(),
        ) {
            PearlDetailAuthorBar(
                displayName = pearl.safeDisplayName.ifBlank { "Unknown" },
                avatarUrl = null,
                createdAtMillis = pearl.createdAtMillis ?: System.currentTimeMillis(),
                theme = theme,
                caption = "Shared by",
                userId = pearl.userId,
                onResolveAvatarUrl = onResolveAvatarUrl,
                onOpenProfile = { onOpenUserProfile(pearl.userId) },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = PearlLayout.screenHorizontalPadding)
                    .padding(top = 12.dp, bottom = PearlLayout.detailScrollBottomPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (!pearl.isFromTwitterScraper) {
                    PearlDetailTitleBar(title = pearl.titleDisplay)
                }

                PublicPearlDetailBody(
                    pearl = pearl,
                    theme = theme,
                    onOpenMedia = { mediaViewerRequest = it },
                    onOpenUrl = { url -> openExternalUrl(context, url) },
                    onOpenTweet = {
                        pearl.preferredPreviewUrl?.let { openExternalUrl(context, it) }
                    },
                )

                PublicPearlEngagementBar(
                    likeCount = likeCount,
                    commentCount = commentCount,
                    isLiked = isLiked,
                    theme = theme,
                    modifier = Modifier.fillMaxWidth(),
                    onToggleLike = onToggleLike,
                    onOpenComments = {
                        commentsSheetOpen = true
                        onOpenComments()
                    },
                )
            }
        }

        LiquidDetailDock(
            theme = theme,
            onBack = onBack,
            actions = listOf(
                DetailDockAction(
                    id = "save",
                    label = if (savedFeedback) "Saved!" else "Save",
                    icon = if (savedFeedback) Icons.Default.CheckCircle else Icons.Default.Add,
                    tint = if (savedFeedback) theme.primary else null,
                    isActive = savedFeedback,
                    onClick = {
                        onAddToMyFeed()
                        savedFeedback = true
                    },
                ),
                DetailDockAction(
                    id = "hide",
                    label = "Hide",
                    icon = Icons.Default.VisibilityOff,
                    tint = Color(0xFF8C8C99),
                    onClick = onHide,
                ),
            ),
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
