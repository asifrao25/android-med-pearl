package com.knowledgepearls.app.ui.shell

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.zIndex
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
import com.knowledgepearls.app.data.badges.InboxBadgeEntryPoint
import com.knowledgepearls.app.data.connectivity.BackendHealthMonitor
import com.knowledgepearls.app.data.connectivity.ConnectivityMonitor
import com.knowledgepearls.app.ui.components.ConnectivityOverlays
import com.knowledgepearls.app.ui.components.interactiveKeyboardDismiss
import com.knowledgepearls.app.ui.components.FeedChromeAnchor
import com.knowledgepearls.app.ui.components.FeedChromeMetrics
import com.knowledgepearls.app.ui.components.FloatingInboxButton
import com.knowledgepearls.app.ui.components.FloatingInboxReminderCallout
import com.knowledgepearls.app.ui.components.CacheClearedSuccessAlert
import com.knowledgepearls.app.ui.components.LiquidTabBar
import com.knowledgepearls.app.ui.components.LocalFeedChromeVisibility
import com.knowledgepearls.app.ui.components.feedChromeSlide
import com.knowledgepearls.app.ui.components.rememberFeedChromeVisibility
import com.knowledgepearls.app.navigation.AppNavigationBus
import com.knowledgepearls.app.navigation.AppNavigationEvent
import com.knowledgepearls.app.navigation.ShareImportPayload
import com.knowledgepearls.app.ui.components.PearlActionSuccessToast
import com.knowledgepearls.app.ui.components.PearlActionOutcome
import com.knowledgepearls.app.ui.components.PearlShareReceivedToast
import com.knowledgepearls.app.ui.favourites.FavouritesViewModel
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
import com.knowledgepearls.app.ui.publicfeed.MovedToSeenTabToast
import com.knowledgepearls.app.ui.publicfeed.PublicFeedSectionTabs
import com.knowledgepearls.app.ui.publicfeed.PublicFeedSeenFocusScrim
import com.knowledgepearls.app.ui.publicfeed.PublicFeedViewModel
import com.knowledgepearls.app.ui.publicfeed.PublicPearlSaveOverlay
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.DisposableEffect

