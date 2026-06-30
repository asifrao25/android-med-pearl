package com.knowledgepearls.app.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** True when the software keyboard is visible. */
@Composable
fun isKeyboardVisible(): Boolean {
    val density = LocalDensity.current
    return WindowInsets.ime.getBottom(density) > 0
}

/**
 * Bottom padding for pinned input bars (chat composer, comment field, save footer).
 * Sits flush on the keyboard when open; uses [fallbackWhenHidden] or the nav bar when closed.
 */
fun Modifier.inputBarBottomPadding(fallbackWhenHidden: Dp = 0.dp): Modifier = composed {
    val density = LocalDensity.current
    val imeBottomPx = WindowInsets.ime.getBottom(density)
    val navBottomPx = WindowInsets.navigationBars.getBottom(density)
    val bottom = with(density) {
        when {
            imeBottomPx > 0 -> imeBottomPx.toDp()
            fallbackWhenHidden > 0.dp -> fallbackWhenHidden
            navBottomPx > 0 -> navBottomPx.toDp()
            else -> 0.dp
        }
    }
    padding(bottom = bottom)
}

/** Bottom inset for scrollable forms so fields stay above the keyboard. */
fun Modifier.keyboardScrollPadding(): Modifier = composed {
    windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
}
