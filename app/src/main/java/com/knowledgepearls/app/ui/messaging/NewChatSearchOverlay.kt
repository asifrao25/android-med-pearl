package com.knowledgepearls.app.ui.messaging

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.model.MessageProfileResult
import com.knowledgepearls.app.ui.components.AvatarView
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.components.inputBarBottomPadding
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import kotlinx.coroutines.delay

@Composable
fun NewChatSearchScreen(
    theme: TabTheme,
    onBack: () -> Unit,
    onLoadRecent: suspend () -> List<MessageProfileResult>,
    onSearch: suspend (String) -> List<MessageProfileResult>,
    onSelectUser: (MessageProfileResult) -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    val focusRequester = remember { FocusRequester() }

    var query by remember { mutableStateOf("") }
    var recentRecipients by remember { mutableStateOf<List<MessageProfileResult>>(emptyList()) }
    var results by remember { mutableStateOf<List<MessageProfileResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    val isQuerying = query.trim().length >= 2
    val listProfiles = if (isQuerying) results else recentRecipients

    LaunchedEffect(Unit) {
        recentRecipients = runCatching { onLoadRecent() }.getOrDefault(emptyList())
        focusRequester.requestFocus()
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
        results = runCatching { onSearch(trimmed) }.getOrDefault(emptyList())
        isSearching = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PearlLayout.screenHorizontalPadding, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PearlColors.heroPrimary(darkTheme),
                )
            }
            Text(
                text = "New Message",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PearlColors.heroPrimary(darkTheme),
            )
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PearlLayout.screenHorizontalPadding)
                .focusRequester(focusRequester),
            placeholder = { Text("Search by name") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 12.dp)
                .inputBarBottomPadding(fallbackWhenHidden = 12.dp),
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
                            color = PearlColors.heroPrimary(darkTheme),
                        )
                        Text(
                            text = if (isQuerying) {
                                "Try a different name or check that the user accepts messages."
                            } else {
                                "People you've messaged recently will appear here."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = PearlColors.heroSecondary(darkTheme),
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
                                    color = PearlColors.heroSecondary(darkTheme),
                                    modifier = Modifier.padding(bottom = 4.dp),
                                )
                            }
                        }
                        items(listProfiles, key = { it.id }) { profile ->
                            NewChatProfileRow(
                                profile = profile,
                                onClick = { onSelectUser(profile) },
                            )
                        }
                        if (!isQuerying && recentRecipients.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Or search by name above",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PearlColors.heroSecondary(darkTheme),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewChatProfileRow(
    profile: MessageProfileResult,
    onClick: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()

    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        cornerRadius = 14.dp,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarView(
                url = profile.avatarUrl,
                displayName = profile.name,
                size = 44.dp,
            )
            Text(
                text = profile.name,
                fontWeight = FontWeight.SemiBold,
                color = PearlColors.heroPrimary(darkTheme),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