@Composable
fun MainScaffold(
    accountViewModel: AccountViewModel = hiltViewModel(),
    foldersViewModel: FoldersViewModel = hiltViewModel(),
    feedViewModel: FeedViewModel = hiltViewModel(),
    publicFeedViewModel: PublicFeedViewModel = hiltViewModel(),
    favouritesViewModel: FavouritesViewModel = hiltViewModel(),
    inboxViewModel: InboxViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    connectivityMonitor: ConnectivityMonitor,
    backendHealthMonitor: BackendHealthMonitor,
    navigationBus: AppNavigationBus,
    initialShareImport: ShareImportPayload? = null,
    onShareImportConsumed: () -> Unit = {},
    onRequestPushNotifications: () -> Unit = {},
) {
    val accountState by accountViewModel.uiState.collectAsStateWithLifecycle()
    val publicFeedState by publicFeedViewModel.uiState.collectAsStateWithLifecycle()
    val feedState by feedViewModel.uiState.collectAsStateWithLifecycle()
    val folders by foldersViewModel.foldersWithCounts.collectAsStateWithLifecycle()
    val inboxState by inboxViewModel.inboxState.collectAsStateWithLifecycle()
    val threadState by inboxViewModel.threadState.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val appearanceMode by settingsViewModel.appearanceMode.collectAsStateWithLifecycle()
    val appFontChoice by settingsViewModel.appFontChoice.collectAsStateWithLifecycle()
    val connectivityState by connectivityMonitor.state.collectAsStateWithLifecycle()
    val backendHealthState by backendHealthMonitor.state.collectAsStateWithLifecycle()
    val activityContext = LocalContext.current
    val scope = rememberCoroutineScope()
    val inboxLauncherBadgeManager = remember {
        EntryPointAccessors.fromApplication(
            activityContext.applicationContext,
            InboxBadgeEntryPoint::class.java,
        ).inboxLauncherBadgeManager()
    }

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
    var pendingPublicPearlId by rememberSaveable { mutableStateOf<String?>(null) }
    var feedRootVisible by rememberSaveable { mutableStateOf(true) }
    var publicFeedRootVisible by rememberSaveable { mutableStateOf(true) }
    val tabPopHandlers = remember {
        object {
            var feed: (() -> Unit)? = null
            var favourites: (() -> Unit)? = null
            var publicFeed: (() -> Unit)? = null
            var folder: (() -> Unit)? = null
        }
    }

    fun reselectTab(tab: MainTab) {
        when (tab) {
            MainTab.Feed -> tabPopHandlers.feed?.invoke()
            MainTab.Favourites -> tabPopHandlers.favourites?.invoke()
            MainTab.PublicFeed -> tabPopHandlers.publicFeed?.invoke()
            MainTab.Folders -> {
                when {
                    openedFolderId != null -> tabPopHandlers.folder?.invoke()
                    foldersMenuOpen -> foldersMenuOpen = false
                    else -> {
                        if (selectedTab != MainTab.Folders) {
                            tabBeforeFolders = selectedTab
                        }
                        selectedTab = MainTab.Folders
                        foldersMenuOpen = true
                    }
                }
            }
        }
    }

    fun openFoldersDrawer() {
        if (selectedTab != MainTab.Folders) {
            tabBeforeFolders = selectedTab
        }
        selectedTab = MainTab.Folders
        openedFolderId = null
        openedFolderName = null
        foldersMenuOpen = true
    }

    LaunchedEffect(selectedTab) {
        feedViewModel.setSearchActive(false)
        publicFeedViewModel.setSearchActive(false)
        favouritesViewModel.setSearchActive(false)
    }

    LaunchedEffect(settingsOpen, inboxOpen, foldersMenuOpen, openedFolderId, profileUserId, authOpen, editProfileOpen) {
        if (settingsOpen || inboxOpen || foldersMenuOpen || openedFolderId != null || profileUserId != null || authOpen || editProfileOpen) {
            feedViewModel.setSearchActive(false)
            publicFeedViewModel.setSearchActive(false)
            favouritesViewModel.setSearchActive(false)
        }
    }

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
                is AppNavigationEvent.OpenPublicFeed -> {
                    selectedTab = MainTab.PublicFeed
                }
                is AppNavigationEvent.OpenPublicPearl -> {
                    selectedTab = MainTab.PublicFeed
                    pendingPublicPearlId = event.pearlId
                }
                is AppNavigationEvent.OpenPendingSubmissions -> {
                    settingsOpen = true
                    settingsRoute = SettingsRoute.PendingSubmissions
                    settingsViewModel.loadPendingSubmissions()
                }
                is AppNavigationEvent.ImportShare -> {
                    selectedTab = MainTab.Feed
                    shareImport = ShareImportPayload(event.text, event.url)
                }
                is AppNavigationEvent.PearlShareReceivedToast -> {
                    pearlShareToast = event.senderName to event.pearlTitle
                    inboxViewModel.refreshBadge()
                }
                AppNavigationEvent.RefreshInboxBadge -> {
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

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, showSplash, accountState.isSignedIn) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && !showSplash && accountState.isSignedIn) {
                inboxViewModel.refreshBadge()
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
            !wasNetworkConnected &&
            selectedTab == MainTab.PublicFeed
        ) {
            publicFeedViewModel.refreshFeed(force = true)
        }
        wasNetworkConnected = connected
    }

    LaunchedEffect(selectedTab, showSplash, accountState.isSignedIn) {
        if (showSplash || !accountState.isSignedIn || selectedTab != MainTab.PublicFeed) return@LaunchedEffect
        publicFeedViewModel.onTabEntered()
        while (true) {
            delay(PUBLIC_FEED_STALE_CHECK_MS)
            publicFeedViewModel.refreshFeedIfStale()
        }
    }

    LaunchedEffect(accountState.isSignedIn, showSplash) {
        if (!showSplash && accountState.isSignedIn) {
            onRequestPushNotifications()
            inboxViewModel.refreshBadge()
        } else if (!accountState.isSignedIn) {
            inboxBadgeCount = 0
        }
    }

    LaunchedEffect(accountState.isSignedIn, showSplash) {
        if (showSplash || !accountState.isSignedIn) return@LaunchedEffect
        while (true) {
            delay(INBOX_BADGE_REFRESH_MS)
            inboxViewModel.refreshBadge()
        }
    }

    LaunchedEffect(inboxState.unreadBadge) {
        inboxBadgeCount = inboxState.unreadBadge
        if (inboxState.unreadBadge > 0) {
            inboxReminderDismissed = false
        }
    }

    LaunchedEffect(inboxBadgeCount, accountState.isSignedIn, showSplash) {
        if (showSplash || !accountState.isSignedIn) {
            inboxLauncherBadgeManager.clear()
        } else {
            inboxLauncherBadgeManager.sync(inboxBadgeCount)
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

    val feedChromeVisibility = rememberFeedChromeVisibility()

    LaunchedEffect(selectedTab) {
        feedChromeVisibility.forceShow()
    }

    CompositionLocalProvider(LocalFeedChromeVisibility provides feedChromeVisibility) {
    Box(Modifier.fillMaxSize().interactiveKeyboardDismiss()) {
        val onFeedListScreen = when (selectedTab) {
            MainTab.Feed -> feedRootVisible
            MainTab.PublicFeed -> publicFeedRootVisible
            else -> false
        }

        val blockingFloatingChromeOverlay =
            publicFeedState.savePickerPearl != null ||
                publicFeedState.showEmptyFilterAlert ||
                publicFeedState.actionOutcome == PearlActionOutcome.AlreadyInMyFeed ||
                publicFeedState.actionOutcome == PearlActionOutcome.SavedToMyFeed ||
                publicFeedState.actionOutcome == PearlActionOutcome.SavedToFolder ||
                publicFeedState.actionOutcome == PearlActionOutcome.RemovedFromFeed ||
                (selectedTab == MainTab.Feed && feedState.showEmptyFilterAlert) ||
                settingsState.cacheClearedAlert != null ||
                pearlShareToast != null

        val showFloatingInboxChrome = !showSplash &&
            accountState.isSignedIn &&
            !inboxOpen &&
            !settingsOpen &&
            !authOpen &&
            !editProfileOpen &&
            !foldersMenuOpen &&
            openedFolderId == null &&
            profileUserId == null &&
            !accountState.needsProfileSetup &&
            onFeedListScreen &&
            !blockingFloatingChromeOverlay

        val addFabBottomPadding = when (selectedTab) {
            MainTab.PublicFeed -> {
                if (connectivityState.isConnected && !connectivityState.isOfflineMode) {
                    PearlLayout.publicFeedAddButtonBottomPadding
                } else {
                    PearlLayout.addButtonBottomPadding
                }
            }
            else -> PearlLayout.addButtonBottomPadding
        }

        val hasFloatingAddButton = when (selectedTab) {
            MainTab.Feed -> true
            MainTab.PublicFeed -> connectivityState.isConnected && !connectivityState.isOfflineMode
            else -> false
        }

        val inboxButtonBottomPadding = if (hasFloatingAddButton) {
            PearlLayout.inboxButtonBottomPadding(addFabBottomPadding)
        } else {
            addFabBottomPadding
        }

        val floatingChromeTheme = when (selectedTab) {
            MainTab.PublicFeed -> TabTheme.PublicFeed
            MainTab.Favourites -> TabTheme.Favourites
            else -> TabTheme.Feed
        }

        val showPublicFeedBottomChrome = selectedTab == MainTab.PublicFeed &&
            publicFeedRootVisible &&
            accountState.isSignedIn &&
            connectivityState.isConnected &&
            !connectivityState.isOfflineMode

        val publicFeedSeenFocus = showPublicFeedBottomChrome && publicFeedState.showSeenToast

        val showInboxReminder = showFloatingInboxChrome &&
            !inboxReminderDismissed &&
            inboxBadgeCount > 0

        val feedChromeScrollEnabled = onFeedListScreen &&
            (selectedTab == MainTab.Feed || selectedTab == MainTab.PublicFeed) &&
            !blockingFloatingChromeOverlay

        LaunchedEffect(feedChromeScrollEnabled) {
            if (!feedChromeScrollEnabled) {
                feedChromeVisibility.forceShow()
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
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
                    onFeedRootVisibilityChange = { feedRootVisible = it },
                    onRegisterPopToRoot = { handler -> tabPopHandlers.feed = handler },
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
                    onRegisterPopToRoot = { handler -> tabPopHandlers.favourites = handler },
                )
                MainTab.PublicFeed -> PublicFeedTabScreen(
                    onOpenSettings = { settingsOpen = true },
                    onOpenInbox = {
                        inboxOpen = true
                        inboxViewModel.loadInbox()
                    },
                    inboxBadgeCount = inboxBadgeCount,
                    onPublicFeedRootVisibilityChange = { publicFeedRootVisible = it },
                    onRegisterPopToRoot = { handler -> tabPopHandlers.publicFeed = handler },
                    connectivityState = connectivityState,
                    onRetryConnection = connectivityMonitor::retryConnection,
                    onOpenUserProfile = openUserProfile,
                    viewModel = publicFeedViewModel,
                    accountViewModel = accountViewModel,
                    initialPearlId = pendingPublicPearlId,
                    onInitialPearlConsumed = { pendingPublicPearlId = null },
                )
                MainTab.Folders -> FeedTabScreen(
                    onOpenSettings = { settingsOpen = true },
                    onSignInRequired = { authOpen = true },
                    onOpenUserProfile = openUserProfile,
                )
            }
        }

            if (showFloatingInboxChrome) {
                FloatingInboxButton(
                    badgeCount = inboxBadgeCount,
                    theme = floatingChromeTheme,
                    onClick = {
                        inboxReminderDismissed = true
                        inboxOpen = true
                        inboxViewModel.loadInbox()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .feedChromeSlide(
                            anchor = FeedChromeAnchor.Bottom,
                            hideDistance = FeedChromeMetrics.inboxFabHideDistance,
                            minAlpha = if (inboxBadgeCount > 0) 0.42f else 0f,
                        )
                        .padding(end = 20.dp, bottom = inboxButtonBottomPadding),
                )
            }

            if (showInboxReminder) {
                FloatingInboxReminderCallout(
                    visible = showInboxReminder,
                    unreadCount = inboxBadgeCount,
                    theme = floatingChromeTheme,
                    onOpenInbox = {
                        inboxReminderDismissed = true
                        inboxOpen = true
                        inboxViewModel.loadInbox()
                    },
                    onDismiss = { inboxReminderDismissed = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = PearlLayout.inboxReminderBottomPadding(inboxButtonBottomPadding)),
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
                selectedTab = MainTab.Folders
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
                    onRegisterPopToRoot = { handler -> tabPopHandlers.folder = handler },
                    onClose = {
                        openedFolderId = null
                        openedFolderName = null
                    },
                    onSignInRequired = { authOpen = true },
                )
            }
        }

        if (publicFeedSeenFocus) {
            PublicFeedSeenFocusScrim(
                visible = true,
                theme = TabTheme.PublicFeed,
                modifier = Modifier.fillMaxSize(),
            )
        }

        if (showPublicFeedBottomChrome) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .zIndex(2f)
                    .padding(
                        start = PearlLayout.screenHorizontalPadding,
                        end = PearlLayout.screenHorizontalPadding,
                        bottom = PearlLayout.publicFeedSectionTabsBottomPadding,
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MovedToSeenTabToast(
                    visible = publicFeedState.showSeenToast,
                    theme = TabTheme.PublicFeed,
                    onDismiss = publicFeedViewModel::dismissSeenToast,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                PublicFeedSectionTabs(
                    selected = publicFeedState.section,
                    newCount = publicFeedState.newCount,
                    seenCount = publicFeedState.seenCount,
                    theme = TabTheme.PublicFeed,
                    onSelected = publicFeedViewModel::setSection,
                    modifier = Modifier.feedChromeSlide(
                        anchor = FeedChromeAnchor.Bottom,
                        hideDistance = FeedChromeMetrics.sectionTabsHideDistance,
                    ),
                )
            }
        }

        LiquidTabBar(
            selected = selectedTab,
            publicFeedNewCount = publicFeedState.newCount,
            foldersMenuOpen = foldersMenuOpen,
            onTabSelected = { tab ->
                if (tab == selectedTab) {
                    reselectTab(tab)
                } else {
                    foldersMenuOpen = false
                    if (tab != MainTab.Folders) {
                        openedFolderId = null
                        openedFolderName = null
                    }
                    selectedTab = tab
                }
            },
            onFoldersTap = {
                if (selectedTab == MainTab.Folders && (openedFolderId != null || foldersMenuOpen)) {
                    reselectTab(MainTab.Folders)
                } else if (foldersMenuOpen) {
                    foldersMenuOpen = false
                } else {
                    openFoldersDrawer()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .feedChromeSlide(
                    anchor = FeedChromeAnchor.Bottom,
                    hideDistance = FeedChromeMetrics.tabBarHideDistance,
                )
                .padding(bottom = PearlLayout.tabBarBottomPadding),
        )

        publicFeedState.savePickerPearl?.let { pearl ->
            PublicPearlSaveOverlay(
                folders = folders,
                theme = TabTheme.PublicFeed,
                bottomInset = PearlLayout.tabBarOverlayInset,
                onSaveToMyFeed = {
                    publicFeedViewModel.dismissSavePicker()
                    publicFeedViewModel.addToMyFeed(pearl)
                },
                onSaveToFolder = { folder ->
                    publicFeedViewModel.dismissSavePicker()
                    publicFeedViewModel.saveToFolder(pearl, folder.folder.id, folder.folder.name)
                },
                onCreateFolder = { name ->
                    publicFeedViewModel.dismissSavePicker()
                    publicFeedViewModel.createFolderAndSavePearl(pearl, name)
                },
                onDismiss = publicFeedViewModel::dismissSavePicker,
                modifier = Modifier.fillMaxSize(),
            )
        }

        when (val outcome = publicFeedState.actionOutcome) {
            PearlActionOutcome.SavedToMyFeed,
            PearlActionOutcome.SavedToFolder,
            PearlActionOutcome.RemovedFromFeed,
            -> {
                PearlActionSuccessToast(
                    outcome = outcome,
                    theme = TabTheme.PublicFeed,
                    folderName = publicFeedState.actionSuccessMessage,
                    onDismiss = publicFeedViewModel::dismissActionSuccess,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = PearlLayout.tabBarOverlayInset),
                )
            }
            else -> Unit
        }

        SettingsScreen(
            visible = settingsOpen,
            route = settingsRoute,
            accountState = accountState,
            settingsState = settingsState,
            appearanceMode = appearanceMode,
            appFontChoice = appFontChoice,
            onDismiss = { settingsOpen = false },
            onNavigate = { settingsRoute = it },
            onSignIn = { authOpen = true },
            onOpenProfile = {
                settingsOpen = false
                accountState.userId?.let(openUserProfile)
            },
            onSignOut = { accountViewModel.signOut() },
            onLoadPending = settingsViewModel::loadPendingSubmissions,
            onWithdrawSubmission = settingsViewModel::withdrawSubmission,
            onSetAppearance = settingsViewModel::setAppearanceMode,
            onSetAppFontChoice = settingsViewModel::setAppFontChoice,
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
            onOpenUserProfile = { userId ->
                settingsOpen = false
                openUserProfile(userId)
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
}

private const val PUBLIC_FEED_STALE_CHECK_MS = 5 * 60_000L
private const val INBOX_BADGE_REFRESH_MS = 30_000L
