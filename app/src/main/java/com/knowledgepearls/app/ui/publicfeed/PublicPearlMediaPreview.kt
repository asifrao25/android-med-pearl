package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.data.model.PublicPearlMediaItem
import com.knowledgepearls.app.ui.feed.LinkPearlPreviewSection
import com.knowledgepearls.app.ui.feed.PearlDetailMetrics
import com.knowledgepearls.app.ui.feed.parseOpenableUrl
import com.knowledgepearls.app.ui.media.DocumentAttachmentActions
import com.knowledgepearls.app.ui.media.PearlDocumentPreviewCard
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

private val previewHeight = 158.dp
private val previewShape = RoundedCornerShape(12.dp)

sealed interface PublicPearlMediaSlide {
    val id: String

    data class Image(val url: String) : PublicPearlMediaSlide {
        override val id: String = url
    }

    data class Video(val url: String, val filename: String) : PublicPearlMediaSlide {
        override val id: String = "$url-$filename"
    }

    data class Document(val url: String, val filename: String) : PublicPearlMediaSlide {
        override val id: String = "$url-$filename"
    }
}

fun publicPearlMediaSlides(items: List<PublicPearlMediaItem>): List<PublicPearlMediaSlide> =
    items.mapNotNull { item ->
        val url = item.loadableUrl ?: return@mapNotNull null
        when {
            item.isVideo -> PublicPearlMediaSlide.Video(url, item.resolvedFilename)
            item.isDocument -> PublicPearlMediaSlide.Document(url, item.resolvedFilename)
            else -> PublicPearlMediaSlide.Image(url)
        }
    }

fun publicPearlMediaSlides(pearl: PublicPearl): List<PublicPearlMediaSlide> =
    publicPearlMediaSlides(pearl.resolvedMediaItems)

@Composable
fun PublicPearlCardMediaPreview(
    pearl: PublicPearl,
    theme: TabTheme,
    modifier: Modifier = Modifier,
) {
    val slides = publicPearlMediaSlides(pearl)
    when {
        slides.size > 1 -> {
            PublicPearlMediaCarousel(
                slides = slides,
                theme = theme,
                height = previewHeight,
                interactive = false,
                modifier = modifier,
            )
        }
        slides.size == 1 -> {
            PublicPearlMediaSlideView(
                slide = slides.first(),
                theme = theme,
                height = previewHeight,
                interactive = false,
                modifier = modifier,
            )
        }
        pearl.resolvedLinkPreviewImageUrl != null -> {
            PublicPearlImagePreview(
                url = pearl.resolvedLinkPreviewImageUrl!!,
                height = previewHeight,
                modifier = modifier,
            )
        }
        pearl.isLinkPearl && !pearl.sourceUrl.isNullOrBlank() -> {
            LinkPreviewStrip(
                url = pearl.sourceUrl!!,
                theme = theme,
                modifier = modifier.height(previewHeight),
            )
        }
    }
}

@Composable
fun PublicPearlDetailMediaSection(
    pearl: PublicPearl,
    theme: TabTheme,
    onOpenMedia: (PublicPearlMediaViewerRequest) -> Unit,
    modifier: Modifier = Modifier,
    includeLinkTunnel: Boolean = true,
    onOpenUrl: (String) -> Unit = {},
) {
    val detailHeight = PearlDetailMetrics.openedMediaCarouselHeight
    val documents = pearl.resolvedMediaItems.filter { it.isDocument }
    val galleryItems = pearl.resolvedMediaItems.filter { !it.isDocument }
    val gallerySlides = publicPearlMediaSlides(galleryItems)
    val darkTheme = isPearlDarkTheme()
    val openSlide: (PublicPearlMediaSlide) -> Unit = { slide ->
        val index = gallerySlides.indexOfFirst { it.id == slide.id }.coerceAtLeast(0)
        onOpenMedia(PublicPearlMediaViewerRequest(gallerySlides, index))
    }

    val linkUrl = when {
        pearl.isFromTwitterScraper -> pearl.preferredPreviewUrl
        pearl.isLinkPearl -> pearl.sourceUrl?.trim()?.takeIf { it.isNotEmpty() }
        else -> null
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when {
            includeLinkTunnel && linkUrl != null -> {
                LinkPearlPreviewSection(
                    url = linkUrl,
                    theme = theme,
                    onOpenExternal = onOpenUrl,
                    onOpenBrowser = {
                        parseOpenableUrl(linkUrl)?.let(onOpenUrl)
                    },
                )
            }
            gallerySlides.isNotEmpty() -> {
                if (gallerySlides.size > 1) {
                    Text(
                        text = "Attachments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PearlColors.heroPrimary(darkTheme),
                    )
                    PublicPearlMediaCarousel(
                        slides = gallerySlides,
                        theme = theme,
                        height = detailHeight,
                        interactive = true,
                        onOpenAtIndex = { index ->
                            onOpenMedia(PublicPearlMediaViewerRequest(gallerySlides, index))
                        },
                    )
                } else {
                    PublicPearlMediaSlideView(
                        slide = gallerySlides.first(),
                        theme = theme,
                        height = detailHeight,
                        interactive = true,
                        onOpen = { openSlide(gallerySlides.first()) },
                    )
                }

                documents.forEach { document ->
                    val url = document.loadableUrl ?: return@forEach
                    val slide = PublicPearlMediaSlide.Document(url, document.resolvedFilename)
                    PublicPearlMediaSlideView(
                        slide = slide,
                        theme = theme,
                        height = detailHeight,
                        interactive = true,
                        onOpen = {
                            onOpenMedia(PublicPearlMediaViewerRequest(listOf(slide), 0))
                        },
                    )
                }
            }
            pearl.resolvedLinkPreviewImageUrl != null -> {
                PublicPearlImagePreview(
                    url = pearl.resolvedLinkPreviewImageUrl!!,
                    height = detailHeight,
                    onClick = {
                        onOpenMedia(
                            PublicPearlMediaViewerRequest(
                                slides = listOf(PublicPearlMediaSlide.Image(pearl.resolvedLinkPreviewImageUrl!!)),
                            ),
                        )
                    },
                )
            }
        }
    }
}

