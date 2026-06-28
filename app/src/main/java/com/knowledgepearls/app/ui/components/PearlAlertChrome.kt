package com.knowledgepearls.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun PearlAlertScrim(
    onDismiss: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PearlColors.scrim(darkTheme, 0.58f))
            .then(
                if (onDismiss != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    )
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        ) {
            content()
        }
    }
}

@Composable
fun PearlAlertCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        opaque = true,
        content = {
            Column(
                modifier = Modifier.padding(bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content,
            )
        },
    )
}

@Composable
fun PearlAlertIcon(
    icon: ImageVector,
    tint: Color,
) {
    Box(
        modifier = Modifier
            .padding(top = 28.dp, bottom = 18.dp)
            .size(72.dp)
            .background(tint.copy(alpha = 0.16f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(32.dp),
        )
    }
}

@Composable
fun PearlAlertTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = PearlColors.heroPrimary(isPearlDarkTheme()),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
    )
}

@Composable
fun PearlAlertMessage(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = PearlColors.heroSecondary(isPearlDarkTheme()),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    )
}

@Composable
fun PearlAlertPrimaryButton(
    text: String,
    theme: TabTheme,
    onClick: () -> Unit,
    primaryOverride: Color? = null,
    secondaryOverride: Color? = null,
) {
    val primary = primaryOverride ?: theme.primary
    val secondary = secondaryOverride ?: theme.secondary
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = ButtonDefaults.ContentPadding,
        shape = RoundedCornerShape(14.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(listOf(primary, secondary)),
                    RoundedCornerShape(14.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(text, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun PearlAlertSecondaryButton(
    text: String,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = PearlColors.heroSecondary(isPearlDarkTheme()),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun PearlToastCard(
    title: String,
    message: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = PearlLayout.cardCornerRadius,
        opaque = true,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(title, fontWeight = FontWeight.SemiBold, color = PearlColors.heroPrimary(isPearlDarkTheme()))
            Text(message, style = MaterialTheme.typography.bodySmall, color = accent)
        }
    }
}

@Composable
fun PearlMaterialAlertDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable (() -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
) {
    val darkTheme = isPearlDarkTheme()
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = PearlColors.popupSurface(darkTheme),
        titleContentColor = PearlColors.heroPrimary(darkTheme),
        textContentColor = PearlColors.heroSecondary(darkTheme),
        title = title,
        text = text,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
    )
}
