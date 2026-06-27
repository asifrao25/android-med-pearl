package com.knowledgepearls.app.ui.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledgepearls.app.data.local.model.FolderWithCount
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.repository.KnowledgePearlRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FoldersViewModel @Inject constructor(
    private val repository: KnowledgePearlRepository,
) : ViewModel() {
    val foldersWithCounts: StateFlow<List<FolderWithCount>> =
        repository.observeFoldersWithCounts()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun observePearlsInFolder(folderId: String): Flow<List<PearlWithMedia>> =
        repository.observePearlsInFolder(folderId)

    fun observePearlFolderIds(pearlId: String): Flow<Set<String>> =
        repository.observePearlFolderIds(pearlId)

    fun createFolder(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { repository.createFolder(trimmed) }
    }

    fun renameFolder(folderId: String, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { repository.renameFolder(folderId, trimmed) }
    }

    fun deleteFolder(folder: FolderWithCount) {
        viewModelScope.launch {
            repository.deleteFolder(folder.folder)
        }
    }

    fun togglePearlFolderMembership(pearlId: String, folderId: String) {
        viewModelScope.launch {
            repository.togglePearlFolderMembership(pearlId, folderId)
        }
    }

    fun createFolderAndAddPearl(name: String, pearlId: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val folder = repository.createFolder(trimmed)
            repository.addPearlToFolder(pearlId, folder.id)
        }
    }

    fun deletePearl(id: String) {
        viewModelScope.launch { repository.deletePearl(id) }
    }
}
