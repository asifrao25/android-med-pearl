package com.knowledgepearls.app.ui.publicfeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledgepearls.app.data.analytics.AnalyticsContentType
import com.knowledgepearls.app.data.analytics.AnalyticsEventKind
import com.knowledgepearls.app.data.analytics.AnalyticsService
import com.knowledgepearls.app.data.model.ContentTypeFilter
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.data.model.PearlComment
import com.knowledgepearls.app.data.model.normalizeUserId
import com.knowledgepearls.app.data.repository.AccountRepository
import com.knowledgepearls.app.data.repository.PublicFeedEngagementRepository
import com.knowledgepearls.app.data.repository.PublicFeedRepository
import com.knowledgepearls.app.data.repository.PublicPearlEngagementManager
import com.knowledgepearls.app.data.repository.AddToMyFeedResult
import com.knowledgepearls.app.data.repository.MediaImportResult
import com.knowledgepearls.app.data.search.PublicPearlSearchIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class PublicFeedSection(val label: String) {
    NEW("New"),
    SEEN("Seen"),
}

data class PublicFeedUiState(
    val pearls: List<PublicPearl> = emptyList(),
    val filteredPearls: List<PublicPearl> = emptyList(),
    val feedRefreshGeneration: Int = 0,
    val section: PublicFeedSection = PublicFeedSection.NEW,
    val contentTypeFilter: ContentTypeFilter = ContentTypeFilter.ALL,
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val errorMessage: String? = null,
    val actionSuccessMessage: String? = null,
    val actionOutcome: com.knowledgepearls.app.ui.components.PearlActionOutcome? = null,
    val showEmptyFilterAlert: Boolean = false,
    val showSeenToast: Boolean = false,
    val savePickerPearl: PublicPearl? = null,
    val seenIds: Set<String> = emptySet(),
    val likedPearlIds: Set<String> = emptySet(),
    val commentCounts: Map<String, Int> = emptyMap(),
    val commentsForPearl: List<PearlComment> = emptyList(),
    val commentsPearlId: String? = null,
    val isCommentsLoading: Boolean = false,
    val isPostingComment: Boolean = false,
    val commentsError: String? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val topSearchTags: List<String> = emptyList(),
) {
    val unseenPearls: List<PublicPearl>
        get() = pearls.filter { it.id !in seenIds }

    val seenPearls: List<PublicPearl>
        get() = pearls.filter { it.id in seenIds }

    val newCount: Int get() = unseenPearls.size

    val seenCount: Int get() = seenPearls.size
}

