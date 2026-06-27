package com.knowledgepearls.app.ui.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowledgepearls.app.data.capture.CaptureRepository
import com.knowledgepearls.app.data.capture.LinkPreview
import com.knowledgepearls.app.data.capture.LinkPreviewFetcher
import com.knowledgepearls.app.data.capture.PickedMedia
import com.knowledgepearls.app.data.capture.parseTags
import com.knowledgepearls.app.data.local.model.ClinicalCasePayload
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
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) = save {
        captureRepository.saveQuickPearl(
            title = title,
            notes = notes,
            sourceReference = sourceReference,
            tags = parseTags(tagsRaw),
            mediaItems = media,
        )
    }.invoke(onSuccess, onError)

    fun saveWebLinkPearl(
        title: String,
        notes: String,
        sourceReference: String,
        tagsRaw: String,
        url: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        val normalized = normalizeUrl(url) ?: run {
            onError("Enter a valid URL")
            return
        }
        val preview = _linkPreview.value
        save {
            captureRepository.saveWebLinkPearl(
                title = title.ifBlank { preview.preview?.title.orEmpty() },
                notes = notes,
                sourceReference = sourceReference.ifBlank { normalized },
                tags = parseTags(tagsRaw),
                sourceURL = normalized,
                linkPreviewImagePath = preview.previewImagePath,
                linkPreviewDescription = preview.preview?.description.orEmpty(),
            )
        }.invoke(onSuccess, onError)
    }

    fun saveMediaPearl(
        title: String,
        notes: String,
        sourceReference: String,
        tagsRaw: String,
        media: List<PickedMedia>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) = save {
        captureRepository.saveMediaPearl(
            title = title,
            notes = notes,
            sourceReference = sourceReference,
            tags = parseTags(tagsRaw),
            mediaItems = media,
        )
    }.invoke(onSuccess, onError)

    fun saveClinicalCasePearl(
        title: String,
        payload: ClinicalCasePayload,
        tagsRaw: String,
        sectionMedia: Map<String, List<PickedMedia>>,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) = save {
        captureRepository.saveClinicalCasePearl(
            title = title,
            payload = payload,
            tags = parseTags(tagsRaw),
            sectionMedia = sectionMedia,
        )
    }.invoke(onSuccess, onError)

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
