package com.knowledgepearls.app.ui.publicfeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledgepearls.app.data.model.ContentTypeFilter
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.data.model.PearlComment
import com.knowledgepearls.app.data.model.normalizeUserId
import com.knowledgepearls.app.data.repository.AccountRepository
import com.knowledgepearls.app.data.repository.PublicFeedEngagementRepository
import com.knowledgepearls.app.data.repository.PublicFeedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PublicFeedSection(val label: String) {
    NEW("New"),
    SEEN("Seen"),
}

data class PublicFeedUiState(
    val pearls: List<PublicPearl> = emptyList(),
    val section: PublicFeedSection = PublicFeedSection.NEW,
    val contentTypeFilter: ContentTypeFilter = ContentTypeFilter.ALL,
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null,
    val actionOutcome: com.knowledgepearls.app.ui.components.PearlActionOutcome? = null,
    val showEmptyFilterAlert: Boolean = false,
    val showSeenToast: Boolean = false,
    val seenIds: Set<String> = emptySet(),
    val likedPearlIds: Set<String> = emptySet(),
    val commentCounts: Map<String, Int> = emptyMap(),
    val commentsForPearl: List<PearlComment> = emptyList(),
    val commentsPearlId: String? = null,
    val isCommentsLoading: Boolean = false,
    val isPostingComment: Boolean = false,
    val commentsError: String? = null,
) {
    val unseenPearls: List<PublicPearl>
        get() = pearls.filter { it.id !in seenIds }

    val seenPearls: List<PublicPearl>
        get() = pearls.filter { it.id in seenIds }

    val newCount: Int get() = unseenPearls.size

    val seenCount: Int get() = seenPearls.size

    val filteredPearls: List<PublicPearl>
        get() {
            val sectionPearls = when (section) {
                PublicFeedSection.NEW -> unseenPearls
                PublicFeedSection.SEEN -> seenPearls
            }
            return sectionPearls.filter { it.matches(contentTypeFilter) }
        }
}

