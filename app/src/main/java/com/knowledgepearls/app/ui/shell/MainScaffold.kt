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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowledgepearls.app.ui.account.AccountViewModel
import com.knowledgepearls.app.ui.account.AuthScreen
import com.knowledgepearls.app.ui.account.EditProfileScreen
import com.knowledgepearls.app.ui.account.ProfileSetupScreen
import com.knowledgepearls.app.ui.components.LiquidTabBar
import com.knowledgepearls.app.ui.feed.FeedAuthorContext
import com.knowledgepearls.app.ui.feed.FeedViewModel
import com.knowledgepearls.app.ui.folders.FolderContentsScreen
import com.knowledgepearls.app.ui.folders.FoldersViewModel
import com.knowledgepearls.app.ui.tabs.FavouritesTabScreen
import com.knowledgepearls.app.ui.tabs.FeedTabScreen
import com.knowledgepearls.app.ui.tabs.PublicFeedTabScreen
import com.knowledgepearls.app.ui.publicfeed.PublicFeedViewModel
import com.knowledgepearls.app.ui.theme.PearlLayout

@Composable
fun MainScaffold(
    accountViewModel: AccountViewModel = hiltViewModel(),
    foldersViewModel: FoldersViewModel = hiltViewModel(),
    feedViewModel: FeedViewModel = hiltViewModel(),
    publicFeedViewModel: PublicFeedViewModel = hiltViewModel(),
) {
    val accountState by accountViewModel.uiState.collectAsStateWithLifecycle()
    val publicFeedState by publicFeedViewModel.uiState.collectAsStateWithLifecycle()
    val activityContext = LocalContext.current

    var showSplash by rememberSaveable { mutableStateOf(true) }
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Feed) }
    var tabBeforeFolders by rememberSaveable { mutableStateOf(MainTab.Feed) }
    var foldersMenuOpen by rememberSaveable { mutableStateOf(false) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var authOpen by rememberSaveable { mutableStateOf(false) }
    var editProfileOpen by rememberSaveable { mutableStateOf(false) }
    var openedFolderId by rememberSaveable { mutableStateOf<String?>(null) }
    var openedFolderName by rememberSaveable { mutableStateOf<String?>(null) }

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

    val backdropTab = when {
        selectedTab == MainTab.Folders || foldersMenuOpen -> tabBeforeFolders
        else -> selectedTab
    }

    Box(Modifier.fillMaxSize()) {
        Crossfade(targetState = backdropTab, label = "tabBackdrop") { tab ->
            when (tab) {
                MainTab.Feed -> FeedTabScreen(onOpenSettings = { settingsOpen = true })
                MainTab.Favourites -> FavouritesTabScreen(onOpenSettings = { settingsOpen = true })
                MainTab.PublicFeed -> PublicFeedTabScreen(
                    onOpenSettings = { settingsOpen = true },
                    onSignIn = { authOpen = true },
                    viewModel = publicFeedViewModel,
                    accountViewModel = accountViewModel,
                )
                MainTab.Folders -> FeedTabScreen(onOpenSettings = { settingsOpen = true })
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
                    onClose = {
                        openedFolderId = null
                        openedFolderName = null
                    },
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

        SettingsSheet(
            visible = settingsOpen,
            accountState = accountState,
            onDismiss = { settingsOpen = false },
            onSignIn = { authOpen = true },
            onEditProfile = {
                settingsOpen = false
                editProfileOpen = true
            },
            onSignOut = { accountViewModel.signOut() },
        )

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

        if (editProfileOpen && accountState.isSignedIn) {
            EditProfileScreen(
                uiState = accountState,
                onUpdateProfile = accountViewModel::updateProfile,
                onUploadAvatar = accountViewModel::uploadAvatar,
                onDismiss = { editProfileOpen = false },
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
    }
}
