package com.knowledgepearls.app.ui.capture

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
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.knowledgepearls.app.data.capture.PickedMedia
import com.knowledgepearls.app.data.local.entity.MediaType
import com.knowledgepearls.app.ui.media.MediaThumbnailUtils
import com.knowledgepearls.app.ui.media.PearlDocumentPreviewCard
import com.knowledgepearls.app.ui.media.cachePickedMedia
import com.knowledgepearls.app.ui.media.pickedMediaSlides
import com.knowledgepearls.app.ui.media.treatsAsDocument
import com.knowledgepearls.app.ui.media.treatsAsVideo
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerOverlay
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerRequest
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureAttachmentSection(
    attachments: MutableList<PickedMedia>,
    pickers: MediaPickers,
    accent: Color,
    theme: TabTheme = TabTheme.Feed,
) {
    val context = LocalContext.current
    var viewerRequest by remember { mutableStateOf<PublicPearlMediaViewerRequest?>(null) }
    var showSourcePicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Attachments", color = accent, fontWeight = FontWeight.SemiBold)

        CaptureAttachmentGrid(
            attachments = attachments,
            cacheDir = context.cacheDir,
            accent = accent,
            theme = theme,
            onAddClick = { showSourcePicker = true },
            onOpenViewer = { viewerRequest = it },
        )
    }

    if (showSourcePicker) {
        AttachmentSourcePickerSheet(
            onDismiss = { showSourcePicker = false },
            onCamera = {
                showSourcePicker = false
                pickers.takePhoto()
            },
            onGallery = {
                showSourcePicker = false
                pickers.pickGallery()
            },
            onFiles = {
                showSourcePicker = false
                pickers.pickDocument()
            },
            accent = accent,
        )
    }

    PublicPearlMediaViewerOverlay(
        request = viewerRequest,
        theme = theme,
        onDismiss = { viewerRequest = null },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AttachmentSourcePickerSheet(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onFiles: () -> Unit,
    accent: Color,
) {
    val darkTheme = isPearlDarkTheme()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PearlColors.popupSurface(darkTheme),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = "Add attachment",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                fontWeight = FontWeight.SemiBold,
                color = PearlColors.heroPrimary(darkTheme),
            )
            Text(
                text = "Choose how to add media to this section",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = PearlColors.heroSecondary(darkTheme),
            )
            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp),
                color = PearlColors.heroSecondary(darkTheme).copy(alpha = 0.15f),
            )
            AttachmentSourceOption(
                icon = Icons.Default.CameraAlt,
                label = "Take photo",
                subtitle = "Use the camera",
                accent = accent,
                onClick = onCamera,
            )
            AttachmentSourceOption(
                icon = Icons.Default.PhotoLibrary,
                label = "Photo library",
                subtitle = "Choose photos or videos",
                accent = accent,
                onClick = onGallery,
            )
            AttachmentSourceOption(
                icon = Icons.Default.Description,
                label = "Choose file",
                subtitle = "PDFs and other documents",
                accent = accent,
                onClick = onFiles,
            )
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text("Cancel", color = PearlColors.heroSecondary(darkTheme))
            }
        }
    }
}

@Composable
private fun AttachmentSourceOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                color = PearlColors.heroPrimary(darkTheme),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = PearlColors.heroSecondary(darkTheme),
            )
        }
    }
}

@Composable
private fun CaptureAttachmentGrid(
    attachments: MutableList<PickedMedia>,
    cacheDir: java.io.File,
    accent: Color,
    theme: TabTheme,
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
                                    theme = theme,
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
    theme: TabTheme,
    modifier: Modifier = Modifier,
    onRemove: () -> Unit,
    onOpen: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    val cachedUri = remember(item) { cachePickedMedia(item, cacheDir) }
    var imageThumb by remember(item) { mutableStateOf<ImageBitmap?>(null) }
    var videoThumb by remember(item) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(item) {
        if (item.type == MediaType.IMAGE) {
            imageThumb = withContext(Dispatchers.Default) {
                BitmapFactory.decodeByteArray(item.bytes, 0, item.bytes.size)?.asImageBitmap()
            }
        }
    }

    LaunchedEffect(item, cachedUri) {
        if (treatsAsVideo(item.type, item.filename)) {
            videoThumb = MediaThumbnailUtils.loadVideoThumbnail(File(java.net.URI(cachedUri)).absolutePath)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(PearlColors.controlFill(darkTheme)),
    ) {
        when {
            treatsAsDocument(item.type, item.filename, cachedUri) -> {
                PearlDocumentPreviewCard(
                    url = cachedUri,
                    filename = item.filename,
                    theme = theme,
                    modifier = Modifier.fillMaxSize(),
                    interactive = true,
                    onOpen = onOpen,
                )
            }
            item.type == MediaType.IMAGE && imageThumb != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onOpen),
                ) {
                    Image(
                        bitmap = imageThumb!!,
                        contentDescription = item.filename,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
            videoThumb != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onOpen),
                ) {
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
            }
            treatsAsVideo(item.type, item.filename) -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onOpen),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.PlayCircle,
                        null,
                        tint = TabTheme.Feed.primary,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onOpen),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(item.filename, color = PearlColors.heroSecondary(darkTheme))
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
