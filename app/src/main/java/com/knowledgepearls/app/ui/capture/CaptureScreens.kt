package com.knowledgepearls.app.ui.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowledgepearls.app.data.capture.PickedMedia

@Composable
fun QuickTextCaptureScreen(
    viewModel: CaptureViewModel,
    isSignedIn: Boolean,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var sourceReference by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var shareToPublicFeed by remember { mutableStateOf(false) }
    val attachments = remember { mutableStateListOf<PickedMedia>() }
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val pickers = rememberMediaPickers { attachments.add(it) }
    val kind = CaptureKind.QuickText

    CaptureShell(
        kind = kind,
        saveTitle = if (isSaving) "Saving…" else "Save",
        isSaveDisabled = title.isBlank(),
        isSaving = isSaving,
        onBack = onBack,
        showShareToPublicToggle = isSignedIn,
        shareToPublicFeed = shareToPublicFeed,
        onShareToPublicFeedChange = { shareToPublicFeed = it },
        onSave = {
            viewModel.saveQuickPearl(
                title = title,
                notes = notes,
                sourceReference = sourceReference,
                tagsRaw = tags,
                media = attachments.toList(),
                shareToPublicFeed = shareToPublicFeed,
                onSuccess = { onSaved() },
                onError = { },
            )
        },
    ) {
        CaptureTextField("Title", title, { title = it }, kind.primary, placeholder = "Give your pearl a title")
        CaptureNotesField("Description", notes, { notes = it }, kind.primary)
        AttachmentSection(attachments, pickers, kind.primary)
        CaptureTextField("Source / reference", sourceReference, { sourceReference = it }, kind.secondary)
        CaptureTextField("Tags", tags, { tags = it }, kind.primary, placeholder = "cardiology, fluids")
    }
}

@Composable
fun WebLinkCaptureScreen(
    viewModel: CaptureViewModel,
    isSignedIn: Boolean,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    var url by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var sourceReference by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var shareToPublicFeed by remember { mutableStateOf(false) }
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val previewState by viewModel.linkPreview.collectAsStateWithLifecycle()
    val kind = CaptureKind.WebLink

    CaptureShell(
        kind = kind,
        saveTitle = if (isSaving) "Saving…" else "Save",
        isSaveDisabled = url.isBlank(),
        isSaving = isSaving,
        onBack = onBack,
        showShareToPublicToggle = isSignedIn,
        shareToPublicFeed = shareToPublicFeed,
        onShareToPublicFeedChange = { shareToPublicFeed = it },
        onSave = {
            viewModel.saveWebLinkPearl(
                title = title,
                notes = notes,
                sourceReference = sourceReference,
                tagsRaw = tags,
                url = url,
                shareToPublicFeed = shareToPublicFeed,
                onSuccess = { onSaved() },
                onError = { },
            )
        },
    ) {
        CaptureTextField("URL", url, {
            url = it
            viewModel.fetchLinkPreview(it)
        }, kind.primary, placeholder = "https://…")
        previewState.error?.let {
            Text(it, color = Color(0xFFFF6B6B), style = MaterialTheme.typography.bodySmall)
        }
        previewState.preview?.description?.takeIf { it.isNotBlank() }?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        CaptureTextField("Title", title, { title = it }, kind.primary, placeholder = previewState.preview?.title ?: "Title")
        CaptureNotesField("Description", notes, { notes = it }, kind.primary)
        CaptureTextField("Source / reference", sourceReference, { sourceReference = it }, kind.secondary, placeholder = "Usually the same URL")
        CaptureTextField("Tags", tags, { tags = it }, kind.primary, placeholder = "guidelines, link")
    }
}

