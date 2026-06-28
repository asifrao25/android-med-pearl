package com.knowledgepearls.app.ui.messaging

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.knowledgepearls.app.data.model.ConversationRow
import com.knowledgepearls.app.data.model.DirectMessage
import com.knowledgepearls.app.data.model.isFrom
import com.knowledgepearls.app.data.model.PearlShareInboxRow
import com.knowledgepearls.app.ui.components.AvatarView
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.components.PullToDismissSheet
import com.knowledgepearls.app.ui.components.TabScreenHeader
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private object ChatBubbleMetrics {
    val avatarSize = 30.dp
    val avatarSpacing = 8.dp
    val rowSpacing = 4.dp
    val sectionSpacing = 14.dp
    val oppositeSideMinSpace = 48.dp
    const val maxBubbleWidthRatio = 0.74f
}

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

    val listState = rememberLazyListState()

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = theme, intensity = 0.6f)

        PullToDismissSheet(
            onDismiss = onDismiss,
            listState = listState,
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
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
                }

                item {
                    InboxTabRow(
                        selected = inboxState.selectedTab,
                        theme = theme,
                        onSelected = onTabSelected,
                    )
                }

                when {
                    inboxState.isLoading && inboxState.conversations.isEmpty() && inboxState.pendingShares.isEmpty() -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = theme.primary)
                            }
                        }
                    }
                    inboxState.selectedTab == InboxTab.Messages -> {
                        if (inboxState.conversations.isEmpty()) {
                            item {
                                EmptyInboxMessage("No conversations yet.", darkTheme)
                            }
                        } else {
                            items(inboxState.conversations, key = { it.id }) { row ->
                                ConversationRowItem(
                                    row = row,
                                    theme = theme,
                                    onClick = { onConversationClick(row) },
                                    modifier = Modifier.padding(horizontal = PearlLayout.screenHorizontalPadding),
                                )
                            }
                        }
                    }
                    inboxState.pendingShares.isEmpty() -> {
                        item {
                            EmptyInboxMessage("No pending pearl shares.", darkTheme)
                        }
                    }
                    else -> {
                        items(inboxState.pendingShares, key = { it.share.id }) { row ->
                            PearlShareRowItem(
                                row = row,
                                theme = theme,
                                isResponding = inboxState.respondingShareId == row.share.id,
                                onAccept = { onAcceptShare(row.share.id) },
                                onDecline = { onDeclineShare(row.share.id) },
                                modifier = Modifier.padding(horizontal = PearlLayout.screenHorizontalPadding),
                            )
                        }
                    }
                }

                inboxState.errorMessage?.let { message ->
                    item {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = PearlLayout.screenHorizontalPadding),
                        )
                    }
                }
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

        PullToDismissSheet(
            onDismiss = onBack,
            listState = listState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
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
                        horizontal = 12.dp,
                        vertical = 12.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(ChatBubbleMetrics.rowSpacing),
                ) {
                    itemsIndexed(
                        items = state.messages,
                        key = { _, message -> "${message.id}-${message.senderId}" },
                    ) { index, message ->
                        val previous = state.messages.getOrNull(index - 1)
                        val isOutgoing = message.isFrom(state.currentUserId)
                        val previousOutgoing = previous?.isFrom(state.currentUserId)
                        val topPadding = if (previous != null && isOutgoing != previousOutgoing) {
                            ChatBubbleMetrics.sectionSpacing
                        } else {
                            0.dp
                        }

                        ChatMessageRow(
                            message = message,
                            isOutgoing = isOutgoing,
                            topPadding = topPadding,
                            outgoingAvatarUrl = state.currentUserAvatarUrl,
                            outgoingDisplayName = state.currentUserDisplayName,
                            incomingAvatarUrl = state.otherAvatarUrl,
                            incomingDisplayName = state.otherDisplayName,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = PearlLayout.screenHorizontalPadding)
                    .padding(top = 10.dp, bottom = 12.dp),
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
                    .semantics(mergeDescendants = true) {
                        contentDescription = if (tab == InboxTab.Messages) "Messages" else "Shared pearls"
                        role = Role.Tab
                        this.selected = isSelected
                    }
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
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()

    GlassSurface(cornerRadius = PearlLayout.cardCornerRadius, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {
                    contentDescription = buildString {
                        append(row.otherDisplayName)
                        append(", ")
                        append(row.lastMessageBody.orEmpty().ifBlank { "No messages yet" })
                        if (row.unreadCount > 0) {
                            append(", ")
                            append(row.unreadCount)
                            append(" unread")
                        }
                    }
                }
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
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    val payload = row.share.pearlPayload

    GlassSurface(cornerRadius = PearlLayout.cardCornerRadius, modifier = modifier) {
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
private fun ChatMessageRow(
    message: DirectMessage,
    isOutgoing: Boolean,
    topPadding: Dp,
    outgoingAvatarUrl: String?,
    outgoingDisplayName: String,
    incomingAvatarUrl: String?,
    incomingDisplayName: String,
) {
    val darkTheme = isPearlDarkTheme()
    val configuration = LocalConfiguration.current
    val maxBubbleWidth = configuration.screenWidthDp.dp * ChatBubbleMetrics.maxBubbleWidthRatio
    val sentTeal = Color(0xFF14B8A6)
    val sentCyan = Color(0xFF22D3EE)
    val bubbleColor = if (darkTheme) Color(0xFF383838) else Color(0xFFF0F0F0)
    val outgoingShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = 18.dp,
        bottomEnd = 4.dp,
    )
    val incomingShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = 4.dp,
        bottomEnd = 18.dp,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding),
        verticalAlignment = Alignment.Bottom,
    ) {
        if (isOutgoing) {
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = ChatBubbleMetrics.oppositeSideMinSpace),
            )
            MessageBubbleColumn(
                body = message.body,
                createdAt = message.createdAt,
                isOutgoing = true,
                maxWidth = maxBubbleWidth,
                textColor = Color.White,
                bubbleShape = outgoingShape,
                bubbleBrush = Brush.linearGradient(listOf(sentTeal, sentCyan)),
            )
            Spacer(modifier = Modifier.width(ChatBubbleMetrics.avatarSpacing))
            AvatarView(
                url = outgoingAvatarUrl,
                displayName = outgoingDisplayName,
                size = ChatBubbleMetrics.avatarSize,
            )
        } else {
            AvatarView(
                url = incomingAvatarUrl,
                displayName = incomingDisplayName,
                size = ChatBubbleMetrics.avatarSize,
            )
            Spacer(modifier = Modifier.width(ChatBubbleMetrics.avatarSpacing))
            MessageBubbleColumn(
                body = message.body,
                createdAt = message.createdAt,
                isOutgoing = false,
                maxWidth = maxBubbleWidth,
                textColor = PearlColors.heroPrimary(darkTheme),
                bubbleShape = incomingShape,
                bubbleColor = bubbleColor,
            )
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .widthIn(min = ChatBubbleMetrics.oppositeSideMinSpace),
            )
        }
    }
}

