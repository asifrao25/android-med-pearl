package com.knowledgepearls.app.ui.publicfeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledgepearls.app.data.model.ContentTypeFilter
import com.knowledgepearls.app.data.model.PublicPearl
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
    val showEmptyFilterAlert: Boolean = false,
    val seenIds: Set<String> = emptySet(),
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
) : ViewModel() {
    private var currentOffset = 0
    private var seenIds = repository.getSeenIds()
    private var hiddenIds = repository.getHiddenIds()

    private val _uiState = MutableStateFlow(PublicFeedUiState(seenIds = seenIds))
    val uiState: StateFlow<PublicFeedUiState> = _uiState.asStateFlow()

    fun loadInitial() {
        viewModelScope.launch {
            currentOffset = 0
            seenIds = repository.getSeenIds()
            hiddenIds = repository.getHiddenIds()
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
            val visible = page.filter { it.id !in hiddenIds }
            _uiState.update { current ->
                current.copy(
                    pearls = if (reset) visible else current.pearls + visible,
                    isLoading = false,
                    showEmptyFilterAlert = shouldShowEmptyFilterAlert(current.contentTypeFilter, reset, visible),
                ).copy(seenIds = seenIds)
            }
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

    fun markSeen(pearl: PublicPearl) {
        if (pearl.id in seenIds) return
        repository.markSeen(pearl.id)
        seenIds = repository.getSeenIds()
        _uiState.update { it.copy(seenIds = seenIds) }
    }

    fun markUnseen(pearl: PublicPearl) {
        repository.markUnseen(pearl.id)
        seenIds = repository.getSeenIds()
        _uiState.update { it.copy(seenIds = seenIds) }
    }

    fun hide(pearl: PublicPearl) {
        hiddenIds = hiddenIds + pearl.id
        repository.hide(pearl.id)
        _uiState.update { current ->
            current.copy(pearls = current.pearls.filter { it.id != pearl.id }, seenIds = seenIds)
        }
    }

    fun addToMyFeed(pearl: PublicPearl) {
        viewModelScope.launch {
            runCatching { repository.addToMyFeed(pearl) }
                .onSuccess {
                    _uiState.update { it.copy(actionSuccessMessage = "Saved to My Feed") }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "Could not save pearl.")
                    }
                }
        }
    }

    fun dismissActionSuccess() {
        _uiState.update { it.copy(actionSuccessMessage = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
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
