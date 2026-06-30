package com.knowledgepearls.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import com.knowledgepearls.app.ui.components.isKeyboardVisible

private val inboxFloatingShape = RoundedCornerShape(28.dp)

@Composable
fun FloatingInboxSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState? = null,
    content: @Composable () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    val keyboardVisible = isKeyboardVisible()
    val sheetBottomInset = if (keyboardVisible) 0.dp else PearlLayout.inboxSheetBottomInset
    val configuration = LocalConfiguration.current
    val sheetHeight = (
        configuration.screenHeightDp * PearlLayout.inboxSheetHeightFraction
        ).dp
        .coerceAtMost(PearlLayout.inboxSheetMaxHeight)
        .coerceAtLeast(PearlLayout.inboxSheetMinHeight)

    Box(modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(PearlColors.scrim(darkTheme, 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )

        PullToDismissSheet(
            onDismiss = onDismiss,
            listState = listState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = PearlLayout.inboxSheetHorizontalInset)
                .padding(bottom = sheetBottomInset)
                .fillMaxWidth()
                .height(sheetHeight)
                .shadow(28.dp, inboxFloatingShape, clip = false)
                .clip(inboxFloatingShape)
                .background(PearlColors.popupSurface(darkTheme))
                .border(1.dp, PearlColors.strongBorder(darkTheme), inboxFloatingShape),
        ) {
            content()
        }
    }
}
