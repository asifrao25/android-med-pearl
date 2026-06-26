package com.knowledgepearls.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Tab accent colours and per-tab canvas tints for ambient backgrounds.
 */
enum class TabTheme(
    val title: String,
    val primary: Color,
    val secondary: Color,
    val canvasTintDark: Color,
    val canvasTintLight: Color,
) {
    Feed(
        title = "My Feed",
        primary = Color(0xFF7C6CF0),
        secondary = Color(0xFF5B7CFA),
        canvasTintDark = Color(0xFF0B0914),
        canvasTintLight = Color(0xFFEBE8F8),
    ),
    Favourites(
        title = "Favourites",
        primary = Color(0xFFE84393),
        secondary = Color(0xFFFF6B9D),
        canvasTintDark = Color(0xFF120910),
        canvasTintLight = Color(0xFFFAE9F0),
    ),
    PublicFeed(
        title = "Public Feed",
        primary = Color(0xFF14B8A6),
        secondary = Color(0xFF22D3EE),
        canvasTintDark = Color(0xFF071110),
        canvasTintLight = Color(0xFFE4F7F4),
    ),
    Folders(
        title = "Folders",
        primary = Color(0xFFF59E0B),
        secondary = Color(0xFFFB923C),
        canvasTintDark = Color(0xFF100E08),
        canvasTintLight = Color(0xFFFDF3E7),
    ),
    Settings(
        title = "Settings",
        primary = Color(0xFF94A3B8),
        secondary = Color(0xFF64748B),
        canvasTintDark = Color(0xFF090A0D),
        canvasTintLight = Color(0xFFF0F2F5),
    ),
}