@HiltViewModel
class PublicFeedViewModel @Inject constructor(
    private val repository: PublicFeedRepository,
    private val engagementRepository: PublicFeedEngagementRepository,
    private val accountRepository: AccountRepository,
) : ViewModel() {
    private var currentOffset = 0
    private var seenIds = repository.getSeenIds()
    private var hiddenIds = repository.getHiddenIds()
    private var blockedUserIds = repository.getBlockedUserIds()

    private fun isVisible(pearl: PublicPearl): Boolean =
        pearl.id !in hiddenIds && normalizeUserId(pearl.userId) !in blockedUserIds

    private val _uiState = MutableStateFlow(PublicFeedUiState(seenIds = seenIds))
    val uiState: StateFlow<PublicFeedUiState> = _uiState.asStateFlow()

    fun loadInitial() {
        viewModelScope.launch {
            currentOffset = 0
            seenIds = repository.getSeenIds()
            hiddenIds = repository.getHiddenIds()
            blockedUserIds = repository.getBlockedUserIds()
            _uiState.update {
                it.copy(
                    pearls = emptyList(),
                    isLoading = true,
                    hasMore = true,
                    errorMessage = null,
                ).copy(seenIds = seenIds)
            }
            loadNextPageInternal(reset = true)
        }
    }

    fun loadNextPage() {
        viewModelScope.launch { loadNextPageInternal(reset = false) }
    }

    private suspend fun loadNextPageInternal(reset: Boolean) {
        val state = _uiState.value
        if (!reset && (state.isLoading || !state.hasMore)) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        runCatching {
            repository.fetchPage(offset = currentOffset)
        }.onSuccess { page ->
            if (page.size < PublicFeedRepository.PAGE_SIZE) {
                _uiState.update { it.copy(hasMore = false) }
            }
            currentOffset += page.size
            val visible = page.filter(::isVisible)
            _uiState.update { current ->
                current.copy(
                    pearls = if (reset) visible else current.pearls + visible,
                    isLoading = false,
                    showEmptyFilterAlert = shouldShowEmptyFilterAlert(current.contentTypeFilter, reset, visible),
                ).copy(seenIds = seenIds)
            }
            syncEngagement(visible.map { it.id })
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Could not load public feed.",
                )
            }
        }
    }

    fun setSection(section: PublicFeedSection) {
        _uiState.update { it.copy(section = section) }
    }

    fun setContentTypeFilter(filter: ContentTypeFilter) {
        _uiState.update {
            it.copy(
                contentTypeFilter = filter,
                showEmptyFilterAlert = shouldShowEmptyFilterAlert(filter, reset = false, latestPage = emptyList()),
            )
        }
    }

    fun resetContentTypeFilter() {
        setContentTypeFilter(ContentTypeFilter.ALL)
        dismissEmptyFilterAlert()
    }

    fun dismissEmptyFilterAlert() {
        _uiState.update { it.copy(showEmptyFilterAlert = false) }
    }

    fun markSeen(pearl: PublicPearl, showToast: Boolean = false) {
        if (pearl.id in seenIds) return
        repository.markSeen(pearl.id)
        seenIds = repository.getSeenIds()
        _uiState.update { it.copy(seenIds = seenIds, showSeenToast = showToast) }
    }

    fun dismissSeenToast() {
        _uiState.update { it.copy(showSeenToast = false) }
    }

    fun markUnseen(pearl: PublicPearl) {
        repository.markUnseen(pearl.id)
        seenIds = repository.getSeenIds()
        _uiState.update { it.copy(seenIds = seenIds) }
    }

    fun blockUser(userId: String) {
        repository.blockUser(userId)
        blockedUserIds = repository.getBlockedUserIds()
        _uiState.update { current ->
            current.copy(
                pearls = current.pearls.filter { normalizeUserId(it.userId) !in blockedUserIds },
                actionOutcome = com.knowledgepearls.app.ui.components.PearlActionOutcome.RemovedFromFeed,
            )
        }
    }

    fun hide(pearl: PublicPearl) {
        hiddenIds = hiddenIds + pearl.id
        repository.hide(pearl.id)
        _uiState.update { current ->
            current.copy(
                pearls = current.pearls.filter { it.id != pearl.id },
                seenIds = seenIds,
                actionOutcome = com.knowledgepearls.app.ui.components.PearlActionOutcome.RemovedFromFeed,
            )
        }
    }

    fun addToMyFeed(pearl: PublicPearl) {
        viewModelScope.launch {
            runCatching { repository.addToMyFeed(pearl) }
                .onSuccess {
                    _uiState.update {
                        it.copy(actionOutcome = com.knowledgepearls.app.ui.components.PearlActionOutcome.SavedToMyFeed)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "Could not save pearl.")
                    }
                }
        }
    }

    fun saveToFolder(pearl: PublicPearl, folderId: String, folderName: String) {
        viewModelScope.launch {
            runCatching { repository.saveToFolder(pearl, folderId) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            actionOutcome = com.knowledgepearls.app.ui.components.PearlActionOutcome.SavedToFolder,
                            actionSuccessMessage = folderName,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "Could not save pearl.")
                    }
                }
        }
    }

    fun createFolderAndSavePearl(pearl: PublicPearl, folderName: String) {
        viewModelScope.launch {
            runCatching {
                val folder = repository.createFolder(folderName)
                repository.saveToFolder(pearl, folder.id)
                folder.name
            }.onSuccess { name ->
                _uiState.update {
                    it.copy(
                        actionOutcome = com.knowledgepearls.app.ui.components.PearlActionOutcome.SavedToFolder,
                        actionSuccessMessage = name,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "Could not save pearl.")
                }
            }
        }
    }

    fun dismissActionSuccess() {
        _uiState.update { it.copy(actionSuccessMessage = null, actionOutcome = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun toggleLike(pearl: PublicPearl) {
        viewModelScope.launch {
            val userId = accountRepository.currentUserId() ?: return@launch
            if (userId.isBlank()) return@launch
            val pearlId = pearl.id.lowercase()
            val currentlyLiked = pearlId in _uiState.value.likedPearlIds.map { it.lowercase() }.toSet()
            runCatching {
                if (currentlyLiked) {
                    engagementRepository.unlike(pearl.id)
                } else {
                    engagementRepository.like(pearl.id)
                }
            }.onSuccess {
                val liked = _uiState.value.likedPearlIds.toMutableSet()
                if (currentlyLiked) liked.remove(pearlId) else liked.add(pearlId)
                val delta = if (currentlyLiked) -1 else 1
                _uiState.update { state ->
                    state.copy(
                        likedPearlIds = liked,
                        pearls = state.pearls.map { item ->
                            if (item.id == pearl.id) {
                                item.replacing(likeCount = (item.likeCount + delta).coerceAtLeast(0))
                            } else {
                                item
                            }
                        },
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "Could not update like.")
                }
            }
        }
    }

    fun isLiked(pearlId: String): Boolean =
        pearlId.lowercase() in _uiState.value.likedPearlIds.map { it.lowercase() }.toSet()

    fun commentCount(pearlId: String): Int =
        _uiState.value.commentCounts[pearlId.lowercase()] ?: 0

    fun openComments(pearlId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    commentsPearlId = pearlId,
                    isCommentsLoading = true,
                    commentsError = null,
                    commentsForPearl = emptyList(),
                )
            }
            runCatching {
                val comments = engagementRepository.fetchComments(pearlId)
                val count = comments.size
                _uiState.update { state ->
                    state.copy(
                        commentsForPearl = comments,
                        isCommentsLoading = false,
                        commentCounts = state.commentCounts + (pearlId.lowercase() to count),
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isCommentsLoading = false,
                        commentsError = error.message ?: "Could not load comments.",
                    )
                }
            }
        }
    }

    fun closeComments() {
        _uiState.update {
            it.copy(
                commentsPearlId = null,
                commentsForPearl = emptyList(),
                commentsError = null,
            )
        }
    }

    fun postComment(pearlId: String, body: String) {
        viewModelScope.launch {
            val userId = accountRepository.currentUserId() ?: return@launch
            _uiState.update { it.copy(isPostingComment = true, commentsError = null) }
            runCatching {
                engagementRepository.postComment(pearlId, userId, body)
            }.onSuccess {
                openComments(pearlId)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isPostingComment = false,
                        commentsError = error.message ?: "Could not post comment.",
                    )
                }
            }
            _uiState.update { it.copy(isPostingComment = false) }
        }
    }

    private suspend fun syncEngagement(pearlIds: List<String>) {
        val userId = accountRepository.currentUserId() ?: return
        if (pearlIds.isEmpty()) return
        runCatching {
            val liked = engagementRepository.fetchLikedPearlIds(userId, pearlIds)
            val counts = pearlIds.associateWith { id ->
                runCatching { engagementRepository.fetchCommentCount(id) }.getOrDefault(0)
            }.mapKeys { it.key.lowercase() }
            _uiState.update { it.copy(likedPearlIds = liked, commentCounts = it.commentCounts + counts) }
        }
    }

    private fun shouldShowEmptyFilterAlert(
        filter: ContentTypeFilter,
        reset: Boolean,
        latestPage: List<PublicPearl>,
    ): Boolean {
        if (filter == ContentTypeFilter.ALL) return false
        val pearls = if (reset) latestPage else _uiState.value.pearls
        return pearls.none { it.matches(filter) }
    }
}
