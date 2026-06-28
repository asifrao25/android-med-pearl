package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerRequest
import com.knowledgepearls.app.ui.theme.TabTheme

@Composable
fun SavedPublicPearlDetailContent(
    publicPearl: PublicPearl,
    theme: TabTheme,
    onOpenMedia: (PublicPearlMediaViewerRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    PublicPearlDetailBody(
        pearl = publicPearl,
        theme = theme,
        onOpenMedia = onOpenMedia,
        onOpenUrl = { url -> openExternalUrl(context, url) },
        onOpenTweet = {
            publicPearl.preferredPreviewUrl?.let { openExternalUrl(context, it) }
        },
        modifier = modifier,
    )
}
