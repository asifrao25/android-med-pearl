package com.knowledgepearls.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Semantic surface colors ported from iOS `PearlSurfaceColors.swift`.
 * Use these instead of raw Material colours so light/dark parity matches Med Pearls iOS.
 */
object PearlColors {
    val canvasDark = Color(0xFF06060A)
    val canvasLight = Color(0xFFF4F5F9)

    fun canvas(darkTheme: Boolean): Color = if (darkTheme) canvasDark else canvasLight

    fun liquidBlobOpacity(darkTheme: Boolean): Float = if (darkTheme) 0.5f else 0.38f

    /** Solid surface for modal sheets and alert cards — fully opaque over scrims. */
    fun popupSurface(darkTheme: Boolean): Color = tabBarFill(darkTheme)

    fun glassOverlay(darkTheme: Boolean): Color =
        if (darkTheme) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.52f)

    fun cardBorder(darkTheme: Boolean): Color =
        if (darkTheme) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)

    fun strongBorder(darkTheme: Boolean): Color =
        if (darkTheme) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.12f)

    fun divider(darkTheme: Boolean): Color =
        if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    fun scrim(darkTheme: Boolean, baseOpacity: Float): Color {
        val adjusted = if (darkTheme) baseOpacity else minOf(baseOpacity * 0.72f, 0.42f)
        return Color.Black.copy(alpha = adjusted)
    }

    fun tabInactive(darkTheme: Boolean): Color =
        if (darkTheme) Color.White.copy(alpha = 0.45f) else Color(0xFF1A1A1E).copy(alpha = 0.50f)

    fun dragHandle(darkTheme: Boolean): Color =
        if (darkTheme) Color.White.copy(alpha = 0.28f) else Color(0xFF1A1A1E).copy(alpha = 0.22f)

    fun heroPrimary(darkTheme: Boolean): Color =
        if (darkTheme) Color.White else Color(0xFF1A1A1E)

    fun heroSecondary(darkTheme: Boolean): Color =
        if (darkTheme) Color.White.copy(alpha = 0.5f) else Color(0xFF6B7280)

    fun mutedTileBackground(darkTheme: Boolean): Color =
        if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)

    fun selectedPillFill(darkTheme: Boolean): Color =
        if (darkTheme) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.88f)

    fun segmentedInactive(darkTheme: Boolean): Color =
        if (darkTheme) Color.White.copy(alpha = 0.58f) else Color(0xFF1A1A1E).copy(alpha = 0.55f)

    fun controlFill(darkTheme: Boolean): Color =
        if (darkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f)

    /** Solid tab bar capsule — avoids glass blur so tabs stay readable over scrolling content. */
    fun tabBarFill(darkTheme: Boolean): Color =
        if (darkTheme) Color(0xFF12121A) else Color(0xFFF8F9FC)

    /** Detail action dock — matches tab bar opacity for consistent chrome. */
    fun detailDockFill(darkTheme: Boolean): Color = tabBarFill(darkTheme)

    fun sectionHeaderGradient(theme: TabTheme, darkTheme: Boolean): Brush =
        Brush.horizontalGradient(
            colors = listOf(
                theme.primary.copy(alpha = if (darkTheme) 0.52f else 0.58f),
                theme.secondary.copy(alpha = if (darkTheme) 0.34f else 0.40f),
                theme.secondary.copy(alpha = if (darkTheme) 0.12f else 0.16f),
            ),
        )

}

/** Layout tokens from iOS `LiquidTabBarLayout` and `TabScreenHeaderMetrics`. */
object PearlLayout {
    val screenHorizontalPadding = 20.dp
    val tabBarHeight = 64.dp
    val tabBarIconSize = 26.dp
    /** Small lift from the physical screen bottom — matches iOS `screenBottomPadding`. */
    val tabBarBottomPadding = 8.dp
    /** Space reserved above the floating tab bar for FABs and bottom actions. */
    val tabBarOverlayInset = tabBarHeight + tabBarBottomPadding + 16.dp
    val addButtonSize = 52.dp
    val floatingActionStackSpacing = 10.dp
    /** Matches [addButtonSize] — inbox FAB is the same diameter as the add button. */
    val inboxButtonSize = addButtonSize
    /** Section tabs row height including inner padding (matches `PublicFeedSectionTabs`). */
    val publicFeedSectionTabsHeight = 40.dp
    val fabAboveSectionTabsSpacing = 12.dp
    val publicFeedSectionTabsSpacing = 2.dp

    /** Bottom inset for the feed add button — flush above the tab bar. */
    val addButtonBottomPadding: androidx.compose.ui.unit.Dp
        get() = tabBarBottomPadding + tabBarHeight

    /** Bottom inset for the public-feed add button — sits above New/Seen pills. */
    val publicFeedAddButtonBottomPadding: androidx.compose.ui.unit.Dp
        get() = addButtonBottomPadding +
            publicFeedSectionTabsHeight +
            fabAboveSectionTabsSpacing

    /** Bottom inset for public-feed section tabs above the tab bar. */
    val publicFeedSectionTabsBottomPadding: androidx.compose.ui.unit.Dp
        get() = addButtonBottomPadding

    fun captureMenuBottomPadding(fabBottomPadding: androidx.compose.ui.unit.Dp): androidx.compose.ui.unit.Dp =
        fabBottomPadding + addButtonSize + 10.dp

    /** Positions the floating inbox button just above the add button. */
    fun inboxButtonBottomPadding(addFabBottomPadding: androidx.compose.ui.unit.Dp): androidx.compose.ui.unit.Dp =
        addFabBottomPadding + addButtonSize + floatingActionStackSpacing

    /** Positions the inbox reminder bubble just above the floating inbox button. */
    fun inboxReminderBottomPadding(inboxButtonBottom: androidx.compose.ui.unit.Dp): androidx.compose.ui.unit.Dp =
        inboxButtonBottom + addButtonSize + 8.dp

    val inboxSheetHorizontalInset = 14.dp
    /** Gap between the floating inbox card and the tab bar. */
    val inboxSheetBottomInset = tabBarOverlayInset
    const val inboxSheetHeightFraction = 0.68f
    val inboxSheetMaxHeight = 560.dp
    val inboxSheetMinHeight = 360.dp
    /** Floating detail dock — matches iOS `LiquidDetailDock` + `LiquidTabBarLayout.actionBarBottomPadding`. */
    val detailDockHeight = 64.dp
    val detailDockTopPadding = 6.dp
    /** Bottom inset so the detail dock sits flush above the floating tab bar. */
    val detailDockBottomPadding = tabBarHeight + tabBarBottomPadding + 12.dp
    val detailScrollBottomPadding = detailDockHeight + detailDockTopPadding + detailDockBottomPadding + 8.dp
    val cardCornerRadius = 18.dp
    val headerContentHeight = 56.dp
    val headerActionSize = 44.dp
    val headerIconSize = 24.dp
}
