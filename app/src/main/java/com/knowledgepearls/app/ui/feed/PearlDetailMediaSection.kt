package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.ui.media.localPearlMediaSlides
import com.knowledgepearls.app.ui.media.mediaUriForPath
import com.knowledgepearls.app.ui.media.pearlMediaViewerRequest
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaCarousel
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaSlide
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaSlideView
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerRequest
import com.knowledgepearls.app.ui.theme.TabTheme

@Composable
fun PearlDetailSectionMedia(
    pearl: PearlWithMedia,
    sectionTag: String,
    theme: TabTheme,
    onOpenMedia: (PublicPearlMediaViewerRequest) -> Unit,
    modifier: Modifier = Modifier,
    carouselHeight: Dp = PearlDetailMetrics.clinicalSectionMediaHeight,
) {
    val sectionItems = pearl.mediaItems.filter { it.sectionTag.equals(sectionTag, ignoreCase = true) }
    if (sectionItems.isEmpty()) return

    PearlDetailMediaSection(
        pearl = pearl.copy(mediaItems = sectionItems),
        theme = theme,
        onOpenMedia = onOpenMedia,
        modifier = modifier,
        carouselHeight = carouselHeight,
        showAttachmentLabel = false,
    )
}

@Composable
fun PearlDetailMediaSection(
    pearl: PearlWithMedia,
    theme: TabTheme,
    onOpenMedia: (PublicPearlMediaViewerRequest) -> Unit,
    modifier: Modifier = Modifier,
    carouselHeight: Dp = PearlDetailMetrics.openedMediaCarouselHeight,
    showAttachmentLabel: Boolean = true,
    onOpenUrl: (String) -> Unit = {},
) {
    val entity = pearl.pearl
    val slides = localPearlMediaSlides(pearl.mediaItems)
    val linkUrl = entity.sourceURL?.trim()?.takeIf { it.isNotEmpty() && slides.isEmpty() }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when {
            linkUrl != null -> {
                LinkPearlPreviewSection(
                    url = linkUrl,
                    theme = theme,
                    onOpenExternal = onOpenUrl,
                    onOpenBrowser = {
                        parseOpenableUrl(linkUrl)?.let(onOpenUrl)
                    },
                )
            }
            slides.isNotEmpty() -> {
                if (slides.size > 1) {
                    PublicPearlMediaCarousel(
                        slides = slides,
                        theme = theme,
                        height = carouselHeight,
                        interactive = true,
                        onOpenAtIndex = { index ->
                            onOpenMedia(PublicPearlMediaViewerRequest(slides, index))
                        },
                    )
                } else {
                    val slide = slides.first()
                    PublicPearlMediaSlideView(
                        slide = slide,
                        theme = theme,
                        height = carouselHeight,
                        interactive = true,
                        onOpen = {
                            pearlMediaViewerRequest(pearl, slide)?.let(onOpenMedia)
                        },
                    )
                }
            }
            !entity.linkPreviewImagePath.isNullOrBlank() -> {
                val path = entity.linkPreviewImagePath!!
                val slide = PublicPearlMediaSlide.Image(mediaUriForPath(path))
                PublicPearlMediaSlideView(
                    slide = slide,
                    theme = theme,
                    height = carouselHeight,
                    interactive = true,
                    onOpen = {
                        onOpenMedia(PublicPearlMediaViewerRequest(slides = listOf(slide)))
                    },
                )
            }
        }
    }
}
