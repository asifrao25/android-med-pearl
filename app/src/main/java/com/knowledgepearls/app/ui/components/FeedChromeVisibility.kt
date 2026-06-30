package com.knowledgepearls.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlinx.coroutines.flow.distinctUntilChanged

@Stable
class FeedChromeVisibility {
    private val _targetProgress = mutableFloatStateOf(1f)
    val targetProgress: Float
        get() = _targetProgress.floatValue

    private var scrollAccumulator = 0f
    private var lastCompositeOffset = 0
    private var suppressDepth = 0

    val isSuppressed: Boolean
        get() = suppressDepth > 0

    fun suppress() {
        suppressDepth++
        forceShow()
    }

    fun releaseSuppress() {
        suppressDepth = max(0, suppressDepth - 1)
    }

    fun forceShow() {
        scrollAccumulator = 0f
        lastCompositeOffset = 0
        _targetProgress.floatValue = 1f
    }

    fun hide() {
        scrollAccumulator = HIDE_THRESHOLD_PX.toFloat()
        _targetProgress.floatValue = 0f
    }

    fun onScrollComposite(composite: Int, canScroll: Boolean) {
        if (!canScroll || isSuppressed) return

        if (composite <= 0) {
            forceShow()
            lastCompositeOffset = composite
            return
        }

        val delta = composite - lastCompositeOffset
        lastCompositeOffset = composite

        when {
            delta > SCROLL_DEAD_ZONE_PX -> {
                scrollAccumulator += delta
                if (scrollAccumulator >= HIDE_THRESHOLD_PX) {
                    _targetProgress.floatValue = 0f
                }
            }
            delta < -REVEAL_THRESHOLD_PX -> {
                scrollAccumulator = 0f
                _targetProgress.floatValue = 1f
            }
        }
    }

    private companion object {
        const val HIDE_THRESHOLD_PX = 48
        const val REVEAL_THRESHOLD_PX = 8
        const val SCROLL_DEAD_ZONE_PX = 4
    }
}

val LocalFeedChromeVisibility = compositionLocalOf<FeedChromeVisibility?> { null }

@Composable
fun rememberFeedChromeVisibility(): FeedChromeVisibility = remember { FeedChromeVisibility() }

object FeedChromeMetrics {
    val topChromeHideDistance = 64.dp
    val tabBarHideDistance = 72.dp
    val sectionTabsHideDistance = 132.dp
    val fabHideDistance = 168.dp
    val inboxFabHideDistance = 228.dp
}

enum class FeedChromeAnchor {
    Top,
    Bottom,
}

@Composable
fun TrackFeedChromeScroll(
    listState: LazyListState,
    enabled: Boolean = true,
) {
    val chrome = LocalFeedChromeVisibility.current ?: return

    LaunchedEffect(listState, enabled) {
        if (!enabled) {
            chrome.forceShow()
            return@LaunchedEffect
        }

        snapshotFlow {
            listState.firstVisibleItemIndex * 100_000 + listState.firstVisibleItemScrollOffset
        }
            .distinctUntilChanged()
            .collect { composite ->
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val canScroll = totalItems > 0 && (
                layoutInfo.visibleItemsInfo.size < totalItems ||
                    composite > 0 ||
                    (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) < totalItems - 1
                )
            chrome.onScrollComposite(composite, canScroll)
        }
    }
}

@Composable
fun feedChromeBottomPadding(fullPadding: Dp, collapsedPadding: Dp = 24.dp): Dp {
    val chrome = LocalFeedChromeVisibility.current ?: return fullPadding
    // Snap to target (not animated progress) so LazyColumn padding changes at most once per
    // hide/show — avoids relayout every spring frame while the user is still scrolling.
    return if (chrome.targetProgress > 0.5f) fullPadding else collapsedPadding
}

fun Modifier.feedChromeSlide(
    anchor: FeedChromeAnchor,
    hideDistance: Dp,
    minAlpha: Float = 0f,
): Modifier = composed {
    val chrome = LocalFeedChromeVisibility.current
    if (chrome == null) return@composed this

    val progress by animateFloatAsState(
        targetValue = chrome.targetProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "feedChromeSlide",
    )

    val hidePx = with(LocalDensity.current) { hideDistance.toPx() }
    val direction = when (anchor) {
        FeedChromeAnchor.Top -> -1f
        FeedChromeAnchor.Bottom -> 1f
    }
    val alpha = minAlpha + (1f - minAlpha) * progress

    graphicsLayer {
        this.alpha = alpha.coerceIn(0f, 1f)
        translationY = (1f - progress) * hidePx * direction
    }
}
