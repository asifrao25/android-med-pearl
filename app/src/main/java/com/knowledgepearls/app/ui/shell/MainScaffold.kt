package com.knowledgepearls.app.ui.shell

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowledgepearls.app.ui.account.AccountViewModel
import com.knowledgepearls.app.ui.account.AuthScreen
import com.knowledgepearls.app.ui.account.EditProfileScreen
import com.knowledgepearls.app.data.model.normalizeUserId
import com.knowledgepearls.app.ui.profile.UserProfileScreen
import com.knowledgepearls.app.ui.account.ProfileSetupScreen
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.connectivity.BackendHealthMonitor
import com.knowledgepearls.app.data.connectivity.ConnectivityMonitor
import com.knowledgepearls.app.ui.components.ConnectivityOverlays
import com.knowledgepearls.app.ui.components.interactiveKeyboardDismiss
import com.knowledgepearls.app.ui.components.InboxUnreadReminderChip
import com.knowledgepearls.app.ui.components.CacheClearedSuccessAlert
import com.knowledgepearls.app.ui.components.LiquidTabBar
import com.knowledgepearls.app.navigation.AppNavigationBus
import com.knowledgepearls.app.navigation.AppNavigationEvent
import com.knowledgepearls.app.navigation.ShareImportPayload
import com.knowledgepearls.app.ui.components.PearlShareReceivedToast
import com.knowledgepearls.app.ui.feed.FeedAuthorContext
import com.knowledgepearls.app.ui.feed.FeedViewModel
import com.knowledgepearls.app.ui.folders.FolderContentsScreen
import com.knowledgepearls.app.ui.folders.FoldersViewModel
import com.knowledgepearls.app.ui.messaging.InboxScreen
import com.knowledgepearls.app.ui.messaging.InboxViewModel
import com.knowledgepearls.app.ui.settings.SettingsRoute
import com.knowledgepearls.app.ui.settings.SettingsScreen
import com.knowledgepearls.app.ui.settings.SettingsViewModel
import com.knowledgepearls.app.ui.tabs.FavouritesTabScreen
import com.knowledgepearls.app.ui.tabs.FeedTabScreen
import com.knowledgepearls.app.ui.tabs.PublicFeedTabScreen
import com.knowledgepearls.app.ui.publicfeed.PublicFeedViewModel
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.DisposableEffect

