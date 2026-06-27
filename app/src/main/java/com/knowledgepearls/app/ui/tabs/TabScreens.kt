package com.knowledgepearls.app.ui.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.knowledgepearls.app.ui.components.TabScreenHeader
import com.knowledgepearls.app.ui.feed.FeedScreen
import com.knowledgepearls.app.ui.feed.FeedViewModel
import com.knowledgepearls.app.ui.feed.PearlDetailScreen
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun FeedTabScreen(
    onOpenSettings: () -> Unit,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.resetContentTypeFilter()
    }

    NavHost(
        navController = navController,
        startDestination = "feed",
    ) {
        composable("feed") {
            FeedScreen(
                uiState = uiState,
                onOpenSettings = onOpenSettings,
                onPearlClick = { id -> navController.navigate("pearl/$id") },
                onSearchQueryChange = viewModel::setSearchQuery,
                onSearchActiveChange = viewModel::setSearchActive,
                onTagSelected = viewModel::setSelectedTag,
                onContentTypeSelected = viewModel::setContentTypeFilter,
                onDeleteRequest = viewModel::requestDelete,
                onDeleteConfirm = viewModel::confirmDelete,
                onDeleteCancel = viewModel::cancelDelete,
                onEmptyFilterShowAll = viewModel::dismissEmptyFilterAlert,
                onEmptyFilterDismiss = viewModel::dismissEmptyFilterAlert,
                onActionSuccessDismiss = viewModel::dismissActionSuccess,
            )
        }
        composable("pearl/{pearlId}") { entry ->
            val pearlId = entry.arguments?.getString("pearlId").orEmpty()
            PearlDetailScreen(
                pearlId = pearlId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
fun FavouritesTabScreen(onOpenSettings: () -> Unit) {
    TabRoot(
        theme = TabTheme.Favourites,
        title = "Favourites",
        subtitle = "Saved pearls",
        onOpenSettings = onOpenSettings,
        placeholder = "Favourites coming in Stage 7",
    )
}

@Composable
fun PublicFeedTabScreen(onOpenSettings: () -> Unit) {
    TabRoot(
        theme = TabTheme.PublicFeed,
        title = "Public Feed",
        subtitle = "Community pearls",
        onOpenSettings = onOpenSettings,
        placeholder = "Public feed coming in Stage 8",
    )
}

@Composable
private fun TabRoot(
    theme: TabTheme,
    title: String,
    subtitle: String,
    onOpenSettings: () -> Unit,
    placeholder: String,
) {
    val darkTheme = isPearlDarkTheme()

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = theme)

        Column(Modifier.statusBarsPadding()) {
            TabScreenHeader(
                title = title,
                subtitle = subtitle,
                theme = theme,
                onSettingsClick = onOpenSettings,
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = PearlLayout.screenHorizontalPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PearlColors.heroSecondary(darkTheme),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
