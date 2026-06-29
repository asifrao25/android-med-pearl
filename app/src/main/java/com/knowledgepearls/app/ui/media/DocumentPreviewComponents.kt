package com.knowledgepearls.app.ui.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

private val previewShape = RoundedCornerShape(12.dp)
private val documentPreviewBackground = Color(0xFFF4F4F5)
private val documentPreviewForeground = Color(0xFF1C1C1E)

@Composable
fun PearlDocumentPreviewCard(
    url: String,
    filename: String,
    theme: TabTheme,
    height: Dp? = null,
    modifier: Modifier = Modifier,
    interactive: Boolean = false,
    onOpen: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val effectiveName = effectiveMediaFilename(filename, url)
    val isPdf = DocumentSupport.isPdf(effectiveName, url)
    val isOffice = DocumentSupport.isOfficeDocument(effectiveName, url)
    val canRenderPreview = isPdf || isOffice
    var thumb by remember(url, effectiveName) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isLoading by remember(url, effectiveName) { mutableStateOf(canRenderPreview) }

    LaunchedEffect(url, effectiveName) {
        if (!canRenderPreview) {
            thumb = null
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        thumb = MediaThumbnailUtils.loadDocumentThumbnail(
            cacheDir = context.cacheDir,
            url = url,
            filename = effectiveName,
        )
        isLoading = false
    }

    val clickModifier = if (interactive && onOpen != null) {
        Modifier.clickable(onClick = onOpen)
    } else {
        Modifier
    }

    val cardBackground = when {
        isPdf -> Color.White
        isOffice -> documentPreviewBackground
        else -> documentPreviewBackground
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (height != null) Modifier.height(height) else Modifier.fillMaxSize())
            .clip(previewShape)
            .background(cardBackground)
            .semantics(mergeDescendants = true) {
                contentDescription = if (interactive && onOpen != null) {
                    "Document $effectiveName, tap to preview"
                } else {
                    "Document $effectiveName"
                }
            }
            .then(clickModifier),
    ) {
        when {
            thumb != null -> {
                Image(
                    bitmap = thumb!!.asImageBitmap(),
                    contentDescription = effectiveName,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentScale = ContentScale.Fit,
                )
            }
            isLoading -> {
                DocumentPreviewPlaceholder(
                    filename = effectiveName,
                    theme = theme,
                    isLoading = true,
                    isOffice = isOffice,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            else -> {
                OfficeDocumentPreviewContent(
                    filename = effectiveName,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        if (interactive && onOpen != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = DocumentSupport.documentLabel(effectiveName),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun OfficeDocumentPreviewContent(
    filename: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Default.Description,
            contentDescription = null,
            tint = documentPreviewForeground.copy(alpha = 0.82f),
            modifier = Modifier.height(44.dp),
        )
        Text(
            text = DocumentSupport.documentLabel(filename),
            color = documentPreviewForeground,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = filename,
            color = documentPreviewForeground.copy(alpha = 0.72f),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DocumentPreviewPlaceholder(
    filename: String,
    theme: TabTheme,
    isLoading: Boolean,
    isOffice: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(if (isOffice) documentPreviewBackground else Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                tint = if (isOffice) documentPreviewForeground.copy(alpha = 0.82f) else theme.primary,
                modifier = Modifier.height(40.dp),
            )
            Text(
                text = DocumentSupport.documentLabel(filename),
                color = if (isOffice) documentPreviewForeground else theme.primary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = filename,
                color = if (isOffice) documentPreviewForeground.copy(alpha = 0.72f) else PearlColors.heroSecondary(isPearlDarkTheme()),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            if (isLoading) {
                CircularProgressIndicator(
                    color = if (isOffice) documentPreviewForeground else theme.primary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.height(24.dp),
                )
            }
        }
    }
}

@Composable
fun PearlDocumentDetailPreview(
    url: String,
    filename: String,
    theme: TabTheme,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenPdfInApp: (com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerRequest) -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PearlDocumentPreviewCard(
            url = url,
            filename = filename,
            theme = theme,
            height = 220.dp,
            interactive = true,
            onOpen = onOpen,
        )
        DocumentAttachmentActions(
            url = url,
            filename = filename,
            theme = theme,
            showFilename = true,
            onOpenPdfInApp = onOpenPdfInApp,
        )
    }
}
