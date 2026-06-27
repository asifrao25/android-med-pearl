package com.knowledgepearls.app.ui.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.repository.KnowledgePearlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val repository: KnowledgePearlRepository,
) : ViewModel() {
    val favourites: StateFlow<List<PearlWithMedia>> =
        repository.observeFavourites()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deletePearl(id: String) {
        viewModelScope.launch { repository.deletePearl(id) }
    }

    fun toggleFavourite(pearlId: String) {
        viewModelScope.launch { repository.toggleFavourite(pearlId) }
    }
}
