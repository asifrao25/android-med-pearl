package com.knowledgepearls.app.ui.tabs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.knowledgepearls.app.data.capture.CaptureSheet
import com.knowledgepearls.app.navigation.ShareImportPayload
import com.knowledgepearls.app.ui.capture.AddMediaCaptureScreen
import com.knowledgepearls.app.ui.capture.CaptureViewModel
import com.knowledgepearls.app.ui.capture.ClinicalCaseCaptureScreen
import com.knowledgepearls.app.ui.capture.PearlCaptureDestination
import com.knowledgepearls.app.ui.capture.QuickTextCaptureScreen
import com.knowledgepearls.app.ui.capture.WebLinkCaptureScreen
import com.knowledgepearls.app.ui.feed.FeedScreen
import com.knowledgepearls.app.ui.account.AccountViewModel
import com.knowledgepearls.app.ui.feed.FeedAuthorContext
import com.knowledgepearls.app.ui.feed.FeedViewModel
import com.knowledgepearls.app.ui.feed.PearlDetailScreen
import com.knowledgepearls.app.ui.publicfeed.PublicFeedScreen
import com.knowledgepearls.app.ui.publicfeed.PublicFeedViewModel
import com.knowledgepearls.app.data.connectivity.ConnectivityState
import com.knowledgepearls.app.ui.publicfeed.PublicPearlDetailScreen

