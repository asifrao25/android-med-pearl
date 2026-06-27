package com.knowledgepearls.app.ui.tabs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.knowledgepearls.app.data.capture.CaptureSheet
import com.knowledgepearls.app.ui.capture.AddMediaCaptureScreen
import com.knowledgepearls.app.ui.capture.CaptureViewModel
import com.knowledgepearls.app.ui.capture.ClinicalCaseCaptureScreen
import com.knowledgepearls.app.ui.capture.QuickTextCaptureScreen
import com.knowledgepearls.app.ui.capture.WebLinkCaptureScreen
import com.knowledgepearls.app.ui.feed.FeedScreen
import com.knowledgepearls.app.ui.account.AccountViewModel
import com.knowledgepearls.app.ui.feed.FeedAuthorContext
import com.knowledgepearls.app.ui.feed.FeedViewModel
import com.knowledgepearls.app.ui.feed.PearlDetailScreen
import com.knowledgepearls.app.ui.components.TabScreenHeader
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun FeedTabScreen(
    onOpenSettings: () -> Unit,
    feedViewModel: FeedViewModel = hiltViewModel(),
    captureViewModel: CaptureViewModel = hiltViewModel(),
    accountViewModel: AccountViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val uiState by feedViewModel.uiState.collectAsStateWithLifecycle()
    val accountState by accountViewModel.uiState.collectAsStateWithLifecycle()
    var captureMenuOpen by rememberSaveable { mutableStateOf(false) }
    val feedAuthorContext = FeedAuthorContext(
        userId = accountState.userId,
        userEmail = accountState.userEmail,
        userProfile = accountState.userProfile,
    )

    LaunchedEffect(Unit) {
        feedViewModel.resetContentTypeFilter()
    }

    NavHost(
        navController = navController,
        startDestination = "feed",
    ) {
        composable("feed") {
            FeedScreen(
                uiState = uiState,
                feedAuthorContext = feedAuthorContext,
                onResolveAvatarUrl = feedViewModel::fetchAvatarUrl,
                onOpenSettings = onOpenSettings,
                onPearlClick = { id -> navController.navigate("pearl/$id") },
                onSearchQueryChange = feedViewModel::setSearchQuery,
                onSearchActiveChange = feedViewModel::setSearchActive,
                onTagSelected = feedViewModel::setSelectedTag,
                onContentTypeSelected = feedViewModel::setContentTypeFilter,
                onDeleteRequest = feedViewModel::requestDelete,
                onDeleteConfirm = feedViewModel::confirmDelete,
                onDeleteCancel = feedViewModel::cancelDelete,
                onEmptyFilterShowAll = feedViewModel::dismissEmptyFilterAlert,
                onEmptyFilterDismiss = feedViewModel::dismissEmptyFilterAlert,
                onActionSuccessDismiss = feedViewModel::dismissActionSuccess,
                captureMenuOpen = captureMenuOpen,
                onCaptureMenuOpenChange = { captureMenuOpen = it },
                onCaptureSheetSelected = { sheet ->
                    captureMenuOpen = false
                    when (sheet) {
                        CaptureSheet.QuickText -> navController.navigate("capture/quick")
                        CaptureSheet.WebLink -> navController.navigate("capture/link")
                        CaptureSheet.ClinicalCase -> navController.navigate("capture/clinical")
                        CaptureSheet.Camera -> navController.navigate("capture/media/camera")
                        CaptureSheet.PhotoLibrary -> navController.navigate("capture/media/gallery")
                        CaptureSheet.Files -> navController.navigate("capture/media/files")
                    }
                },
            )
        }
        composable("pearl/{pearlId}") { entry ->
            val pearlId = entry.arguments?.getString("pearlId").orEmpty()
            PearlDetailScreen(
                pearlId = pearlId,
                viewModel = feedViewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable("capture/quick") {
            QuickTextCaptureScreen(
                viewModel = captureViewModel,
                onBack = { navController.popBackStack() },
                onSaved = {
                    feedViewModel.showCaptureSavedMessage()
                    navController.popBackStack()
                },
            )
        }
        composable("capture/link") {
            WebLinkCaptureScreen(
                viewModel = captureViewModel,
                onBack = { navController.popBackStack() },
                onSaved = {
                    feedViewModel.showCaptureSavedMessage()
                    navController.popBackStack()
                },
            )
        }
        composable("capture/clinical") {
            ClinicalCaseCaptureScreen(
                viewModel = captureViewModel,
                onBack = { navController.popBackStack() },
                onSaved = {
                    feedViewModel.showCaptureSavedMessage()
                    navController.popBackStack()
                },
            )
        }
        composable("capture/media/{route}") { entry ->
            val route = entry.arguments?.getString("route")
            AddMediaCaptureScreen(
                viewModel = captureViewModel,
                initialRoute = route,
                onBack = { navController.popBackStack() },
                onSaved = {
                    feedViewModel.showCaptureSavedMessage()
                    navController.popBackStack()
                },
            )
        }
    }
}

@Composable
fun FavouritesTabScreen(
    onOpenSettings: () -> Unit,
    favouritesViewModel: com.knowledgepearls.app.ui.favourites.FavouritesViewModel = hiltViewModel(),
    feedViewModel: FeedViewModel = hiltViewModel(),
    accountViewModel: AccountViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val accountState by accountViewModel.uiState.collectAsStateWithLifecycle()
    val feedAuthorContext = FeedAuthorContext(
        userId = accountState.userId,
        userEmail = accountState.userEmail,
        userProfile = accountState.userProfile,
    )

    NavHost(navController = navController, startDestination = "favourites") {
        composable("favourites") {
            com.knowledgepearls.app.ui.favourites.FavouritesScreen(
                viewModel = favouritesViewModel,
                feedAuthorContext = feedAuthorContext,
                onResolveAvatarUrl = feedViewModel::fetchAvatarUrl,
                onOpenSettings = onOpenSettings,
                onPearlClick = { id -> navController.navigate("pearl/$id") },
            )
        }
        composable("pearl/{pearlId}") { entry ->
            val pearlId = entry.arguments?.getString("pearlId").orEmpty()
            PearlDetailScreen(
                pearlId = pearlId,
                viewModel = feedViewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
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