@Composable
private fun MessageBubbleColumn(
    body: String,
    createdAt: String,
    isOutgoing: Boolean,
    maxWidth: Dp,
    textColor: Color,
    bubbleShape: RoundedCornerShape,
    bubbleColor: Color = Color.Transparent,
    bubbleBrush: Brush? = null,
) {
    val darkTheme = isPearlDarkTheme()
    val horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    val timestamp = formatMessageTime(createdAt)

    Column(
        modifier = Modifier.widthIn(max = maxWidth),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = body,
            modifier = Modifier
                .then(
                    if (bubbleBrush != null) {
                        Modifier.background(brush = bubbleBrush, shape = bubbleShape)
                    } else {
                        Modifier.background(bubbleColor, bubbleShape)
                    },
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (timestamp.isNotBlank()) {
            Text(
                text = timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = PearlColors.heroSecondary(darkTheme),
            )
        }
    }
}

private fun formatMessageTime(createdAt: String): String {
    if (createdAt.isBlank()) return ""
    val instant = runCatching { Instant.parse(createdAt) }.getOrNull()
        ?: runCatching {
            Instant.parse(
                createdAt.replace(" ", "T").let { value ->
                    if (value.endsWith("Z")) value else "${value}Z"
                },
            )
        }.getOrNull()
        ?: return ""
    return DateTimeFormatter.ofPattern("h:mm a")
        .withZone(ZoneId.systemDefault())
        .format(instant)
}

@Composable
private fun EmptyInboxMessage(message: String, darkTheme: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PearlLayout.screenHorizontalPadding, vertical = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = PearlColors.heroSecondary(darkTheme),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
