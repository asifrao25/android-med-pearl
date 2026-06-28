package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.model.PearlComment
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PearlCommentsSheet(
    visible: Boolean,
    comments: List<PearlComment>,
    isLoading: Boolean,
    isPosting: Boolean,
    errorMessage: String?,
    theme: TabTheme,
    onDismiss: () -> Unit,
    onPostComment: (String) -> Unit,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val darkTheme = isPearlDarkTheme()
    var draft by remember(visible) { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PearlColors.popupSurface(darkTheme),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = PearlLayout.screenHorizontalPadding),
        ) {
            Text(
                text = "Comments",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PearlColors.heroPrimary(darkTheme),
                modifier = Modifier.padding(bottom = 12.dp),
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = theme.primary)
                    }
                }
                comments.isEmpty() -> {
                    Text(
                        text = "No comments yet. Be the first.",
                        color = PearlColors.heroSecondary(darkTheme),
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        comments.forEach { comment ->
                            CommentRow(comment = comment, theme = theme)
                        }
                    }
                }
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add a comment…") },
                    singleLine = false,
                    maxLines = 3,
                )
                Button(
                    onClick = {
                        val body = draft.trim()
                        if (body.isNotEmpty()) {
                            onPostComment(body)
                            draft = ""
                        }
                    },
                    enabled = draft.trim().isNotEmpty() && !isPosting,
                ) {
                    if (isPosting) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(4.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Post")
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentRow(
    comment: PearlComment,
    theme: TabTheme,
) {
    val darkTheme = isPearlDarkTheme()
    val author = comment.authorName.ifBlank { "Community member" }

    GlassSurface(cornerRadius = 14.dp) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = author,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = theme.primary,
            )
            Text(
                text = comment.body,
                style = MaterialTheme.typography.bodyMedium,
                color = PearlColors.heroPrimary(darkTheme),
            )
        }
    }
}
