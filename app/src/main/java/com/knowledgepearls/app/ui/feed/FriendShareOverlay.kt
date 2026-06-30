package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.model.ShareProfileResult
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.components.PearlMaterialAlertDialog
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FriendShareOverlay(
    visible: Boolean,
    theme: TabTheme,
    onDismiss: () -> Unit,
    onLoadRecent: suspend () -> List<ShareProfileResult>,
    onSearch: suspend (String) -> List<ShareProfileResult>,
    onSend: (List<ShareProfileResult>) -> Unit,
) {
    if (!visible) return

    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var recentRecipients by remember { mutableStateOf<List<ShareProfileResult>>(emptyList()) }
    var results by remember { mutableStateOf<List<ShareProfileResult>>(emptyList()) }
    var selectedProfiles by remember { mutableStateOf<Map<String, ShareProfileResult>>(emptyMap()) }
    var isSearching by remember { mutableStateOf(false) }
    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSentToast by remember { mutableStateOf(false) }
    var sentCount by remember { mutableStateOf(0) }

    LaunchedEffect(visible) {
        if (visible) {
            query = ""
            selectedProfiles = emptyMap()
            recentRecipients = runCatching { onLoadRecent() }.getOrDefault(emptyList())
        }
    }

    LaunchedEffect(query) {
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            results = emptyList()
            isSearching = false
            return@LaunchedEffect
        }
        isSearching = true
        delay(300)
        if (query.trim() != trimmed) return@LaunchedEffect
        runCatching { onSearch(trimmed) }
            .onSuccess { results = it.filter { profile -> profile.allowPearlShares } }
            .onFailure { errorMessage = it.message ?: "Search failed" }
        isSearching = false
    }

    LaunchedEffect(showSentToast) {
        if (showSentToast) {
            delay(2200)
            showSentToast = false
            onDismiss()
        }
    }

    val isQuerying = query.trim().length >= 2
    val listProfiles = if (isQuerying) results else recentRecipients

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = theme, intensity = 0.9f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Share with Friends",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PearlColors.heroPrimary(isPearlDarkTheme()),
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PearlLayout.screenHorizontalPadding),
                placeholder = { Text("Search by name") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            ) {
                when {
                    isSearching -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = theme.primary,
                        )
                    }
                    listProfiles.isEmpty() -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = if (isQuerying) "No users found" else "Type a name to search",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PearlColors.heroPrimary(isPearlDarkTheme()),
                            )
                            Text(
                                text = if (isQuerying) {
                                    "Try a different name or check that the user allows pearl shares."
                                } else {
                                    "People you've shared with recently will appear here."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = PearlColors.heroSecondary(isPearlDarkTheme()),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = PearlLayout.screenHorizontalPadding,
                                vertical = 8.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (!isQuerying && recentRecipients.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Recent",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PearlColors.heroSecondary(isPearlDarkTheme()),
                                        modifier = Modifier.padding(bottom = 4.dp),
                                    )
                                }
                            }
                            items(listProfiles, key = { it.id }) { profile ->
                                FriendShareProfileRow(
                                    profile = profile,
                                    selected = profile.id in selectedProfiles,
                                    theme = theme,
                                    onToggle = {
                                        selectedProfiles = if (profile.id in selectedProfiles) {
                                            selectedProfiles - profile.id
                                        } else {
                                            selectedProfiles + (profile.id to profile)
                                        }
                                    },
                                )
                            }
                            if (!isQuerying && recentRecipients.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Or search by name above",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PearlColors.heroSecondary(isPearlDarkTheme()),
                                        modifier = Modifier.padding(top = 8.dp),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (selectedProfiles.isEmpty() || isSending) return@Button
                    isSending = true
                    scope.launch {
                        runCatching { onSend(selectedProfiles.values.toList()) }
                            .onSuccess {
                                sentCount = selectedProfiles.size
                                showSentToast = true
                            }
                            .onFailure { errorMessage = it.message ?: "Share failed" }
                        isSending = false
                    }
                },
                enabled = selectedProfiles.isNotEmpty() && !isSending,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PearlLayout.screenHorizontalPadding)
                    .padding(bottom = PearlLayout.detailScrollBottomPadding)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                shape = RoundedCornerShape(14.dp),
            ) {
                if (isSending) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.height(20.dp))
                } else {
                    Text("Share", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (showSentToast) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                GlassSurface(cornerRadius = 20.dp, opaque = true) {
                    Column(
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = theme.primary)
                        Text(
                            text = if (sentCount == 1) "Sent to 1 person" else "Sent to $sentCount people",
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }

    errorMessage?.let { message ->
        PearlMaterialAlertDialog(
            onDismissRequest = { errorMessage = null },
            confirmButton = {
                Button(onClick = { errorMessage = null }) { Text("OK") }
            },
            title = { Text("Couldn't Share") },
            text = { Text(message) },
        )
    }
}

@Composable
private fun FriendShareProfileRow(
    profile: ShareProfileResult,
    selected: Boolean,
    theme: TabTheme,
    onToggle: () -> Unit,
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        cornerRadius = 14.dp,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(theme.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = profile.name.firstOrNull()?.uppercase()?.toString() ?: "?",
                    fontWeight = FontWeight.Bold,
                    color = theme.primary,
                )
            }
            Text(
                text = profile.name.ifBlank { "Unknown" },
                fontWeight = FontWeight.SemiBold,
                color = PearlColors.heroPrimary(isPearlDarkTheme()),
                modifier = Modifier.weight(1f),
            )
            if (selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = theme.primary,
                )
            }
        }
    }
}
