package com.knowledgepearls.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.AppBrand
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

/**
 * Stage 1 placeholder — proves design tokens render correctly.
 * Replaced by [com.knowledgepearls.app.ui.shell.MainScaffold] in Stage 2.
 */
@Composable
fun StageOnePreviewScreen() {
    val darkTheme = isPearlDarkTheme()

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = TabTheme.Feed)

        Text(
            text = "${AppBrand.NAME}\nStage 1 — design tokens loaded",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = PearlLayout.screenHorizontalPadding),
            color = PearlColors.heroPrimary(darkTheme),
            textAlign = TextAlign.Center,
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
        )
    }
}
