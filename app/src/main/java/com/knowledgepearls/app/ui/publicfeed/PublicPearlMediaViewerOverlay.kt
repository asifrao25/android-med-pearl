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
import androidx.compose.ui.graphics.Color
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
import coil3.compose.SubcomposeAsyncImage
import com.knowledgepearls.app.ui.theme.TabTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (val slide = request.slides[page]) {
                    is PublicPearlMediaSlide.Image -> FullscreenImage(url = slide.url)
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
            )
        }
    }
}

@Composable
private fun ViewerChrome(
    title: String,
    theme: TabTheme,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
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
private fun FullscreenImage(url: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            loading = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            },
            error = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Unable to load image", color = Color.White.copy(alpha = 0.8f))
                }
            },
        )
    }
}

@Composable
private fun FullscreenVideo(url: String, filename: String) {
    val context = LocalContext.current
    val exoPlayer = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            repeatMode = Player.REPEAT_MODE_OFF
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(url) {
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
    if (isPdf(filename, url)) {
        PdfDocumentViewer(url = url, filename = filename)
    } else {
        EmbeddedDocumentWebViewer(url = url, filename = filename)
    }
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
                val file = cacheRemoteFile(context.cacheDir, url, filename)
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
        error != null -> {
            EmbeddedDocumentWebViewer(url = url, filename = filename)
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
private fun EmbeddedDocumentWebViewer(url: String, filename: String) {
    val encodedUrl = remember(url) { URLEncoder.encode(url, Charsets.UTF_8.name()) }
    val viewerUrl = remember(url) {
        "https://docs.google.com/gviewer?embedded=true&url=$encodedUrl"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
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

private fun isPdf(filename: String, url: String): Boolean {
    val lowerName = filename.lowercase()
    val lowerUrl = url.lowercase()
    return lowerName.endsWith(".pdf") || lowerUrl.contains(".pdf")
}

private fun cacheRemoteFile(cacheDir: File, url: String, filename: String): File {
    val cacheRoot = File(cacheDir, "media_viewer").apply { mkdirs() }
    val digest = MessageDigest.getInstance("SHA-256")
        .digest(url.toByteArray())
        .joinToString("") { "%02x".format(it) }
    val extension = filename.substringAfterLast('.', "").takeIf { it.isNotBlank() } ?: "bin"
    val target = File(cacheRoot, "$digest.$extension")
    if (target.exists() && target.length() > 0L) return target

    URL(url).openStream().use { input ->
        target.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return target
}

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
