package com.knowledgepearls.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppearanceMode {
    System,
    Light,
    Dark,
}

private val DarkScheme = darkColorScheme(
    primary = TabTheme.Feed.primary,
    onPrimary = Color.White,
    secondary = TabTheme.Feed.secondary,
    background = PearlColors.canvasDark,
    surface = PearlColors.canvasDark,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightScheme = lightColorScheme(
    primary = TabTheme.Feed.primary,
    onPrimary = Color.White,
    secondary = TabTheme.Feed.secondary,
    background = PearlColors.canvasLight,
    surface = PearlColors.canvasLight,
    onBackground = Color(0xFF1A1A1E),
    onSurface = Color(0xFF1A1A1E),
)

val LocalAppearanceMode = staticCompositionLocalOf { AppearanceMode.System }

@Composable
fun MedPearlsTheme(
    appearanceMode: AppearanceMode = AppearanceMode.Dark,
    fontChoice: AppFontChoice = AppFontChoice.Inter,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (appearanceMode) {
        AppearanceMode.System -> isSystemInDarkTheme()
        AppearanceMode.Light -> false
        AppearanceMode.Dark -> true
    }

    val colorScheme = if (darkTheme) DarkScheme else LightScheme

    CompositionLocalProvider(LocalAppearanceMode provides appearanceMode) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = pearlTypography(fontChoice),
            content = content,
        )
    }
}

@Composable
fun isPearlDarkTheme(): Boolean {
    return when (LocalAppearanceMode.current) {
        AppearanceMode.System -> isSystemInDarkTheme()
        AppearanceMode.Light -> false
        AppearanceMode.Dark -> true
    }
}
