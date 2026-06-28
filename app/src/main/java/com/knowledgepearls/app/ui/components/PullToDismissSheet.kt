package com.knowledgepearls.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.offset
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import kotlin.math.roundToInt

private const val PullDismissThresholdPx = 140f

@Composable
fun SheetDragHandle(modifier: Modifier = Modifier) {
    val darkTheme = isPearlDarkTheme()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .height(5.dp)
                .fillMaxWidth(0.12f)
                .clip(RoundedCornerShape(999.dp))
                .background(PearlColors.dragHandle(darkTheme)),
        )
    }
}

@Composable
fun PullToDismissSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState? = null,
    showDragHandle: Boolean = true,
    content: @Composable () -> Unit,
) {
    var dismissDrag by remember { mutableFloatStateOf(0f) }
    val animatedDrag by animateFloatAsState(dismissDrag, label = "pullToDismissDrag")
    val canPullDismiss = listState?.let { !it.canScrollBackward } ?: true

    Box(
        modifier = modifier
            .fillMaxSize()
            .offset { IntOffset(0, animatedDrag.roundToInt()) }
            .pointerInput(canPullDismiss) {
                if (!canPullDismiss) return@pointerInput
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dismissDrag > PullDismissThresholdPx) {
                            onDismiss()
                        } else {
                            dismissDrag = 0f
                        }
                    },
                    onDragCancel = { dismissDrag = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount > 0f || dismissDrag > 0f) {
                            dismissDrag = (dismissDrag + dragAmount).coerceAtLeast(0f)
                        }
                    },
                )
            },
    ) {
        Column(Modifier.fillMaxSize()) {
            if (showDragHandle) {
                SheetDragHandle()
            }
            Box(Modifier.weight(1f, fill = true)) {
                content()
            }
        }
    }
}
