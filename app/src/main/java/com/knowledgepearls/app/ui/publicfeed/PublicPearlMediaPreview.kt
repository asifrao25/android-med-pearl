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
import com.knowledgepearls.app.data.model.PublicPearlMediaUrls
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
            item.isPhoto || PublicPearlMediaUrls.isImageUrl(url) -> PublicPearlMediaSlide.Image(url)
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
) {
    val slides = publicPearlMediaSlides(pearl.resolvedMediaItems.filter { !it.isDocument })
    val documents = pearl.resolvedMediaItems.filter { it.isDocument }
    val darkTheme = isPearlDarkTheme()
    val openSlide: (PublicPearlMediaSlide) -> Unit = { slide ->
        val galleryIndex = slides.indexOfFirst { it.id == slide.id }.coerceAtLeast(0)
        onOpenMedia(
            PublicPearlMediaViewerRequest(
                slides = if (slide is PublicPearlMediaSlide.Document) {
                    listOf(slide)
                } else {
                    slides
                },
                initialIndex = if (slide is PublicPearlMediaSlide.Document) 0 else galleryIndex,
            ),
        )
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (slides.isNotEmpty()) {
            Text(
                text = if (slides.size > 1) "Attachments" else "Attachment",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PearlColors.heroPrimary(darkTheme),
            )
            if (slides.size > 1) {
                PublicPearlMediaCarousel(
                    slides = slides,
                    theme = theme,
                    height = 220.dp,
                    interactive = true,
                    onOpenAtIndex = { index ->
                        onOpenMedia(PublicPearlMediaViewerRequest(slides, index))
                    },
                )
            } else {
                PublicPearlMediaSlideView(
                    slide = slides.first(),
                    theme = theme,
                    height = 220.dp,
                    interactive = true,
                    onOpen = { openSlide(slides.first()) },
                )
            }
        } else if (pearl.resolvedLinkPreviewImageUrl != null) {
            Text(
                text = "Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PearlColors.heroPrimary(darkTheme),
            )
            PublicPearlImagePreview(
                url = pearl.resolvedLinkPreviewImageUrl!!,
                height = 220.dp,
                onClick = {
                    onOpenMedia(
                        PublicPearlMediaViewerRequest(
                            slides = listOf(PublicPearlMediaSlide.Image(pearl.resolvedLinkPreviewImageUrl!!)),
                        ),
                    )
                },
            )
        }

        documents.forEach { item ->
            item.loadableUrl?.let { url ->
                DocumentAttachmentRow(
                    filename = item.resolvedFilename,
                    url = url,
                    theme = theme,
                    onOpen = {
                        openSlide(PublicPearlMediaSlide.Document(url, item.resolvedFilename))
                    },
                )
            }
        }
    }
}

@Composable
private fun PublicPearlMediaCarousel(
    slides: List<PublicPearlMediaSlide>,
    theme: TabTheme,
    height: androidx.compose.ui.unit.Dp,
    interactive: Boolean,
    modifier: Modifier = Modifier,
    onOpenAtIndex: ((Int) -> Unit)? = null,
) {
    val pagerState = rememberPagerState(pageCount = { slides.size })

    Box(modifier = modifier.fillMaxWidth()) {
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
}

@Composable
private fun PublicPearlMediaSlideView(
    slide: PublicPearlMediaSlide,
    theme: TabTheme,
    height: androidx.compose.ui.unit.Dp,
    interactive: Boolean,
    modifier: Modifier = Modifier,
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
        is PublicPearlMediaSlide.Document -> DocumentPreviewCard(
            filename = slide.filename,
            theme = theme,
            height = height,
            modifier = modifier,
            interactive = interactive,
            onOpen = if (interactive) onOpen else null,
        )
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
private fun DocumentPreviewCard(
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
                    "Document $filename, tap to preview"
                } else {
                    "Document $filename"
                }
            }
            .background(PearlColors.controlFill(isPearlDarkTheme()))
            .then(if (interactive && onOpen != null) Modifier.clickable(onClick = onOpen) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Description, contentDescription = null, tint = theme.primary, modifier = Modifier.height(40.dp))
            Text(
                text = filename,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PearlColors.heroPrimary(isPearlDarkTheme()),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            if (interactive && onOpen != null) {
                Text(
                    text = "Tap to preview",
                    style = MaterialTheme.typography.labelSmall,
                    color = theme.primary,
                )
            }
        }
    }
}

@Composable
private fun DocumentAttachmentRow(
    filename: String,
    url: String,
    theme: TabTheme,
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
        Icon(Icons.Default.Description, contentDescription = null, tint = theme.primary)
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
