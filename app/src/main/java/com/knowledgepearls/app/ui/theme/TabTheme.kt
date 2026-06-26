package com.knowledgepearls.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Tab jewel tones from iOS `TabTheme.swift`.
 */
enum class TabTheme(
    val title: String,
    val primary: Color,
    val secondary: Color,
) {
    Feed(
        title = "My Feed",
        primary = Color(0xFF6C5CE7),
        secondary = Color(0xFF4F6EF7),
    ),
    Favourites(
        title = "Favourites",
        primary = Color(0xFFEC407A),
        secondary = Color(0xFFFF6B8B),
    ),
    PublicFeed(
        title = "Public Feed",
        primary = Color(0xFF14B8A6),
        secondary = Color(0xFF22D3EE),
    ),
    Folders(
        title = "Folders",
        primary = Color(0xFFF4A640),
        secondary = Color(0xFFFF8A3D),
    ),
    Settings(
        title = "Settings",
        primary = Color(0xFF7C8696),
        secondary = Color(0xFF5B6B82),
    ),
}
