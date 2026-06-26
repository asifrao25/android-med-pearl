package com.knowledgepearls.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowledgepearls.app.ui.shell.MainTab
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun LiquidTabBar(
    selected: MainTab,
    publicFeedNewCount: Int,
    foldersMenuOpen: Boolean,
    onTabSelected: (MainTab) -> Unit,
    onFoldersTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PearlLayout.screenHorizontalPadding),
        cornerRadius = 999.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(PearlLayout.tabBarHeight)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TabChip(
                tab = MainTab.Feed,
                icon = Icons.Default.List,
                label = "My Feed",
                selected = selected == MainTab.Feed,
                expand = selected != MainTab.Feed,
                darkTheme = darkTheme,
                onClick = { onTabSelected(MainTab.Feed) },
            )
            TabChip(
                tab = MainTab.Folders,
                icon = Icons.Default.Folder,
                label = "Folders",
                selected = selected == MainTab.Folders || foldersMenuOpen,
                expand = selected != MainTab.Folders && !foldersMenuOpen,
                darkTheme = darkTheme,
                onClick = onFoldersTap,
            )
            PublicFeedTabChip(
                selected = selected == MainTab.PublicFeed,
                expand = selected != MainTab.PublicFeed,
                newCount = publicFeedNewCount,
                darkTheme = darkTheme,
                onClick = { onTabSelected(MainTab.PublicFeed) },
            )
            TabChip(
                tab = MainTab.Favourites,
                icon = Icons.Default.Favorite,
                label = "Favourites",
                selected = selected == MainTab.Favourites,
                expand = selected != MainTab.Favourites,
                darkTheme = darkTheme,
                onClick = { onTabSelected(MainTab.Favourites) },
            )
        }
    }
}

@Composable
private fun RowScope.TabChip(
    tab: MainTab,
    icon: ImageVector,
    label: String,
    selected: Boolean,
    expand: Boolean,
    darkTheme: Boolean,
    onClick: () -> Unit,
) {
    val inactive = PearlColors.tabInactive(darkTheme)
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = Modifier
            .then(if (expand) Modifier.weight(1f) else Modifier)
            .height(PearlLayout.tabBarHeight - 12.dp)
            .clip(shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .then(
                if (selected) {
                    Modifier.background(
                        brush = Brush.linearGradient(
                            listOf(tab.theme.primary, tab.theme.secondary),
                        ),
                        shape = shape,
                    )
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (selected) 16.dp else 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (selected) Color.White else inactive,
            )
            if (selected) {
                Text(
                    text = label,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun RowScope.PublicFeedTabChip(
    selected: Boolean,
    expand: Boolean,
    newCount: Int,
    darkTheme: Boolean,
    onClick: () -> Unit,
) {
    val tab = MainTab.PublicFeed
    val inactive = PearlColors.tabInactive(darkTheme)
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = Modifier
            .then(if (expand) Modifier.weight(1f) else Modifier)
            .height(PearlLayout.tabBarHeight - 12.dp)
            .clip(shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .then(
                if (selected) {
                    Modifier.background(
                        brush = Brush.linearGradient(
                            listOf(tab.theme.primary, tab.theme.secondary),
                        ),
                        shape = shape,
                    )
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (selected) 16.dp else 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "Public Feed",
                    modifier = Modifier.size(20.dp),
                    tint = if (selected) Color.White else inactive,
                )
                if (newCount > 0) {
                    val badge = if (newCount > 99) "99+" else newCount.toString()
                    Text(
                        text = badge,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-7).dp)
                            .background(Color.Red, CircleShape)
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            if (selected) {
                Text(
                    text = "Public",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
