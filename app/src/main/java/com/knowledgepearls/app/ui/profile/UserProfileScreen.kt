package com.knowledgepearls.app.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knowledgepearls.app.data.model.UserProfile
import com.knowledgepearls.app.data.remote.SupabaseConfig
import com.knowledgepearls.app.ui.publicfeed.PublicFeedCard
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.TabTheme

@Composable
fun UserProfileScreen(
    userId: String,
    isOwnProfile: Boolean,
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val theme = TabTheme.PublicFeed
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deleteConfirmText by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        viewModel.load(userId)
    }

    Box(modifier = modifier.fillMaxSize()) {
        LiquidBackground(theme = theme)

        when {
            uiState.isLoading -> ProfileLoadingState()
            uiState.errorMessage != null -> ProfileErrorState(
                message = uiState.errorMessage!!,
                onRetry = { viewModel.load(userId) },
            )
            uiState.profile != null -> UserProfileContent(
                profile = uiState.profile!!,
                pearlCount = uiState.pearlCount,
                totalLikes = uiState.totalLikesReceived,
                pearls = uiState.pearls,
                isOwnProfile = isOwnProfile,
                onDismiss = onDismiss,
                onEditProfile = onEditProfile,
                onDeleteAccount = { showDeleteConfirm = true },
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirm = false
                deleteConfirmText = ""
            },
            title = { Text("Delete account?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "This removes your profile, public pearls, messages, and push tokens from Med Pearls. " +
                            "Pearls saved only on this device stay on the device unless you delete them separately.",
                    )
                    OutlinedTextField(
                        value = deleteConfirmText,
                        onValueChange = { deleteConfirmText = it.uppercase() },
                        label = { Text("Type DELETE") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        deleteConfirmText = ""
                        onDeleteAccount()
                        onDismiss()
                    },
                    enabled = deleteConfirmText == "DELETE",
                ) {
                    Text("Delete permanently", color = Color(0xFFFF453A))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    deleteConfirmText = ""
                }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun UserProfileContent(
    profile: UserProfile,
    pearlCount: Int,
    totalLikes: Int,
    pearls: List<com.knowledgepearls.app.data.model.PublicPearl>,
    isOwnProfile: Boolean,
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    val theme = TabTheme.PublicFeed
    val context = LocalContext.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val heroHeight = maxHeight * 0.52f

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight)
                    .clip(RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp)),
            ) {
                ProfileHeroPhoto(
                    url = profile.avatarUrl,
                    displayName = profile.displayName,
                    modifier = Modifier.fillMaxSize(),
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0f to Color.Transparent,
                                    0.35f to Color.Black.copy(alpha = 0.25f),
                                    0.72f to Color.Black.copy(alpha = 0.72f),
                                    1f to Color.Black.copy(alpha = 0.88f),
                                ),
                            ),
                        ),
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                ) {
                    ProfileTopBar(
                        isOwnProfile = isOwnProfile,
                        onDismiss = onDismiss,
                        onEditProfile = onEditProfile,
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = profile.displayName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                        )

                        ProfileMetaLine(
                            grade = profile.grade,
                            deanery = profile.deanery,
                            specialty = profile.specialty,
                            showEmail = profile.showEmail,
                            publicEmail = profile.publicEmail,
                        )

                        profile.bio?.takeIf { it.isNotBlank() }?.let { bio ->
                            Text(
                                text = bio,
                                color = Color.White.copy(alpha = 0.86f),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                            )
                        }

                        ProfileStatsBar(
                            pearlCount = pearlCount,
                            totalLikes = totalLikes,
                            memberSince = formatMemberSince(profile.createdAt),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .padding(horizontal = 20.dp)
                    .background(Color.White.copy(alpha = 0.10f)),
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 40.dp),
            ) {
                item {
                    Text(
                        text = "PUBLIC PEARLS",
                        color = Color.White.copy(alpha = 0.4f),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                    )
                }

                if (pearls.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = theme.primary.copy(alpha = 0.5f),
                                modifier = Modifier.height(32.dp),
                            )
                            Text(
                                text = "No pearls shared yet",
                                color = Color.White.copy(alpha = 0.55f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                } else {
                    items(pearls, key = { it.id }) { pearl ->
                        PublicFeedCard(
                            pearl = pearl,
                            theme = theme,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            onClick = {},
                        )
                    }
                }

                if (isOwnProfile) {
                    item {
                        OwnAccountSection(
                            onContactSupport = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:${SupabaseConfig.SUPPORT_EMAIL}")
                                }
                                context.startActivity(intent)
                            },
                            onDeleteAccount = onDeleteAccount,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTopBar(
    isOwnProfile: Boolean,
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isOwnProfile) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.9f))
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Edit Profile",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.12f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onEditProfile,
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.9f))
            }
        }
    }
}

@Composable
private fun OwnAccountSection(
    onContactSupport: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp),
    ) {
        Text(
            text = "ACCOUNT",
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.22f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
        ) {
            ProfileAccountRow(
                icon = Icons.Default.Email,
                title = "Contact support",
                subtitle = SupabaseConfig.SUPPORT_EMAIL,
                onClick = onContactSupport,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color.White.copy(alpha = 0.08f)),
            )
            ProfileAccountRow(
                icon = Icons.Default.Delete,
                title = "Delete account",
                subtitle = "Permanently remove your Med Pearls account",
                tint = Color(0xFFFF453A),
                onClick = onDeleteAccount,
            )
        }
    }
}

@Composable
private fun ProfileAccountRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color = Color.White,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint.copy(alpha = 0.9f))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = tint, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, color = Color.White.copy(alpha = 0.55f), style = MaterialTheme.typography.bodySmall)
        }
    }
}
