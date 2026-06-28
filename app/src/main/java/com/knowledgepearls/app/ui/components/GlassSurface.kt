package com.knowledgepearls.app.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp,
    darkTheme: Boolean = isPearlDarkTheme(),
    opaque: Boolean = false,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val border = PearlColors.cardBorder(darkTheme)
    val fill = if (opaque) {
        PearlColors.popupSurface(darkTheme)
    } else if (darkTheme) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.White.copy(alpha = 0.72f)
    }

    Box(
        modifier = modifier
            .background(fill, shape)
            .border(width = 1.dp, color = border, shape = shape),
    ) {
        content()
    }
}
