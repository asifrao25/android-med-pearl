package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun PublicPearlDetailScreen(
    pearl: PublicPearl,
    likeCount: Int,
    commentCount: Int,
    isLiked: Boolean,
    commentsVisible: Boolean,
    comments: List<com.knowledgepearls.app.data.model.PearlComment>,
    isCommentsLoading: Boolean,
    isPostingComment: Boolean,
    commentsError: String?,
    onBack: () -> Unit,
    onAddToMyFeed: () -> Unit,
    onHide: () -> Unit,
    onToggleLike: () -> Unit,
    onOpenComments: () -> Unit,
    onCloseComments: () -> Unit,
    onPostComment: (String) -> Unit,
) {
    val theme = TabTheme.PublicFeed
    val darkTheme = isPearlDarkTheme()
    var mediaViewerRequest by remember(pearl.id) { mutableStateOf<PublicPearlMediaViewerRequest?>(null) }
    var commentsSheetOpen by remember(commentsVisible) { mutableStateOf(commentsVisible) }

    PublicPearlMediaViewerOverlay(
        request = mediaViewerRequest,
        theme = theme,
        onDismiss = { mediaViewerRequest = null },
    )

    if (commentsVisible) {
        commentsSheetOpen = true
    }

    PearlCommentsSheet(
        visible = commentsSheetOpen,
        comments = comments,
        isLoading = isCommentsLoading,
        isPosting = isPostingComment,
        errorMessage = commentsError,
        theme = theme,
        onDismiss = {
            commentsSheetOpen = false
            onCloseComments()
        },
        onPostComment = onPostComment,
    )

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = theme, intensity = 0.5f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PearlColors.heroPrimary(darkTheme),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = PearlLayout.screenHorizontalPadding)
                    .padding(bottom = PearlLayout.tabBarOverlayInset),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = pearl.titleDisplay,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PearlColors.heroPrimary(darkTheme),
                )

                if (pearl.safeDisplayName.isNotBlank()) {
                    Text(
                        text = "Shared by ${pearl.safeDisplayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.primary,
                    )
                }

                PublicPearlDetailMediaSection(
                    pearl = pearl,
                    theme = theme,
                    onOpenMedia = { mediaViewerRequest = it },
                )

                PublicPearlEngagementBar(
                    likeCount = likeCount,
                    commentCount = commentCount,
                    isLiked = isLiked,
                    theme = theme,
                    modifier = Modifier.fillMaxWidth(),
                    onToggleLike = onToggleLike,
                    onOpenComments = {
                        commentsSheetOpen = true
                        onOpenComments()
                    },
                )

                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = PearlLayout.cardCornerRadius,
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (pearl.isClinicalCase) {
                            pearl.casePayload?.history?.takeIf { it.isNotBlank() }?.let {
                                DetailSection("History", it, darkTheme)
                            }
                            pearl.casePayload?.examination?.takeIf { it.isNotBlank() }?.let {
                                DetailSection("Examination", it, darkTheme)
                            }
                            pearl.casePayload?.investigation?.takeIf { it.isNotBlank() }?.let {
                                DetailSection("Investigation", it, darkTheme)
                            }
                            pearl.casePayload?.diagnosis?.takeIf { it.isNotBlank() }?.let {
                                DetailSection("Diagnosis", it, darkTheme)
                            }
                            pearl.casePayload?.discussion?.takeIf { it.isNotBlank() }?.let {
                                DetailSection("Discussion", it, darkTheme)
                            }
                        } else if (pearl.notes.isNotBlank()) {
                            DetailSection("Notes", pearl.notes, darkTheme)
                        }

                        pearl.linkPreviewDescription?.takeIf { it.isNotBlank() }?.let {
                            DetailSection("Preview", it, darkTheme)
                        }

                        pearl.effectiveSourceReference.takeIf { it.isNotBlank() }?.let {
                            DetailSection("Source", it, darkTheme)
                        }

                        if (pearl.tags.isNotEmpty()) {
                            DetailSection("Tags", pearl.tags.joinToString(", "), darkTheme)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PearlLayout.screenHorizontalPadding)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = onAddToMyFeed, modifier = Modifier.fillMaxWidth()) {
                    Text("Save to My Feed")
                }
                Button(onClick = onHide, modifier = Modifier.fillMaxWidth()) {
                    Text("Hide from feed")
                }
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, body: String, darkTheme: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = PearlColors.heroSecondary(darkTheme),
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = PearlColors.heroPrimary(darkTheme),
        )
    }
}
