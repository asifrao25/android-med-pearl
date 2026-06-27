package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.ui.media.documentSlides
import com.knowledgepearls.app.ui.media.gallerySlides
import com.knowledgepearls.app.ui.media.localPearlMediaSlides
import com.knowledgepearls.app.ui.media.PearlDocumentDetailPreview
import com.knowledgepearls.app.ui.media.mediaUriForPath
import com.knowledgepearls.app.ui.media.pearlMediaViewerRequest
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaCarousel
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaSlide
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaSlideView
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerRequest
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun PearlDetailSectionMedia(
    pearl: PearlWithMedia,
    sectionTag: String,
    theme: TabTheme,
    onOpenMedia: (PublicPearlMediaViewerRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sectionItems = pearl.mediaItems.filter { it.sectionTag.equals(sectionTag, ignoreCase = true) }
    if (sectionItems.isEmpty()) return

    PearlDetailMediaSection(
        pearl = pearl.copy(mediaItems = sectionItems),
        theme = theme,
        onOpenMedia = onOpenMedia,
        modifier = modifier,
    )
}

@Composable
fun PearlDetailMediaSection(
    pearl: PearlWithMedia,
    theme: TabTheme,
    onOpenMedia: (PublicPearlMediaViewerRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gallery = gallerySlides(pearl.mediaItems)
    val documents = documentSlides(pearl.mediaItems)
    val darkTheme = isPearlDarkTheme()
    val openSlide: (PublicPearlMediaSlide) -> Unit = { slide ->
        pearlMediaViewerRequest(pearl, slide)?.let(onOpenMedia)
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (gallery.isNotEmpty()) {
            Text(
                text = if (gallery.size > 1) "Attachments" else "Attachment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PearlColors.heroPrimary(darkTheme),
            )
            if (gallery.size > 1) {
                PublicPearlMediaCarousel(
                    slides = gallery,
                    theme = theme,
                    height = 220.dp,
                    interactive = true,
                    onOpenAtIndex = { index ->
                        onOpenMedia(PublicPearlMediaViewerRequest(gallery, index))
                    },
                )
            } else {
                PublicPearlMediaSlideView(
                    slide = gallery.first(),
                    theme = theme,
                    height = 220.dp,
                    interactive = true,
                    onOpen = { openSlide(gallery.first()) },
                )
            }
        } else {
            pearl.pearl.linkPreviewImagePath?.takeIf { it.isNotBlank() }?.let { path ->
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PearlColors.heroPrimary(darkTheme),
                )
                PublicPearlMediaSlideView(
                    slide = PublicPearlMediaSlide.Image(mediaUriForPath(path)),
                    theme = theme,
                    height = 220.dp,
                    interactive = true,
                    onOpen = {
                        onOpenMedia(
                            PublicPearlMediaViewerRequest(
                                slides = listOf(PublicPearlMediaSlide.Image(mediaUriForPath(path))),
                            ),
                        )
                    },
                )
            }
        }

        if (documents.isNotEmpty()) {
            Text(
                text = if (documents.size > 1) "Documents" else "Document",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PearlColors.heroPrimary(darkTheme),
            )
            documents.forEach { item ->
                val path = item.localPath ?: return@forEach
                val filename = item.filename.ifBlank { "Document" }
                val uri = mediaUriForPath(path)
                val slide = localPearlMediaSlides(listOf(item)).firstOrNull() as? PublicPearlMediaSlide.Document
                    ?: return@forEach
                PearlDocumentDetailPreview(
                    url = uri,
                    filename = filename,
                    theme = theme,
                    onOpen = { openSlide(slide) },
                )
            }
        }
    }
}
