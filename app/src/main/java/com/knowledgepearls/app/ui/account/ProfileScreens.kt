package com.knowledgepearls.app.ui.account

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.knowledgepearls.app.AppBrand
import com.knowledgepearls.app.data.model.ProfileConstants
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import java.io.ByteArrayOutputStream

private val Teal = Color(0xFF14B8A6)
private val Cyan = Color(0xFF22D3EE)

@Composable
fun ProfileSetupScreen(
    uiState: AccountUiState,
    onCreateProfile: (
        name: String,
        bio: String,
        deanery: String,
        specialty: String,
        grade: String,
        allowMessages: Boolean,
        showEmail: Boolean,
        publicEmail: String,
        allowPearlShares: Boolean,
        notifyPearlSharesEmail: Boolean,
        avatarUrl: String?,
    ) -> Unit,
    onUploadAvatar: (ByteArray, (String) -> Unit) -> Unit,
    onSignOut: () -> Unit,
) {
    ProfileFormScreen(
        title = "Complete Your Profile",
        subtitle = "Only your name is required.\nThe rest helps personalise your feed.",
        uiState = uiState,
        initialName = "",
        initialBio = "",
        initialDeanery = "",
        initialSpecialty = "",
        initialGrade = "",
        initialAllowMessages = true,
        initialAllowPearlShares = true,
        initialNotifyPearlSharesEmail = true,
        initialShowEmail = false,
        initialPublicEmail = uiState.userEmail.orEmpty(),
        initialAvatarUrl = null,
        submitLabel = "Continue to ${AppBrand.name}",
        onSubmit = onCreateProfile,
        onUploadAvatar = onUploadAvatar,
        onSignOut = onSignOut,
    )
}

