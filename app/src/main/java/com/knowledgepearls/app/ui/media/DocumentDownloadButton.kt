package com.knowledgepearls.app.ui.media

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme
import kotlinx.coroutines.launch

@Composable
fun DocumentDownloadButton(
    url: String,
    filename: String,
    theme: TabTheme,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isDownloading by remember(url, filename) { mutableStateOf(false) }
    val effectiveName = effectiveMediaFilename(filename, url)
    val darkTheme = isPearlDarkTheme()

    if (compact) {
        Button(
            onClick = {
                if (isDownloading) return@Button
                scope.launch {
                    isDownloading = true
                    DocumentDownloader.download(context, context.cacheDir, url, effectiveName)
                    isDownloading = false
                }
            },
            enabled = !isDownloading,
            modifier = modifier.height(40.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = theme.primary.copy(alpha = 0.22f),
                contentColor = theme.primary,
            ),
            contentPadding = ButtonDefaults.ContentPadding,
        ) {
            if (isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = theme.primary,
                )
            } else {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Text(
                text = if (isDownloading) "Saving…" else "Download",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
        return
    }

    Button(
        onClick = {
            if (isDownloading) return@Button
            scope.launch {
                isDownloading = true
                DocumentDownloader.download(context, context.cacheDir, url, effectiveName)
                isDownloading = false
            }
        },
        enabled = !isDownloading,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = theme.primary.copy(alpha = 0.18f),
            contentColor = theme.primary,
        ),
    ) {
        if (isDownloading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = theme.primary,
            )
        } else {
            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Text(
            text = if (isDownloading) "Saving…" else "Download",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun DocumentAttachmentActions(
    url: String,
    filename: String,
    theme: TabTheme,
    modifier: Modifier = Modifier,
    showFilename: Boolean = true,
) {
    val effectiveName = effectiveMediaFilename(filename, url)
    val darkTheme = isPearlDarkTheme()

    androidx.compose.foundation.layout.Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
    ) {
        if (showFilename && effectiveName.isNotBlank()) {
            Text(
                text = effectiveName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = PearlColors.heroPrimary(darkTheme),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        DocumentDownloadButton(
            url = url,
            filename = effectiveName,
            theme = theme,
        )
    }
}
