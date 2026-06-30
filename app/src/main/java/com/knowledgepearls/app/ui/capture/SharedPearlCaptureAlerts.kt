package com.knowledgepearls.app.ui.capture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.knowledgepearls.app.data.local.model.effectiveSourceReference
import com.knowledgepearls.app.ui.components.ClinicalCaseDeidentificationAlert
import com.knowledgepearls.app.ui.components.PearlAlertCard
import com.knowledgepearls.app.ui.components.PearlAlertMessage
import com.knowledgepearls.app.ui.components.PearlAlertPrimaryButton
import com.knowledgepearls.app.ui.components.PearlAlertScrim
import com.knowledgepearls.app.ui.components.PearlAlertTitle
import com.knowledgepearls.app.ui.components.SharedPearlSubmissionSuccessAlert
import com.knowledgepearls.app.ui.components.SharedPearlSubmitAlert
import com.knowledgepearls.app.ui.components.SourceReferenceRequiredAlert
import com.knowledgepearls.app.ui.theme.TabTheme

class SharedPearlCaptureAlertState {
    var showSourceReferenceRequired by mutableStateOf(false)
    var showSubmitConfirmation by mutableStateOf(false)
    var showSubmissionSuccess by mutableStateOf(false)
    var showDeidentification by mutableStateOf(false)
    var submissionError by mutableStateOf<String?>(null)
}

@Composable
fun rememberSharedPearlCaptureAlertState(): SharedPearlCaptureAlertState =
    remember { SharedPearlCaptureAlertState() }

fun attemptSharedPearlSave(
    shareToPublicFeed: Boolean,
    sourceReference: String,
    sourceUrl: String? = null,
    pearlTitle: String,
    isClinicalCase: Boolean,
    state: SharedPearlCaptureAlertState,
    onSave: () -> Unit,
) {
    if (shareToPublicFeed && effectiveSourceReference(sourceReference, sourceUrl).isBlank()) {
        state.showSourceReferenceRequired = true
        return
    }
    if (shareToPublicFeed && isClinicalCase) {
        state.showDeidentification = true
        return
    }
    if (shareToPublicFeed) {
        state.showSubmitConfirmation = true
        return
    }
    onSave()
}

@Composable
fun SharedPearlCaptureOverlays(
    pearlTitle: String,
    state: SharedPearlCaptureAlertState,
    theme: TabTheme = TabTheme.PublicFeed,
    onConfirmSubmit: () -> Unit,
    onSubmissionSuccessDismiss: () -> Unit,
) {
    if (state.showSourceReferenceRequired) {
        SourceReferenceRequiredAlert(
            theme = theme,
            onDismiss = { state.showSourceReferenceRequired = false },
        )
    }

    if (state.showDeidentification) {
        ClinicalCaseDeidentificationAlert(
            theme = theme,
            onContinue = {
                state.showDeidentification = false
                state.showSubmitConfirmation = true
            },
            onCancel = { state.showDeidentification = false },
        )
    }

    if (state.showSubmitConfirmation) {
        SharedPearlSubmitAlert(
            pearlTitle = pearlTitle,
            theme = theme,
            onSubmit = {
                state.showSubmitConfirmation = false
                onConfirmSubmit()
            },
            onCancel = { state.showSubmitConfirmation = false },
        )
    }

    if (state.showSubmissionSuccess) {
        SharedPearlSubmissionSuccessAlert(
            theme = theme,
            onDismiss = {
                state.showSubmissionSuccess = false
                onSubmissionSuccessDismiss()
            },
        )
    }

    state.submissionError?.let { message ->
        PearlAlertScrim(onDismiss = { state.submissionError = null }) {
            PearlAlertCard {
                PearlAlertTitle("Couldn't Submit")
                PearlAlertMessage(message)
                PearlAlertPrimaryButton(
                    text = "OK",
                    theme = theme,
                    onClick = { state.submissionError = null },
                )
            }
        }
    }
}
