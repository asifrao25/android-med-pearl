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
import com.knowledgepearls.app.ui.publicfeed.PublicFeedScreen
import com.knowledgepearls.app.ui.publicfeed.PublicFeedViewModel
import com.knowledgepearls.app.ui.publicfeed.PublicPearlDetailScreen

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
fun PublicFeedTabScreen(
    onOpenSettings: () -> Unit,
    onSignIn: () -> Unit,
    viewModel: PublicFeedViewModel = hiltViewModel(),
    accountViewModel: AccountViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val accountState by accountViewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = "public_feed",
    ) {
        composable("public_feed") {
            PublicFeedScreen(
                uiState = uiState,
                isSignedIn = accountState.isSignedIn,
                onOpenSettings = onOpenSettings,
                onSignIn = onSignIn,
                onPearlClick = { pearlId ->
                    navController.navigate("public_pearl/$pearlId")
                },
                onLoadInitial = viewModel::loadInitial,
                onLoadNextPage = viewModel::loadNextPage,
                onSectionSelected = viewModel::setSection,
                onContentTypeSelected = viewModel::setContentTypeFilter,
                onResetContentTypeFilter = viewModel::resetContentTypeFilter,
                onDismissEmptyFilterAlert = viewModel::dismissEmptyFilterAlert,
                onDismissActionSuccess = viewModel::dismissActionSuccess,
                onDismissError = viewModel::dismissError,
            )
        }

        composable("public_pearl/{pearlId}") { entry ->
            val pearlId = entry.arguments?.getString("pearlId").orEmpty()
            val pearl = uiState.pearls.firstOrNull { it.id == pearlId }
            if (pearl == null) {
                PublicFeedScreen(
                    uiState = uiState,
                    isSignedIn = accountState.isSignedIn,
                    onOpenSettings = onOpenSettings,
                    onSignIn = onSignIn,
                    onPearlClick = {},
                    onLoadInitial = viewModel::loadInitial,
                    onLoadNextPage = viewModel::loadNextPage,
                    onSectionSelected = viewModel::setSection,
                    onContentTypeSelected = viewModel::setContentTypeFilter,
                    onResetContentTypeFilter = viewModel::resetContentTypeFilter,
                    onDismissEmptyFilterAlert = viewModel::dismissEmptyFilterAlert,
                    onDismissActionSuccess = viewModel::dismissActionSuccess,
                    onDismissError = viewModel::dismissError,
                )
                return@composable
            }

            LaunchedEffect(pearl.id) {
                viewModel.markSeen(pearl)
            }

            PublicPearlDetailScreen(
                pearl = pearl,
                onBack = { navController.popBackStack() },
                onAddToMyFeed = { viewModel.addToMyFeed(pearl) },
                onHide = {
                    viewModel.hide(pearl)
                    navController.popBackStack()
                },
            )
        }
    }
}
