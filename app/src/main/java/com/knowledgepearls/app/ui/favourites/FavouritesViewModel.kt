package com.knowledgepearls.app.ui.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.local.model.belongsInMyFeed
import com.knowledgepearls.app.data.repository.KnowledgePearlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val repository: KnowledgePearlRepository,
) : ViewModel() {
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val favourites: StateFlow<List<PearlWithMedia>> =
        repository.observeFavourites()
            .map { pearls -> pearls.filter { it.pearl.belongsInMyFeed() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deletePearl(id: String) {
        viewModelScope.launch {
            runCatching {
                repository.deletePearl(id)
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Could not delete pearl"
            }
        }
    }

    fun toggleFavourite(pearlId: String) {
        viewModelScope.launch {
            runCatching {
                repository.toggleFavourite(pearlId)
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Could not update favourite"
            }
        }
    }

    fun dismissError() {
        _errorMessage.update { null }
    }
}
