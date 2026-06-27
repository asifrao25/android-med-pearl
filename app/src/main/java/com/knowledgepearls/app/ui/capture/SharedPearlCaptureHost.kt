package com.knowledgepearls.app.ui.capture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.knowledgepearls.app.ui.theme.TabTheme

@Composable
fun SharedPearlCaptureHost(
    pearlTitle: String,
    sourceReference: String,
    shareToPublicFeed: Boolean,
    isClinicalCase: Boolean,
    onPerformSave: (onSuccess: (String) -> Unit, onError: (String) -> Unit) -> Unit,
    onSaved: () -> Unit,
    content: @Composable (requestSave: () -> Unit) -> Unit,
) {
    val alertState = rememberSharedPearlCaptureAlertState()

    Box(Modifier.fillMaxSize()) {
        content {
            attemptSharedPearlSave(
                shareToPublicFeed = shareToPublicFeed,
                sourceReference = sourceReference,
                pearlTitle = pearlTitle,
                isClinicalCase = isClinicalCase,
                state = alertState,
                onSave = {
                    onPerformSave(
                        { _ ->
                            if (shareToPublicFeed) {
                                alertState.showSubmissionSuccess = true
                            } else {
                                onSaved()
                            }
                        },
                        { message -> alertState.submissionError = message },
                    )
                },
            )
        }

        SharedPearlCaptureOverlays(
            pearlTitle = pearlTitle,
            state = alertState,
            theme = TabTheme.PublicFeed,
            onConfirmSubmit = {
                onPerformSave(
                    { _ -> alertState.showSubmissionSuccess = true },
                    { message -> alertState.submissionError = message },
                )
            },
            onSubmissionSuccessDismiss = onSaved,
        )
    }
}
