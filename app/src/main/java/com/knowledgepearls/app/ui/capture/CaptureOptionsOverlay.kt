package com.knowledgepearls.app.ui.capture

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.capture.CaptureSheet
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun CaptureOptionsOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSelect: (CaptureSheet) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    val darkTheme = isPearlDarkTheme()
    val theme = TabTheme.Feed

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PearlColors.scrim(darkTheme, 0.42f))
                .clickable(onClick = onDismiss),
        )

        GlassSurface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 120.dp)
                .fillMaxWidth(0.82f)
                .clickable(enabled = false, onClick = {}),
            cornerRadius = 22.dp,
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("New Pearl", fontWeight = FontWeight.Bold, color = PearlColors.heroPrimary(darkTheme))
                        Text(
                            "Choose how to capture",
                            style = MaterialTheme.typography.bodySmall,
                            color = PearlColors.heroSecondary(darkTheme),
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    captureOptions.forEach { option ->
                        CaptureOptionRow(option = option, onClick = {
                            onSelect(option.sheet)
                            onDismiss()
                        })
                    }
                }
            }
        }
    }
}

private data class CaptureOption(
    val sheet: CaptureSheet,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val primary: Color,
    val secondary: Color,
)

private val captureOptions = listOf(
    CaptureOption(CaptureSheet.ClinicalCase, "Clinical Case", "Structured patient case study", Icons.Default.LocalHospital, Color(0xFFF5A623), Color(0xFFC47D0E)),
    CaptureOption(CaptureSheet.WebLink, "Web Link", "Save a page with preview", Icons.Default.Link, Color(0xFF22D3EE), Color(0xFF3380F2)),
    CaptureOption(CaptureSheet.Files, "Files", "Import PDF or documents", Icons.Default.Description, Color(0xFFA88448), Color(0xFFD18548)),
    CaptureOption(CaptureSheet.Camera, "Camera", "Take a photo", Icons.Default.CameraAlt, Color(0xFF14B8A6), Color(0xFF2EB8D4)),
    CaptureOption(CaptureSheet.PhotoLibrary, "Photo Library", "Choose an existing image", Icons.Default.PhotoLibrary, Color(0xFF598FFA), Color(0xFF7359F2)),
    CaptureOption(CaptureSheet.QuickText, "Quick Pearl", "Jot a pearl with optional media", Icons.Default.AutoAwesome, Color(0xFF4ADE80), Color(0xFF33C77A)),
)

@Composable
private fun CaptureOptionRow(
    option: CaptureOption,
    onClick: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PearlColors.controlFill(darkTheme))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(option.primary, option.secondary))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(option.icon, contentDescription = null, tint = Color.White)
        }
        Column(Modifier.weight(1f)) {
            Text(option.title, fontWeight = FontWeight.SemiBold, color = PearlColors.heroPrimary(darkTheme))
            Text(option.subtitle, style = MaterialTheme.typography.bodySmall, color = PearlColors.heroSecondary(darkTheme))
        }
    }
}