@Composable
fun AddMediaCaptureScreen(
    viewModel: CaptureViewModel,
    isSignedIn: Boolean,
    initialRoute: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var sourceReference by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var shareToPublicFeed by remember { mutableStateOf(false) }
    val attachments = remember { mutableStateListOf<PickedMedia>() }
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val pickers = rememberMediaPickers { attachments.add(it) }
    val kind = CaptureKind.Media

    androidx.compose.runtime.LaunchedEffect(initialRoute) {
        when (initialRoute) {
            "camera" -> pickers.takePhoto()
            "gallery" -> pickers.pickGallery()
            "files" -> pickers.pickDocument()
        }
    }

    CaptureShell(
        kind = kind,
        saveTitle = if (isSaving) "Saving…" else "Save",
        isSaveDisabled = title.isBlank() || attachments.isEmpty(),
        isSaving = isSaving,
        onBack = onBack,
        showShareToPublicToggle = isSignedIn,
        shareToPublicFeed = shareToPublicFeed,
        onShareToPublicFeedChange = { shareToPublicFeed = it },
        onSave = {
            viewModel.saveMediaPearl(
                title = title,
                notes = notes,
                sourceReference = sourceReference,
                tagsRaw = tags,
                media = attachments.toList(),
                shareToPublicFeed = shareToPublicFeed,
                onSuccess = { onSaved() },
                onError = { },
            )
        },
    ) {
        AttachmentSection(attachments, pickers, kind.primary)
        CaptureTextField("Title", title, { title = it }, kind.primary, placeholder = "Give your pearl a title")
        CaptureNotesField("Description", notes, { notes = it }, kind.primary)
        CaptureTextField("Source / reference", sourceReference, { sourceReference = it }, kind.secondary)
        CaptureTextField("Tags", tags, { tags = it }, kind.primary)
    }
}

@Composable
fun ClinicalCaseCaptureScreen(
    viewModel: CaptureViewModel,
    isSignedIn: Boolean,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var history by remember { mutableStateOf("") }
    var examination by remember { mutableStateOf("") }
    var investigation by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var discussion by remember { mutableStateOf("") }
    var references by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var shareToPublicFeed by remember { mutableStateOf(false) }
    val examMedia = remember { mutableStateListOf<PickedMedia>() }
    val investigationMedia = remember { mutableStateListOf<PickedMedia>() }
    val discussionMedia = remember { mutableStateListOf<PickedMedia>() }
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val kind = CaptureKind.ClinicalCase

    CaptureShell(
        kind = kind,
        saveTitle = if (isSaving) "Saving…" else "Save",
        isSaveDisabled = title.isBlank() || history.isBlank(),
        isSaving = isSaving,
        onBack = onBack,
        showShareToPublicToggle = isSignedIn,
        shareToPublicFeed = shareToPublicFeed,
        onShareToPublicFeedChange = { shareToPublicFeed = it },
        onSave = {
            viewModel.saveClinicalCasePearl(
                title = title.take(120),
                payload = com.knowledgepearls.app.data.local.model.ClinicalCasePayload(
                    history = history,
                    examination = examination,
                    investigation = investigation,
                    diagnosis = diagnosis,
                    discussion = discussion,
                    references = references,
                ),
                tagsRaw = tags,
                sectionMedia = mapOf(
                    "examination" to examMedia.toList(),
                    "investigation" to investigationMedia.toList(),
                    "discussion" to discussionMedia.toList(),
                ),
                shareToPublicFeed = shareToPublicFeed,
                onSuccess = { onSaved() },
                onError = { },
            )
        },
    ) {
        CaptureTextField("Title of the case", title.take(120), { title = it.take(120) }, kind.primary, placeholder = "One sentence — max 120 characters")
        CaptureNotesField("History", history, { history = it }, kind.primary, minLines = 5)
        SectionWithMedia("Examination", examination, { examination = it }, examMedia, kind)
        SectionWithMedia("Investigation", investigation, { investigation = it }, investigationMedia, kind)
        CaptureNotesField("Diagnosis", diagnosis, { diagnosis = it }, kind.primary, minLines = 3)
        SectionWithMedia("Discussion", discussion, { discussion = it }, discussionMedia, kind)
        CaptureNotesField("References", references, { references = it }, kind.secondary, minLines = 2)
        CaptureTextField("Tags", tags, { tags = it }, kind.primary)
    }
}

@Composable
private fun SectionWithMedia(
    label: String,
    text: String,
    onTextChange: (String) -> Unit,
    media: MutableList<PickedMedia>,
    kind: CaptureKind,
) {
    val pickers = rememberMediaPickers { picked ->
        media.add(picked.copy(sectionTag = label.lowercase()))
    }
    CaptureNotesField(label, text, onTextChange, kind.primary, minLines = 3)
    AttachmentSection(media, pickers, kind.secondary)
}

@Composable
private fun AttachmentSection(
    attachments: MutableList<PickedMedia>,
    pickers: MediaPickers,
    accent: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Attachments", color = accent, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = pickers.pickGallery) { Text("Gallery") }
            TextButton(onClick = pickers.takePhoto) { Text("Camera") }
            TextButton(onClick = pickers.pickDocument) { Text("Files") }
        }
        attachments.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(item.filename, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { attachments.removeAt(index) }) { Text("Remove") }
            }
        }
    }
}
