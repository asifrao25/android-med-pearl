package com.knowledgepearls.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.analytics.AnalyticsContentType
import com.knowledgepearls.app.data.analytics.AnalyticsService
import com.knowledgepearls.app.data.capture.CaptureRepository
import com.knowledgepearls.app.data.capture.parseTags
import com.knowledgepearls.app.data.search.PearlSearchIndex
import com.knowledgepearls.app.data.local.model.ClinicalCasePayload
import com.knowledgepearls.app.data.local.model.clinicalCasePayload
import com.knowledgepearls.app.data.local.model.effectiveSourceReference
import com.knowledgepearls.app.data.local.model.belongsInMyFeed
import com.knowledgepearls.app.data.local.model.isClinicalCase
import com.knowledgepearls.app.data.local.model.matches
import com.knowledgepearls.app.data.local.model.toPickedMedia
import com.knowledgepearls.app.data.model.ContentTypeFilter
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.data.model.ShareProfileResult
import com.knowledgepearls.app.data.repository.AccountRepository
import com.knowledgepearls.app.data.repository.KnowledgePearlRepository
import com.knowledgepearls.app.data.repository.PearlShareRepository
import com.knowledgepearls.app.data.repository.PublicFeedRepository
import com.knowledgepearls.app.data.repository.PublicFeedSharingRepository
import com.knowledgepearls.app.data.repository.PublicFeedWithdrawal
import com.knowledgepearls.app.data.prefs.RecentShareRecipient
import com.knowledgepearls.app.data.prefs.RecentShareRecipientsStore
import com.knowledgepearls.app.data.repository.PublicPearlEngagementManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class FeedUiState(
    val pearls: List<PearlWithMedia> = emptyList(),
    val filteredPearls: List<PearlWithMedia> = emptyList(),
    val allTags: List<String> = emptyList(),
    val topSearchTags: List<String> = emptyList(),
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
    private val publicFeedWithdrawal: PublicFeedWithdrawal,
    private val publicFeedRepository: PublicFeedRepository,
    private val pearlShareRepository: PearlShareRepository,
    private val recentShareRecipientsStore: RecentShareRecipientsStore,
    private val captureRepository: CaptureRepository,
    private val engagementManager: PublicPearlEngagementManager,
    private val analyticsService: AnalyticsService,
) : ViewModel() {
    val likedPearlIds = engagementManager.likedPearlIds
    val likeCountOverrides = engagementManager.likeCountOverrides

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

        val feedPearls = pearls.filter { it.pearl.belongsInMyFeed() }
        val searchIndex = PearlSearchIndex.build(feedPearls)

        val filtered = feedPearls.filter { pearl ->
            pearl.matches(filter) &&
                (tag == null || tag in pearl.pearl.tags) &&
                (query.isBlank() || searchIndex.matchesPearl(pearl.pearl.id, query))
        }

        val emptyFilterAlert = filter != ContentTypeFilter.ALL &&
            feedPearls.none { it.matches(filter) }

        FeedUiState(
            pearls = pearls,
            filteredPearls = filtered,
            allTags = feedPearls.flatMap { it.pearl.tags }.distinct().sorted(),
            topSearchTags = PearlSearchIndex.topTags(feedPearls),
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
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FeedUiState())

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
            runCatching {
                analyticsService.trackPearlDeleted(target.pearl, target.mediaItems)
                pearlRepository.deletePearl(target.pearl.id)
            }.onSuccess {
                deleteTarget.value = null
                actionSuccessMessage.value = "Pearl deleted"
            }.onFailure { error ->
                deleteTarget.value = null
                shareErrorMessage.value = error.message ?: "Could not delete pearl"
            }
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

    suspend fun loadRecentShareRecipients(): List<ShareProfileResult> =
        recentShareRecipientsStore.getRecent().map { recipient ->
            ShareProfileResult(
                id = recipient.id,
                name = recipient.name,
                allowPearlShares = true,
            )
        }

    fun sendFriendShare(
        pearl: PearlWithMedia,
        recipients: List<ShareProfileResult>,
        onSuccess: () -> Unit,
    ) {
        val recipientIds = recipients.map { it.id }
        viewModelScope.launch {
            isSharingPearl.value = true
            runCatching {
                val duplicates = pearlShareRepository.checkShareDuplicates(pearl, recipientIds)
                if (duplicates.isNotEmpty()) {
                    val names = duplicates.joinToString(", ") { it.recipientName }
                    error("These recipients already have this pearl: $names")
                }
                pearlShareRepository.sharePearlWithFriends(
                    pearl = pearl,
                    recipientIds = recipientIds,
                )
            }.onSuccess {
                analyticsService.trackPearlSharedFriend(pearl, recipientIds.size)
                recentShareRecipientsStore.recordShares(
                    recipients.map { recipient ->
                        RecentShareRecipient(
                            id = recipient.id,
                            name = recipient.name,
                        )
                    },
                )
                onSuccess()
            }.onFailure { shareErrorMessage.value = it.message ?: "Share failed" }
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
                analyticsService.trackPearlSharedPublic(
                    pearlId = publicId,
                    contentType = AnalyticsContentType.forPearl(pearl),
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
                publicFeedWithdrawal.withdrawSubmission(publicId)
                actionSuccessMessage.value = "Withdrawn from Public Feed"
            }.onFailure { shareErrorMessage.value = it.message ?: "Withdraw failed" }
            isSharingPearl.value = false
        }
    }

    fun showCaptureSavedMessage() {
        actionSuccessMessage.value = "Pearl saved to My Feed"
    }

    fun showPublicSubmissionMessage() {
        actionSuccessMessage.value = "Submitted to Public Feed for review"
    }

    suspend fun fetchPublicPearlForCard(publicPearlId: String): PublicPearl? =
        publicFeedRepository.fetchPearlById(publicPearlId)

    fun observePearl(id: String) = pearlRepository.observePearl(id)

    fun confirmDeleteForPearl(pearlId: String) {
        viewModelScope.launch {
            runCatching {
                pearlRepository.getPearlWithMedia(pearlId)?.let { pearlWithMedia ->
                    analyticsService.trackPearlDeleted(pearlWithMedia.pearl, pearlWithMedia.mediaItems)
                }
                pearlRepository.deletePearl(pearlId)
            }.onSuccess {
                actionSuccessMessage.value = "Pearl deleted"
            }.onFailure { error ->
                shareErrorMessage.value = error.message ?: "Could not delete pearl"
            }
        }
    }

    fun toggleFavourite(pearlId: String) {
        viewModelScope.launch {
            runCatching {
                val pearl = pearlRepository.getPearlWithMedia(pearlId)?.pearl
                val wasFavourite = pearl?.isFavourite == true
                pearlRepository.toggleFavourite(pearlId)
                if (!wasFavourite) {
                    analyticsService.track(
                        kind = com.knowledgepearls.app.data.analytics.AnalyticsEventKind.PearlFavourited,
                        pearlId = pearlId,
                    )
                }
            }.onFailure { error ->
                shareErrorMessage.value = error.message ?: "Could not update favourite"
            }
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

    fun isPublicPearlLiked(pearlId: String): Boolean = engagementManager.isLiked(pearlId)

    fun publicPearlLikeCount(pearl: PublicPearl): Int = engagementManager.likeCount(pearl)

    fun togglePublicPearlLike(pearl: PublicPearl) {
        viewModelScope.launch {
            engagementManager.toggleLike(pearl).onFailure { error ->
                shareErrorMessage.value = error.message ?: "Could not update like."
            }
        }
    }

    fun syncPublicPearlEngagement(pearls: List<PublicPearl>) {
        viewModelScope.launch {
            engagementManager.mergeServerPearls(pearls)
            engagementManager.syncLikedState(pearls.map { it.id })
        }
    }

    fun trackCardOpened(pearl: PearlWithMedia) {
        val ownerLabel = pearl.pearl.sharedByName.takeIf { it.isNotBlank() }
        val isPublic = pearl.pearl.publicPearlID != null
        analyticsService.trackCardOpened(
            pearl = pearl,
            isPublic = isPublic,
            ownerLabel = ownerLabel,
        )
    }

    fun trackLinkOpened(url: String, pearlId: String?) {
        analyticsService.trackLinkOpened(url, pearlId)
    }

    suspend fun fetchAvatarUrl(userId: String): String? =
        accountRepository.fetchAvatarUrl(userId)
}
