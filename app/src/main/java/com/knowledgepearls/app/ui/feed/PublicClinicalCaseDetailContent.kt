package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.local.model.ClinicalCasePayload
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.data.model.PublicPearlMediaItem
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaCarousel
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaSlide
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaSlideView
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerRequest
import com.knowledgepearls.app.ui.publicfeed.publicPearlMediaSlides
import com.knowledgepearls.app.ui.theme.TabTheme

@Composable
fun PublicClinicalCaseDetailContent(
    pearl: PublicPearl,
    theme: TabTheme,
    onOpenMedia: (PublicPearlMediaViewerRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val payload = pearl.casePayload ?: ClinicalCasePayload()
    val mediaItems = pearl.caseSectionMediaItems

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        publicClinicalSection("History", payload.history, null, mediaItems, theme, onOpenMedia)
        publicClinicalSection("Examination", payload.examination, "examination", mediaItems, theme, onOpenMedia)
        publicClinicalSection("Investigation", payload.investigation, "investigation", mediaItems, theme, onOpenMedia)
        publicClinicalSection("Diagnosis", payload.diagnosis, null, mediaItems, theme, onOpenMedia)
        publicClinicalSection("Discussion", payload.discussion, "discussion", mediaItems, theme, onOpenMedia)
        if (payload.references.isNotBlank()) {
            publicClinicalSection("References", payload.references, null, mediaItems, theme, onOpenMedia, linkifyBody = true)
        }
    }
}

@Composable
private fun publicClinicalSection(
    title: String,
    body: String,
    sectionKey: String?,
    mediaItems: List<PublicPearlMediaItem>,
    theme: TabTheme,
    onOpenMedia: (PublicPearlMediaViewerRequest) -> Unit,
    linkifyBody: Boolean = false,
) {
    val trimmed = body.trim()
    val sectionMedia = sectionKey?.let { key ->
        mediaItems.filter { it.section.equals(key, ignoreCase = true) }
    }.orEmpty()
    val documents = sectionMedia.filter { it.isDocument }
    val galleryItems = sectionMedia.filter { !it.isDocument }
    val slides = publicPearlMediaSlides(galleryItems)

    if (trimmed.isEmpty() && sectionMedia.isEmpty()) return

    PearlDetailClinicalCaseSection(
        title = title,
        body = trimmed,
        theme = theme,
        linkifyBody = linkifyBody,
    ) {
        if (slides.isNotEmpty()) {
            if (slides.size > 1) {
                PublicPearlMediaCarousel(
                    slides = slides,
                    theme = theme,
                    height = PearlDetailMetrics.clinicalSectionMediaHeight,
                    interactive = true,
                    onOpenAtIndex = { index ->
                        onOpenMedia(PublicPearlMediaViewerRequest(slides, index))
                    },
                )
            } else {
                PublicPearlMediaSlideView(
                    slide = slides.first(),
                    theme = theme,
                    height = PearlDetailMetrics.clinicalSectionMediaHeight,
                    interactive = true,
                    onOpen = {
                        onOpenMedia(PublicPearlMediaViewerRequest(slides, 0))
                    },
                )
            }
        }

        documents.forEach { document ->
            val url = document.loadableUrl ?: return@forEach
            val slide = PublicPearlMediaSlide.Document(url, document.resolvedFilename)
            PublicPearlMediaSlideView(
                slide = slide,
                theme = theme,
                height = PearlDetailMetrics.clinicalSectionMediaHeight,
                interactive = true,
                onOpen = {
                    onOpenMedia(PublicPearlMediaViewerRequest(listOf(slide), 0))
                },
            )
        }
    }
}
