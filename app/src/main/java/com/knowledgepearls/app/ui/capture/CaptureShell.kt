package com.knowledgepearls.app.ui.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun CaptureShell(
    kind: CaptureKind,
    saveTitle: String,
    isSaveDisabled: Boolean,
    isSaving: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
    content: @Composable () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    val theme = TabTheme.Feed

    Box(Modifier.fillMaxSize()) {
        LiquidBackground(theme = theme, intensity = 0.65f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = kind.navTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PearlColors.heroPrimary(darkTheme),
                    modifier = Modifier.weight(1f),
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = PearlLayout.screenHorizontalPadding)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                GlassSurface(cornerRadius = PearlLayout.cardCornerRadius) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = kind.heroTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = kind.primary,
                        )
                        Text(
                            text = kind.heroSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PearlColors.heroSecondary(darkTheme),
                        )
                    }
                }

                content()
            }

            Button(
                onClick = onSave,
                enabled = !isSaveDisabled && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        start = PearlLayout.screenHorizontalPadding,
                        end = PearlLayout.screenHorizontalPadding,
                        top = 12.dp,
                        bottom = PearlLayout.tabBarOverlayInset,
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = kind.primary,
                    disabledContainerColor = kind.primary.copy(alpha = 0.45f),
                    disabledContentColor = Color.White.copy(alpha = 0.85f),
                ),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(saveTitle, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CaptureTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    accent: Color,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    placeholder: String = "",
) {
    val darkTheme = isPearlDarkTheme()
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = accent, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
        GlassSurface(cornerRadius = 14.dp) {
            androidx.compose.material3.OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(placeholder) },
                singleLine = singleLine,
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = PearlColors.heroPrimary(darkTheme)),
            )
        }
    }
}

@Composable
fun CaptureNotesField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    accent: Color,
    minLines: Int = 4,
) {
    val darkTheme = isPearlDarkTheme()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = accent, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
        GlassSurface(cornerRadius = 14.dp) {
            androidx.compose.material3.OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Add details…") },
                minLines = minLines,
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = PearlColors.heroPrimary(darkTheme)),
            )
        }
    }
}

@Composable
fun GlowingAddButton(
    isMenuOpen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    listOf(Color(0xFF6C5CE7), Color(0xFF22D3EE)),
                ),
            )
            .border(2.5.dp, Color.White.copy(alpha = 0.35f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isMenuOpen) "✕" else "+",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}
