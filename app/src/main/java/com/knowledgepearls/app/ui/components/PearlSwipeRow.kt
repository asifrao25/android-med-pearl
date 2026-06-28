package com.knowledgepearls.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.knowledgepearls.app.ui.theme.PearlLayout
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

data class PearlSwipeAction(
    val icon: ImageVector,
    val title: String,
    val color: Color,
    val onClick: () -> Unit,
)

object SwipeRowLayout {
    val actionWidth = 72.dp
    val cornerRadius = PearlLayout.cardCornerRadius
    val capWidth = actionWidth + cornerRadius

    private val cardMaskColor = Color(0xFF0A0D17)

    fun maxSwipeOffsetPx(density: androidx.compose.ui.unit.Density): Float =
        with(density) { actionWidth.toPx() }

    fun capWidthPx(density: androidx.compose.ui.unit.Density): Float =
        with(density) { capWidth.toPx() }

    fun cornerRadiusPx(density: androidx.compose.ui.unit.Density): Float =
        with(density) { cornerRadius.toPx() }

    fun clampedOffsetPx(offsetPx: Float, density: androidx.compose.ui.unit.Density): Float {
        val max = maxSwipeOffsetPx(density)
        return offsetPx.coerceIn(-max, max)
    }

    fun capClipWidthPx(offsetPx: Float, density: androidx.compose.ui.unit.Density): Float {
        val amount = abs(offsetPx)
        if (amount <= 1f) return 0f
        val action = maxSwipeOffsetPx(density)
        val cap = capWidthPx(density)
        return if (amount < action) amount else cap
    }

    fun showsCornerWrap(offsetPx: Float, density: androidx.compose.ui.unit.Density): Boolean {
        val threshold = maxSwipeOffsetPx(density) * 0.35f
        return abs(offsetPx) >= threshold
    }

    fun snapOffsetPx(totalPx: Float, startingOffsetPx: Float, density: androidx.compose.ui.unit.Density): Float {
        val max = maxSwipeOffsetPx(density)
        return when {
            startingOffsetPx < -max * 0.5f -> if (totalPx > -max * 0.35f) 0f else -max
            startingOffsetPx > max * 0.5f -> if (totalPx < max * 0.35f) 0f else max
            totalPx < -max * 0.35f -> -max
            totalPx > max * 0.35f -> max
            else -> 0f
        }
    }

    val cardMaskColorValue: Color get() = cardMaskColor

    val settleSpring = spring<Float>(
        dampingRatio = 0.82f,
        stiffness = Spring.StiffnessMedium,
    )
}

