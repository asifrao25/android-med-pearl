package com.knowledgepearls.app.ui.media

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs

private const val MinScale = 1f
private const val MaxScale = 5f
private const val ZoomThreshold = 1.01f

@Composable
fun ZoomableFullscreenSurface(
    key: Any,
    modifier: Modifier = Modifier,
    onDismissProgress: (Float) -> Unit = {},
    onDismiss: () -> Unit = {},
    onZoomChanged: (Boolean) -> Unit = {},
    enablePullToDismiss: Boolean = true,
    content: @Composable BoxScope.(contentModifier: Modifier) -> Unit,
) {
    val scaleState = remember(key) { mutableFloatStateOf(MinScale) }
    val offsetState = remember(key) { mutableStateOf(Offset.Zero) }
    val dismissDragState = remember(key) { mutableFloatStateOf(0f) }
    val layoutSizeState = remember(key) { mutableStateOf(IntSize.Zero) }

    val scale by scaleState
    val offset by offsetState
    val dismissDrag by dismissDragState

    val animatedDismiss by animateFloatAsState(
        targetValue = dismissDrag,
        label = "dismissDrag",
    )

    fun clampOffset(raw: Offset, currentScale: Float, size: IntSize): Offset {
        if (size.width == 0 || size.height == 0 || currentScale <= ZoomThreshold) {
            return Offset.Zero
        }
        val maxX = size.width * (currentScale - 1f) / 2f
        val maxY = size.height * (currentScale - 1f) / 2f
        return Offset(
            x = raw.x.coerceIn(-maxX, maxX),
            y = raw.y.coerceIn(-maxY, maxY),
        )
    }

    fun setZoom(newScale: Float, focalPoint: Offset = Offset.Zero) {
        val size = layoutSizeState.value
        val clamped = newScale.coerceIn(MinScale, MaxScale)

        if (clamped <= ZoomThreshold) {
            scaleState.floatValue = MinScale
            offsetState.value = Offset.Zero
            dismissDragState.floatValue = 0f
            onDismissProgress(0f)
            onZoomChanged(false)
            return
        }

        val previousScale = scaleState.floatValue
        if (previousScale <= ZoomThreshold) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val anchor = if (focalPoint != Offset.Zero) focalPoint else center
            offsetState.value = clampOffset((center - anchor) * (clamped - 1f), clamped, size)
        } else if (abs(clamped - previousScale) > 0.001f) {
            val center = Offset(size.width / 2f, size.height / 2f)
            offsetState.value = clampOffset(
                (offsetState.value - center) * (clamped / previousScale) + center,
                clamped,
                size,
            )
        }

        scaleState.floatValue = clamped
        onZoomChanged(true)
    }

    val contentModifier = Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
        translationX = offset.x
        translationY = offset.y + animatedDismiss
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { layoutSizeState.value = it }
            .pointerInput(key) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val zoomChange = event.calculateZoom()
                        val panChange = event.calculatePan()
                        val currentScale = scaleState.floatValue

                        if (abs(zoomChange - 1f) > 0.001f) {
                            setZoom(currentScale * zoomChange)
                        }

                        if (scaleState.floatValue > ZoomThreshold) {
                            val size = layoutSizeState.value
                            offsetState.value = clampOffset(
                                offsetState.value + panChange,
                                scaleState.floatValue,
                                size,
                            )
                            dismissDragState.floatValue = 0f
                            onDismissProgress(0f)
                            onZoomChanged(true)
                            event.changes.forEach { change ->
                                if (change.positionChanged()) change.consume()
                            }
                        } else if (enablePullToDismiss && event.changes.size == 1) {
                            val verticalPan = panChange.y
                            if (verticalPan > 0f || dismissDragState.floatValue > 0f) {
                                dismissDragState.floatValue =
                                    (dismissDragState.floatValue + verticalPan).coerceAtLeast(0f)
                                onDismissProgress(
                                    (dismissDragState.floatValue / 220f).coerceIn(0f, 1f),
                                )
                                event.changes.forEach { it.consume() }
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    if (enablePullToDismiss && scaleState.floatValue <= ZoomThreshold) {
                        if (dismissDragState.floatValue > 120f) {
                            onDismiss()
                        } else {
                            dismissDragState.floatValue = 0f
                            onDismissProgress(0f)
                        }
                    }
                }
            }
            .pointerInput(key) {
                detectTapGestures(
                    onDoubleTap = { tapOffset ->
                        if (scaleState.floatValue > ZoomThreshold) {
                            setZoom(MinScale)
                        } else {
                            val size = layoutSizeState.value
                            if (size.width > 0 && size.height > 0) {
                                setZoom(2.5f, focalPoint = tapOffset)
                            }
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        content(contentModifier)
    }
}