@Composable
fun EditProfileScreen(
    uiState: AccountUiState,
    onUpdateProfile: (
        name: String,
        bio: String,
        deanery: String,
        specialty: String,
        grade: String,
        allowMessages: Boolean,
        showEmail: Boolean,
        publicEmail: String,
        allowPearlShares: Boolean,
        notifyPearlSharesEmail: Boolean,
        avatarUrl: String?,
    ) -> Unit,
    onUploadAvatar: (ByteArray, (String) -> Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    val profile = uiState.userProfile
    ProfileFormScreen(
        title = "Edit Profile",
        subtitle = "Update how others see you in Med Pearls.",
        uiState = uiState,
        initialName = profile?.name.orEmpty(),
        initialBio = profile?.bio.orEmpty(),
        initialDeanery = profile?.deanery.orEmpty(),
        initialSpecialty = profile?.specialty.orEmpty(),
        initialGrade = profile?.grade.orEmpty(),
        initialAllowMessages = profile?.allowMessages ?: true,
        initialAllowPearlShares = profile?.allowPearlShares ?: true,
        initialNotifyPearlSharesEmail = profile?.notifyPearlSharesEmail ?: true,
        initialShowEmail = profile?.showEmail ?: false,
        initialPublicEmail = profile?.publicEmail.orEmpty(),
        initialAvatarUrl = profile?.avatarUrl,
        submitLabel = "Save changes",
        onSubmit = onUpdateProfile,
        onUploadAvatar = onUploadAvatar,
        onSignOut = onDismiss,
        signOutLabel = "Close",
    )
}

@Composable
private fun ProfileFormScreen(
    title: String,
    subtitle: String,
    uiState: AccountUiState,
    initialName: String,
    initialBio: String,
    initialDeanery: String,
    initialSpecialty: String,
    initialGrade: String,
    initialAllowMessages: Boolean,
    initialAllowPearlShares: Boolean,
    initialNotifyPearlSharesEmail: Boolean,
    initialShowEmail: Boolean,
    initialPublicEmail: String,
    initialAvatarUrl: String?,
    submitLabel: String,
    onSubmit: (
        name: String,
        bio: String,
        deanery: String,
        specialty: String,
        grade: String,
        allowMessages: Boolean,
        showEmail: Boolean,
        publicEmail: String,
        allowPearlShares: Boolean,
        notifyPearlSharesEmail: Boolean,
        avatarUrl: String?,
    ) -> Unit,
    onUploadAvatar: (ByteArray, (String) -> Unit) -> Unit,
    onSignOut: () -> Unit,
    signOutLabel: String = "Sign out",
) {
    val darkTheme = isPearlDarkTheme()
    var name by remember(initialName) { mutableStateOf(initialName) }
    var bio by remember(initialBio) { mutableStateOf(initialBio) }
    var deanery by remember(initialDeanery) { mutableStateOf(initialDeanery) }
    var specialty by remember(initialSpecialty) { mutableStateOf(initialSpecialty) }
    var grade by remember(initialGrade) { mutableStateOf(initialGrade) }
    var allowMessages by remember(initialAllowMessages) { mutableStateOf(initialAllowMessages) }
    var allowPearlShares by remember(initialAllowPearlShares) { mutableStateOf(initialAllowPearlShares) }
    var notifyPearlSharesEmail by remember(initialNotifyPearlSharesEmail) { mutableStateOf(initialNotifyPearlSharesEmail) }
    var showEmail by remember(initialShowEmail) { mutableStateOf(initialShowEmail) }
    var publicEmail by remember(initialPublicEmail) { mutableStateOf(initialPublicEmail) }
    var avatarUrl by remember(initialAvatarUrl) { mutableStateOf(initialAvatarUrl) }

    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream) ?: return@runCatching
                val scaled = Bitmap.createScaledBitmap(bitmap, 512, 512, true)
                val bytes = ByteArrayOutputStream().use { output ->
                    scaled.compress(Bitmap.CompressFormat.JPEG, 82, output)
                    output.toByteArray()
                }
                onUploadAvatar(bytes) { uploaded -> avatarUrl = uploaded }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = TabTheme.PublicFeed)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(PearlColors.controlFill(darkTheme))
                        .clickable { picker.launch("image/*") },
                    contentAlignment = Alignment.Center,
                ) {
                    if (avatarUrl.isNullOrBlank()) {
                        Text(
                            text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            style = MaterialTheme.typography.headlineMedium,
                            color = PearlColors.heroPrimary(darkTheme),
                        )
                    } else {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = PearlColors.heroPrimary(darkTheme))
                Text(subtitle, color = PearlColors.heroSecondary(darkTheme), textAlign = TextAlign.Center)
            }

            AccountSectionLabel("Full name", required = true)
            AccountGlassTextField(value = name, onValueChange = { name = it }, label = "Dr. Jane Smith")
            AccountSectionLabel("Bio")
            AccountGlassTextField(value = bio, onValueChange = { if (it.length <= 280) bio = it }, label = "Bio", singleLine = false)
            AccountPickerField("NHS Deanery", deanery, ProfileConstants.deaneries, "Select deanery") { deanery = it }
            AccountPickerField("Specialty", specialty, ProfileConstants.specialties, "Select specialty") { specialty = it }
            AccountPickerField("Grade", grade, ProfileConstants.grades, "Select grade") { grade = it }

            Text("PRIVACY", color = PearlColors.heroSecondary(darkTheme), style = MaterialTheme.typography.labelSmall)
            AccountToggleRow("Allow messages", "Others can send you direct messages", allowMessages) { allowMessages = it }
            AccountToggleRow("Allow pearl shares", "Others can send you private pearls", allowPearlShares) { allowPearlShares = it }
            AccountToggleRow("Email on pearl shares", "Get notified when someone shares a pearl", notifyPearlSharesEmail) { notifyPearlSharesEmail = it }
            AccountToggleRow("Show email on profile", "Visible to users who view your profile", showEmail) { showEmail = it }
            if (showEmail) {
                AccountGlassTextField(
                    value = publicEmail,
                    onValueChange = { publicEmail = it },
                    label = "Public contact email",
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                )
            }

            uiState.errorMessage?.let {
                Text(it, color = Color(0xFFFF6B6B), style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    onSubmit(
                        name.trim(),
                        bio.trim(),
                        deanery,
                        specialty,
                        grade,
                        allowMessages,
                        showEmail,
                        publicEmail.trim(),
                        allowPearlShares,
                        notifyPearlSharesEmail,
                        avatarUrl,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = name.isNotBlank() && !uiState.isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                } else {
                    Text(submitLabel, fontWeight = FontWeight.Bold)
                }
            }

            TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text(signOutLabel, color = PearlColors.heroSecondary(darkTheme))
            }
        }
    }
}
