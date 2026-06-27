package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.knowledgepearls.app.data.local.model.PearlWithMedia
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
import java.io.File

private val previewHeight = 158.dp
private val previewShape = RoundedCornerShape(12.dp)

@Composable
fun PearlCardMediaPreview(
    pearl: PearlWithMedia,
    theme: TabTheme,
    modifier: Modifier = Modifier,
    interactive: Boolean = false,
    onOpenMedia: ((PublicPearlMediaViewerRequest) -> Unit)? = null,
) {
    val entity = pearl.pearl
    val slides = localPearlMediaSlides(pearl.mediaItems)

    when {
        slides.size > 1 -> {
            PublicPearlMediaCarousel(
                slides = slides,
                theme = theme,
                height = previewHeight,
                interactive = interactive,
                modifier = modifier,
                onOpenAtIndex = if (interactive && onOpenMedia != null) {
                    { index -> onOpenMedia(PublicPearlMediaViewerRequest(slides, index)) }
                } else {
                    null
                },
            )
        }
        slides.size == 1 -> {
            val slide = slides.first()
            PublicPearlMediaSlideView(
                slide = slide,
                theme = theme,
                height = previewHeight,
                interactive = interactive,
                modifier = modifier,
                onOpen = if (interactive && onOpenMedia != null) {
                    { pearlMediaViewerRequest(pearl, slide)?.let(onOpenMedia) }
                } else {
                    null
                },
            )
        }
        !entity.linkPreviewImagePath.isNullOrBlank() -> {
            PearlLocalImagePreview(
                path = entity.linkPreviewImagePath!!,
                interactive = interactive,
                onClick = {
                    onOpenMedia?.invoke(
                        PublicPearlMediaViewerRequest(
                            slides = listOf(PublicPearlMediaSlide.Image(mediaUriForPath(entity.linkPreviewImagePath!!))),
                        ),
                    )
                },
                modifier = modifier,
            )
        }
        !entity.sourceURL.isNullOrBlank() -> {
            LinkPreviewStrip(
                url = entity.sourceURL!!,
                theme = theme,
                modifier = modifier.height(previewHeight),
            )
        }
    }
}

@Composable
private fun PearlLocalImagePreview(
    path: String,
    modifier: Modifier = Modifier,
    interactive: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    SubcomposeAsyncImage(
        model = File(path),
        contentDescription = if (interactive) "Photo, tap to open fullscreen" else "Photo",
        modifier = modifier
            .fillMaxWidth()
            .height(previewHeight)
            .clip(previewShape)
            .then(if (interactive && onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentScale = ContentScale.Crop,
        loading = {
            MediaPlaceholderBox(height = previewHeight, subtitle = null)
        },
        error = {
            MediaPlaceholderBox(height = previewHeight, subtitle = "Image unavailable")
        },
    )
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
        contentAlignment = androidx.compose.ui.Alignment.CenterStart,
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
    subtitle: String?,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(PearlColors.controlFill(isPearlDarkTheme())),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = PearlColors.heroSecondary(isPearlDarkTheme()))
        }
    }
}
