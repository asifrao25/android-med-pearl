package com.knowledgepearls.app.ui.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil3.compose.SubcomposeAsyncImage

@Composable
fun ZoomableFullscreenImage(
    model: Any,
    modifier: Modifier = Modifier,
    onDismissProgress: (Float) -> Unit = {},
    onDismiss: () -> Unit = {},
    onZoomChanged: (Boolean) -> Unit = {},
) {
    ZoomableFullscreenSurface(
        key = model,
        modifier = modifier,
        onDismissProgress = onDismissProgress,
        onDismiss = onDismiss,
        onZoomChanged = onZoomChanged,
    ) { contentModifier ->
        SubcomposeAsyncImage(
            model = model,
            contentDescription = "Photo",
            modifier = Modifier
                .fillMaxSize()
                .then(contentModifier),
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
fun ZoomableFullscreenBitmap(
    bitmap: ImageBitmap,
    contentDescription: String,
    modifier: Modifier = Modifier,
    key: Any = bitmap,
    onDismissProgress: (Float) -> Unit = {},
    onDismiss: () -> Unit = {},
    onZoomChanged: (Boolean) -> Unit = {},
    enablePullToDismiss: Boolean = true,
) {
    ZoomableFullscreenSurface(
        key = key,
        modifier = modifier,
        onDismissProgress = onDismissProgress,
        onDismiss = onDismiss,
        onZoomChanged = onZoomChanged,
        enablePullToDismiss = enablePullToDismiss,
    ) { contentModifier ->
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .then(contentModifier),
            contentScale = ContentScale.Fit,
        )
    }
}
