package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Share
import com.knowledgepearls.app.ui.components.PearlMaterialAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.local.model.clinicalCasePayload
import com.knowledgepearls.app.data.local.model.effectiveSourceReference
import com.knowledgepearls.app.data.local.model.isClinicalCase
import com.knowledgepearls.app.data.local.model.decodedPublicPearl
import com.knowledgepearls.app.data.local.model.isSharedToPublicFeed
import com.knowledgepearls.app.ui.components.DetailDockAction
import com.knowledgepearls.app.ui.components.LiquidDetailDock
import com.knowledgepearls.app.ui.components.SharedPearlSubmissionSuccessAlert
import com.knowledgepearls.app.ui.components.SharedPearlSubmitAlert
import com.knowledgepearls.app.ui.components.SourceReferenceRequiredAlert
import com.knowledgepearls.app.ui.folders.FolderPickerOverlay
import com.knowledgepearls.app.ui.folders.FoldersViewModel
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerOverlay
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerRequest
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun PearlDetailScreen(
    pearlId: String,
    viewModel: FeedViewModel,
    feedAuthorContext: FeedAuthorContext,
    onResolveAvatarUrl: suspend (String) -> String?,
    onBack: () -> Unit,
    isSignedIn: Boolean,
    onSignInRequired: () -> Unit,
    onOpenUserProfile: (String) -> Unit = {},
    foldersViewModel: FoldersViewModel = hiltViewModel(),
) {
    val pearl by viewModel.observePearl(pearlId).collectAsStateWithLifecycle(initialValue = null)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val folders by foldersViewModel.foldersWithCounts.collectAsStateWithLifecycle()
    val memberFolderIds by foldersViewModel.observePearlFolderIds(pearlId)
        .collectAsStateWithLifecycle(initialValue = emptySet())
    var showFolderPicker by remember { mutableStateOf(false) }
    var mediaViewerRequest by remember { mutableStateOf<PublicPearlMediaViewerRequest?>(null) }
    var showShareMenu by remember { mutableStateOf(false) }
    var showFriendShare by remember { mutableStateOf(false) }
    var showShareSubmitAlert by remember { mutableStateOf(false) }
    var showShareSuccessAlert by remember { mutableStateOf(false) }
    var showSourceReferenceRequired by remember { mutableStateOf(false) }
    val theme = TabTheme.Feed
    val darkTheme = isPearlDarkTheme()

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = theme, intensity = 0.5f)

        if (pearl == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading…", color = PearlColors.heroSecondary(darkTheme))
            }
        } else {
            val item = pearl!!
            val entity = item.pearl
            val publicPearl = entity.decodedPublicPearl()
            val isSharedToPublic = entity.isSharedToPublicFeed()
            val author = FeedPearlAuthorInfo.resolve(item, feedAuthorContext, publicPearl)
            val profileUserId = author.userId ?: feedAuthorContext.userId

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding(),
            ) {
                PearlDetailAuthorBar(
                    displayName = author.displayName,
                    avatarUrl = author.avatarUrl,
                    createdAtMillis = entity.createdAt,
                    theme = theme,
                    caption = if (entity.isSharedFromFriend) "Shared by" else null,
                    userId = author.userId,
                    onResolveAvatarUrl = onResolveAvatarUrl,
                    onOpenProfile = profileUserId?.let { userId -> { onOpenUserProfile(userId) } },
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = PearlLayout.screenHorizontalPadding)
                        .padding(top = 12.dp, bottom = PearlLayout.detailScrollBottomPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    PearlDetailTitleBar(title = entity.title.ifBlank { "Pearl" })

                    if (publicPearl != null) {
                        SavedPublicPearlDetailContent(
                            publicPearl = publicPearl,
                            theme = theme,
                            onOpenMedia = { mediaViewerRequest = it },
                        )
                    } else if (item.pearl.isClinicalCase()) {
                        LocalClinicalCaseDetailContent(
                            pearl = item,
                            theme = theme,
                            onOpenMedia = { mediaViewerRequest = it },
                        )
                    } else {
                        StandardLocalPearlDetailContent(
                            pearl = item,
                            theme = theme,
                            onOpenMedia = { mediaViewerRequest = it },
                        )
                    }
                }
            }

            LiquidDetailDock(
                theme = theme,
                onBack = onBack,
                actions = listOf(
                    DetailDockAction(
                        id = "folder",
                        label = "Folder",
                        icon = Icons.Default.Folder,
                        onClick = { showFolderPicker = true },
                    ),
                    DetailDockAction(
                        id = "favourite",
                        label = if (entity.isFavourite) "Favourited" else "Favourite",
                        icon = if (entity.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        tint = if (entity.isFavourite) TabTheme.Favourites.primary else null,
                        isActive = entity.isFavourite,
                        onClick = { viewModel.toggleFavourite(entity.id) },
                    ),
                    DetailDockAction(
                        id = "share",
                        label = if (isSharedToPublic) "Shared" else "Share",
                        icon = Icons.Default.Share,
                        tint = if (isSharedToPublic) TabTheme.PublicFeed.primary else null,
                        isActive = isSharedToPublic,
                        showsProgress = uiState.isSharingPearl,
                        disabled = uiState.isSharingPearl,
                        onClick = {
                            when {
                                !isSignedIn -> onSignInRequired()
                                entity.effectiveSourceReference().isBlank() -> showSourceReferenceRequired = true
                                else -> showShareMenu = true
                            }
                        },
                    ),
                ),
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        if (showFolderPicker && pearl != null) {
            FolderPickerOverlay(
                pearl = pearl!!,
                folders = folders,
                memberFolderIds = memberFolderIds,
                onDismiss = { showFolderPicker = false },
                onToggleFolder = { folderId ->
                    foldersViewModel.togglePearlFolderMembership(pearlId, folderId)
                },
                onCreateFolder = { name ->
                    foldersViewModel.createFolderAndAddPearl(name, pearlId)
                },
            )
        }

        PearlShareOptionsOverlay(
            visible = showShareMenu,
            isSharedToPublicFeed = pearl?.pearl?.isSharedToPublicFeed() == true,
            theme = theme,
            onDismiss = { showShareMenu = false },
            onSelect = { destination ->
                when (destination) {
                    PearlShareDestination.PublicFeed -> {
                        val current = pearl ?: return@PearlShareOptionsOverlay
                        if (current.pearl.isSharedToPublicFeed()) {
                            viewModel.withdrawPearlFromPublicFeed(current)
                        } else {
                            showShareSubmitAlert = true
                        }
                    }
                    PearlShareDestination.Friends -> showFriendShare = true
                }
            },
        )

        FriendShareOverlay(
            visible = showFriendShare,
            theme = TabTheme.PublicFeed,
            onDismiss = { showFriendShare = false },
            onSearch = viewModel::searchShareProfiles,
            onSend = { recipientIds ->
                pearl?.let { current ->
                    viewModel.sendFriendShare(current, recipientIds) {}
                }
            },
        )

        if (showShareSubmitAlert && pearl != null) {
            SharedPearlSubmitAlert(
                pearlTitle = pearl!!.pearl.title,
                theme = TabTheme.PublicFeed,
                onSubmit = {
                    showShareSubmitAlert = false
                    viewModel.sharePearlToPublicFeed(pearl!!) {
                        showShareSuccessAlert = true
                    }
                },
                onCancel = { showShareSubmitAlert = false },
            )
        }

        if (showShareSuccessAlert) {
            SharedPearlSubmissionSuccessAlert(
                theme = TabTheme.PublicFeed,
                onDismiss = { showShareSuccessAlert = false },
            )
        }

        if (showSourceReferenceRequired) {
            SourceReferenceRequiredAlert(
                theme = theme,
                onDismiss = { showSourceReferenceRequired = false },
            )
        }

        uiState.shareErrorMessage?.let { message ->
            PearlMaterialAlertDialog(
                onDismissRequest = viewModel::dismissShareError,
                confirmButton = {
                    Button(onClick = viewModel::dismissShareError) { Text("OK") }
                },
                title = { Text("Couldn't Share") },
                text = { Text(message) },
            )
        }

        uiState.actionSuccessMessage?.let { message ->
            PearlActionSuccessBanner(
                message = message,
                theme = theme,
                onDismiss = viewModel::dismissActionSuccess,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        PublicPearlMediaViewerOverlay(
            request = mediaViewerRequest,
            theme = theme,
            onDismiss = { mediaViewerRequest = null },
        )
    }
}

@Composable
private fun StandardLocalPearlDetailContent(
    pearl: PearlWithMedia,
    theme: TabTheme,
    onOpenMedia: (PublicPearlMediaViewerRequest) -> Unit,
) {
    val entity = pearl.pearl
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PearlDetailMediaSection(
            pearl = pearl,
            theme = theme,
            onOpenMedia = onOpenMedia,
            onOpenUrl = { url -> openExternalUrl(context, url) },
        )

        PearlDetailDescriptionSection(text = entity.notes, theme = theme)

        if (entity.sourceReference.isNotBlank()) {
            PearlDetailSection(
                title = "Source / Reference",
                body = entity.sourceReference,
                theme = theme,
            )
        }

        if (entity.tags.isNotEmpty()) {
            PearlDetailSection(
                title = "Tags",
                body = entity.tags.joinToString(", "),
                theme = theme,
                linkifyBody = false,
            )
        }
    }
}

@Composable
private fun LocalClinicalCaseDetailContent(
    pearl: PearlWithMedia,
    theme: TabTheme,
    onOpenMedia: (PublicPearlMediaViewerRequest) -> Unit,
) {
    val payload = pearl.pearl.clinicalCasePayload()

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        localClinicalSection("History", payload.history, null, pearl, theme, onOpenMedia)
        localClinicalSection("Examination", payload.examination, "examination", pearl, theme, onOpenMedia)
        localClinicalSection("Investigation", payload.investigation, "investigation", pearl, theme, onOpenMedia)
        localClinicalSection("Diagnosis", payload.diagnosis, null, pearl, theme, onOpenMedia)
        localClinicalSection("Discussion", payload.discussion, "discussion", pearl, theme, onOpenMedia)
        if (payload.references.isNotBlank()) {
            localClinicalSection("References", payload.references, null, pearl, theme, onOpenMedia, linkifyBody = true)
        }
    }
}

@Composable
private fun localClinicalSection(
    title: String,
    body: String,
    sectionTag: String?,
    pearl: PearlWithMedia,
    theme: TabTheme,
    onOpenMedia: (PublicPearlMediaViewerRequest) -> Unit,
    linkifyBody: Boolean = false,
) {
    val trimmed = body.trim()
    val sectionItems = sectionTag?.let { tag ->
        pearl.mediaItems.filter { it.sectionTag.equals(tag, ignoreCase = true) }
    }.orEmpty()
    if (trimmed.isEmpty() && sectionItems.isEmpty()) return

    PearlDetailClinicalCaseSection(
        title = title,
        body = trimmed,
        theme = theme,
        linkifyBody = linkifyBody,
    ) {
        if (sectionItems.isNotEmpty()) {
            PearlDetailMediaSection(
                pearl = pearl.copy(mediaItems = sectionItems),
                theme = theme,
                onOpenMedia = onOpenMedia,
                carouselHeight = PearlDetailMetrics.clinicalSectionMediaHeight,
                showAttachmentLabel = false,
            )
        }
    }
}
