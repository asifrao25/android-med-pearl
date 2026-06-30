package com.knowledgepearls.app.ui.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledgepearls.app.data.analytics.AnalyticsService
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.local.model.belongsInMyFeed
import com.knowledgepearls.app.data.repository.KnowledgePearlRepository
import com.knowledgepearls.app.data.search.PearlSearchIndex
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavouritesUiState(
    val favourites: List<PearlWithMedia> = emptyList(),
    val filteredFavourites: List<PearlWithMedia> = emptyList(),
    val topSearchTags: List<String> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val repository: KnowledgePearlRepository,
    private val analyticsService: AnalyticsService,
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val isSearchActive = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<FavouritesUiState> = combine(
        repository.observeFavourites(),
        searchQuery,
        isSearchActive,
        errorMessage,
    ) { pearls, query, searching, error ->
        val feedPearls = pearls.filter { it.pearl.belongsInMyFeed() }
        val searchIndex = PearlSearchIndex.build(feedPearls)
        val filtered = feedPearls.filter { pearl ->
            query.isBlank() || searchIndex.matchesPearl(pearl.pearl.id, query)
        }
        FavouritesUiState(
            favourites = feedPearls,
            filteredFavourites = filtered,
            topSearchTags = PearlSearchIndex.topTags(feedPearls),
            searchQuery = query,
            isSearchActive = searching,
            errorMessage = error,
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavouritesUiState())

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setSearchActive(active: Boolean) {
        isSearchActive.value = active
        if (!active) searchQuery.value = ""
    }

    fun deletePearl(id: String) {
        viewModelScope.launch {
            runCatching {
                repository.getPearlWithMedia(id)?.let { pearlWithMedia ->
                    analyticsService.trackPearlDeleted(pearlWithMedia.pearl, pearlWithMedia.mediaItems)
                }
                repository.deletePearl(id)
            }.onFailure { error ->
                errorMessage.value = error.message ?: "Could not delete pearl"
            }
        }
    }

    fun toggleFavourite(pearlId: String) {
        viewModelScope.launch {
            runCatching {
                repository.toggleFavourite(pearlId)
            }.onFailure { error ->
                errorMessage.value = error.message ?: "Could not update favourite"
            }
        }
    }

    fun dismissError() {
        errorMessage.update { null }
    }
}
