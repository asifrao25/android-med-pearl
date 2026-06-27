package com.knowledgepearls.app.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.min

enum class SwipeActionEdge {
    Leading,
    Trailing,
}

/** iOS SwipeActionBackgroundShape — outer edge rounded, inner tabs tuck under the card curve. */
class SwipeActionBackgroundShape(
    private val edge: SwipeActionEdge,
    private val cornerRadiusPx: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val r = min(cornerRadiusPx, min(size.width / 2f, size.height / 2f))
        val w = size.width
        val h = size.height
        val mainWidth = w - r

        val path = Path().apply {
            when (edge) {
                SwipeActionEdge.Leading -> {
                    moveTo(r, 0f)
                    lineTo(w, 0f)
                    lineTo(w, r)
                    lineTo(mainWidth, r)
                    lineTo(mainWidth, h - r)
                    lineTo(w, h - r)
                    lineTo(w, h)
                    lineTo(r, h)
                    arcTo(
                        rect = Rect(0f, h - 2 * r, 2 * r, h),
                        startAngleDegrees = 90f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    lineTo(0f, r)
                    arcTo(
                        rect = Rect(0f, 0f, 2 * r, 2 * r),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    close()
                }
                SwipeActionEdge.Trailing -> {
                    moveTo(0f, 0f)
                    lineTo(w - r, 0f)
                    arcTo(
                        rect = Rect(w - 2 * r, 0f, w, 2 * r),
                        startAngleDegrees = 270f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    lineTo(w, h - r)
                    arcTo(
                        rect = Rect(w - 2 * r, h - 2 * r, w, h),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false,
                    )
                    lineTo(0f, h)
                    lineTo(0f, h - r)
                    lineTo(r, h - r)
                    lineTo(r, r)
                    lineTo(0f, r)
                    close()
                }
            }
        }
        return Outline.Generic(path)
    }
}

/** iOS SwipeActionInnerCornerShape — fills the gap between card and action strip. */
fun swipeActionInnerCornerPath(
    edge: SwipeActionEdge,
    isTop: Boolean,
    size: Size,
    cornerRadiusPx: Float,
): Path {
    val r = min(cornerRadiusPx, min(size.width, size.height))
    val w = size.width
    val h = size.height
    return Path().apply {
        when {
            edge == SwipeActionEdge.Leading && isTop -> {
                moveTo(0f, 0f)
                lineTo(r, 0f)
                lineTo(r, r)
                addArc(
                    oval = Rect(-r, 0f, r, 2f * r),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                )
                close()
            }
            edge == SwipeActionEdge.Leading && !isTop -> {
                moveTo(0f, h)
                lineTo(r, h)
                lineTo(r, h - r)
                addArc(
                    oval = Rect(-r, h - 2f * r, r, h),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90f,
                )
                close()
            }
            edge == SwipeActionEdge.Trailing && isTop -> {
                moveTo(w, 0f)
                lineTo(w - r, 0f)
                lineTo(w - r, r)
                addArc(
                    oval = Rect(w - r, 0f, w + r, 2f * r),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                )
                close()
            }
            else -> {
                moveTo(w, h)
                lineTo(w - r, h)
                lineTo(w - r, h - r)
                addArc(
                    oval = Rect(w - r, h - 2f * r, w + r, h),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = 90f,
                )
                close()
            }
        }
    }
}

/** Card-shaped mask so swipe strips do not bleed through glass cards. */
class SwipeRowCardMaskShape(
    private val cornerRadiusPx: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline = Outline.Rounded(
        RoundRect(
            rect = Rect(0f, 0f, size.width, size.height),
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
        ),
    )
}
