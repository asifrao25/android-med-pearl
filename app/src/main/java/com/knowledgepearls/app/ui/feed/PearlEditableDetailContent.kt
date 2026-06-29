package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.local.model.ClinicalCasePayload
import com.knowledgepearls.app.data.local.model.PearlWithMedia
import com.knowledgepearls.app.data.local.model.isClinicalCase
import com.knowledgepearls.app.ui.capture.CaptureNotesField
import com.knowledgepearls.app.ui.capture.CaptureTextField
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerRequest
import com.knowledgepearls.app.ui.theme.TabTheme

@Composable
fun PearlEditableDetailContent(
    pearl: PearlWithMedia,
    theme: TabTheme,
    title: String,
    onTitleChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    sourceReference: String,
    onSourceReferenceChange: (String) -> Unit,
    tags: String,
    onTagsChange: (String) -> Unit,
    clinicalPayload: ClinicalCasePayload,
    onClinicalPayloadChange: (ClinicalCasePayload) -> Unit,
    onOpenMedia: (PublicPearlMediaViewerRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CaptureTextField(
            label = "Title",
            value = title,
            onValueChange = onTitleChange,
            accent = theme.primary,
            placeholder = "Give your pearl a title",
        )

        if (!pearl.pearl.isClinicalCase()) {
            PearlDetailMediaSection(
                pearl = pearl,
                theme = theme,
                onOpenMedia = onOpenMedia,
            )
        }

        if (pearl.pearl.isClinicalCase()) {
            ClinicalCaseEditableFields(
                payload = clinicalPayload,
                onPayloadChange = onClinicalPayloadChange,
                theme = theme,
            )
        } else {
            CaptureNotesField(
                label = "Description",
                value = notes,
                onValueChange = onNotesChange,
                accent = theme.primary,
            )
            CaptureTextField(
                label = "Source / reference",
                value = sourceReference,
                onValueChange = onSourceReferenceChange,
                accent = theme.secondary,
            )
        }

        CaptureTextField(
            label = "Tags",
            value = tags,
            onValueChange = onTagsChange,
            accent = theme.primary,
            placeholder = "cardiology, fluids",
        )
    }
}

@Composable
private fun ClinicalCaseEditableFields(
    payload: ClinicalCasePayload,
    onPayloadChange: (ClinicalCasePayload) -> Unit,
    theme: TabTheme,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CaptureNotesField(
            label = "History",
            value = payload.history,
            onValueChange = { onPayloadChange(payload.copy(history = it)) },
            accent = theme.primary,
            minLines = 5,
        )
        CaptureNotesField(
            label = "Examination",
            value = payload.examination,
            onValueChange = { onPayloadChange(payload.copy(examination = it)) },
            accent = theme.primary,
            minLines = 3,
        )
        CaptureNotesField(
            label = "Investigation",
            value = payload.investigation,
            onValueChange = { onPayloadChange(payload.copy(investigation = it)) },
            accent = theme.primary,
            minLines = 3,
        )
        CaptureNotesField(
            label = "Diagnosis",
            value = payload.diagnosis,
            onValueChange = { onPayloadChange(payload.copy(diagnosis = it)) },
            accent = theme.primary,
            minLines = 3,
        )
        CaptureNotesField(
            label = "Discussion",
            value = payload.discussion,
            onValueChange = { onPayloadChange(payload.copy(discussion = it)) },
            accent = theme.primary,
            minLines = 4,
        )
        CaptureNotesField(
            label = "References",
            value = payload.references,
            onValueChange = { onPayloadChange(payload.copy(references = it)) },
            accent = theme.secondary,
            minLines = 2,
        )
    }
}
