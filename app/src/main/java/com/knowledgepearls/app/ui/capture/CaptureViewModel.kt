package com.knowledgepearls.app.ui.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledgepearls.app.data.capture.CaptureRepository
import com.knowledgepearls.app.data.capture.LinkPreview
import com.knowledgepearls.app.data.capture.LinkPreviewFetcher
import com.knowledgepearls.app.data.capture.PickedMedia
import com.knowledgepearls.app.data.capture.parseTags
import com.knowledgepearls.app.data.local.model.ClinicalCasePayload
import com.knowledgepearls.app.data.repository.KnowledgePearlRepository
import com.knowledgepearls.app.data.repository.PublicFeedSharingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LinkPreviewUiState(
    val isLoading: Boolean = false,
    val preview: LinkPreview? = null,
    val previewImagePath: String? = null,
    val error: String? = null,
)

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val captureRepository: CaptureRepository,
    private val linkPreviewFetcher: LinkPreviewFetcher,
    private val publicFeedSharingRepository: PublicFeedSharingRepository,
    private val pearlRepository: KnowledgePearlRepository,
) : ViewModel() {
    private val _linkPreview = MutableStateFlow(LinkPreviewUiState())
    val linkPreview: StateFlow<LinkPreviewUiState> = _linkPreview.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private var previewJob: Job? = null

    fun fetchLinkPreview(url: String) {
        previewJob?.cancel()
        val normalized = normalizeUrl(url)
        if (normalized == null) {
            _linkPreview.value = LinkPreviewUiState()
            return
        }
        previewJob = viewModelScope.launch {
            _linkPreview.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val preview = linkPreviewFetcher.fetch(normalized)
                val imagePath = preview.imageUrl?.let { linkPreviewFetcher.downloadPreviewImage(it) }
                _linkPreview.update {
                    it.copy(isLoading = false, preview = preview, previewImagePath = imagePath)
                }
            }.onFailure { error ->
                _linkPreview.update {
                    it.copy(isLoading = false, error = error.message ?: "Could not fetch preview")
                }
            }
        }
    }

    fun saveQuickPearl(
        title: String,
        notes: String,
        sourceReference: String,
        tagsRaw: String,
        media: List<PickedMedia>,
        shareToPublicFeed: Boolean = false,
        saveToMyFeed: Boolean = true,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) = save {
        val tags = parseTags(tagsRaw)
        persistPearl(
            shareToPublicFeed = shareToPublicFeed,
            saveToMyFeed = saveToMyFeed,
            share = {
                publicFeedSharingRepository.shareStandardPearl(
                    title = title,
                    notes = notes,
                    tags = tags,
                    sourceUrl = null,
                    linkPreviewDescription = "",
                    sourceReference = sourceReference,
                    mediaItems = media,
                )
            },
            saveLocal = {
                captureRepository.saveQuickPearl(
                    title = title,
                    notes = notes,
                    sourceReference = sourceReference,
                    tags = tags,
                    mediaItems = media,
                )
            },
        )
    }.invoke(onSuccess, onError)

    fun saveWebLinkPearl(
        title: String,
        notes: String,
        sourceReference: String,
        tagsRaw: String,
        url: String,
        shareToPublicFeed: Boolean = false,
        saveToMyFeed: Boolean = true,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        val normalized = normalizeUrl(url) ?: run {
            onError("Enter a valid URL")
            return
        }
        val preview = _linkPreview.value
        val tags = parseTags(tagsRaw)
        save {
            val resolvedTitle = title.ifBlank { preview.preview?.title.orEmpty() }
            val resolvedReference = sourceReference.ifBlank { normalized }
            persistPearl(
                shareToPublicFeed = shareToPublicFeed,
                saveToMyFeed = saveToMyFeed,
                share = {
                    publicFeedSharingRepository.shareStandardPearl(
                        title = resolvedTitle,
                        notes = notes,
                        tags = tags,
                        sourceUrl = normalized,
                        linkPreviewDescription = preview.preview?.description.orEmpty(),
                        sourceReference = resolvedReference,
                        mediaItems = emptyList(),
                    )
                },
                saveLocal = {
                    captureRepository.saveWebLinkPearl(
                        title = resolvedTitle,
                        notes = notes,
                        sourceReference = resolvedReference,
                        tags = tags,
                        sourceURL = normalized,
                        linkPreviewImagePath = preview.previewImagePath,
                        linkPreviewDescription = preview.preview?.description.orEmpty(),
                    )
                },
            )
        }.invoke(onSuccess, onError)
    }

    fun saveMediaPearl(
        title: String,
        notes: String,
        sourceReference: String,
        tagsRaw: String,
        media: List<PickedMedia>,
        shareToPublicFeed: Boolean = false,
        saveToMyFeed: Boolean = true,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) = save {
        val tags = parseTags(tagsRaw)
        persistPearl(
            shareToPublicFeed = shareToPublicFeed,
            saveToMyFeed = saveToMyFeed,
            share = {
                publicFeedSharingRepository.shareStandardPearl(
                    title = title,
                    notes = notes,
                    tags = tags,
                    sourceUrl = null,
                    linkPreviewDescription = "",
                    sourceReference = sourceReference,
                    mediaItems = media,
                )
            },
            saveLocal = {
                captureRepository.saveMediaPearl(
                    title = title,
                    notes = notes,
                    sourceReference = sourceReference,
                    tags = tags,
                    mediaItems = media,
                )
            },
        )
    }.invoke(onSuccess, onError)

    fun saveClinicalCasePearl(
        title: String,
        payload: ClinicalCasePayload,
        tagsRaw: String,
        sectionMedia: Map<String, List<PickedMedia>>,
        shareToPublicFeed: Boolean = false,
        saveToMyFeed: Boolean = true,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) = save {
        val tags = parseTags(tagsRaw)
        persistPearl(
            shareToPublicFeed = shareToPublicFeed,
            saveToMyFeed = saveToMyFeed,
            share = {
                publicFeedSharingRepository.shareClinicalCase(
                    title = title,
                    payload = payload,
                    tags = tags,
                    sectionMedia = sectionMedia,
                )
            },
            saveLocal = {
                captureRepository.saveClinicalCasePearl(
                    title = title,
                    payload = payload,
                    tags = tags,
                    sectionMedia = sectionMedia,
                )
            },
        )
    }.invoke(onSuccess, onError)

    /** Remote-first when sharing publicly (matches iOS PearlCaptureSaver). */
    private suspend fun persistPearl(
        shareToPublicFeed: Boolean,
        saveToMyFeed: Boolean,
        share: suspend () -> String,
        saveLocal: suspend () -> String,
    ): String {
        val publicId = if (shareToPublicFeed) share() else null
        val localId = if (saveToMyFeed) saveLocal() else ""
        if (publicId != null && saveToMyFeed && localId.isNotBlank()) {
            pearlRepository.updatePublicPearlStatus(
                pearlId = localId,
                publicPearlId = publicId,
                status = "pending",
                isSharedPublicly = true,
            )
        }
        return localId.ifBlank { publicId ?: "submitted" }
    }

    private fun save(block: suspend () -> String): (onSuccess: (String) -> Unit, onError: (String) -> Unit) -> Unit {
        return { onSuccess, onError ->
            viewModelScope.launch {
                _isSaving.value = true
                runCatching { block() }
                    .onSuccess(onSuccess)
                    .onFailure { onError(it.message ?: "Save failed") }
                _isSaving.value = false
            }
        }
    }

    private fun normalizeUrl(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return null
        val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "https://$trimmed"
        }
        return runCatching { java.net.URI(withScheme).toURL().toString() }.getOrNull()
    }
}