@HiltViewModel
class PublicFeedViewModel @Inject constructor(
    private val repository: PublicFeedRepository,
    private val engagementRepository: PublicFeedEngagementRepository,
    private val engagementManager: PublicPearlEngagementManager,
    private val accountRepository: AccountRepository,
    private val analyticsService: AnalyticsService,
) : ViewModel() {
    private var currentOffset = 0
    private var seenIds = repository.getSeenIds()
    private var hiddenIds = repository.getHiddenIds()
    private var blockedUserIds = repository.getBlockedUserIds()
    private var lastSuccessfulRefreshAt = 0L
    private var lastForcedRefreshAt = 0L
    private val feedMutex = Mutex()

    private fun isVisible(pearl: PublicPearl): Boolean =
        pearl.id !in hiddenIds && normalizeUserId(pearl.userId) !in blockedUserIds

    private val _uiState = MutableStateFlow(
        computeFilteredPearls(
            PublicFeedUiState(seenIds = seenIds),
        ),
    )
    val uiState: StateFlow<PublicFeedUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            engagementManager.likedPearlIds.collect { liked ->
                _uiState.update { it.copy(likedPearlIds = liked) }
            }
        }
        viewModelScope.launch {
            engagementManager.likeCountOverrides.collect { counts ->
                publish { state ->
                    state.copy(
                        pearls = state.pearls.map { pearl ->
                            counts[pearl.id.lowercase()]?.let { pearl.replacing(likeCount = it) } ?: pearl
                        },
                    )
                }
            }
        }
    }

    /** Called when the Public Feed tab becomes active — always fetches latest page-0 pearls. */
    fun onTabEntered() {
        refreshFeed(force = true)
    }

    /** Background stale check while the tab stays open (does not run on every resume). */
    fun refreshFeedIfStale() {
        refreshFeed(force = false)
    }

    fun loadInitial() {
        viewModelScope.launch {
            feedMutex.withLock {
                currentOffset = 0
                reloadLocalVisibilityState()
                publish {
                    it.copy(
                        pearls = emptyList(),
                        feedRefreshGeneration = it.feedRefreshGeneration + 1,
                        isLoading = true,
                        hasMore = true,
                        errorMessage = null,
                        seenIds = seenIds,
                    )
                }
                loadNextPageInternal(reset = true)
            }
        }
    }

    fun refreshFeed(force: Boolean = false) {
        viewModelScope.launch {
            if (!shouldRunRefresh(force)) return@launch
            feedMutex.withLock {
                reloadLocalVisibilityState()
                val existing = _uiState.value.pearls
                if (existing.isEmpty()) {
                    currentOffset = 0
                    publish {
                        it.copy(
                            feedRefreshGeneration = it.feedRefreshGeneration + 1,
                            isLoading = true,
                            hasMore = true,
                            errorMessage = null,
                            seenIds = seenIds,
                        )
                    }
                    loadNextPageInternal(reset = true)
                    return@withLock
                }

                publish { it.copy(isLoading = true, errorMessage = null, seenIds = seenIds) }
                runCatching {
                    repository.fetchPage(offset = 0)
                }.onSuccess { page ->
                    val visible = page.filter(::isVisible)
                    val freshIds = visible.map { it.id }.toSet()
                    val hadNew = visible.any { pearl -> existing.none { it.id == pearl.id } }
                    publish { current ->
                        current.copy(
                            pearls = sortNewestFirst(visible + existing.filter { it.id !in freshIds }),
                            isLoading = false,
                            feedRefreshGeneration = if (hadNew) {
                                current.feedRefreshGeneration + 1
                            } else {
                                current.feedRefreshGeneration
                            },
                            seenIds = seenIds,
                        )
                    }
                    markRefreshSuccess(force)
                    syncEngagement(visible.map { it.id })
                }.onFailure { error ->
                    publish {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Could not refresh public feed.",
                        )
                    }
                }
            }
        }
    }

    fun loadNextPage() {
        viewModelScope.launch {
            feedMutex.withLock {
                loadNextPageInternal(reset = false)
            }
        }
    }

    private suspend fun loadNextPageInternal(reset: Boolean) {
        val state = _uiState.value
        if (!reset && (state.isLoading || !state.hasMore)) return

        publish { it.copy(isLoading = true, errorMessage = null) }

        runCatching {
            repository.fetchPage(offset = currentOffset)
        }.onSuccess { page ->
            if (page.size < PublicFeedRepository.PAGE_SIZE) {
                publish { it.copy(hasMore = false) }
            }
            currentOffset += page.size
            val visible = page.filter(::isVisible)
            publish { current ->
                current.copy(
                    pearls = sortNewestFirst(
                        if (reset) visible else current.pearls + visible,
                    ),
                    isLoading = false,
                    showEmptyFilterAlert = shouldShowEmptyFilterAlert(
                        current.contentTypeFilter,
                        reset,
                        visible,
                    ),
                    seenIds = seenIds,
                )
            }
            if (reset) {
                markRefreshSuccess(force = true)
            }
            syncEngagement(visible.map { it.id })
        }.onFailure { error ->
            publish {
                it.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Could not load public feed.",
                )
            }
        }
    }

    fun setSection(section: PublicFeedSection) {
        publish { it.copy(section = section) }
    }

    fun setSearchQuery(query: String) {
        publish { it.copy(searchQuery = query) }
    }

    fun setSearchActive(active: Boolean) {
        publish {
            if (active) {
                it.copy(isSearchActive = true)
            } else {
                it.copy(isSearchActive = false, searchQuery = "")
            }
        }
    }

    fun setContentTypeFilter(filter: ContentTypeFilter) {
        publish {
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
        publish { it.copy(seenIds = seenIds, showSeenToast = showToast) }
    }

    fun dismissSeenToast() {
        _uiState.update { it.copy(showSeenToast = false) }
    }

    fun markUnseen(pearl: PublicPearl) {
        repository.markUnseen(pearl.id)
        seenIds = repository.getSeenIds()
        publish { it.copy(seenIds = seenIds) }
    }

    fun blockUser(userId: String) {
        repository.blockUser(userId)
        blockedUserIds = repository.getBlockedUserIds()
        publish { current ->
            current.copy(
                pearls = current.pearls.filter { normalizeUserId(it.userId) !in blockedUserIds },
                actionOutcome = com.knowledgepearls.app.ui.components.PearlActionOutcome.RemovedFromFeed,
            )
        }
    }

    fun hide(pearl: PublicPearl) {
        hiddenIds = hiddenIds + pearl.id
        repository.hide(pearl.id)
        publish { current ->
            current.copy(
                pearls = current.pearls.filter { it.id != pearl.id },
                seenIds = seenIds,
                actionOutcome = com.knowledgepearls.app.ui.components.PearlActionOutcome.RemovedFromFeed,
            )
        }
    }

    fun trackAuthGateShown() {
        analyticsService.track(AnalyticsEventKind.TrialGateHit)
    }

    fun trackPublicCardOpened(pearl: PublicPearl) {
        analyticsService.trackPublicCardOpened(pearl)
    }

    fun addToMyFeed(pearl: PublicPearl) {
        viewModelScope.launch {
            val userId = accountRepository.currentUserId()
            runCatching { repository.addToMyFeed(pearl, userId) }
                .onSuccess { result ->
                    if (result is AddToMyFeedResult.Saved) {
                        analyticsService.trackAddedToFeed(
                            publicPearlId = pearl.id,
                            contentType = AnalyticsContentType.forPublicPearl(pearl),
                        )
                    }
                    val importNote = mediaImportNote(result.mediaImport)
                    _uiState.update {
                        it.copy(
                            actionOutcome = when (result) {
                                is AddToMyFeedResult.Saved ->
                                    com.knowledgepearls.app.ui.components.PearlActionOutcome.SavedToMyFeed
                                is AddToMyFeedResult.AlreadyInFeed ->
                                    com.knowledgepearls.app.ui.components.PearlActionOutcome.AlreadyInMyFeed
                            },
                            actionSuccessMessage = pearl.titleDisplay,
                            errorMessage = importNote,
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

    suspend fun fetchPearlById(pearlId: String): PublicPearl? =
        repository.fetchPearlById(pearlId)

    private fun mediaImportNote(import: MediaImportResult?): String? {
        if (import == null || import.attempted == 0) return null
        return when {
            import.isCompleteFailure -> "Saved, but attachments could not be downloaded."
            import.hasPartialFailure ->
                "Saved, but ${import.failed} attachment(s) could not be downloaded."
            else -> null
        }
    }

    fun saveToFolder(pearl: PublicPearl, folderId: String, folderName: String) {
        viewModelScope.launch {
            val userId = accountRepository.currentUserId()
            runCatching { repository.saveToFolder(pearl, folderId, userId) }
                .onSuccess { result ->
                    val importNote = mediaImportNote(result.mediaImport)
                    _uiState.update {
                        it.copy(
                            actionOutcome = when (result) {
                                is AddToMyFeedResult.Saved ->
                                    com.knowledgepearls.app.ui.components.PearlActionOutcome.SavedToFolder
                                is AddToMyFeedResult.AlreadyInFeed ->
                                    com.knowledgepearls.app.ui.components.PearlActionOutcome.AlreadyInMyFeed
                            },
                            actionSuccessMessage = folderName,
                            errorMessage = importNote,
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
            val userId = accountRepository.currentUserId()
            runCatching {
                val folder = repository.createFolder(folderName)
                val result = repository.saveToFolder(pearl, folder.id, userId)
                folder.name to result
            }.onSuccess { (name, result) ->
                val importNote = mediaImportNote(result.mediaImport)
                _uiState.update {
                    it.copy(
                        actionOutcome = when (result) {
                            is AddToMyFeedResult.Saved ->
                                com.knowledgepearls.app.ui.components.PearlActionOutcome.SavedToFolder
                            is AddToMyFeedResult.AlreadyInFeed ->
                                com.knowledgepearls.app.ui.components.PearlActionOutcome.AlreadyInMyFeed
                        },
                        actionSuccessMessage = name,
                        errorMessage = importNote,
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

    fun openSavePicker(pearl: PublicPearl) {
        _uiState.update { it.copy(savePickerPearl = pearl) }
    }

    fun dismissSavePicker() {
        _uiState.update { it.copy(savePickerPearl = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun toggleLike(pearl: PublicPearl) {
        viewModelScope.launch {
            engagementManager.toggleLike(pearl).onFailure { error ->
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "Could not update like.")
                }
            }
        }
    }

    fun isLiked(pearlId: String): Boolean = engagementManager.isLiked(pearlId)

    fun likeCount(pearl: PublicPearl): Int = engagementManager.likeCount(pearl)

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
        if (pearlIds.isEmpty()) return
        val pearls = _uiState.value.pearls.filter { it.id in pearlIds }
        engagementManager.mergeServerPearls(pearls)
        engagementManager.syncLikedState(pearlIds)
        runCatching {
            val counts = engagementRepository.fetchCommentCounts(pearlIds)
            _uiState.update { it.copy(commentCounts = it.commentCounts + counts) }
        }
    }

    private fun shouldRunRefresh(force: Boolean): Boolean {
        val now = System.currentTimeMillis()
        if (force) {
            if (lastForcedRefreshAt != 0L && now - lastForcedRefreshAt < TAB_ENTER_DEBOUNCE_MS) {
                return false
            }
            lastForcedRefreshAt = now
            return true
        }
        if (lastSuccessfulRefreshAt == 0L) return true
        return now - lastSuccessfulRefreshAt >= STALE_AFTER_MS
    }

    private fun markRefreshSuccess(force: Boolean) {
        lastSuccessfulRefreshAt = System.currentTimeMillis()
        if (force) {
            lastForcedRefreshAt = lastSuccessfulRefreshAt
        }
    }

    private fun reloadLocalVisibilityState() {
        seenIds = repository.getSeenIds()
        hiddenIds = repository.getHiddenIds()
        blockedUserIds = repository.getBlockedUserIds()
    }

    private fun publish(transform: (PublicFeedUiState) -> PublicFeedUiState) {
        _uiState.update { computeFilteredPearls(transform(it)) }
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

    private fun sortNewestFirst(pearls: List<PublicPearl>): List<PublicPearl> =
        pearls.sortedWith(
            compareByDescending<PublicPearl> { it.feedSortMillis }
                .thenByDescending { it.id },
        )

    private companion object {
        const val TAB_ENTER_DEBOUNCE_MS = 2_000L
        const val STALE_AFTER_MS = 5 * 60_000L
    }
}

private fun computeFilteredPearls(state: PublicFeedUiState): PublicFeedUiState {
    val sectionPearls = when (state.section) {
        PublicFeedSection.NEW -> state.pearls.filter { it.id !in state.seenIds }
        PublicFeedSection.SEEN -> state.pearls.filter { it.id in state.seenIds }
    }
    val searchablePearls = if (state.searchQuery.isNotBlank()) state.pearls else sectionPearls
    val searchIndex = PublicPearlSearchIndex.build(searchablePearls)
    val filtered = searchablePearls
        .filter { it.matches(state.contentTypeFilter) }
        .filter { searchIndex.matchesPearl(it.id, state.searchQuery) }
        .sortedByDescending { it.feedSortMillis }
    return state.copy(
        filteredPearls = filtered,
        topSearchTags = PublicPearlSearchIndex.topTags(state.pearls),
    )
}
