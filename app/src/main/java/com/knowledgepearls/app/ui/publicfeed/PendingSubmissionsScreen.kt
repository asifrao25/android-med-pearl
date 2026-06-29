package com.knowledgepearls.app.ui.publicfeed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.components.TabScreenHeader
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun PendingSubmissionsScreen(
    submissions: List<PublicPearl>,
    isLoading: Boolean,
    withdrawingId: String?,
    errorMessage: String?,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onWithdraw: (PublicPearl) -> Unit,
    embeddedInSheet: Boolean = false,
) {
    val theme = if (embeddedInSheet) TabTheme.Settings else TabTheme.PublicFeed
    val darkTheme = isPearlDarkTheme()

    val screenContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PearlColors.heroPrimary(darkTheme),
                )
            }
        }

        TabScreenHeader(
            title = "Pending submissions",
            subtitle = "Awaiting moderation",
            theme = theme,
            showsSettingsButton = false,
        )

        when {
            isLoading && submissions.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = theme.primary)
                }
            }
            submissions.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(PearlLayout.screenHorizontalPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "No pending pearls.",
                            color = PearlColors.heroSecondary(darkTheme),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        OutlinedButton(onClick = onRefresh) {
                            Text("Refresh")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = PearlLayout.screenHorizontalPadding,
                        vertical = 12.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(submissions, key = { it.id }) { pearl ->
                        PendingSubmissionRow(
                            pearl = pearl,
                            theme = theme,
                            isWithdrawing = withdrawingId == pearl.id,
                            onWithdraw = { onWithdraw(pearl) },
                        )
                    }
                }
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(PearlLayout.screenHorizontalPadding),
            )
        }

        if (embeddedInSheet) {
            Spacer(Modifier.height(PearlLayout.tabBarOverlayInset))
        }
    }

    if (embeddedInSheet) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            screenContent()
        }
    } else {
        Box(Modifier.fillMaxSize()) {
            LiquidBackground(theme = theme, intensity = 0.55f)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
            ) {
                screenContent()
            }
        }
    }
}

@Composable
private fun PendingSubmissionRow(
    pearl: PublicPearl,
    theme: TabTheme,
    isWithdrawing: Boolean,
    onWithdraw: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()

    GlassSurface(cornerRadius = PearlLayout.cardCornerRadius) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = pearl.titleDisplay,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PearlColors.heroPrimary(darkTheme),
            )
            if (pearl.notes.isNotBlank()) {
                Text(
                    text = pearl.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PearlColors.heroSecondary(darkTheme),
                    maxLines = 3,
                )
            }
            Text(
                text = "Status: pending review",
                style = MaterialTheme.typography.labelSmall,
                color = theme.primary,
            )
            OutlinedButton(
                onClick = onWithdraw,
                enabled = !isWithdrawing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isWithdrawing) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Text("Withdraw")
                }
            }
        }
    }
}