@Composable
fun PearlSwipeRow(
    leadingAction: PearlSwipeAction?,
    trailingAction: PearlSwipeAction?,
    modifier: Modifier = Modifier,
    enableSwipeHint: Boolean = false,
    onSwipeHintDismiss: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val settledOffset = remember { Animatable(0f) }
    val hintOffset = remember { Animatable(0f) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isHorizontalSwipe by remember { mutableStateOf(false) }
    var suppressHint by remember { mutableStateOf(false) }
    var didReportSwipe by remember { mutableStateOf(false) }

    val hintActive = enableSwipeHint && !suppressHint
    val cornerRadiusPx = SwipeRowLayout.cornerRadiusPx(density)
    val cardMaskShape = remember(cornerRadiusPx) { SwipeRowCardMaskShape(cornerRadiusPx) }
    val actionWidthPx = SwipeRowLayout.maxSwipeOffsetPx(density)

    val displayedOffsetPx = SwipeRowLayout.clampedOffsetPx(
        settledOffset.value + dragOffset + if (
            isHorizontalSwipe || settledOffset.value != 0f || !hintActive
        ) {
            0f
        } else {
            hintOffset.value
        },
        density,
    )
    val showCornerWrap = SwipeRowLayout.showsCornerWrap(displayedOffsetPx, density)

    LaunchedEffect(enableSwipeHint) {
        if (!enableSwipeHint) {
            hintOffset.snapTo(0f)
            return@LaunchedEffect
        }
        SwipeRowHintAnimation.run(
            hintOffset = hintOffset,
            actionWidthPx = actionWidthPx,
        )
    }

    fun registerUserSwipe() {
        if (didReportSwipe) return
        didReportSwipe = true
        suppressHint = true
        scope.launch {
            hintOffset.snapTo(0f)
        }
        onSwipeHintDismiss()
    }

    fun close() {
        scope.launch {
            settledOffset.animateTo(0f, SwipeRowLayout.settleSpring)
        }
        dragOffset = 0f
    }

    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(Modifier.matchParentSize()) {
            if (leadingAction != null && displayedOffsetPx > 1f) {
                SwipeActionRevealColumn(
                    edge = SwipeActionEdge.Leading,
                    offsetPx = displayedOffsetPx,
                    action = leadingAction,
                    cornerRadiusPx = cornerRadiusPx,
                    onActionClick = { click ->
                        close()
                        click()
                    },
                    modifier = Modifier.align(Alignment.CenterStart),
                )
            }

            if (trailingAction != null && displayedOffsetPx < -1f) {
                SwipeActionRevealColumn(
                    edge = SwipeActionEdge.Trailing,
                    offsetPx = displayedOffsetPx,
                    action = trailingAction,
                    cornerRadiusPx = cornerRadiusPx,
                    onActionClick = { click ->
                        close()
                        click()
                    },
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(displayedOffsetPx.roundToInt(), 0) }
                .pointerInput(leadingAction, trailingAction) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var dragStartSettled = settledOffset.value
                        var totalDragX = 0f
                        var totalDragY = 0f
                        isHorizontalSwipe = dragStartSettled != 0f
                        if (dragStartSettled != 0f) {
                            registerUserSwipe()
                        }

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) {
                                if (isHorizontalSwipe) {
                                    val snapped = SwipeRowLayout.snapOffsetPx(
                                        totalPx = dragStartSettled + totalDragX,
                                        startingOffsetPx = dragStartSettled,
                                        density = density,
                                    )
                                    scope.launch {
                                        settledOffset.snapTo(dragStartSettled + dragOffset)
                                        dragOffset = 0f
                                        settledOffset.animateTo(snapped, SwipeRowLayout.settleSpring)
                                        if (snapped != 0f) {
                                            registerUserSwipe()
                                        }
                                    }
                                } else {
                                    dragOffset = 0f
                                }
                                isHorizontalSwipe = false
                                break
                            }

                            val delta = change.positionChange()
                            if (!change.isConsumed) {
                                totalDragX += delta.x
                                totalDragY += delta.y
                            }

                            val dx = abs(totalDragX)
                            val dy = abs(totalDragY)

                            if (!isHorizontalSwipe) {
                                if (dx <= 8f) continue
                                if (dy >= dx) continue
                                isHorizontalSwipe = true
                                registerUserSwipe()
                            }

                            if (isHorizontalSwipe) {
                                val proposed = dragStartSettled + totalDragX
                                dragOffset = SwipeRowLayout.clampedOffsetPx(proposed, density) - dragStartSettled
                                change.consume()
                            }
                        }
                    }
                },
        ) {
            if (showCornerWrap) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(SwipeRowLayout.cardMaskColorValue, cardMaskShape),
                )
            }

            Box(Modifier.fillMaxWidth()) {
                content()
            }

            if (showCornerWrap && displayedOffsetPx > 0f && leadingAction != null) {
                SwipeActionCornerWrap(
                    edge = SwipeActionEdge.Leading,
                    color = leadingAction.color,
                    cornerRadiusPx = cornerRadiusPx,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = -SwipeRowLayout.cornerRadius)
                        .zIndex(1f),
                )
            }
            if (showCornerWrap && displayedOffsetPx < 0f && trailingAction != null) {
                SwipeActionCornerWrap(
                    edge = SwipeActionEdge.Trailing,
                    color = trailingAction.color,
                    cornerRadiusPx = cornerRadiusPx,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = SwipeRowLayout.cornerRadius)
                        .zIndex(1f),
                )
            }
        }
    }
}

@Composable
private fun SwipeActionRevealColumn(
    edge: SwipeActionEdge,
    offsetPx: Float,
    action: PearlSwipeAction,
    cornerRadiusPx: Float,
    onActionClick: (() -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val clipWidthPx = SwipeRowLayout.capClipWidthPx(offsetPx, density)
    if (clipWidthPx <= 1f) return

    val clipWidth = with(density) { clipWidthPx.toDp() }
    val capWidth = SwipeRowLayout.capWidth
    val backgroundShape = remember(edge, cornerRadiusPx) {
        SwipeActionBackgroundShape(edge, cornerRadiusPx)
    }

    Box(
        modifier = modifier
            .width(clipWidth)
            .fillMaxHeight(),
        contentAlignment = if (edge == SwipeActionEdge.Leading) Alignment.CenterStart else Alignment.CenterEnd,
    ) {
        Box(
            modifier = Modifier
                .width(capWidth)
                .fillMaxHeight()
                .clip(backgroundShape)
                .background(action.color)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onActionClick(action.onClick) },
                )
                .semantics { contentDescription = action.title },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .width(SwipeRowLayout.actionWidth)
                    .padding(horizontal = 4.dp, vertical = 8.dp),
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = Color.White,
                )
                Text(
                    text = action.title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun SwipeActionCornerWrap(
    edge: SwipeActionEdge,
    color: Color,
    cornerRadiusPx: Float,
    modifier: Modifier = Modifier,
) {
    val cornerSize = with(LocalDensity.current) { cornerRadiusPx.toDp() }
    Canvas(
        modifier = modifier
            .width(cornerSize)
            .fillMaxHeight(),
    ) {
        val cornerSizePx = cornerRadiusPx
        val topSize = Size(cornerSizePx, cornerSizePx)
        drawPath(
            path = swipeActionInnerCornerPath(
                edge = edge,
                isTop = true,
                size = topSize,
                cornerRadiusPx = cornerRadiusPx,
            ),
            color = color,
        )
        translate(top = this.size.height - cornerSizePx) {
            drawPath(
                path = swipeActionInnerCornerPath(
                    edge = edge,
                    isTop = false,
                    size = topSize,
                    cornerRadiusPx = cornerRadiusPx,
                ),
                color = color,
            )
        }
    }
}
