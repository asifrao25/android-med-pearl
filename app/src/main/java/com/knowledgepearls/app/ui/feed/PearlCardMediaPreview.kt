package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.knowledgepearls.app.data.local.entity.MediaType
import com.knowledgepearls.app.data.local.entity.PearlMediaEntity
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.ui.media.MediaThumbnailUtils
import com.knowledgepearls.app.ui.media.gallerySlides
import com.knowledgepearls.app.ui.media.localPearlMediaSlides
import com.knowledgepearls.app.ui.media.PearlDocumentPreviewCard
import com.knowledgepearls.app.ui.media.mediaUriForPath
import com.knowledgepearls.app.ui.media.pearlMediaViewerRequest
import com.knowledgepearls.app.ui.media.treatsAsDocument
import com.knowledgepearls.app.ui.media.treatsAsVideo
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaSlide
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
    val galleryItems = pearl.mediaItems.filter { item ->
        val path = item.localPath ?: return@filter false
        !treatsAsDocument(item.type, item.filename.ifBlank { File(path).name })
    }
    val documentItems = pearl.mediaItems.filter { item ->
        val path = item.localPath ?: return@filter false
        treatsAsDocument(item.type, item.filename.ifBlank { File(path).name })
    }

    when {
        galleryItems.size > 1 -> {
            PearlMediaCarousel(
                items = galleryItems,
                pearl = pearl,
                theme = theme,
                interactive = interactive,
                onOpenMedia = onOpenMedia,
                modifier = modifier,
            )
        }
        galleryItems.size == 1 -> {
            PearlMediaPreviewItem(
                item = galleryItems.first(),
                pearl = pearl,
                theme = theme,
                interactive = interactive,
                onOpenMedia = onOpenMedia,
                modifier = modifier,
            )
        }
        documentItems.size == 1 -> {
            PearlMediaPreviewItem(
                item = documentItems.first(),
                pearl = pearl,
                theme = theme,
                interactive = interactive,
                onOpenMedia = onOpenMedia,
                modifier = modifier,
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
private fun PearlMediaCarousel(
    items: List<PearlMediaEntity>,
    pearl: PearlWithMedia,
    theme: TabTheme,
    interactive: Boolean,
    onOpenMedia: ((PublicPearlMediaViewerRequest) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { items.size })
    val gallery = gallerySlides(pearl.mediaItems)

    Box(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(previewHeight)
                .clip(previewShape)
                .then(
                    if (interactive && onOpenMedia != null) {
                        Modifier.clickable {
                            onOpenMedia(PublicPearlMediaViewerRequest(gallery, pagerState.currentPage))
                        }
                    } else {
                        Modifier
                    },
                ),
            userScrollEnabled = false,
        ) { page ->
            PearlMediaPreviewItem(
                item = items[page],
                pearl = pearl,
                theme = theme,
                interactive = false,
                onOpenMedia = null,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Text(
            text = "${pagerState.currentPage + 1}/${items.size}",
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

@Composable
private fun PearlMediaPreviewItem(
    item: PearlMediaEntity,
    pearl: PearlWithMedia,
    theme: TabTheme,
    interactive: Boolean,
    onOpenMedia: ((PublicPearlMediaViewerRequest) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val path = item.localPath.orEmpty()
    val filename = item.filename.ifBlank { File(path).name }
    val openRequest = remember(item.id, pearl.mediaItems.size) {
        localPearlMediaSlides(listOf(item)).firstOrNull()?.let { slide ->
            pearlMediaViewerRequest(pearl, slide)
        }
    }
    val clickModifier = if (interactive && onOpenMedia != null && openRequest != null) {
        Modifier.clickable { onOpenMedia(openRequest) }
    } else {
        Modifier
    }

    when {
        treatsAsVideo(item.type, filename) -> VideoPreviewCard(
            path = path,
            filename = filename,
            theme = theme,
            modifier = modifier.height(previewHeight).then(clickModifier),
        )
        treatsAsDocument(item.type, filename) -> PearlDocumentPreviewCard(
            url = mediaUriForPath(path),
            filename = filename,
            theme = theme,
            height = previewHeight,
            modifier = modifier.then(clickModifier),
            interactive = interactive,
            onOpen = openRequest?.let { request -> { onOpenMedia?.invoke(request) } },
        )
        path.isNotBlank() -> PearlLocalImagePreview(
            path = path,
            interactive = interactive,
            onClick = openRequest?.let { request -> { onOpenMedia?.invoke(request) } },
            modifier = modifier.then(clickModifier),
        )
        else -> MediaPlaceholderBox(
            height = previewHeight,
            icon = Icons.Default.Photo,
            subtitle = "Image unavailable",
        )
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
            MediaPlaceholderBox(height = previewHeight, icon = Icons.Default.Photo, subtitle = null)
        },
        error = {
            MediaPlaceholderBox(height = previewHeight, icon = Icons.Default.Photo, subtitle = "Image unavailable")
        },
    )
}

@Composable
private fun VideoPreviewCard(
    path: String,
    filename: String,
    theme: TabTheme,
    modifier: Modifier = Modifier,
) {
    var thumb by remember(path) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(path) {
        if (path.isNotBlank()) thumb = MediaThumbnailUtils.loadVideoThumbnail(path)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(previewShape)
            .background(theme.primary.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        thumb?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = filename,
                modifier = Modifier.fillMaxWidth().height(previewHeight),
                contentScale = ContentScale.Crop,
            )
        }
        Icon(
            Icons.Default.PlayCircle,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = filename,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(10.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (subtitle == null) {
                CircularProgressIndicator(color = TabTheme.Feed.primary)
            } else {
                Icon(icon, contentDescription = null, tint = PearlColors.heroSecondary(isPearlDarkTheme()))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = PearlColors.heroSecondary(isPearlDarkTheme()),
                )
            }
        }
    }
}
