package com.knowledgepearls.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.capture.CaptureRepository
import com.knowledgepearls.app.data.capture.parseTags
import com.knowledgepearls.app.data.local.model.ClinicalCasePayload
import com.knowledgepearls.app.data.local.model.clinicalCasePayload
import com.knowledgepearls.app.data.local.model.effectiveSourceReference
import com.knowledgepearls.app.data.local.model.belongsInMyFeed
import com.knowledgepearls.app.data.local.model.isClinicalCase
import com.knowledgepearls.app.data.local.model.matches
import com.knowledgepearls.app.data.local.model.toPickedMedia
import com.knowledgepearls.app.data.model.ContentTypeFilter
import com.knowledgepearls.app.data.model.ShareProfileResult
import com.knowledgepearls.app.data.repository.AccountRepository
import com.knowledgepearls.app.data.repository.KnowledgePearlRepository
import com.knowledgepearls.app.data.repository.PearlShareRepository
import com.knowledgepearls.app.data.repository.PublicFeedSharingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedUiState(
    val pearls: List<PearlWithMedia> = emptyList(),
    val filteredPearls: List<PearlWithMedia> = emptyList(),
    val allTags: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedTag: String? = null,
    val contentTypeFilter: ContentTypeFilter = ContentTypeFilter.ALL,
    val isSearchActive: Boolean = false,
    val showEmptyFilterAlert: Boolean = false,
    val deleteTarget: PearlWithMedia? = null,
    val actionSuccessMessage: String? = null,
    val isSharingPearl: Boolean = false,
    val shareErrorMessage: String? = null,
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val pearlRepository: KnowledgePearlRepository,
    private val accountRepository: AccountRepository,
    private val publicFeedSharingRepository: PublicFeedSharingRepository,
    private val pearlShareRepository: PearlShareRepository,
    private val captureRepository: CaptureRepository,
) : ViewModel() {
    private val searchQuery = MutableStateFlow("")
    private val selectedTag = MutableStateFlow<String?>(null)
    private val contentTypeFilter = MutableStateFlow(ContentTypeFilter.ALL)
    private val isSearchActive = MutableStateFlow(false)
    private val deleteTarget = MutableStateFlow<PearlWithMedia?>(null)
    private val actionSuccessMessage = MutableStateFlow<String?>(null)
    private val isSharingPearl = MutableStateFlow(false)
    private val shareErrorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<FeedUiState> = combine(
        pearlRepository.observeAllPearls(),
        searchQuery,
        selectedTag,
        contentTypeFilter,
        isSearchActive,
        deleteTarget,
        actionSuccessMessage,
        isSharingPearl,
        shareErrorMessage,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val pearls = values[0] as List<PearlWithMedia>
        val query = values[1] as String
        val tag = values[2] as String?
        val filter = values[3] as ContentTypeFilter
        val searching = values[4] as Boolean
        val delete = values[5] as PearlWithMedia?
        val success = values[6] as String?
        val sharing = values[7] as Boolean
        val shareError = values[8] as String?

        val filtered = pearls.filter { pearl ->
            pearl.pearl.belongsInMyFeed() &&
                pearl.matches(filter) &&
                (tag == null || tag in pearl.pearl.tags) &&
                (query.isBlank() || pearlMatchesQuery(pearl, query))
        }

        val emptyFilterAlert = filter != ContentTypeFilter.ALL &&
            pearls.none { it.pearl.belongsInMyFeed() && it.matches(filter) }

        FeedUiState(
            pearls = pearls,
            filteredPearls = filtered,
            allTags = pearls.flatMap { it.pearl.tags }.distinct().sorted(),
            searchQuery = query,
            selectedTag = tag,
            contentTypeFilter = filter,
            isSearchActive = searching,
            showEmptyFilterAlert = emptyFilterAlert,
            deleteTarget = delete,
            actionSuccessMessage = success,
            isSharingPearl = sharing,
            shareErrorMessage = shareError,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeedUiState())

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setSearchActive(active: Boolean) {
        isSearchActive.value = active
        if (!active) searchQuery.value = ""
    }

    fun setSelectedTag(tag: String?) {
        selectedTag.value = tag
    }

    fun setContentTypeFilter(filter: ContentTypeFilter) {
        contentTypeFilter.value = filter
    }

    fun dismissEmptyFilterAlert() {
        contentTypeFilter.value = ContentTypeFilter.ALL
    }

    fun resetContentTypeFilter() {
        contentTypeFilter.value = ContentTypeFilter.ALL
    }

    fun requestDelete(pearl: PearlWithMedia) {
        deleteTarget.value = pearl
    }

    fun cancelDelete() {
        deleteTarget.value = null
    }

    fun confirmDelete() {
        val target = deleteTarget.value ?: return
        viewModelScope.launch {
            pearlRepository.deletePearl(target.pearl.id)
            deleteTarget.value = null
            actionSuccessMessage.value = "Pearl deleted"
        }
    }

    fun dismissActionSuccess() {
        actionSuccessMessage.value = null
    }

    fun dismissShareError() {
        shareErrorMessage.value = null
    }

    suspend fun searchShareProfiles(query: String): List<ShareProfileResult> =
        pearlShareRepository.searchProfiles(query)

    fun sendFriendShare(
        pearl: PearlWithMedia,
        recipientIds: List<String>,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            isSharingPearl.value = true
            runCatching {
                pearlShareRepository.sharePearlWithFriends(
                    pearl = pearl,
                    recipientIds = recipientIds,
                )
            }.onSuccess { onSuccess() }
                .onFailure { shareErrorMessage.value = it.message ?: "Share failed" }
            isSharingPearl.value = false
        }
    }

    fun sharePearlToPublicFeed(
        pearl: PearlWithMedia,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            isSharingPearl.value = true
            runCatching {
                val entity = pearl.pearl
                val publicId = if (entity.isClinicalCase()) {
                    publicFeedSharingRepository.shareClinicalCase(
                        title = entity.title,
                        payload = entity.clinicalCasePayload(),
                        tags = entity.tags,
                        sectionMedia = pearl.mediaItems
                            .filter { it.sectionTag.isNotBlank() }
                            .groupBy { it.sectionTag }
                            .mapValues { (_, items) -> items.toPickedMedia() },
                    )
                } else {
                    publicFeedSharingRepository.shareStandardPearl(
                        title = entity.title,
                        notes = entity.notes,
                        tags = entity.tags,
                        sourceUrl = entity.sourceURL,
                        linkPreviewDescription = entity.linkPreviewDescription,
                        sourceReference = entity.effectiveSourceReference(),
                        mediaItems = pearl.mediaItems.toPickedMedia(),
                    )
                }
                pearlRepository.updatePublicPearlStatus(
                    pearlId = entity.id,
                    publicPearlId = publicId,
                    status = "pending",
                    isSharedPublicly = true,
                )
            }.onSuccess { onSuccess() }
                .onFailure { shareErrorMessage.value = it.message ?: "Share failed" }
            isSharingPearl.value = false
        }
    }

    fun withdrawPearlFromPublicFeed(pearl: PearlWithMedia) {
        val publicId = pearl.pearl.publicPearlID ?: return
        viewModelScope.launch {
            isSharingPearl.value = true
            runCatching {
                publicFeedSharingRepository.withdraw(publicId)
                pearlRepository.updatePublicPearlStatus(
                    pearlId = pearl.pearl.id,
                    publicPearlId = null,
                    status = "",
                    isSharedPublicly = false,
                )
                actionSuccessMessage.value = "Withdrawn from Public Feed"
            }.onFailure { shareErrorMessage.value = it.message ?: "Withdraw failed" }
            isSharingPearl.value = false
        }
    }

    fun showCaptureSavedMessage() {
        actionSuccessMessage.value = "Pearl saved to My Feed"
    }

    fun observePearl(id: String) = pearlRepository.observePearl(id)

    fun confirmDeleteForPearl(pearlId: String) {
        viewModelScope.launch {
            pearlRepository.deletePearl(pearlId)
            actionSuccessMessage.value = "Pearl deleted"
        }
    }

    fun toggleFavourite(pearlId: String) {
        viewModelScope.launch {
            pearlRepository.toggleFavourite(pearlId)
        }
    }

    fun savePearlEdits(
        pearl: PearlWithMedia,
        title: String,
        notes: String,
        sourceReference: String,
        tagsRaw: String,
        clinicalPayload: ClinicalCasePayload? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            runCatching {
                val entity = pearl.pearl
                val tags = parseTags(tagsRaw)
                if (entity.isClinicalCase() && clinicalPayload != null) {
                    captureRepository.updateClinicalCasePearl(
                        existing = entity,
                        title = title,
                        payload = clinicalPayload,
                        tags = tags,
                    )
                } else {
                    captureRepository.updateStandardPearl(
                        existing = entity,
                        title = title,
                        notes = notes,
                        sourceReference = sourceReference,
                        tags = tags,
                    )
                }
            }.onSuccess {
                actionSuccessMessage.value = "Pearl updated"
                onSuccess()
            }.onFailure { error ->
                onError(error.message ?: "Could not save changes")
            }
        }
    }

    suspend fun fetchAvatarUrl(userId: String): String? =
        accountRepository.fetchAvatarUrl(userId)

    private fun pearlMatchesQuery(pearl: PearlWithMedia, query: String): Boolean {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) return true
        val p = pearl.pearl
        if (p.title.lowercase().contains(needle)) return true
        if (p.notes.lowercase().contains(needle)) return true
        if (p.sourceReference.lowercase().contains(needle)) return true
        if (p.tags.any { it.lowercase().contains(needle) }) return true
        if (p.isClinicalCase()) {
            val payload = p.clinicalCasePayload()
            return payload.history.lowercase().contains(needle) ||
                payload.diagnosis.lowercase().contains(needle)
        }
        return false
    }
}