@Composable
fun MainScaffold(
    accountViewModel: AccountViewModel = hiltViewModel(),
    foldersViewModel: FoldersViewModel = hiltViewModel(),
    feedViewModel: FeedViewModel = hiltViewModel(),
    publicFeedViewModel: PublicFeedViewModel = hiltViewModel(),
    inboxViewModel: InboxViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    connectivityMonitor: ConnectivityMonitor,
    backendHealthMonitor: BackendHealthMonitor,
    navigationBus: AppNavigationBus,
    initialShareImport: ShareImportPayload? = null,
    onShareImportConsumed: () -> Unit = {},
) {
    val accountState by accountViewModel.uiState.collectAsStateWithLifecycle()
    val publicFeedState by publicFeedViewModel.uiState.collectAsStateWithLifecycle()
    val inboxState by inboxViewModel.inboxState.collectAsStateWithLifecycle()
    val threadState by inboxViewModel.threadState.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val appearanceMode by settingsViewModel.appearanceMode.collectAsStateWithLifecycle()
    val connectivityState by connectivityMonitor.state.collectAsStateWithLifecycle()
    val backendHealthState by backendHealthMonitor.state.collectAsStateWithLifecycle()
    val activityContext = LocalContext.current
    val scope = rememberCoroutineScope()

    var showSplash by rememberSaveable { mutableStateOf(true) }
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Feed) }
    var tabBeforeFolders by rememberSaveable { mutableStateOf(MainTab.Feed) }
    var foldersMenuOpen by rememberSaveable { mutableStateOf(false) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var settingsRoute by rememberSaveable { mutableStateOf(SettingsRoute.Main) }
    var inboxOpen by rememberSaveable { mutableStateOf(false) }
    var authOpen by rememberSaveable { mutableStateOf(false) }
    var editProfileOpen by rememberSaveable { mutableStateOf(false) }
    var profileUserId by rememberSaveable { mutableStateOf<String?>(null) }
    var openedFolderId by rememberSaveable { mutableStateOf<String?>(null) }
    var openedFolderName by rememberSaveable { mutableStateOf<String?>(null) }
    var inboxBadgeCount by rememberSaveable { mutableIntStateOf(0) }
    var inboxReminderDismissed by rememberSaveable { mutableStateOf(false) }
    var shareImport by remember { mutableStateOf(initialShareImport) }
    var pearlShareToast by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(initialShareImport) {
        if (initialShareImport != null) {
            shareImport = initialShareImport
        }
    }

    LaunchedEffect(Unit) {
        navigationBus.events.collect { event ->
            when (event) {
                AppNavigationEvent.OpenInbox -> {
                    selectedTab = MainTab.PublicFeed
                    inboxOpen = true
                    inboxViewModel.loadInbox()
                }
                AppNavigationEvent.OpenSharedPearls -> {
                    selectedTab = MainTab.PublicFeed
                    inboxOpen = true
                    inboxViewModel.openSharedPearlsTab()
                    inboxViewModel.loadInbox()
                }
                is AppNavigationEvent.OpenPearlShare -> {
                    selectedTab = MainTab.PublicFeed
                    inboxOpen = true
                    inboxViewModel.openPearlShareById(event.shareId)
                }
                is AppNavigationEvent.OpenConversation -> {
                    selectedTab = MainTab.PublicFeed
                    inboxOpen = true
                    inboxViewModel.openConversationById(event.conversationId)
                }
                is AppNavigationEvent.ImportShare -> {
                    selectedTab = MainTab.Feed
                    shareImport = ShareImportPayload(event.text, event.url)
                }
                is AppNavigationEvent.PearlShareReceivedToast -> {
                    pearlShareToast = event.senderName to event.pearlTitle
                    inboxViewModel.refreshBadge()
                }
            }
        }
    }

    val feedAuthorContext = FeedAuthorContext(
        userId = accountState.userId,
        userEmail = accountState.userEmail,
        userProfile = accountState.userProfile,
    )

    LaunchedEffect(Unit) {
        accountViewModel.restoreSession()
    }

    LaunchedEffect(showSplash, accountState.isSignedIn) {
        if (!showSplash && accountState.isSignedIn) {
            accountViewModel.runForegroundSync()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, showSplash, accountState.isSignedIn, selectedTab) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && !showSplash && accountState.isSignedIn) {
                inboxViewModel.refreshBadge()
            }
            if (
                event == Lifecycle.Event.ON_RESUME &&
                !showSplash &&
                accountState.isSignedIn &&
                selectedTab == MainTab.PublicFeed
            ) {
                publicFeedViewModel.refreshFeed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var wasNetworkConnected by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(connectivityState.isConnected, connectivityState.isOfflineMode, showSplash, accountState.isSignedIn) {
        val connected = connectivityState.isConnected && !connectivityState.isOfflineMode
        if (
            !showSplash &&
            accountState.isSignedIn &&
            connected &&
            !wasNetworkConnected
        ) {
            publicFeedViewModel.refreshFeed()
        }
        wasNetworkConnected = connected
    }

    LaunchedEffect(selectedTab, showSplash, accountState.isSignedIn) {
        if (showSplash || !accountState.isSignedIn || selectedTab != MainTab.PublicFeed) return@LaunchedEffect
        publicFeedViewModel.refreshFeed()
        while (true) {
            delay(PUBLIC_FEED_AUTO_REFRESH_MS)
            publicFeedViewModel.refreshFeed()
        }
    }

    LaunchedEffect(accountState.isSignedIn, showSplash) {
        if (!showSplash && accountState.isSignedIn) {
            inboxViewModel.refreshBadge()
            inboxBadgeCount = inboxState.unreadBadge
            publicFeedViewModel.refreshFeed()
        } else if (!accountState.isSignedIn) {
            inboxBadgeCount = 0
        }
    }

    LaunchedEffect(inboxState.unreadBadge) {
        inboxBadgeCount = inboxState.unreadBadge
        if (inboxState.unreadBadge > 0) {
            inboxReminderDismissed = false
        }
    }

    LaunchedEffect(settingsOpen) {
        if (settingsOpen) {
            settingsViewModel.loadPendingSubmissions()
        } else {
            settingsRoute = SettingsRoute.Main
        }
    }

    val backdropTab = when {
        selectedTab == MainTab.Folders || foldersMenuOpen -> tabBeforeFolders
        else -> selectedTab
    }

    val openUserProfile: (String) -> Unit = { userId ->
        profileUserId = userId
    }

    Box(Modifier.fillMaxSize().interactiveKeyboardDismiss()) {
        Crossfade(targetState = backdropTab, label = "tabBackdrop") { tab ->
            when (tab) {
                MainTab.Feed -> FeedTabScreen(
                    onOpenSettings = { settingsOpen = true },
                    onSignInRequired = { authOpen = true },
                    onOpenUserProfile = openUserProfile,
                    onOpenInbox = {
                        inboxOpen = true
                        inboxViewModel.loadInbox()
                    },
                    inboxBadgeCount = inboxBadgeCount,
                    shareImport = shareImport,
                    onShareImportConsumed = {
                        shareImport = null
                        onShareImportConsumed()
                    },
                )
                MainTab.Favourites -> FavouritesTabScreen(
                    onOpenSettings = { settingsOpen = true },
                    onSignInRequired = { authOpen = true },
                    onOpenUserProfile = openUserProfile,
                    onOpenInbox = {
                        inboxOpen = true
                        inboxViewModel.loadInbox()
                    },
                    inboxBadgeCount = inboxBadgeCount,
                )
                MainTab.PublicFeed -> PublicFeedTabScreen(
                    onOpenSettings = { settingsOpen = true },
                    onOpenInbox = {
                        inboxOpen = true
                        inboxViewModel.loadInbox()
                    },
                    inboxBadgeCount = inboxBadgeCount,
                    connectivityState = connectivityState,
                    onRetryConnection = connectivityMonitor::retryConnection,
                    onSignIn = { authOpen = true },
                    onOpenUserProfile = openUserProfile,
                    viewModel = publicFeedViewModel,
                    accountViewModel = accountViewModel,
                )
                MainTab.Folders -> FeedTabScreen(
                    onOpenSettings = { settingsOpen = true },
                    onSignInRequired = { authOpen = true },
                    onOpenUserProfile = openUserProfile,
                )
            }
        }

        FolderMenuOverlay(
            visible = foldersMenuOpen,
            viewModel = foldersViewModel,
            onDismiss = { foldersMenuOpen = false },
            onSelectFolder = { folder ->
                openedFolderId = folder.folder.id
                openedFolderName = folder.folder.name
                foldersMenuOpen = false
            },
        )

        openedFolderId?.let { folderId ->
            openedFolderName?.let { folderName ->
                FolderContentsScreen(
                    folderId = folderId,
                    folderName = folderName,
                    viewModel = foldersViewModel,
                    feedAuthorContext = feedAuthorContext,
                    onResolveAvatarUrl = feedViewModel::fetchAvatarUrl,
                    onOpenUserProfile = openUserProfile,
                    onClose = {
                        openedFolderId = null
                        openedFolderName = null
                    },
                    onSignInRequired = { authOpen = true },
                )
            }
        }

        LiquidTabBar(
            selected = selectedTab,
            publicFeedNewCount = publicFeedState.newCount,
            foldersMenuOpen = foldersMenuOpen,
            onTabSelected = { tab ->
                foldersMenuOpen = false
                selectedTab = tab
            },
            onFoldersTap = {
                if (foldersMenuOpen) {
                    foldersMenuOpen = false
                } else {
                    if (selectedTab != MainTab.Folders) {
                        tabBeforeFolders = selectedTab
                    }
                    selectedTab = MainTab.Folders
                    foldersMenuOpen = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = PearlLayout.tabBarBottomPadding),
        )

        SettingsScreen(
            visible = settingsOpen,
            route = settingsRoute,
            accountState = accountState,
            settingsState = settingsState,
            appearanceMode = appearanceMode,
            onDismiss = { settingsOpen = false },
            onNavigate = { settingsRoute = it },
            onSignIn = { authOpen = true },
            onOpenProfile = { accountState.userId?.let(openUserProfile) },
            onSignOut = { accountViewModel.signOut() },
            onLoadPending = settingsViewModel::loadPendingSubmissions,
            onWithdrawSubmission = settingsViewModel::withdrawSubmission,
            onSetAppearance = settingsViewModel::setAppearanceMode,
            onLoadBackups = settingsViewModel::loadBackups,
            onCreateBackup = settingsViewModel::createBackup,
            onRestoreBackup = settingsViewModel::restoreBackup,
            onMeasureCache = settingsViewModel::measureCache,
            onClearCache = settingsViewModel::clearCache,
            onDeleteAccount = {
                settingsViewModel.deleteAccount {
                    settingsOpen = false
                    accountViewModel.signOut()
                }
            },
        )

        if (inboxOpen) {
            InboxScreen(
                inboxState = inboxState,
                threadState = threadState,
                onDismiss = {
                    inboxOpen = false
                    inboxViewModel.closeThread()
                    inboxViewModel.refreshBadge()
                },
                onLoad = inboxViewModel::loadInbox,
                onTabSelected = inboxViewModel::setTab,
                onConversationClick = inboxViewModel::openConversation,
                onCloseThread = inboxViewModel::closeThread,
                onSendMessage = inboxViewModel::sendMessage,
                onAcceptShare = { shareId -> inboxViewModel.respondToShare(shareId, accept = true) },
                onDeclineShare = { shareId -> inboxViewModel.respondToShare(shareId, accept = false) },
            )
        }

        if (authOpen) {
            AuthScreen(
                uiState = accountState,
                onDismiss = { authOpen = false },
                onSignIn = accountViewModel::signIn,
                onSignUp = accountViewModel::signUp,
                onGoogleSignIn = { accountViewModel.signInWithGoogle(activityContext) },
                onVerifyCode = accountViewModel::verifySignupCode,
                onResendCode = accountViewModel::resendVerificationCode,
                onClearSignInSuccess = accountViewModel::clearSignInSuccess,
            )
        }

        profileUserId?.let { userId ->
            UserProfileScreen(
                userId = userId,
                isOwnProfile = normalizeUserId(userId) == accountState.userId?.let(::normalizeUserId),
                isSignedIn = accountState.isSignedIn,
                onDismiss = {
                    profileUserId = null
                    accountViewModel.refreshProfile()
                },
                onEditProfile = {
                    profileUserId = null
                    editProfileOpen = true
                },
                onDeleteAccount = {
                    settingsViewModel.deleteAccount {
                        profileUserId = null
                        settingsOpen = false
                        accountViewModel.signOut()
                    }
                },
                onSignInRequired = { authOpen = true },
                onOpenMessage = { target ->
                    profileUserId = null
                    inboxOpen = true
                    inboxViewModel.openConversationWithUser(
                        otherUserId = target.otherUserId,
                        otherDisplayName = target.otherDisplayName,
                        otherAvatarUrl = target.otherAvatarUrl,
                    )
                },
                onBlockUser = { blockedUserId ->
                    publicFeedViewModel.blockUser(blockedUserId)
                },
            )
        }

        if (editProfileOpen && accountState.isSignedIn) {
            EditProfileScreen(
                uiState = accountState,
                onUpdateProfile = accountViewModel::updateProfile,
                onUploadAvatar = accountViewModel::uploadAvatar,
                onDismiss = {
                    editProfileOpen = false
                    accountViewModel.refreshProfile()
                },
            )
        }

        if (!showSplash && accountState.needsProfileSetup) {
            ProfileSetupScreen(
                uiState = accountState,
                onCreateProfile = accountViewModel::createProfile,
                onUploadAvatar = accountViewModel::uploadAvatar,
                onSignOut = accountViewModel::signOut,
            )
        }

        if (showSplash) {
            LaunchSplashScreen(onFinished = { showSplash = false })
        }

        val showInboxReminder = !showSplash &&
            accountState.isSignedIn &&
            !inboxOpen &&
            !inboxReminderDismissed &&
            inboxBadgeCount > 0 &&
            (selectedTab == MainTab.Feed || selectedTab == MainTab.PublicFeed)

        if (showInboxReminder) {
            InboxUnreadReminderChip(
                unreadCount = inboxBadgeCount,
                theme = if (selectedTab == MainTab.PublicFeed) TabTheme.PublicFeed else TabTheme.Feed,
                onOpenInbox = {
                    inboxReminderDismissed = true
                    inboxOpen = true
                },
                onDismiss = { inboxReminderDismissed = true },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 72.dp),
            )
        }

        settingsState.cacheClearedAlert?.let { alert ->
            CacheClearedSuccessAlert(
                bytesFreedLabel = alert.bytesFreedLabel,
                effectSummary = alert.effectSummary,
                theme = TabTheme.Settings,
                onDismiss = settingsViewModel::dismissCacheClearedAlert,
            )
        }

        ConnectivityOverlays(
            connectivity = connectivityState,
            backendHealth = backendHealthState,
            isActive = !showSplash,
            onContinueOffline = connectivityMonitor::continueOffline,
            onRetryConnection = connectivityMonitor::retryConnection,
            onDismissBackendAlert = backendHealthMonitor::dismissUnavailableAlert,
            onRetryBackend = backendHealthMonitor::retryNow,
            onDismissRestoredToast = backendHealthMonitor::dismissRestoredNotice,
            scope = scope,
        )

        pearlShareToast?.let { (sender, title) ->
            PearlShareReceivedToast(
                visible = true,
                senderName = sender,
                onOpenInbox = {
                    pearlShareToast = null
                    inboxOpen = true
                    inboxViewModel.loadInbox()
                },
                onDismiss = { pearlShareToast = null },
            )
        }
    }
}

private const val PUBLIC_FEED_AUTO_REFRESH_MS = 45_000L
