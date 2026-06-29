package com.knowledgepearls.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.theme.TabTheme

enum class PearlActionOutcome {
    SavedToMyFeed,
    SavedToFolder,
    AlreadyInMyFeed,
    PearlDeleted,
    RemovedFromFeed,
    FoldersUpdated,
}

@Composable
fun PearlAlreadyInFeedAlert(
    pearlTitle: String,
    theme: TabTheme,
    onDismiss: () -> Unit,
) {
    PearlAlertScrim(onDismiss = onDismiss) {
        PearlAlertCard {
            PearlAlertIcon(icon = Icons.Default.Info, tint = theme.primary)
            PearlAlertTitle("Already in My Feed")
            PearlAlertMessage(
                "\"${pearlTitle.ifBlank { "Untitled pearl" }}\" is already saved on this device. " +
                    "Open My Feed to view it — saving again won't create a duplicate.",
            )
            PearlAlertPrimaryButton(text = "Got it", theme = theme, onClick = onDismiss)
        }
    }
}

@Composable
fun PearlActionSuccessAlert(
    outcome: PearlActionOutcome,
    theme: TabTheme,
    folderName: String? = null,
    onDismiss: () -> Unit,
) {
    val (icon, title, message) = when (outcome) {
        PearlActionOutcome.SavedToMyFeed -> Triple(
            Icons.Default.CheckCircle,
            "Saved to My Feed",
            "This pearl is now in your personal feed.",
        )
        PearlActionOutcome.SavedToFolder -> Triple(
            Icons.Default.Folder,
            "Saved to Folder",
            "Added to \"${folderName.orEmpty()}\".",
        )
        PearlActionOutcome.PearlDeleted -> Triple(
            Icons.Default.Delete,
            "Pearl Deleted",
            "The pearl has been removed from this device.",
        )
        PearlActionOutcome.RemovedFromFeed -> Triple(
            Icons.Default.Delete,
            "Removed from Feed",
            "This pearl is hidden from your public feed.",
        )
        PearlActionOutcome.FoldersUpdated -> Triple(
            Icons.Default.Folder,
            "Folders Updated",
            "Your folder selections were saved.",
        )
        PearlActionOutcome.AlreadyInMyFeed -> Triple(
            Icons.Default.Info,
            "Already in My Feed",
            "This pearl is already saved on this device.",
        )
    }

    PearlAlertScrim(onDismiss = onDismiss) {
        PearlAlertCard {
            PearlAlertIcon(icon = icon, tint = theme.primary)
            PearlAlertTitle(title)
            PearlAlertMessage(message)
            PearlAlertPrimaryButton(text = "Done", theme = theme, onClick = onDismiss)
        }
    }
}

@Composable
fun NoInternetConnectionAlert(
    onContinueOffline: () -> Unit,
    onTryAgain: () -> Unit,
) {
    val alertRed = Color(0xFFFF3B30)
    PearlAlertScrim {
        PearlAlertCard {
            PearlAlertIcon(icon = Icons.Default.WifiOff, tint = alertRed)
            PearlAlertTitle("No Internet Connection")
            PearlAlertMessage(
                "You can continue in offline mode to browse pearls saved on this device. Public Feed needs an internet connection.",
            )
            PearlAlertPrimaryButton(
                text = "Continue Offline",
                theme = TabTheme.Feed,
                primaryOverride = alertRed,
                secondaryOverride = Color(0xFFFF6B57),
                onClick = onContinueOffline,
            )
            PearlAlertSecondaryButton(text = "Try Again", onClick = onTryAgain)
        }
    }
}

@Composable
fun BackendUnavailableAlert(
    onDismiss: () -> Unit,
    onTryAgain: () -> Unit,
) {
    val alertAmber = Color(0xFFFF9F38)
    PearlAlertScrim {
        PearlAlertCard {
            PearlAlertIcon(icon = Icons.Default.Storage, tint = alertAmber)
            PearlAlertTitle("Server Unavailable")
            PearlAlertMessage(
                "We can't reach Knowledge Pearls right now. Your saved pearls on this device still work, and we'll let you know when the connection is restored.",
            )
            PearlAlertPrimaryButton(
                text = "Try Again",
                theme = TabTheme.Settings,
                primaryOverride = alertAmber,
                secondaryOverride = Color(0xFFFFB86A),
                onClick = onTryAgain,
            )
            PearlAlertSecondaryButton(text = "Continue Offline", onClick = onDismiss)
        }
    }
}

