package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.ui.publicfeed.PublicPearlLikeButton
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerRequest
import com.knowledgepearls.app.ui.theme.TabTheme

@Composable
fun SavedPublicPearlDetailContent(
    publicPearl: PublicPearl,
    theme: TabTheme,
    onOpenMedia: (PublicPearlMediaViewerRequest) -> Unit,
    modifier: Modifier = Modifier,
    likeCount: Int = publicPearl.likeCount,
    isLiked: Boolean = false,
    onToggleLike: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PublicPearlDetailBody(
            pearl = publicPearl,
            theme = theme,
            onOpenMedia = onOpenMedia,
            onOpenUrl = { url -> openExternalUrl(context, url) },
            onOpenTweet = {
                publicPearl.preferredPreviewUrl?.let { openExternalUrl(context, it) }
            },
        )
        if (onToggleLike != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PublicPearlLikeButton(
                    likeCount = likeCount,
                    isLiked = isLiked,
                    theme = theme,
                    onToggleLike = onToggleLike,
                )
            }
        }
    }
}
