package com.knowledgepearls.app.ui.messaging

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Done
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.knowledgepearls.app.data.model.ConversationRow
import com.knowledgepearls.app.data.model.DirectMessage
import com.knowledgepearls.app.data.model.isFrom
import com.knowledgepearls.app.data.model.MessageProfileResult
import com.knowledgepearls.app.data.model.PearlShareInboxRow
import com.knowledgepearls.app.ui.components.AvatarView
import com.knowledgepearls.app.ui.components.FloatingInboxSheet
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.components.inputBarBottomPadding
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val INBOX_POLL_INTERVAL_MS = 5_000L

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
    onRefreshInbox: () -> Unit = onLoad,
    onTabSelected: (InboxTab) -> Unit,
    onConversationClick: (ConversationRow) -> Unit,
    onCloseThread: () -> Unit,
    onSendMessage: (String) -> Unit,
    onAcceptShare: (String) -> Unit,
    onDeclineShare: (String) -> Unit,
    onSearchUsers: suspend (String) -> List<MessageProfileResult>,
    onLoadRecentRecipients: suspend () -> List<MessageProfileResult>,
    onStartChat: (MessageProfileResult) -> Unit,
) {
    val theme = TabTheme.PublicFeed
    val inboxListState = rememberLazyListState()
    val inThread = threadState.conversationId.isNotBlank()
    var showNewChatSearch by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { onLoad() }

    LaunchedEffect(inThread, showNewChatSearch) {
        if (inThread || showNewChatSearch) return@LaunchedEffect
        while (true) {
            delay(INBOX_POLL_INTERVAL_MS)
            onRefreshInbox()
        }
    }

    FloatingInboxSheet(
        onDismiss = onDismiss,
        expanded = inThread || showNewChatSearch,
        listState = if (inThread || showNewChatSearch) null else inboxListState,
    ) {
        when {
            inThread -> {
                MessageThreadScreen(
                    state = threadState,
                    theme = theme,
                    onBack = onCloseThread,
                    onSendMessage = onSendMessage,
                )
            }
            showNewChatSearch -> {
                NewChatSearchScreen(
                    theme = theme,
                    onBack = { showNewChatSearch = false },
                    onLoadRecent = onLoadRecentRecipients,
                    onSearch = onSearchUsers,
                    onSelectUser = { profile ->
                        showNewChatSearch = false
                        onStartChat(profile)
                    },
                )
            }
            else -> {
                InboxListContent(
                    inboxState = inboxState,
                    theme = theme,
                    listState = inboxListState,
                    onDismiss = onDismiss,
                    onTabSelected = onTabSelected,
                    onConversationClick = onConversationClick,
                    onAcceptShare = onAcceptShare,
                    onDeclineShare = onDeclineShare,
                    onComposeMessage = { showNewChatSearch = true },
                )
            }
        }
    }
}

