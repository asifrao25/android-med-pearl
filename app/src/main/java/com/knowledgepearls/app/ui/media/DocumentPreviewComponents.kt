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
import androidx.compose.ui.graphics.Brush
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

@Composable
fun PearlDocumentPreviewCard(
    url: String,
    filename: String,
    theme: TabTheme,
    height: Dp,
    modifier: Modifier = Modifier,
    interactive: Boolean = false,
    onOpen: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    var thumb by remember(url, filename) { mutableStateOf<android.graphics.Bitmap?>(null) }
    var isLoading by remember(url, filename) { mutableStateOf(true) }

    LaunchedEffect(url, filename) {
        isLoading = true
        thumb = MediaThumbnailUtils.loadDocumentThumbnail(
            cacheDir = context.cacheDir,
            url = url,
            filename = filename,
        )
        isLoading = false
    }

    val clickModifier = if (interactive && onOpen != null) {
        Modifier.clickable(onClick = onOpen)
    } else {
        Modifier
    }

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
            .then(clickModifier),
    ) {
        thumb?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = filename,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } ?: DocumentPreviewPlaceholder(
            filename = filename,
            theme = theme,
            isLoading = isLoading,
            modifier = Modifier.fillMaxSize(),
        )

        if (interactive && onOpen != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = DocumentSupport.documentLabel(filename),
                    color = androidx.compose.ui.graphics.Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun DocumentPreviewPlaceholder(
    filename: String,
    theme: TabTheme,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(
            brush = Brush.linearGradient(
                listOf(theme.primary.copy(alpha = 0.42f), theme.secondary.copy(alpha = 0.18f)),
            ),
        ),
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
                tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.92f),
                modifier = Modifier.height(40.dp),
            )
            Text(
                text = DocumentSupport.documentLabel(filename),
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.92f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = filename,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            if (isLoading) {
                CircularProgressIndicator(
                    color = androidx.compose.ui.graphics.Color.White,
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
        Text(
            text = filename,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = PearlColors.heroPrimary(isPearlDarkTheme()),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
