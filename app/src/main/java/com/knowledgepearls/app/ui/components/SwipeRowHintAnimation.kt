package com.knowledgepearls.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.spring
import kotlinx.coroutines.delay

/** Matches iOS `SwipeRowHintAnimation` — gentle alternating peeks on the first list card. */
object SwipeRowHintAnimation {
    private const val revealFraction = 0.78f
    private val springIn = spring<Float>(dampingRatio = 0.86f, stiffness = 280f)
    private val springOut = spring<Float>(dampingRatio = 0.88f, stiffness = 320f)

    suspend fun run(
        hintOffset: Animatable<Float, AnimationVector1D>,
        actionWidthPx: Float,
    ) {
        delay(1_600)
        var startsWithLeading = true
        val peek = actionWidthPx * revealFraction

        while (true) {
            val first = if (startsWithLeading) peek else -peek
            val second = if (startsWithLeading) -peek else peek

            hintOffset.animateTo(first, springIn)
            delay(720)

            hintOffset.animateTo(0f, springOut)
            delay(420)

            hintOffset.animateTo(second, springIn)
            delay(720)

            hintOffset.animateTo(0f, springOut)

            startsWithLeading = !startsWithLeading
            delay(3_600)
        }
    }
}
