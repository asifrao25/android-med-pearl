package com.knowledgepearls.app.ui.components

import androidx.compose.runtime.Composable
import com.knowledgepearls.app.ui.theme.TabTheme

data class TabHeaderContext(
    val title: String,
    val subtitle: String?,
    val theme: TabTheme,
    val showsSettingsButton: Boolean = true,
)

fun TabTheme.tabHeaderContext(
    title: String = this.title,
    subtitle: String? = this.defaultSubtitle,
    showsSettingsButton: Boolean = true,
): TabHeaderContext = TabHeaderContext(
    title = title,
    subtitle = subtitle,
    theme = this,
    showsSettingsButton = showsSettingsButton,
)

val TabTheme.defaultSubtitle: String?
    get() = when (this) {
        TabTheme.Feed -> "Your pearls"
        TabTheme.Favourites -> "Saved pearls"
        TabTheme.PublicFeed -> "Community pearls"
        TabTheme.Folders -> "Folder"
        TabTheme.Settings -> null
    }

@Composable
fun PersistentTabScreenHeader(
    context: TabHeaderContext,
    onSettingsClick: () -> Unit = {},
) {
    TabScreenHeader(
        title = context.title,
        subtitle = context.subtitle,
        theme = context.theme,
        onSettingsClick = onSettingsClick,
        showsSettingsButton = context.showsSettingsButton,
    )
}
