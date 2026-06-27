package com.knowledgepearls.app.ui.media

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.knowledgepearls.app.ui.theme.TabTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FullscreenPdfDocumentViewer(
    url: String,
    filename: String,
    onDismissProgress: (Float) -> Unit = {},
    onDismiss: () -> Unit = {},
    onZoomChanged: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val effectiveName = effectiveMediaFilename(filename, url)
    var pageCount by remember(url) { mutableIntStateOf(0) }
    var error by remember(url) { mutableStateOf<String?>(null) }
    var isPageZoomed by remember(url) { mutableStateOf(false) }

    LaunchedEffect(url) {
        pageCount = 0
        error = null
        isPageZoomed = false
        onZoomChanged(false)
        withContext(Dispatchers.IO) {
            runCatching {
                val file = DocumentFileResolver.resolveFile(context.cacheDir, url, effectiveName)
                PdfBitmapUtils.pageCount(file)
            }.onSuccess { count ->
                if (count <= 0) {
                    error = "Unable to open PDF"
                } else {
                    pageCount = count
                }
            }.onFailure { throwable ->
                error = throwable.message ?: "Unable to open PDF"
            }
        }
    }

    when {
        pageCount > 0 -> {
            val pagerState = rememberPagerState(pageCount = { pageCount })

            LaunchedEffect(pagerState.currentPage) {
                isPageZoomed = false
                onZoomChanged(false)
            }

            VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 56.dp),
                userScrollEnabled = !isPageZoomed,
            ) { pageIndex ->
                PdfPageZoomableView(
                    url = url,
                    filename = effectiveName,
                    pageIndex = pageIndex,
                    onDismissProgress = onDismissProgress,
                    onDismiss = onDismiss,
                    onZoomChanged = { zoomed ->
                        isPageZoomed = zoomed
                        onZoomChanged(zoomed)
                    },
                    enablePullToDismiss = pageIndex == 0,
                )
            }
        }
        error != null -> {
            FullscreenOfficeDocumentViewer(
                url = url,
                filename = effectiveName,
                onDismissProgress = onDismissProgress,
                onDismiss = onDismiss,
                onZoomChanged = onZoomChanged,
            )
        }
        else -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 56.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Text("Loading $effectiveName", color = Color.White.copy(alpha = 0.85f))
                }
            }
        }
    }
}

@Composable
private fun PdfPageZoomableView(
    url: String,
    filename: String,
    pageIndex: Int,
    onDismissProgress: (Float) -> Unit,
    onDismiss: () -> Unit,
    onZoomChanged: (Boolean) -> Unit,
    enablePullToDismiss: Boolean,
) {
    val context = LocalContext.current
    var bitmap by remember(url, pageIndex) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(url, pageIndex) {
        bitmap = null
        withContext(Dispatchers.IO) {
            runCatching {
                val file = DocumentFileResolver.resolveFile(context.cacheDir, url, filename)
                PdfBitmapUtils.renderPageAt(file, pageIndex)
            }.getOrNull()?.let { rendered ->
                bitmap = rendered
            }
        }
    }

    when (val pageBitmap = bitmap) {
        null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        else -> {
            ZoomableFullscreenBitmap(
                bitmap = pageBitmap.asImageBitmap(),
                contentDescription = "Page ${pageIndex + 1}",
                key = "$url-$pageIndex",
                onDismissProgress = onDismissProgress,
                onDismiss = onDismiss,
                onZoomChanged = onZoomChanged,
                enablePullToDismiss = enablePullToDismiss,
            )
        }
    }
}

@Composable
fun FullscreenOfficeDocumentViewer(
    url: String,
    filename: String,
    modifier: Modifier = Modifier,
    onDismissProgress: (Float) -> Unit = {},
    onDismiss: () -> Unit = {},
    onZoomChanged: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val effectiveName = effectiveMediaFilename(filename, url)
    var content by remember(url, effectiveName) { mutableStateOf<OfficeDocumentContent?>(null) }
    var isLoading by remember(url, effectiveName) { mutableStateOf(true) }
    var isImageZoomed by remember(url) { mutableStateOf(false) }

    LaunchedEffect(url, effectiveName) {
        isLoading = true
        content = null
        isImageZoomed = false
        onZoomChanged(false)
        content = withContext(Dispatchers.IO) {
            val file = DocumentFileResolver.resolveFile(context.cacheDir, url, effectiveName)
            OfficeDocumentContentLoader.load(file, effectiveName)
        }
        isLoading = false
    }

    when {
        isLoading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(top = 56.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Text("Loading $effectiveName", color = Color.White.copy(alpha = 0.85f))
                }
            }
        }
        content is OfficeDocumentContent.Image -> {
            ZoomableFullscreenBitmap(
                bitmap = (content as OfficeDocumentContent.Image).bitmap.asImageBitmap(),
                contentDescription = effectiveName,
                key = url,
                modifier = modifier.padding(top = 56.dp),
                onDismissProgress = onDismissProgress,
                onDismiss = onDismiss,
                onZoomChanged = { zoomed ->
                    isImageZoomed = zoomed
                    onZoomChanged(zoomed)
                },
            )
        }
        content is OfficeDocumentContent.Html -> {
            DocumentHtmlWebView(
                html = (content as OfficeDocumentContent.Html).html,
                modifier = modifier
                    .fillMaxSize()
                    .padding(top = 56.dp),
            )
        }
        content is OfficeDocumentContent.Slides -> {
            val slides = (content as OfficeDocumentContent.Slides).slides
            val pagerState = rememberPagerState(pageCount = { slides.size })
            VerticalPager(
                state = pagerState,
                modifier = modifier
                    .fillMaxSize()
                    .padding(top = 56.dp),
            ) { pageIndex ->
                DocumentHtmlWebView(
                    html = slides[pageIndex],
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        else -> {
            FullscreenOfficeDocumentFallback(
                url = url,
                filename = effectiveName,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun DocumentHtmlWebView(
    html: String,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                setBackgroundColor(android.graphics.Color.WHITE)
                webViewClient = WebViewClient()
                loadDataWithBaseURL(null, html, "text/html", Charsets.UTF_8.name(), null)
            }
        },
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        update = { webView ->
            webView.loadDataWithBaseURL(null, html, "text/html", Charsets.UTF_8.name(), null)
        },
        onRelease = { webView ->
            webView.destroy()
        },
    )
}

@Composable
private fun FullscreenOfficeDocumentFallback(
    url: String,
    filename: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val label = DocumentSupport.documentLabel(filename)

    fun openDocument() {
        val file = DocumentFileResolver.resolveFile(context.cacheDir, url, filename)
        DocumentPresenter.openLocalFile(context, file)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 56.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF4F4F5)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Preview unavailable for this file type.\nOpen it in $label to view the full document.",
                color = Color(0xFF1C1C1E),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(24.dp),
            )
        }

        Text(
            text = filename,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Text(
            text = "Open document",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.16f))
                .clickable(onClick = ::openDocument)
                .padding(horizontal = 22.dp, vertical = 14.dp),
        )
    }
}