@Composable
fun SharedPearlIntroAlert(
    theme: TabTheme,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
) {
    PearlAlertScrim(onDismiss = onCancel) {
        PearlAlertCard {
            PearlAlertIcon(icon = Icons.Default.Public, tint = theme.primary)
            PearlAlertTitle("Creating a Shared Pearl")
            PearlAlertMessage(
                "This pearl will be submitted to the Public Feed for community approval. It won't appear publicly until approved. Track pending submissions in Settings.",
            )
            PearlAlertPrimaryButton(text = "Continue", theme = theme, onClick = onContinue)
            PearlAlertSecondaryButton(text = "Cancel", onClick = onCancel)
        }
    }
}

@Composable
fun SharedPearlSubmitAlert(
    pearlTitle: String,
    theme: TabTheme,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
) {
    PearlAlertScrim(onDismiss = onCancel) {
        PearlAlertCard {
            PearlAlertIcon(icon = Icons.Default.Send, tint = theme.primary)
            PearlAlertTitle("Submit for Approval?")
            PearlAlertMessage(
                "\"${pearlTitle.ifBlank { "Untitled pearl" }}\" will be sent for moderation. Track it under Pending Submissions in Settings.",
            )
            PearlAlertPrimaryButton(text = "Submit", theme = theme, onClick = onSubmit)
            PearlAlertSecondaryButton(text = "Go Back", onClick = onCancel)
        }
    }
}

@Composable
fun SharedPearlSubmissionSuccessAlert(
    theme: TabTheme,
    onDismiss: () -> Unit,
) {
    PearlAlertScrim(onDismiss = onDismiss) {
        PearlAlertCard {
            PearlAlertIcon(icon = Icons.Default.CheckCircle, tint = theme.primary)
            PearlAlertTitle("Sent for Approval")
            PearlAlertMessage(
                "Your pearl has been sent for approval. You'll be notified once it's been reviewed.",
            )
            PearlAlertPrimaryButton(text = "Okay", theme = theme, onClick = onDismiss)
        }
    }
}

@Composable
fun ClinicalCaseDeidentificationAlert(
    theme: TabTheme,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
) {
    PearlAlertScrim(onDismiss = onCancel) {
        PearlAlertCard {
            PearlAlertIcon(icon = Icons.Default.VerifiedUser, tint = theme.primary)
            PearlAlertTitle("Remove Patient Identifiers")
            PearlAlertMessage(
                "Before sharing, confirm this case contains no patient names, dates of birth, hospital numbers, or other identifying information.",
            )
            PearlAlertPrimaryButton(text = "I Confirm — Continue", theme = theme, onClick = onContinue)
            PearlAlertSecondaryButton(text = "Go Back", onClick = onCancel)
        }
    }
}

@Composable
fun SourceReferenceRequiredAlert(
    theme: TabTheme,
    onDismiss: () -> Unit,
) {
    PearlAlertScrim(onDismiss = onDismiss) {
        PearlAlertCard {
            PearlAlertIcon(icon = Icons.Default.Description, tint = theme.primary)
            PearlAlertTitle("Source Required")
            PearlAlertMessage(
                "Add a source or reference before submitting a shared pearl.",
            )
            PearlAlertPrimaryButton(text = "Okay", theme = theme, onClick = onDismiss)
        }
    }
}

@Composable
fun CacheClearedSuccessAlert(
    bytesFreedLabel: String,
    effectSummary: String,
    theme: TabTheme,
    onDismiss: () -> Unit,
) {
    PearlAlertScrim(onDismiss = onDismiss) {
        PearlAlertCard {
            PearlAlertIcon(icon = Icons.Default.CheckCircle, tint = theme.primary)
            PearlAlertTitle("Cache Cleared")
            Text(
                text = bytesFreedLabel,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = theme.primary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            PearlAlertMessage("$effectSummary\n\nfreed on this device")
            PearlAlertPrimaryButton(text = "Done", theme = theme, onClick = onDismiss)
        }
    }
}

@Composable
fun PublicFeedOfflineState(
    isOfflineMode: Boolean,
    onTryAgain: () -> Unit,
) {
    val theme = TabTheme.PublicFeed
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            PearlAlertIcon(icon = Icons.Default.CloudOff, tint = theme.primary)
            PearlAlertTitle("No Internet Connection")
            PearlAlertMessage(
                "Public Feed needs an active connection to load community pearls. Your saved pearls are still available in My Feed while offline.",
            )
            if (isOfflineMode) {
                Text(
                    text = "Offline mode",
                    style = MaterialTheme.typography.labelMedium,
                    color = theme.primary,
                )
            }
            PearlAlertPrimaryButton(text = "Try Again", theme = theme, onClick = onTryAgain)
        }
    }
}
