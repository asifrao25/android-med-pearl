package com.knowledgepearls.app.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp

/**
 * Matches iOS `interactiveKeyboardDismiss()` + window-level swipe-down dismiss:
 * scroll down to hide the keyboard smoothly, or swipe down anywhere when IME is visible.
 */
@Composable
fun rememberInteractiveKeyboardDismissConnection(): NestedScrollConnection {
    val focusManager = LocalFocusManager.current
    return remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y > 0f) {
                    focusManager.clearFocus()
                }
                return Offset.Zero
            }
        }
    }
}

fun Modifier.interactiveKeyboardDismiss(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val connection = rememberInteractiveKeyboardDismissConnection()
    val imeBottom = WindowInsets.ime.getBottom(density)
    val swipeThresholdPx = with(density) { 40.dp.toPx() }

    this
        .nestedScroll(connection)
        .then(
            if (imeBottom > 0) {
                Modifier.pointerInput(imeBottom) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        var totalDownward = 0f
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull() ?: break
                            if (!change.pressed) {
                                if (totalDownward >= swipeThresholdPx) {
                                    focusManager.clearFocus()
                                }
                                break
                            }
                            val deltaY = change.position.y - change.previousPosition.y
                            if (deltaY > 0f) {
                                totalDownward += deltaY
                                if (totalDownward >= swipeThresholdPx) {
                                    focusManager.clearFocus()
                                    break
                                }
                            }
                        }
                    }
                }
            } else {
                Modifier
            },
        )
}
