package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.ui.graphics.Color
import com.knowledgepearls.app.ui.media.DocumentDownloader
import com.knowledgepearls.app.ui.media.DocumentOpener
import com.knowledgepearls.app.ui.media.DocumentSupport
import com.knowledgepearls.app.ui.media.FullscreenExternalDocumentLauncher
import com.knowledgepearls.app.ui.media.FullscreenPdfDocumentViewer
import com.knowledgepearls.app.ui.media.ZoomableFullscreenImage
import com.knowledgepearls.app.ui.media.effectiveMediaFilename
import com.knowledgepearls.app.ui.theme.TabTheme
import java.io.File
import kotlinx.coroutines.launch

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
                        onDismissProgress = { dismissProgress = it },
                        onDismiss = onDismiss,
                        onZoomChanged = { isImageZoomed = it },
                    )
                }
            }

            val currentSlide = request.slides[pagerState.currentPage]
            ViewerChrome(
                title = viewerTitle(currentSlide, pagerState.currentPage, request.slides.size),
                theme = theme,
                onDismiss = onDismiss,
                documentSlide = currentSlide as? PublicPearlMediaSlide.Document,
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
    documentSlide: PublicPearlMediaSlide.Document? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDownloading by remember(documentSlide?.id) { mutableStateOf(false) }

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
        if (documentSlide != null) {
            IconButton(
                onClick = {
                    if (isDownloading) return@IconButton
                    scope.launch {
                        isDownloading = true
                        DocumentDownloader.download(
                            context = context,
                            cacheDir = context.cacheDir,
                            url = documentSlide.url,
                            filename = documentSlide.filename,
                        )
                        isDownloading = false
                    }
                },
                enabled = !isDownloading,
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Download",
                    tint = Color.White,
                )
            }
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
private fun FullscreenDocument(
    url: String,
    filename: String,
    onDismissProgress: (Float) -> Unit = {},
    onDismiss: () -> Unit = {},
    onZoomChanged: (Boolean) -> Unit = {},
) {
    val effectiveName = effectiveMediaFilename(filename, url)
    if (DocumentOpener.usesInAppPdfViewer(url, effectiveName)) {
        FullscreenPdfDocumentViewer(
            url = url,
            filename = effectiveName,
            onDismissProgress = onDismissProgress,
            onDismiss = onDismiss,
            onZoomChanged = onZoomChanged,
        )
    } else {
        FullscreenExternalDocumentLauncher(
            url = url,
            filename = effectiveName,
            onDismiss = onDismiss,
            autoLaunch = true,
        )
    }
}

private fun resolveMediaUri(url: String): String = when {
    url.startsWith("file:") || url.startsWith("content:") -> url
    !DocumentSupport.isRemoteUrl(url) -> File(url).toURI().toString()
    else -> url
}

private fun resolveMediaModel(url: String): Any =
    if (!DocumentSupport.isRemoteUrl(url) && !url.startsWith("file:")) File(url) else resolveMediaUri(url)
