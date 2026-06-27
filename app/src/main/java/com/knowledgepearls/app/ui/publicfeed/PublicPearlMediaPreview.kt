package com.knowledgepearls.app.ui.publicfeed

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
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
    modifier: Modifier = Modifier,
) {
    val slides = publicPearlMediaSlides(pearl.resolvedMediaItems.filter { !it.isDocument })
    val documents = pearl.resolvedMediaItems.filter { it.isDocument }
    val darkTheme = isPearlDarkTheme()

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
                )
            } else {
                PublicPearlMediaSlideView(
                    slide = slides.first(),
                    theme = theme,
                    height = 220.dp,
                    interactive = true,
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
            )
        }

        documents.forEach { item ->
            item.loadableUrl?.let { url ->
                DocumentAttachmentRow(
                    filename = item.resolvedFilename,
                    url = url,
                    theme = theme,
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
) {
    val context = LocalContext.current
    when (slide) {
        is PublicPearlMediaSlide.Image -> PublicPearlImagePreview(
            url = slide.url,
            height = height,
            modifier = modifier,
            onClick = if (interactive) {
                { openExternalUrl(context, slide.url) }
            } else {
                null
            },
        )
        is PublicPearlMediaSlide.Video -> VideoPreviewCard(
            filename = slide.filename,
            url = slide.url,
            theme = theme,
            height = height,
            modifier = modifier,
            interactive = interactive,
            context = context,
        )
        is PublicPearlMediaSlide.Document -> DocumentPreviewCard(
            filename = slide.filename,
            url = slide.url,
            theme = theme,
            height = height,
            modifier = modifier,
            interactive = interactive,
            context = context,
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
        contentDescription = null,
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
    url: String,
    theme: TabTheme,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    interactive: Boolean,
    context: android.content.Context,
) {
    val open = { openExternalUrl(context, url) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(previewShape)
            .background(theme.primary.copy(alpha = 0.18f))
            .then(if (interactive) Modifier.clickable(onClick = open) else Modifier),
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
            if (interactive) {
                Text(
                    text = "Tap to open video",
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
    url: String,
    theme: TabTheme,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    interactive: Boolean,
    context: android.content.Context,
) {
    val open = { openExternalUrl(context, url) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(previewShape)
            .background(PearlColors.controlFill(isPearlDarkTheme()))
            .then(if (interactive) Modifier.clickable(onClick = open) else Modifier),
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
            if (interactive) {
                Text(
                    text = "Tap to open document",
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
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PearlColors.controlFill(isPearlDarkTheme()))
            .clickable { openExternalUrl(context, url) }
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
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(previewShape)
            .background(PearlColors.controlFill(isPearlDarkTheme()))
            .clickable { openExternalUrl(context, url) }
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

private fun openExternalUrl(context: android.content.Context, url: String) {
    runCatching {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
