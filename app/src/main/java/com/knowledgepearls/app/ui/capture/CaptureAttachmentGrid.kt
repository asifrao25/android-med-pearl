package com.knowledgepearls.app.ui.capture

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.knowledgepearls.app.data.capture.PickedMedia
import com.knowledgepearls.app.data.local.entity.MediaType
import com.knowledgepearls.app.ui.media.MediaThumbnailUtils
import com.knowledgepearls.app.ui.media.cachePickedMedia
import com.knowledgepearls.app.ui.media.pickedMediaSlides
import com.knowledgepearls.app.ui.media.treatsAsDocument
import com.knowledgepearls.app.ui.media.treatsAsVideo
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerOverlay
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerRequest
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun CaptureAttachmentSection(
    attachments: MutableList<PickedMedia>,
    pickers: MediaPickers,
    accent: Color,
    theme: TabTheme = TabTheme.Feed,
) {
    val context = LocalContext.current
    var viewerRequest by remember { mutableStateOf<PublicPearlMediaViewerRequest?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Attachments", color = accent, fontWeight = FontWeight.SemiBold)

        CaptureAttachmentGrid(
            attachments = attachments,
            cacheDir = context.cacheDir,
            accent = accent,
            onAddClick = pickers.pickGallery,
            onOpenViewer = { viewerRequest = it },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            androidx.compose.material3.TextButton(onClick = pickers.pickGallery) { Text("Gallery") }
            androidx.compose.material3.TextButton(onClick = pickers.takePhoto) { Text("Camera") }
            androidx.compose.material3.TextButton(onClick = pickers.pickDocument) { Text("Files") }
        }
    }

    PublicPearlMediaViewerOverlay(
        request = viewerRequest,
        theme = theme,
        onDismiss = { viewerRequest = null },
    )
}

@Composable
private fun CaptureAttachmentGrid(
    attachments: MutableList<PickedMedia>,
    cacheDir: java.io.File,
    accent: Color,
    onAddClick: () -> Unit,
    onOpenViewer: (PublicPearlMediaViewerRequest) -> Unit,
) {
    val cellCount = attachments.size + 1
    val rowCount = (cellCount + 2) / 3

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(rowCount) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(3) { columnIndex ->
                    val index = rowIndex * 3 + columnIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                    ) {
                        when {
                            index < attachments.size -> {
                                val item = attachments[index]
                                CaptureAttachmentTile(
                                    item = item,
                                    cacheDir = cacheDir,
                                    modifier = Modifier.fillMaxSize(),
                                    onRemove = { attachments.removeAt(index) },
                                    onOpen = {
                                        val slides = pickedMediaSlides(attachments, cacheDir)
                                        onOpenViewer(
                                            PublicPearlMediaViewerRequest(
                                                slides,
                                                index.coerceIn(0, slides.lastIndex.coerceAtLeast(0)),
                                            ),
                                        )
                                    },
                                )
                            }
                            index == attachments.size -> {
                                CaptureAddAttachmentTile(
                                    onClick = onAddClick,
                                    accent = accent,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptureAttachmentTile(
    item: PickedMedia,
    cacheDir: java.io.File,
    modifier: Modifier = Modifier,
    onRemove: () -> Unit,
    onOpen: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    val cachedUri = remember(item) { cachePickedMedia(item, cacheDir) }
    var imageThumb by remember(item) { mutableStateOf<ImageBitmap?>(null) }
    var videoThumb by remember(item) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var pdfThumb by remember(item) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(item) {
        if (item.type == MediaType.IMAGE) {
            imageThumb = withContext(Dispatchers.Default) {
                BitmapFactory.decodeByteArray(item.bytes, 0, item.bytes.size)?.asImageBitmap()
            }
        }
    }

    LaunchedEffect(item, cachedUri) {
        when {
            treatsAsVideo(item.type, item.filename) -> {
                videoThumb = MediaThumbnailUtils.loadVideoThumbnail(java.io.File(java.net.URI(cachedUri)).absolutePath)
            }
            treatsAsDocument(item.type, item.filename) && item.type == MediaType.PDF -> {
                pdfThumb = MediaThumbnailUtils.loadPdfThumbnail(java.io.File(java.net.URI(cachedUri)).absolutePath)
            }
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PearlColors.controlFill(darkTheme))
            .clickable(onClick = onOpen),
    ) {
        when {
            item.type == MediaType.IMAGE -> {
                imageThumb?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = item.filename,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
            videoThumb != null -> {
                Image(
                    bitmap = videoThumb!!.asImageBitmap(),
                    contentDescription = item.filename,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Icon(
                    Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(36.dp),
                )
            }
            pdfThumb != null -> {
                Image(
                    bitmap = pdfThumb!!.asImageBitmap(),
                    contentDescription = item.filename,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            treatsAsVideo(item.type, item.filename) -> {
                Icon(Icons.Default.PlayCircle, null, tint = TabTheme.Feed.primary, modifier = Modifier.align(Alignment.Center).size(36.dp))
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = TabTheme.Feed.primary)
                    Text(
                        item.filename,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = PearlColors.heroSecondary(darkTheme),
                    )
                }
            }
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp),
        ) {
            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White)
        }
    }
}

@Composable
private fun CaptureAddAttachmentTile(
    onClick: () -> Unit,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.12f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Default.Add, contentDescription = "Add attachment", tint = accent)
            Text("Add", color = accent, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}