@Composable
private fun InboxListContent(
    inboxState: InboxUiState,
    theme: TabTheme,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onDismiss: () -> Unit,
    onTabSelected: (InboxTab) -> Unit,
    onConversationClick: (ConversationRow) -> Unit,
    onAcceptShare: (String) -> Unit,
    onDeclineShare: (String) -> Unit,
    onComposeMessage: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            InboxSheetHeader(
                theme = theme,
                showComposeButton = inboxState.selectedTab == InboxTab.Messages,
                onComposeMessage = onComposeMessage,
                onDismiss = onDismiss,
            )
        }

        item {
            InboxTabRow(
                selected = inboxState.selectedTab,
                theme = theme,
                messagesUnreadCount = inboxState.conversations.sumOf { it.unreadCount },
                pendingShareCount = inboxState.pendingShares.size,
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

@Composable
private fun InboxSheetHeader(
    theme: TabTheme,
    showComposeButton: Boolean,
    onComposeMessage: () -> Unit,
    onDismiss: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PearlLayout.screenHorizontalPadding)
            .padding(top = 4.dp, bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Inbox",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PearlColors.heroPrimary(darkTheme),
                )
                Text(
                    text = "Messages & shared pearls",
                    style = MaterialTheme.typography.bodySmall,
                    color = PearlColors.heroSecondary(darkTheme),
                )
            }
            if (showComposeButton) {
                IconButton(
                    onClick = onComposeMessage,
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "New message",
                        tint = theme.primary,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(theme.primary.copy(alpha = 0.14f))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "Done",
                    color = theme.primary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge,
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
    val lastOutgoingReadMessageId = remember(state.messages, state.currentUserId) {
        state.messages.lastOrNull { message ->
            message.isFrom(state.currentUserId) && !message.readAt.isNullOrBlank()
        }?.id
    }

    LaunchedEffect(state.conversationId, state.isLoading) {
        if (!state.isLoading && state.messages.isNotEmpty()) {
            listState.scrollToItem(state.messages.lastIndex)
        }
    }

    LaunchedEffect(state.messages.lastOrNull()?.id) {
        val messages = state.messages
        if (messages.isEmpty()) return@LaunchedEffect
        val lastIndex = messages.lastIndex
        val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        val nearBottom = lastVisibleIndex >= lastIndex - 2
        val lastMessage = messages.last()
        if (nearBottom || lastMessage.isFrom(state.currentUserId)) {
            listState.animateScrollToItem(lastIndex)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PearlLayout.screenHorizontalPadding, vertical = 8.dp),
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
                        showSeenReceipt = isOutgoing && message.id == lastOutgoingReadMessageId,
                        topPadding = topPadding,
                        outgoingAvatarUrl = state.currentUserAvatarUrl,
                        outgoingDisplayName = state.currentUserDisplayName,
                        incomingAvatarUrl = state.otherAvatarUrl,
                        incomingDisplayName = state.otherDisplayName,
                    )
                }
            }
        }

        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PearlLayout.screenHorizontalPadding)
                .padding(top = 8.dp)
                .inputBarBottomPadding(fallbackWhenHidden = 12.dp),
            cornerRadius = 20.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message…") },
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
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
                    shape = RoundedCornerShape(14.dp),
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
    messagesUnreadCount: Int,
    pendingShareCount: Int,
    onSelected: (InboxTab) -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    val sharedHighlightAmber = Color(0xFFFFB847)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PearlLayout.screenHorizontalPadding, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        InboxTab.entries.forEach { tab ->
            val isSelected = tab == selected
            val badgeCount = when (tab) {
                InboxTab.Messages -> messagesUnreadCount
                InboxTab.Shared -> pendingShareCount
            }
            val shouldHighlightShared =
                tab == InboxTab.Shared && pendingShareCount > 0 && !isSelected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 42.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .semantics(mergeDescendants = true) {
                        val label = if (tab == InboxTab.Messages) "Messages" else "Shared pearls"
                        contentDescription = if (badgeCount > 0) {
                            "$label, $badgeCount new"
                        } else {
                            label
                        }
                        role = Role.Tab
                        this.selected = isSelected
                    }
                    .background(
                        if (isSelected) {
                            Brush.horizontalGradient(
                                listOf(theme.primary.copy(alpha = 0.28f), theme.secondary.copy(alpha = 0.22f)),
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(PearlColors.controlFill(darkTheme), PearlColors.controlFill(darkTheme)),
                            )
                        },
                    )
                    .border(
                        width = if (shouldHighlightShared) 1.5.dp else 1.dp,
                        color = when {
                            shouldHighlightShared -> sharedHighlightAmber.copy(alpha = 0.85f)
                            isSelected -> theme.primary.copy(alpha = 0.45f)
                            else -> PearlColors.cardBorder(darkTheme)
                        },
                        shape = RoundedCornerShape(999.dp),
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelected(tab) },
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (tab == InboxTab.Messages) "Messages" else "Shared",
                        color = if (isSelected) theme.primary else PearlColors.heroSecondary(darkTheme),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                    if (badgeCount > 0) {
                        InboxTabBadge(
                            count = badgeCount,
                            background = if (tab == InboxTab.Shared) sharedHighlightAmber else theme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InboxTabBadge(
    count: Int,
    background: Color,
) {
    Text(
        text = if (count > 99) "99+" else count.toString(),
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = MaterialTheme.typography.labelSmall.fontSize,
        modifier = Modifier
            .background(background, RoundedCornerShape(999.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
    )
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
    showSeenReceipt: Boolean,
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
                showSeenReceipt = showSeenReceipt,
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
    showSeenReceipt: Boolean = false,
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
        if (timestamp.isNotBlank() || showSeenReceipt) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (timestamp.isNotBlank()) {
                    Text(
                        text = timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = PearlColors.heroSecondary(darkTheme),
                    )
                }
                if (showSeenReceipt) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Seen",
                        modifier = Modifier.size(12.dp),
                        tint = PearlColors.heroSecondary(darkTheme).copy(alpha = 0.85f),
                    )
                    Text(
                        text = "Seen",
                        style = MaterialTheme.typography.labelSmall,
                        color = PearlColors.heroSecondary(darkTheme).copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
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
