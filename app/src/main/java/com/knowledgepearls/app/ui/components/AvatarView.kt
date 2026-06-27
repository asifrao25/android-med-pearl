package com.knowledgepearls.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage

private val avatarTeal = Color(0xFF14B8A6)
private val avatarCyan = Color(0xFF22D3EE)

@Composable
fun AvatarView(
    url: String?,
    displayName: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val initials = avatarInitials(displayName)
    val shape = CircleShape

    Box(
        modifier = modifier
            .size(size)
            .clip(shape),
        contentAlignment = Alignment.Center,
    ) {
        if (!url.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = displayName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    AvatarInitialsCircle(initials = initials, size = size, showSpinner = true)
                },
                error = {
                    AvatarInitialsCircle(initials = initials, size = size)
                },
            )
        } else {
            AvatarInitialsCircle(initials = initials, size = size)
        }
    }
}

@Composable
private fun AvatarInitialsCircle(
    initials: String,
    size: Dp,
    showSpinner: Boolean = false,
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                brush = Brush.linearGradient(listOf(avatarTeal, avatarCyan)),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = MaterialTheme.typography.labelLarge.fontSize * (size.value / 42f),
            ),
        )
        if (showSpinner) {
            CircularProgressIndicator(
                modifier = Modifier.size(size * 0.35f),
                color = Color.White.copy(alpha = 0.85f),
                strokeWidth = 2.dp,
            )
        }
    }
}

private fun avatarInitials(displayName: String): String {
    val words = displayName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.take(2)
    val letters = words.mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }.joinToString("")
    return letters.ifBlank { "?" }
}
