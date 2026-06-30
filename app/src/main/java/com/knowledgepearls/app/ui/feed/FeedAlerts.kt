package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.model.ContentTypeFilter
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun PearlDeleteConfirmationDialog(
    pearlTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    headline: String = "Delete pearl?",
    confirmLabel: String = "Delete",
    message: String? = null,
) {
    val darkTheme = isPearlDarkTheme()
    val body = message ?: "\"${pearlTitle.ifBlank { "Untitled pearl" }}\" will be removed from this device."
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PearlColors.scrim(darkTheme, 0.55f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        GlassSurface(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .clickable(enabled = false, onClick = {}),
            cornerRadius = PearlLayout.cardCornerRadius,
            opaque = true,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PearlColors.heroPrimary(darkTheme),
                )
                Text(
                    text = body,
                    color = PearlColors.heroSecondary(darkTheme),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A)),
                    ) {
                        Text(confirmLabel)
                    }
                }
            }
        }
    }
}

@Composable
fun FeedEmptyFilterAlert(
    filter: ContentTypeFilter,
    theme: TabTheme,
    onShowAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PearlColors.scrim(darkTheme, 0.45f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center,
    ) {
        GlassSurface(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .clickable(enabled = false, onClick = {}),
            cornerRadius = PearlLayout.cardCornerRadius,
            opaque = true,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "No ${filter.label.lowercase()} pearls yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PearlColors.heroPrimary(darkTheme),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Try another filter or capture a new pearl.",
                    color = PearlColors.heroSecondary(darkTheme),
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onShowAll,
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                ) {
                    Text("Show all")
                }
            }
        }
    }
}

@Composable
fun PearlActionSuccessBanner(
    message: String,
    theme: TabTheme,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PearlLayout.screenHorizontalPadding, vertical = 12.dp)
            .background(theme.primary.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .clickable(onClick = onDismiss)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(text = message, color = theme.primary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun PearlInlineErrorBanner(
    message: String,
    theme: TabTheme,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PearlLayout.screenHorizontalPadding, vertical = 8.dp)
            .background(Color(0xFFFF453A).copy(alpha = 0.14f), RoundedCornerShape(14.dp))
            .padding(start = 14.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            color = PearlColors.heroPrimary(darkTheme),
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
        )
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Dismiss error",
                tint = PearlColors.heroSecondary(darkTheme),
            )
        }
    }
}

@Composable
fun PearlDetailLoadingState(
    theme: TabTheme,
    message: String = "Loading pearl…",
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CircularProgressIndicator(color = theme.primary, modifier = Modifier.size(36.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = PearlColors.heroSecondary(darkTheme),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun PearlDetailNotFoundState(
    theme: TabTheme,
    message: String = "This pearl could not be found.",
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = PearlColors.heroSecondary(darkTheme),
            textAlign = TextAlign.Center,
        )
        TextButton(onClick = onBack) {
            Text("Go back", color = theme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun MyFeedFirstRunEmptyState(
    theme: TabTheme,
    onCreatePearl: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = theme.primary,
            modifier = Modifier.height(32.dp),
        )
        Text(
            text = "Your feed is empty",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PearlColors.heroPrimary(darkTheme),
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Capture your first pearl to start building your personal knowledge library.",
            style = MaterialTheme.typography.bodyMedium,
            color = PearlColors.heroSecondary(darkTheme),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = onCreatePearl,
            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
        ) {
            Text("Create your first pearl", fontWeight = FontWeight.Bold)
        }
    }
}
