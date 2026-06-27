package com.knowledgepearls.app.ui.media

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import coil3.compose.SubcomposeAsyncImage

@Composable
fun ZoomableFullscreenImage(
    model: Any,
    modifier: Modifier = Modifier,
    onDismissProgress: (Float) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    var scale by remember(model) { mutableFloatStateOf(1f) }
    var offset by remember(model) { mutableStateOf(Offset.Zero) }
    var dismissDrag by remember(model) { mutableFloatStateOf(0f) }
    var layoutSize by remember(model) { mutableStateOf(IntSize.Zero) }

    val animatedDismiss by animateFloatAsState(
        targetValue = dismissDrag,
        label = "dismissDrag",
    )

    val isZoomed = scale > 1.01f

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        if (scale > 1.01f) {
            offset += panChange
            dismissDrag = 0f
            onDismissProgress(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { layoutSize = it }
            .pointerInput(model, isZoomed) {
                if (!isZoomed) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (dismissDrag > 120f) {
                                onDismiss()
                            } else {
                                dismissDrag = 0f
                                onDismissProgress(0f)
                            }
                        },
                        onVerticalDrag = { _, dragAmount ->
                            if (dragAmount > 0f || dismissDrag > 0f) {
                                dismissDrag = (dismissDrag + dragAmount).coerceAtLeast(0f)
                                onDismissProgress((dismissDrag / 220f).coerceIn(0f, 1f))
                            }
                        },
                    )
                }
            }
            .pointerInput(model, layoutSize, isZoomed) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        if (isZoomed) {
                            scale = 1f
                            offset = Offset.Zero
                            dismissDrag = 0f
                            onDismissProgress(0f)
                        } else if (layoutSize.width > 0 && layoutSize.height > 0) {
                            scale = 2f
                            val center = Offset(layoutSize.width / 2f, layoutSize.height / 2f)
                            offset = (center - tapOffset) * (scale - 1f)
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = model,
            contentDescription = "Photo",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y + animatedDismiss
                }
                .then(if (isZoomed) Modifier.transformable(state = transformState) else Modifier),
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