@Composable
internal fun PublicPearlMediaCarousel(
    slides: List<PublicPearlMediaSlide>,
    theme: TabTheme,
    height: androidx.compose.ui.unit.Dp,
    interactive: Boolean,
    modifier: Modifier = Modifier,
    onOpenAtIndex: ((Int) -> Unit)? = null,
) {
    val pagerState = rememberPagerState(pageCount = { slides.size })

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(previewShape),
                userScrollEnabled = interactive,
            ) { page ->
                PublicPearlMediaSlideView(
                    slide = slides[page],
                    theme = theme,
                    height = height,
                    interactive = interactive,
                    includeDocumentActions = false,
                    onOpen = if (interactive && onOpenAtIndex != null) {
                        { onOpenAtIndex(page) }
                    } else {
                        null
                    },
                )
            }

            if (slides.size > 1) {
                Text(
                    text = "${pagerState.currentPage + 1}/${slides.size}",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        if (interactive) {
            val currentSlide = slides.getOrNull(pagerState.currentPage)
            if (currentSlide is PublicPearlMediaSlide.Document) {
                DocumentAttachmentActions(
                    url = currentSlide.url,
                    filename = currentSlide.filename,
                    theme = theme,
                )
            }
        }
    }
}

@Composable
internal fun PublicPearlMediaSlideView(
    slide: PublicPearlMediaSlide,
    theme: TabTheme,
    height: androidx.compose.ui.unit.Dp,
    interactive: Boolean,
    modifier: Modifier = Modifier,
    includeDocumentActions: Boolean = true,
    onOpen: (() -> Unit)? = null,
) {
    when (slide) {
        is PublicPearlMediaSlide.Image -> PublicPearlImagePreview(
            url = slide.url,
            height = height,
            modifier = modifier,
            onClick = if (interactive) onOpen else null,
        )
        is PublicPearlMediaSlide.Video -> VideoPreviewCard(
            filename = slide.filename,
            theme = theme,
            height = height,
            modifier = modifier,
            interactive = interactive,
            onOpen = if (interactive) onOpen else null,
        )
        is PublicPearlMediaSlide.Document -> {
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PearlDocumentPreviewCard(
                    url = slide.url,
                    filename = slide.filename,
                    theme = theme,
                    height = height,
                    modifier = Modifier.fillMaxWidth(),
                    interactive = interactive,
                    onOpen = if (interactive) onOpen else null,
                )
                if (interactive && includeDocumentActions) {
                    DocumentAttachmentActions(
                        url = slide.url,
                        filename = slide.filename,
                        theme = theme,
                    )
                }
            }
        }
    }
}

@Composable
private fun PublicPearlImagePreview(
    url: String,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = if (onClick != null) "Photo, tap to open fullscreen" else "Photo",
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(previewShape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentScale = ContentScale.Crop,
        loading = {
            MediaPlaceholderBox(height = height, icon = Icons.Default.Photo, subtitle = null)
        },
        error = {
            MediaPlaceholderBox(height = height, icon = Icons.Default.Photo, subtitle = "Image unavailable")
        },
    )
}

@Composable
private fun VideoPreviewCard(
    filename: String,
    theme: TabTheme,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    interactive: Boolean,
    onOpen: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(previewShape)
            .semantics(mergeDescendants = true) {
                contentDescription = if (interactive && onOpen != null) {
                    "Video $filename, tap to play"
                } else {
                    "Video $filename"
                }
            }
            .background(theme.primary.copy(alpha = 0.18f))
            .then(if (interactive && onOpen != null) Modifier.clickable(onClick = onOpen) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = theme.primary, modifier = Modifier.height(48.dp))
            Text(
                text = filename,
                style = MaterialTheme.typography.bodyMedium,
                color = PearlColors.heroPrimary(isPearlDarkTheme()),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            if (interactive && onOpen != null) {
                Text(
                    text = "Tap to play",
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.primary,
                )
            }
        }
    }
}

@Composable
private fun LinkPreviewStrip(
    url: String,
    theme: TabTheme,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(previewShape)
            .background(PearlColors.controlFill(isPearlDarkTheme()))
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Source link",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = theme.primary,
            )
            Text(
                text = url,
                style = MaterialTheme.typography.bodySmall,
                color = PearlColors.heroSecondary(isPearlDarkTheme()),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MediaPlaceholderBox(
    height: androidx.compose.ui.unit.Dp,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    subtitle: String?,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(PearlColors.controlFill(isPearlDarkTheme())),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (subtitle == null) {
                CircularProgressIndicator(color = TabTheme.PublicFeed.primary)
            } else {
                Icon(icon, contentDescription = null, tint = PearlColors.heroSecondary(isPearlDarkTheme()))
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = PearlColors.heroSecondary(isPearlDarkTheme()))
            }
        }
    }
}
