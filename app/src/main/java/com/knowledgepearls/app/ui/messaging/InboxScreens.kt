package com.knowledgepearls.app.ui.messaging

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.model.ConversationRow
import com.knowledgepearls.app.data.model.DirectMessage
import com.knowledgepearls.app.data.model.PearlShareInboxRow
import com.knowledgepearls.app.ui.components.AvatarView
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.components.TabScreenHeader
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun InboxScreen(
    inboxState: InboxUiState,
    threadState: MessageThreadUiState,
    onDismiss: () -> Unit,
    onLoad: () -> Unit,
    onTabSelected: (InboxTab) -> Unit,
    onConversationClick: (ConversationRow) -> Unit,
    onCloseThread: () -> Unit,
    onSendMessage: (String) -> Unit,
    onAcceptShare: (String) -> Unit,
    onDeclineShare: (String) -> Unit,
) {
    val theme = TabTheme.PublicFeed
    val darkTheme = isPearlDarkTheme()

    LaunchedEffect(Unit) { onLoad() }

    if (threadState.conversationId.isNotBlank()) {
        MessageThreadScreen(
            state = threadState,
            theme = theme,
            onBack = onCloseThread,
            onSendMessage = onSendMessage,
        )
        return
    }

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = theme, intensity = 0.6f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            TabScreenHeader(
                title = "Inbox",
                subtitle = "Messages & shares",
                theme = theme,
                showsSettingsButton = false,
                trailing = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.primary)
                    }
                },
            )

            InboxTabRow(
                selected = inboxState.selectedTab,
                theme = theme,
                onSelected = onTabSelected,
            )

            when {
                inboxState.isLoading && inboxState.conversations.isEmpty() && inboxState.pendingShares.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = theme.primary)
                    }
                }
                inboxState.selectedTab == InboxTab.Messages -> {
                    if (inboxState.conversations.isEmpty()) {
                        EmptyInboxMessage("No conversations yet.", darkTheme)
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                horizontal = PearlLayout.screenHorizontalPadding,
                                vertical = 12.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            items(inboxState.conversations, key = { it.id }) { row ->
                                ConversationRowItem(
                                    row = row,
                                    theme = theme,
                                    onClick = { onConversationClick(row) },
                                )
                            }
                        }
                    }
                }
                inboxState.pendingShares.isEmpty() -> {
                    EmptyInboxMessage("No pending pearl shares.", darkTheme)
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            horizontal = PearlLayout.screenHorizontalPadding,
                            vertical = 12.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(inboxState.pendingShares, key = { it.share.id }) { row ->
                            PearlShareRowItem(
                                row = row,
                                theme = theme,
                                isResponding = inboxState.respondingShareId == row.share.id,
                                onAccept = { onAcceptShare(row.share.id) },
                                onDecline = { onDeclineShare(row.share.id) },
                            )
                        }
                    }
                }
            }

            inboxState.errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(PearlLayout.screenHorizontalPadding),
                )
            }
        }
    }
}

@Composable
fun MessageThreadScreen(
    state: MessageThreadUiState,
    theme: TabTheme,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    var draft by remember(state.conversationId) { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = theme, intensity = 0.55f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PearlColors.heroPrimary(darkTheme))
                }
                AvatarView(
                    url = state.otherAvatarUrl,
                    displayName = state.otherDisplayName,
                    size = 36.dp,
                )
                Text(
                    text = state.otherDisplayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PearlColors.heroPrimary(darkTheme),
                    modifier = Modifier.padding(start = 10.dp),
                )
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = theme.primary)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        horizontal = PearlLayout.screenHorizontalPadding,
                        vertical = 12.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.messages, key = { it.id }) { message ->
                        MessageBubble(message = message, theme = theme)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PearlLayout.screenHorizontalPadding)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message…") },
                    maxLines = 3,
                )
                Button(
                    onClick = {
                        val body = draft.trim()
                        if (body.isNotEmpty()) {
                            onSendMessage(body)
                            draft = ""
                        }
                    },
                    enabled = draft.trim().isNotEmpty() && !state.isSending,
                ) {
                    Text("Send")
                }
            }
        }
    }
}

@Composable
private fun InboxTabRow(
    selected: InboxTab,
    theme: TabTheme,
    onSelected: (InboxTab) -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PearlLayout.screenHorizontalPadding, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        InboxTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (isSelected) theme.primary.copy(alpha = 0.25f) else PearlColors.controlFill(darkTheme),
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelected(tab) },
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = if (tab == InboxTab.Messages) "Messages" else "Shared",
                    color = if (isSelected) theme.primary else PearlColors.heroSecondary(darkTheme),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

@Composable
private fun ConversationRowItem(
    row: ConversationRow,
    theme: TabTheme,
    onClick: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()

    GlassSurface(cornerRadius = PearlLayout.cardCornerRadius) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarView(url = row.otherAvatarUrl, displayName = row.otherDisplayName, size = 44.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = row.otherDisplayName,
                    fontWeight = FontWeight.SemiBold,
                    color = PearlColors.heroPrimary(darkTheme),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = row.lastMessageBody.orEmpty().ifBlank { "No messages yet" },
                    style = MaterialTheme.typography.bodySmall,
                    color = PearlColors.heroSecondary(darkTheme),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (row.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(theme.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = row.unreadCount.coerceAtMost(99).toString(),
                        color = PearlColors.heroPrimary(darkTheme),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun PearlShareRowItem(
    row: PearlShareInboxRow,
    theme: TabTheme,
    isResponding: Boolean,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    val payload = row.share.pearlPayload

    GlassSurface(cornerRadius = PearlLayout.cardCornerRadius) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                AvatarView(url = row.senderAvatarUrl, displayName = row.senderName, size = 40.dp)
                Text(
                    text = "From ${row.senderName}",
                    fontWeight = FontWeight.SemiBold,
                    color = theme.primary,
                )
            }
            Text(
                text = payload.title.ifBlank { "Shared pearl" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PearlColors.heroPrimary(darkTheme),
            )
            if (payload.notes.isNotBlank()) {
                Text(
                    text = payload.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PearlColors.heroSecondary(darkTheme),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAccept, enabled = !isResponding, modifier = Modifier.weight(1f)) {
                    Text("Accept")
                }
                OutlinedButton(onClick = onDecline, enabled = !isResponding, modifier = Modifier.weight(1f)) {
                    Text("Decline")
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: DirectMessage,
    theme: TabTheme,
) {
    val darkTheme = isPearlDarkTheme()
    GlassSurface(cornerRadius = 16.dp) {
        Text(
            text = message.body,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            color = PearlColors.heroPrimary(darkTheme),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun EmptyInboxMessage(message: String, darkTheme: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(PearlLayout.screenHorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = PearlColors.heroSecondary(darkTheme),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
