package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.ui.publicfeed.PublicPearlDetailMediaSection
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerRequest
import com.knowledgepearls.app.ui.theme.TabTheme

@Composable
fun StandardPublicPearlDetailContent(
    pearl: PublicPearl,
    theme: TabTheme,
    onOpenMedia: (PublicPearlMediaViewerRequest) -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenTweet: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    showMedia: Boolean = true,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (showMedia) {
            PublicPearlDetailMediaSection(
                pearl = pearl,
                theme = theme,
                onOpenMedia = onOpenMedia,
                onOpenUrl = onOpenUrl,
            )
        }

        PearlDetailDescriptionSection(text = pearl.notes, theme = theme)

        when {
            pearl.canLinkToTwitterOriginalAuthor && onOpenTweet != null -> {
                PearlDetailTwitterAuthorSourceBox(
                    authorName = pearl.twitterOriginalAuthorLinkLabel,
                    theme = theme,
                    onOpenTweet = onOpenTweet,
                )
            }
            pearl.effectiveSourceReference.isNotBlank() -> {
                PearlDetailSection(
                    title = "Source / Reference",
                    body = pearl.effectiveSourceReference,
                    theme = theme,
                )
            }
        }
    }
}

@Composable
fun PublicPearlDetailBody(
    pearl: PublicPearl,
    theme: TabTheme,
    onOpenMedia: (PublicPearlMediaViewerRequest) -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenTweet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when {
            pearl.isClinicalCase -> {
                PublicClinicalCaseDetailContent(
                    pearl = pearl,
                    theme = theme,
                    onOpenMedia = onOpenMedia,
                )
            }
            pearl.isFromTwitterScraper -> {
                ScraperPearlDetailContent(
                    pearl = pearl,
                    theme = theme,
                    onOpenMedia = onOpenMedia,
                    onOpenUrl = onOpenUrl,
                    onOpenTweet = onOpenTweet,
                )
            }
            else -> {
                StandardPublicPearlDetailContent(
                    pearl = pearl,
                    theme = theme,
                    onOpenMedia = onOpenMedia,
                    onOpenUrl = onOpenUrl,
                    onOpenTweet = onOpenTweet,
                )
            }
        }

        if (pearl.tags.isNotEmpty()) {
            PearlDetailSection(
                title = "Tags",
                body = pearl.tags.joinToString(", "),
                theme = theme,
                linkifyBody = false,
            )
        }
    }
}
