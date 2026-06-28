package com.knowledgepearls.app.ui.feed

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

private const val MOBILE_SAFARI_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"

@Composable
fun LinkPearlPreviewSection(
    url: String,
    theme: TabTheme,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = PearlDetailMetrics.linkTunnelHeight,
    onOpenExternal: (String) -> Unit = {},
    onOpenBrowser: () -> Unit = {},
) {
    val cornerRadius = 14.dp
    val shape = RoundedCornerShape(cornerRadius)
    val darkTheme = isPearlDarkTheme()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.12f), shape),
    ) {
        LinkWebTunnel(
            url = url,
            height = height,
            topCornerRadius = cornerRadius,
            bottomCornerRadius = 0.dp,
            onOpenExternal = onOpenExternal,
        )

        LinkPreviewBrowserBar(
            url = url,
            theme = theme,
            onOpenBrowser = onOpenBrowser,
        )
    }
}

@Composable
private fun LinkWebTunnel(
    url: String,
    height: androidx.compose.ui.unit.Dp,
    topCornerRadius: androidx.compose.ui.unit.Dp,
    bottomCornerRadius: androidx.compose.ui.unit.Dp,
    onOpenExternal: (String) -> Unit,
) {
    var isLoading by remember(url) { mutableStateOf(true) }
    var loadError by remember(url) { mutableStateOf<String?>(null) }
    val darkTheme = isPearlDarkTheme()
    val topShape = RoundedCornerShape(
        topStart = topCornerRadius,
        topEnd = topCornerRadius,
        bottomStart = bottomCornerRadius,
        bottomEnd = bottomCornerRadius,
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(topShape)
            .background(PearlColors.controlFill(darkTheme)),
    ) {
        LinkWebTunnelWebView(
            url = url,
            onLoadingChanged = { isLoading = it },
            onLoadError = { loadError = it },
            onOpenExternal = onOpenExternal,
            modifier = Modifier.fillMaxSize(),
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PearlColors.controlFill(darkTheme)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = TabTheme.PublicFeed.primary)
            }
        }

        if (loadError != null && !isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PearlColors.controlFill(darkTheme)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "Couldn't load this page",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = PearlColors.heroPrimary(darkTheme),
                    )
                    Text(
                        text = "Open in browser to view it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PearlColors.heroSecondary(darkTheme),
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun LinkWebTunnelWebView(
    url: String,
    onLoadingChanged: (Boolean) -> Unit,
    onLoadError: (String?) -> Unit,
    onOpenExternal: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val normalizedUrl = remember(url) { parseOpenableUrl(url) ?: url }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true
                settings.userAgentString = MOBILE_SAFARI_USER_AGENT
                setBackgroundColor(android.graphics.Color.TRANSPARENT)

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        onLoadingChanged(true)
                        onLoadError(null)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        onLoadingChanged(false)
                        onLoadError(null)
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?,
                    ) {
                        onLoadingChanged(false)
                        onLoadError(description ?: "Couldn't load this page")
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?,
                    ): Boolean {
                        val destination = request?.url?.toString() ?: return false
                        val scheme = request.url.scheme?.lowercase().orEmpty()
                        if (scheme != "http" && scheme != "https") {
                            if (request.isForMainFrame) {
                                onOpenExternal(destination)
                            }
                            return true
                        }
                        if (!request.isForMainFrame || request.hasGesture()) {
                            onOpenExternal(destination)
                            return true
                        }
                        return false
                    }
                }

                loadUrl(normalizedUrl)
            }
        },
        update = { webView ->
            val current = webView.url
            if (current == null || !current.startsWith(normalizedUrl.substringBefore('?'))) {
                webView.loadUrl(normalizedUrl)
            }
        },
    )
}

@Composable
private fun LinkPreviewBrowserBar(
    url: String,
    theme: TabTheme,
    onOpenBrowser: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    val host = remember(url) {
        runCatching { android.net.Uri.parse(url).host }.getOrNull().orEmpty()
    }
    val subtitle = host.ifBlank { "View full page in browser" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (darkTheme) 0.07f else 0.35f),
                        theme.primary.copy(alpha = 0.14f),
                    ),
                ),
            )
            .clickable(onClick = onOpenBrowser)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            theme.primary.copy(alpha = 0.38f),
                            theme.primary.copy(alpha = 0.14f),
                        ),
                    ),
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "🌐",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Open in browser",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = PearlColors.heroPrimary(darkTheme),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = PearlColors.heroSecondary(darkTheme),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Icon(
            Icons.Default.OpenInNew,
            contentDescription = null,
            tint = theme.primary.copy(alpha = 0.85f),
        )
    }
}
