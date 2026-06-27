package com.knowledgepearls.app.ui.publicfeed

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import com.knowledgepearls.app.ui.media.DocumentFileResolver
import com.knowledgepearls.app.ui.media.DocumentSupport
import com.knowledgepearls.app.ui.media.ZoomableFullscreenImage
import com.knowledgepearls.app.ui.theme.TabTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class PublicPearlMediaViewerRequest(
    val slides: List<PublicPearlMediaSlide>,
    val initialIndex: Int = 0,
)

@Composable
fun PublicPearlMediaViewerOverlay(
    request: PublicPearlMediaViewerRequest?,
    theme: TabTheme,
    onDismiss: () -> Unit,
) {
    if (request == null || request.slides.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = request.initialIndex.coerceIn(0, request.slides.lastIndex),
        pageCount = { request.slides.size },
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        var dismissProgress by remember { mutableStateOf(0f) }
        var isImageZoomed by remember { mutableStateOf(false) }
        val scrimAlpha = 1f - dismissProgress * 0.55f

        LaunchedEffect(pagerState.currentPage) {
            isImageZoomed = false
            dismissProgress = 0f
        }

        val pagerScrollBlock = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    return if (isImageZoomed || dismissProgress > 0f) available else Offset.Zero
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = !isImageZoomed && dismissProgress <= 0f,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(pagerScrollBlock),
            ) { page ->
                when (val slide = request.slides[page]) {
                    is PublicPearlMediaSlide.Image -> ZoomableFullscreenImage(
                        model = resolveMediaModel(slide.url),
                        onDismissProgress = { dismissProgress = it },
                        onDismiss = onDismiss,
                        onZoomChanged = { isImageZoomed = it },
                    )
                    is PublicPearlMediaSlide.Video -> FullscreenVideo(url = slide.url, filename = slide.filename)
                    is PublicPearlMediaSlide.Document -> FullscreenDocument(
                        url = slide.url,
                        filename = slide.filename,
                    )
                }
            }

            ViewerChrome(
                title = viewerTitle(request.slides[pagerState.currentPage], pagerState.currentPage, request.slides.size),
                theme = theme,
                onDismiss = onDismiss,
                modifier = Modifier.graphicsLayer { alpha = scrimAlpha },
            )
        }
    }
}

@Composable
private fun ViewerChrome(
    title: String,
    theme: TabTheme,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun viewerTitle(slide: PublicPearlMediaSlide, index: Int, total: Int): String {
    val label = when (slide) {
        is PublicPearlMediaSlide.Image -> "Photo"
        is PublicPearlMediaSlide.Video -> slide.filename
        is PublicPearlMediaSlide.Document -> slide.filename
    }
    return if (total > 1) "${index + 1}/$total · $label" else label
}

@Composable
private fun FullscreenVideo(url: String, filename: String) {
    val context = LocalContext.current
    val mediaUri = remember(url) { resolveMediaUri(url) }
    val exoPlayer = remember(mediaUri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(mediaUri))
            repeatMode = Player.REPEAT_MODE_OFF
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(mediaUri) {
        onDispose { exoPlayer.release() }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
        Text(
            text = filename,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FullscreenDocument(url: String, filename: String) {
    when {
        DocumentSupport.isPdf(filename, url) -> PdfDocumentViewer(url = url, filename = filename)
        DocumentSupport.isOfficeDocument(filename) && DocumentSupport.isRemoteUrl(url) ->
            EmbeddedDocumentWebViewer(
                viewerUrl = DocumentSupport.officeEmbedUrl(url),
                filename = filename,
            )
        DocumentSupport.isOfficeDocument(filename) ->
            LocalOfficeDocumentViewer(url = url, filename = filename)
        DocumentSupport.isRemoteUrl(url) ->
            EmbeddedDocumentWebViewer(
                viewerUrl = DocumentSupport.googleViewerUrl(url),
                filename = filename,
            )
        else -> LocalOfficeDocumentViewer(url = url, filename = filename)
    }
}

@Composable
private fun LocalOfficeDocumentViewer(url: String, filename: String) {
    val context = LocalContext.current
    var thumb by remember(url, filename) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(url, filename) {
        thumb = com.knowledgepearls.app.ui.media.MediaThumbnailUtils.loadDocumentThumbnail(
            cacheDir = context.cacheDir,
            url = url,
            filename = filename,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 56.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            thumb?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = filename,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth,
                )
            }
            Text(
                text = filename,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "${DocumentSupport.documentLabel(filename)} · Tap below to open the full document",
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Open document",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.16f))
                    .clickable {
                        val file = DocumentFileResolver.resolveFile(context.cacheDir, url, filename)
                        openLocalDocument(context, file)
                    }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun LocalDocumentViewer(url: String, filename: String) {
    LocalOfficeDocumentViewer(url = url, filename = filename)
}

@Composable
private fun PdfDocumentViewer(url: String, filename: String) {
    val context = LocalContext.current
    var pages by remember(url) { mutableStateOf<List<Bitmap>?>(null) }
    var error by remember(url) { mutableStateOf<String?>(null) }

    LaunchedEffect(url) {
        pages = null
        error = null
        withContext(Dispatchers.IO) {
            runCatching {
                val file = DocumentFileResolver.resolveFile(context.cacheDir, url, filename)
                renderPdfPages(file)
            }.onSuccess { rendered ->
                pages = rendered
            }.onFailure { throwable ->
                error = throwable.message ?: "Unable to open PDF"
            }
        }
    }

    when {
        pages != null -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 56.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(pages!!) { index, bitmap ->
                    androidx.compose.foundation.Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Page ${index + 1}",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth,
                    )
                }
            }
        }
        error != null && DocumentSupport.isRemoteUrl(url) && !DocumentSupport.isPdf(filename, url) -> {
            EmbeddedDocumentWebViewer(
                viewerUrl = if (DocumentSupport.isOfficeDocument(filename)) {
                    DocumentSupport.officeEmbedUrl(url)
                } else {
                    DocumentSupport.googleViewerUrl(url)
                },
                filename = filename,
            )
        }
        error != null -> {
            LocalOfficeDocumentViewer(url = url, filename = filename)
        }
        else -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 56.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(color = Color.White)
                    Text("Loading $filename", color = Color.White.copy(alpha = 0.85f))
                }
            }
        }
    }
}

@Composable
private fun EmbeddedDocumentWebViewer(viewerUrl: String, filename: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    webViewClient = WebViewClient()
                    loadUrl(viewerUrl)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 56.dp),
            update = { webView ->
                if (webView.url != viewerUrl) {
                    webView.loadUrl(viewerUrl)
                }
            },
        )
        Text(
            text = filename,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun openLocalDocument(context: android.content.Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
    val mime = DocumentSupport.mimeType(file.name)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mime)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Open document"))
}

private fun resolveMediaUri(url: String): String = when {
    url.startsWith("file:") || url.startsWith("content:") -> url
    !DocumentSupport.isRemoteUrl(url) -> File(url).toURI().toString()
    else -> url
}

private fun resolveMediaModel(url: String): Any =
    if (!DocumentSupport.isRemoteUrl(url) && !url.startsWith("file:")) File(url) else resolveMediaUri(url)

private fun renderPdfPages(file: File): List<Bitmap> {
    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
        PdfRenderer(descriptor).use { renderer ->
            return (0 until renderer.pageCount).map { index ->
                renderer.openPage(index).use { page ->
                    val width = page.width * 2
                    val height = page.height * 2
                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    }
                }
            }
        }
    }
}
