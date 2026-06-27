package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.ui.media.documentSlides
import com.knowledgepearls.app.ui.media.gallerySlides
import com.knowledgepearls.app.ui.media.localPearlMediaSlides
import com.knowledgepearls.app.ui.media.mediaUriForPath
import com.knowledgepearls.app.ui.media.pearlMediaViewerRequest
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaCarousel
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaSlide
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaSlideView
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerRequest
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

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

        documents.forEach { item ->
            val path = item.localPath ?: return@forEach
            val slide = localPearlMediaSlides(listOf(item)).firstOrNull() as? PublicPearlMediaSlide.Document
                ?: return@forEach
            LocalDocumentAttachmentRow(
                filename = item.filename.ifBlank { "Document" },
                onOpen = { openSlide(slide) },
            )
        }
    }
}

@Composable
private fun LocalDocumentAttachmentRow(
    filename: String,
    onOpen: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = "Document $filename, tap to open"
            }
            .background(PearlColors.controlFill(isPearlDarkTheme()))
            .clickable(onClick = onOpen)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Description, contentDescription = null, tint = TabTheme.Feed.primary)
        Text(
            text = filename,
            style = MaterialTheme.typography.bodyMedium,
            color = PearlColors.heroPrimary(isPearlDarkTheme()),
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
