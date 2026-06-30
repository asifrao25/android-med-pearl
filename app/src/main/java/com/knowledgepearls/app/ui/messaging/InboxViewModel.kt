package com.knowledgepearls.app.ui.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledgepearls.app.data.model.ConversationRow
import com.knowledgepearls.app.data.model.DirectMessage
import com.knowledgepearls.app.data.model.PearlShareInboxRow
import com.knowledgepearls.app.data.model.MessageProfileResult
import com.knowledgepearls.app.data.prefs.RecentMessageRecipient
import com.knowledgepearls.app.data.prefs.RecentMessageRecipientsStore
import com.knowledgepearls.app.data.repository.AccountRepository
import com.knowledgepearls.app.data.repository.InboxCountsRepository
import com.knowledgepearls.app.data.repository.MessagingRepository
import com.knowledgepearls.app.data.repository.PearlShareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class InboxTab {
    Messages,
    Shared,
}

data class InboxUiState(
    val selectedTab: InboxTab = InboxTab.Messages,
    val conversations: List<ConversationRow> = emptyList(),
    val pendingShares: List<PearlShareInboxRow> = emptyList(),
    val unreadBadge: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val respondingShareId: String? = null,
)

data class MessageThreadUiState(
    val conversationId: String = "",
    val otherUserId: String = "",
    val otherDisplayName: String = "",
    val otherAvatarUrl: String? = null,
    val currentUserId: String = "",
    val currentUserDisplayName: String = "You",
    val currentUserAvatarUrl: String? = null,
    val messages: List<DirectMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class InboxViewModel @Inject constructor(
    private val messagingRepository: MessagingRepository,
    private val pearlShareRepository: PearlShareRepository,
    private val inboxCountsRepository: InboxCountsRepository,
    private val accountRepository: AccountRepository,
    private val recentMessageRecipientsStore: RecentMessageRecipientsStore,
) : ViewModel() {
    private val _inboxState = MutableStateFlow(InboxUiState())
    val inboxState: StateFlow<InboxUiState> = _inboxState.asStateFlow()

    private val _threadState = MutableStateFlow(MessageThreadUiState())
    val threadState: StateFlow<MessageThreadUiState> = _threadState.asStateFlow()

    private var threadPollJob: Job? = null

    fun loadInbox() {
        viewModelScope.launch {
            val userId = accountRepository.currentUserId() ?: return@launch
            _inboxState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val conversations = messagingRepository.buildConversationRows(userId)
                val shares = pearlShareRepository.fetchPendingShares(userId)
                val badge = inboxCountsRepository.totalInboxBadge(userId)
                _inboxState.update {
                    it.copy(
                        conversations = conversations,
                        pendingShares = shares,
                        unreadBadge = badge,
                        isLoading = false,
                    )
                }
            }.onFailure { error ->
                _inboxState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Could not load inbox.",
                    )
                }
            }
        }
    }

    fun refreshInboxQuietly() {
        viewModelScope.launch {
            val userId = accountRepository.currentUserId() ?: return@launch
            runCatching {
                val conversations = messagingRepository.buildConversationRows(userId)
                val shares = pearlShareRepository.fetchPendingShares(userId)
                val badge = inboxCountsRepository.totalInboxBadge(userId)
                _inboxState.update {
                    it.copy(
                        conversations = conversations,
                        pendingShares = shares,
                        unreadBadge = badge,
                    )
                }
            }
        }
    }

    fun refreshBadge() {
        viewModelScope.launch {
            val userId = accountRepository.currentUserId() ?: return@launch
            val badge = inboxCountsRepository.totalInboxBadge(userId)
            _inboxState.update { it.copy(unreadBadge = badge) }
        }
    }

    fun setTab(tab: InboxTab) {
        _inboxState.update { it.copy(selectedTab = tab) }
    }

    fun openConversation(conversation: ConversationRow) {
        viewModelScope.launch {
            val viewer = loadViewerContext()
            _threadState.value = MessageThreadUiState(
                conversationId = conversation.id,
                otherUserId = conversation.otherUserId,
                otherDisplayName = conversation.otherDisplayName,
                otherAvatarUrl = conversation.otherAvatarUrl,
                currentUserId = viewer.userId,
                currentUserDisplayName = viewer.displayName,
                currentUserAvatarUrl = viewer.avatarUrl,
                isLoading = true,
            )
            runCatching {
                val messages = messagingRepository.fetchMessages(conversation.id)
                messagingRepository.markConversationRead(conversation.id)
                _threadState.update {
                    it.copy(messages = messages, isLoading = false)
                }
                startThreadPolling()
                recordRecentRecipient(
                    userId = conversation.otherUserId,
                    name = conversation.otherDisplayName,
                    avatarUrl = conversation.otherAvatarUrl,
                )
                loadInbox()
            }.onFailure { error ->
                _threadState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Could not load messages.",
                    )
                }
            }
        }
    }

    fun closeThread() {
        stopThreadPolling()
        _threadState.value = MessageThreadUiState()
        refreshBadge()
    }

    fun sendMessage(body: String) {
        val conversationId = _threadState.value.conversationId
        if (conversationId.isBlank()) return
        viewModelScope.launch {
            val userId = accountRepository.currentUserId() ?: return@launch
            _threadState.update { it.copy(isSending = true, errorMessage = null) }
            runCatching {
                val message = messagingRepository.sendMessage(conversationId, userId, body)
                _threadState.update {
                    it.copy(
                        messages = it.messages + message,
                        isSending = false,
                    )
                }
                recordRecentRecipient(
                    userId = _threadState.value.otherUserId,
                    name = _threadState.value.otherDisplayName,
                    avatarUrl = _threadState.value.otherAvatarUrl,
                )
                loadInbox()
            }.onFailure { error ->
                _threadState.update {
                    it.copy(
                        isSending = false,
                        errorMessage = error.message ?: "Could not send message.",
                    )
                }
            }
        }
    }

    fun respondToShare(shareId: String, accept: Boolean) {
        viewModelScope.launch {
            _inboxState.update { it.copy(respondingShareId = shareId, errorMessage = null) }
            runCatching {
                if (accept) {
                    val share = _inboxState.value.pendingShares.firstOrNull { it.share.id == shareId }?.share
                        ?: error("Share not found.")
                    pearlShareRepository.importAcceptedShare(share)
                }
                pearlShareRepository.respondToShare(shareId, accept)
                loadInbox()
            }.onFailure { error ->
                _inboxState.update {
                    it.copy(
                        respondingShareId = null,
                        errorMessage = error.message ?: "Could not respond to share.",
                    )
                }
            }
            _inboxState.update { it.copy(respondingShareId = null) }
        }
    }

    fun dismissError() {
        _inboxState.update { it.copy(errorMessage = null) }
        _threadState.update { it.copy(errorMessage = null) }
    }

    fun openSharedPearlsTab() {
        _inboxState.update { it.copy(selectedTab = InboxTab.Shared) }
    }

    fun openConversationById(conversationId: String) {
        viewModelScope.launch {
            val userId = accountRepository.currentUserId() ?: return@launch
            _inboxState.update { it.copy(isLoading = true, selectedTab = InboxTab.Messages) }
            runCatching {
                val conversations = messagingRepository.buildConversationRows(userId)
                val target = conversations.firstOrNull { it.id.equals(conversationId, ignoreCase = true) }
                _inboxState.update {
                    it.copy(conversations = conversations, isLoading = false)
                }
                target?.let { openConversation(it) } ?: loadInbox()
            }.onFailure {
                loadInbox()
            }
        }
    }

    fun openConversationWithUser(
        otherUserId: String,
        otherDisplayName: String,
        otherAvatarUrl: String?,
    ) {
        viewModelScope.launch {
            val userId = accountRepository.currentUserId() ?: return@launch
            val viewer = loadViewerContext()
            _inboxState.update { it.copy(selectedTab = InboxTab.Messages, errorMessage = null) }
            _threadState.value = MessageThreadUiState(
                otherUserId = otherUserId,
                otherDisplayName = otherDisplayName,
                otherAvatarUrl = otherAvatarUrl,
                currentUserId = viewer.userId,
                currentUserDisplayName = viewer.displayName,
                currentUserAvatarUrl = viewer.avatarUrl,
                isLoading = true,
            )
            runCatching {
                val conversationId = messagingRepository.getOrCreateConversation(otherUserId)
                val messages = messagingRepository.fetchMessages(conversationId)
                messagingRepository.markConversationRead(conversationId)
                val conversations = messagingRepository.buildConversationRows(userId)
                _inboxState.update {
                    it.copy(conversations = conversations, isLoading = false)
                }
                _threadState.update {
                    it.copy(
                        conversationId = conversationId,
                        messages = messages,
                        isLoading = false,
                    )
                }
                startThreadPolling()
                recordRecentRecipient(
                    userId = otherUserId,
                    name = otherDisplayName,
                    avatarUrl = otherAvatarUrl,
                )
            }.onFailure { error ->
                _threadState.value = MessageThreadUiState()
                _inboxState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Couldn't open conversation.",
                    )
                }
            }
        }
    }

    fun openPearlShareById(shareId: String) {
        viewModelScope.launch {
            _inboxState.update { it.copy(isLoading = true, selectedTab = InboxTab.Shared) }
            runCatching {
                val share = pearlShareRepository.fetchShareById(shareId) ?: return@runCatching
                if (share.status != "pending") return@runCatching
                val userId = accountRepository.currentUserId() ?: return@runCatching
                val shares = pearlShareRepository.fetchPendingShares(userId)
                _inboxState.update {
                    it.copy(
                        pendingShares = shares,
                        isLoading = false,
                        selectedTab = InboxTab.Shared,
                    )
                }
            }.onFailure {
                _inboxState.update { it.copy(isLoading = false) }
            }
        }
    }

    suspend fun searchUsersForMessage(query: String): List<MessageProfileResult> {
        val userId = accountRepository.currentUserId() ?: return emptyList()
        return messagingRepository.searchProfilesForMessage(query, userId)
    }

    suspend fun loadRecentMessageRecipients(): List<MessageProfileResult> {
        val stored = recentMessageRecipientsStore.getRecent().map { recipient ->
            MessageProfileResult(
                id = recipient.id,
                name = recipient.name,
                avatarUrl = recipient.avatarUrl,
            )
        }
        if (stored.isNotEmpty()) return stored
        return _inboxState.value.conversations.take(5).map { conversation ->
            MessageProfileResult(
                id = conversation.otherUserId,
                name = conversation.otherDisplayName,
                avatarUrl = conversation.otherAvatarUrl,
            )
        }
    }

    private fun recordRecentRecipient(userId: String, name: String, avatarUrl: String?) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            recentMessageRecipientsStore.recordRecipient(
                RecentMessageRecipient(
                    id = userId,
                    name = name,
                    avatarUrl = avatarUrl,
                ),
            )
        }
    }

    private data class ViewerContext(
        val userId: String,
        val displayName: String,
        val avatarUrl: String?,
    )

    private fun startThreadPolling() {
        threadPollJob?.cancel()
        threadPollJob = viewModelScope.launch {
            while (isActive) {
                refreshActiveThread(markRead = true)
                delay(THREAD_POLL_INTERVAL_MS)
            }
        }
    }

    private fun stopThreadPolling() {
        threadPollJob?.cancel()
        threadPollJob = null
    }

    private suspend fun refreshActiveThread(markRead: Boolean) {
        val conversationId = _threadState.value.conversationId
        if (conversationId.isBlank()) return
        runCatching {
            val messages = messagingRepository.fetchMessages(conversationId)
            if (markRead) {
                messagingRepository.markConversationRead(conversationId)
            }
            _threadState.update { current ->
                if (current.conversationId != conversationId) {
                    current
                } else {
                    current.copy(messages = messages, errorMessage = null)
                }
            }
            refreshBadge()
        }
    }

    override fun onCleared() {
        stopThreadPolling()
        super.onCleared()
    }

    private suspend fun loadViewerContext(): ViewerContext {
        val userId = accountRepository.currentUserId().orEmpty()
        if (userId.isBlank()) {
            return ViewerContext(userId = "", displayName = "You", avatarUrl = null)
        }
        val profile = accountRepository.fetchProfile(userId)
        val displayName = profile?.name?.trim().orEmpty().ifBlank { "You" }
        return ViewerContext(
            userId = userId,
            displayName = displayName,
            avatarUrl = profile?.avatarUrl,
        )
    }

    private companion object {
        const val THREAD_POLL_INTERVAL_MS = 3_000L
    }
}