@Composable
fun FeedTabScreen(
    onOpenSettings: () -> Unit,
    onSignInRequired: () -> Unit = {},
    onOpenUserProfile: (String) -> Unit = {},
    onOpenInbox: () -> Unit = {},
    inboxBadgeCount: Int = 0,
    onFeedRootVisibilityChange: (Boolean) -> Unit = {},
    onRegisterPopToRoot: (((() -> Unit)?) -> Unit)? = null,
    shareImport: ShareImportPayload? = null,
    onShareImportConsumed: () -> Unit = {},
    feedViewModel: FeedViewModel = hiltViewModel(),
    captureViewModel: CaptureViewModel = hiltViewModel(),
    accountViewModel: AccountViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        onFeedRootVisibilityChange(navBackStackEntry?.destination?.route == "feed")
    }
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

    LaunchedEffect(shareImport) {
        val payload = shareImport ?: return@LaunchedEffect
        when {
            !payload.url.isNullOrBlank() -> navController.navigate("capture/link/import")
            !payload.text.isNullOrBlank() -> navController.navigate("capture/quick/import")
        }
        onShareImportConsumed()
    }

    DisposableEffect(navController) {
        onRegisterPopToRoot?.invoke {
            captureMenuOpen = false
            feedViewModel.setSearchActive(false)
            feedViewModel.setSearchQuery("")
            navController.popBackStack("feed", false)
        }
        onDispose { onRegisterPopToRoot?.invoke(null) }
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
                isSignedIn = accountState.isSignedIn,
                inboxBadgeCount = inboxBadgeCount,
                onOpenInbox = onOpenInbox,
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
                feedAuthorContext = feedAuthorContext,
                onResolveAvatarUrl = feedViewModel::fetchAvatarUrl,
                onBack = { navController.popBackStack() },
                isSignedIn = accountState.isSignedIn,
                onSignInRequired = onSignInRequired,
                onOpenUserProfile = onOpenUserProfile,
            )
        }
        composable("capture/quick") {
            QuickTextCaptureScreen(
                viewModel = captureViewModel,
                isSignedIn = accountState.isSignedIn,
                onBack = { navController.popBackStack() },
                onSaved = {
                    feedViewModel.showCaptureSavedMessage()
                    navController.popBackStack()
                },
            )
        }
        composable("capture/quick/import") {
            QuickTextCaptureScreen(
                viewModel = captureViewModel,
                isSignedIn = accountState.isSignedIn,
                initialNotes = shareImport?.text,
                onBack = { navController.popBackStack() },
                onSaved = {
                    feedViewModel.showCaptureSavedMessage()
                    navController.popBackStack("feed", false)
                },
            )
        }
        composable("capture/link") {
            WebLinkCaptureScreen(
                viewModel = captureViewModel,
                isSignedIn = accountState.isSignedIn,
                onBack = { navController.popBackStack() },
                onSaved = {
                    feedViewModel.showCaptureSavedMessage()
                    navController.popBackStack()
                },
            )
        }
        composable("capture/link/import") {
            WebLinkCaptureScreen(
                viewModel = captureViewModel,
                isSignedIn = accountState.isSignedIn,
                initialUrl = shareImport?.url,
                initialNotes = shareImport?.text,
                onBack = { navController.popBackStack() },
                onSaved = {
                    feedViewModel.showCaptureSavedMessage()
                    navController.popBackStack("feed", false)
                },
            )
        }
        composable("capture/clinical") {
            ClinicalCaseCaptureScreen(
                viewModel = captureViewModel,
                isSignedIn = accountState.isSignedIn,
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
                isSignedIn = accountState.isSignedIn,
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
    onSignInRequired: () -> Unit = {},
    onOpenUserProfile: (String) -> Unit = {},
    onOpenInbox: () -> Unit = {},
    inboxBadgeCount: Int = 0,
    onRegisterPopToRoot: (((() -> Unit)?) -> Unit)? = null,
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

    DisposableEffect(navController) {
        onRegisterPopToRoot?.invoke {
            navController.popBackStack("favourites", false)
        }
        onDispose { onRegisterPopToRoot?.invoke(null) }
    }

    NavHost(navController = navController, startDestination = "favourites") {
        composable("favourites") {
            com.knowledgepearls.app.ui.favourites.FavouritesScreen(
                viewModel = favouritesViewModel,
                feedAuthorContext = feedAuthorContext,
                onResolveAvatarUrl = feedViewModel::fetchAvatarUrl,
                onOpenSettings = onOpenSettings,
                isSignedIn = accountState.isSignedIn,
                inboxBadgeCount = inboxBadgeCount,
                onOpenInbox = onOpenInbox,
                onPearlClick = { id -> navController.navigate("pearl/$id") },
            )
        }
        composable("pearl/{pearlId}") { entry ->
            val pearlId = entry.arguments?.getString("pearlId").orEmpty()
            PearlDetailScreen(
                pearlId = pearlId,
                viewModel = feedViewModel,
                feedAuthorContext = feedAuthorContext,
                onResolveAvatarUrl = feedViewModel::fetchAvatarUrl,
                onBack = { navController.popBackStack() },
                isSignedIn = accountState.isSignedIn,
                onSignInRequired = onSignInRequired,
                onOpenUserProfile = onOpenUserProfile,
            )
        }
    }
}

@Composable
fun PublicFeedTabScreen(
    onOpenSettings: () -> Unit,
    onOpenInbox: () -> Unit,
    onOpenUserProfile: (String) -> Unit = {},
    inboxBadgeCount: Int = 0,
    onPublicFeedRootVisibilityChange: (Boolean) -> Unit = {},
    onRegisterPopToRoot: (((() -> Unit)?) -> Unit)? = null,
    connectivityState: ConnectivityState = ConnectivityState(),
    onRetryConnection: () -> Unit = {},
    initialPearlId: String? = null,
    onInitialPearlConsumed: () -> Unit = {},
    viewModel: PublicFeedViewModel = hiltViewModel(),
    feedViewModel: FeedViewModel = hiltViewModel(),
    accountViewModel: AccountViewModel = hiltViewModel(),
    captureViewModel: CaptureViewModel = hiltViewModel(),
) {
    val activityContext = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        onPublicFeedRootVisibilityChange(navBackStackEntry?.destination?.route == "public_feed")
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val accountState by accountViewModel.uiState.collectAsStateWithLifecycle()
    var captureMenuOpen by rememberSaveable { mutableStateOf(false) }
    var showSharedPearlIntro by rememberSaveable { mutableStateOf(false) }

    val showsSectionTabs = accountState.isSignedIn &&
        connectivityState.isConnected &&
        !connectivityState.isOfflineMode

    val publicFeedDestination = PearlCaptureDestination.MyFeedAndPublic
    val onPublicCaptureSaved: () -> Unit = {
        feedViewModel.showCaptureSavedMessage()
        viewModel.refreshFeed(force = true)
        navController.popBackStack("public_feed", false)
    }

    LaunchedEffect(initialPearlId, accountState.isSignedIn, uiState.pearls.isNotEmpty()) {
        val pearlId = initialPearlId ?: return@LaunchedEffect
        if (!accountState.isSignedIn) return@LaunchedEffect
        if (uiState.pearls.isEmpty() && !uiState.isLoading) {
            viewModel.loadInitial()
        }
        navController.navigate("public_pearl/$pearlId")
        onInitialPearlConsumed()
    }

    DisposableEffect(navController) {
        onRegisterPopToRoot?.invoke {
            captureMenuOpen = false
            navController.popBackStack("public_feed", false)
        }
        onDispose { onRegisterPopToRoot?.invoke(null) }
    }

    NavHost(
        navController = navController,
        startDestination = "public_feed",
    ) {
        composable("public_feed") {
            PublicFeedScreen(
                uiState = uiState,
                isSignedIn = accountState.isSignedIn,
                inboxBadgeCount = inboxBadgeCount,
                onOpenSettings = onOpenSettings,
                onOpenInbox = onOpenInbox,
                onSignIn = accountViewModel::signIn,
                onSignUp = accountViewModel::signUp,
                onGoogleSignIn = { accountViewModel.signInWithGoogle(activityContext) },
                onVerifyCode = accountViewModel::verifySignupCode,
                onResendCode = accountViewModel::resendVerificationCode,
                onClearSignInSuccess = accountViewModel::clearSignInSuccess,
                accountState = accountState,
                onPearlClick = { pearlId ->
                    navController.navigate("public_pearl/$pearlId")
                },
                onResolveAvatarUrl = feedViewModel::fetchAvatarUrl,
                onOpenUserProfile = onOpenUserProfile,
                onLoadInitial = viewModel::loadInitial,
                onRefreshFeed = { viewModel.refreshFeed(force = true) },
                onLoadNextPage = viewModel::loadNextPage,
                onSectionSelected = viewModel::setSection,
                onContentTypeSelected = viewModel::setContentTypeFilter,
                onResetContentTypeFilter = viewModel::resetContentTypeFilter,
                onDismissEmptyFilterAlert = viewModel::dismissEmptyFilterAlert,
                onDismissActionSuccess = viewModel::dismissActionSuccess,
                onDismissError = viewModel::dismissError,
                onDismissSeenToast = viewModel::dismissSeenToast,
                onOpenSavePicker = viewModel::openSavePicker,
                onHidePearl = viewModel::hide,
                onSaveToMyFeed = viewModel::addToMyFeed,
                onSaveToFolder = { pearl, folder ->
                    viewModel.saveToFolder(pearl, folder.folder.id, folder.folder.name)
                },
                onCreateFolderAndSave = { pearl, name ->
                    viewModel.createFolderAndSavePearl(pearl, name)
                },
                isNetworkAvailable = connectivityState.isConnected,
                isOfflineMode = connectivityState.isOfflineMode,
                onRetryConnection = onRetryConnection,
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
                showSharedPearlIntro = showSharedPearlIntro,
                onRequestSharedPearlIntro = { showSharedPearlIntro = true },
                onSharedPearlIntroContinue = {
                    showSharedPearlIntro = false
                    captureMenuOpen = true
                },
                onSharedPearlIntroCancel = { showSharedPearlIntro = false },
                showsSectionTabs = showsSectionTabs,
            )
        }

        composable("capture/quick") {
            QuickTextCaptureScreen(
                viewModel = captureViewModel,
                isSignedIn = accountState.isSignedIn,
                captureDestination = publicFeedDestination,
                onBack = { navController.popBackStack() },
                onSaved = onPublicCaptureSaved,
            )
        }
        composable("capture/link") {
            WebLinkCaptureScreen(
                viewModel = captureViewModel,
                isSignedIn = accountState.isSignedIn,
                captureDestination = publicFeedDestination,
                onBack = { navController.popBackStack() },
                onSaved = onPublicCaptureSaved,
            )
        }
        composable("capture/clinical") {
            ClinicalCaseCaptureScreen(
                viewModel = captureViewModel,
                isSignedIn = accountState.isSignedIn,
                captureDestination = publicFeedDestination,
                onBack = { navController.popBackStack() },
                onSaved = onPublicCaptureSaved,
            )
        }
        composable("capture/media/{route}") { entry ->
            val route = entry.arguments?.getString("route")
            AddMediaCaptureScreen(
                viewModel = captureViewModel,
                isSignedIn = accountState.isSignedIn,
                initialRoute = route,
                captureDestination = publicFeedDestination,
                onBack = { navController.popBackStack() },
                onSaved = onPublicCaptureSaved,
            )
        }

        composable("public_pearl/{pearlId}") { entry ->
            val pearlId = entry.arguments?.getString("pearlId").orEmpty()
            val pearl = uiState.pearls.firstOrNull { it.id == pearlId }
            if (pearl == null) {
                PublicFeedScreen(
                    uiState = uiState,
                    isSignedIn = accountState.isSignedIn,
                    inboxBadgeCount = inboxBadgeCount,
                    onOpenSettings = onOpenSettings,
                    onOpenInbox = onOpenInbox,
                    onSignIn = accountViewModel::signIn,
                    onSignUp = accountViewModel::signUp,
                    onGoogleSignIn = { accountViewModel.signInWithGoogle(activityContext) },
                    onVerifyCode = accountViewModel::verifySignupCode,
                    onResendCode = accountViewModel::resendVerificationCode,
                    onClearSignInSuccess = accountViewModel::clearSignInSuccess,
                    accountState = accountState,
                    onPearlClick = {},
                    onResolveAvatarUrl = feedViewModel::fetchAvatarUrl,
                    onOpenUserProfile = onOpenUserProfile,
                    onLoadInitial = viewModel::loadInitial,
                onRefreshFeed = { viewModel.refreshFeed(force = true) },
                    onLoadNextPage = viewModel::loadNextPage,
                    onSectionSelected = viewModel::setSection,
                    onContentTypeSelected = viewModel::setContentTypeFilter,
                    onResetContentTypeFilter = viewModel::resetContentTypeFilter,
                    onDismissEmptyFilterAlert = viewModel::dismissEmptyFilterAlert,
                    onDismissActionSuccess = viewModel::dismissActionSuccess,
                    onDismissError = viewModel::dismissError,
                    onDismissSeenToast = viewModel::dismissSeenToast,
                    onOpenSavePicker = viewModel::openSavePicker,
                    onHidePearl = viewModel::hide,
                    onSaveToMyFeed = viewModel::addToMyFeed,
                    onSaveToFolder = { pearl, folder ->
                        viewModel.saveToFolder(pearl, folder.folder.id, folder.folder.name)
                    },
                    onCreateFolderAndSave = { pearl, name ->
                        viewModel.createFolderAndSavePearl(pearl, name)
                    },
                    isNetworkAvailable = connectivityState.isConnected,
                    isOfflineMode = connectivityState.isOfflineMode,
                    onRetryConnection = onRetryConnection,
                )
                return@composable
            }

            LaunchedEffect(pearl.id) {
                viewModel.markSeen(pearl, showToast = true)
            }

            PublicPearlDetailScreen(
                pearl = pearl,
                likeCount = pearl.likeCount,
                commentCount = viewModel.commentCount(pearl.id),
                isLiked = viewModel.isLiked(pearl.id),
                commentsVisible = uiState.commentsPearlId == pearl.id,
                comments = uiState.commentsForPearl,
                isCommentsLoading = uiState.isCommentsLoading,
                isPostingComment = uiState.isPostingComment,
                commentsError = uiState.commentsError,
                onResolveAvatarUrl = feedViewModel::fetchAvatarUrl,
                onBack = { navController.popBackStack() },
                onOpenUserProfile = onOpenUserProfile,
                onAddToMyFeed = { viewModel.addToMyFeed(pearl) },
                saveOutcome = uiState.actionOutcome,
                saveOutcomePearlTitle = uiState.actionSuccessMessage,
                onDismissSaveOutcome = viewModel::dismissActionSuccess,
                onHide = {
                    viewModel.hide(pearl)
                    navController.popBackStack()
                },
                onToggleLike = { viewModel.toggleLike(pearl) },
                onOpenComments = { viewModel.openComments(pearl.id) },
                onCloseComments = viewModel::closeComments,
                onPostComment = { body -> viewModel.postComment(pearl.id, body) },
            )
        }
    }
}
