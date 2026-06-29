package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.ui.publicfeed.PublicPearlDetailMediaSection
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerRequest
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun ScraperPearlDetailContent(
    pearl: PublicPearl,
    theme: TabTheme,
    onOpenMedia: (PublicPearlMediaViewerRequest) -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenTweet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    val tweetText = pearl.scraperTweetText
    val aiSummary = pearl.scraperAISummary
    val learningPoint = pearl.scraperLearningPoint
    val externalLinks = pearl.scraperExternalLinks

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PearlDetailSectionHeaderBar(
            title = pearl.originalTweetAuthorLabel,
            theme = theme,
        )

        if (tweetText.isNotBlank()) {
            Text(
                text = tweetText,
                style = MaterialTheme.typography.bodyLarge,
                color = PearlColors.heroPrimary(darkTheme),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        PublicPearlDetailMediaSection(
            pearl = pearl,
            theme = theme,
            onOpenMedia = onOpenMedia,
            includeLinkTunnel = false,
        )

        if (aiSummary.isNotBlank()) {
            ScraperDetailPanel(title = "AI Summary", theme = theme) {
                Text(
                    text = aiSummary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PearlColors.heroPrimary(darkTheme),
                )
            }
        }

        if (learningPoint.isNotBlank()) {
            ScraperDetailPanel(title = "Learning Point", theme = theme) {
                Text(
                    text = learningPoint,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = PearlColors.heroPrimary(darkTheme),
                )
            }
        }

        if (externalLinks.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PearlDetailSectionHeaderBar(title = "Links", theme = theme)
                externalLinks.forEach { link ->
                    val openable = parseOpenableUrl(link.url) ?: link.url
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.primary.copy(alpha = 0.08f))
                            .border(1.dp, theme.primary.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
                            .clickable { onOpenUrl(openable) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = theme.primary)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Text(
                                text = link.displayTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PearlColors.heroPrimary(darkTheme),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = link.displaySite,
                                style = MaterialTheme.typography.labelSmall,
                                color = PearlColors.heroSecondary(darkTheme),
                            )
                        }
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = "Open link",
                            tint = PearlColors.heroSecondary(darkTheme),
                        )
                    }
                }
            }
        }

        PearlDetailTwitterAuthorSourceBox(
            authorName = pearl.originalTweetAuthorLabel,
            theme = theme,
            onOpenTweet = onOpenTweet,
        )
    }
}

@Composable
private fun ScraperDetailPanel(
    title: String,
    theme: TabTheme,
    content: @Composable () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(androidx.compose.ui.graphics.Color.White.copy(alpha = if (darkTheme) 0.04f else 0.35f))
            .border(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PearlDetailSectionHeaderBar(title = title, theme = theme)
        content()
    }
}
