package com.knowledgepearls.app.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.data.model.PublicPearl
import com.knowledgepearls.app.ui.publicfeed.PublicPearlDetailMediaSection
import com.knowledgepearls.app.ui.publicfeed.PublicPearlMediaViewerRequest
import com.knowledgepearls.app.ui.theme.TabTheme

@Composable
fun SavedPublicPearlDetailContent(
    publicPearl: PublicPearl,
    theme: TabTheme,
    onOpenMedia: (PublicPearlMediaViewerRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PublicPearlDetailMediaSection(
            pearl = publicPearl,
            theme = theme,
            onOpenMedia = onOpenMedia,
        )

        if (publicPearl.isClinicalCase) {
            publicPearl.casePayload?.history?.takeIf { it.isNotBlank() }?.let {
                PearlDetailSection(title = "History", body = it, theme = theme, linkifyBody = false)
            }
            publicPearl.casePayload?.examination?.takeIf { it.isNotBlank() }?.let {
                PearlDetailSection(title = "Examination", body = it, theme = theme, linkifyBody = false)
            }
            publicPearl.casePayload?.investigation?.takeIf { it.isNotBlank() }?.let {
                PearlDetailSection(title = "Investigation", body = it, theme = theme, linkifyBody = false)
            }
            publicPearl.casePayload?.diagnosis?.takeIf { it.isNotBlank() }?.let {
                PearlDetailSection(title = "Diagnosis", body = it, theme = theme, linkifyBody = false)
            }
            publicPearl.casePayload?.discussion?.takeIf { it.isNotBlank() }?.let {
                PearlDetailSection(title = "Discussion", body = it, theme = theme, linkifyBody = false)
            }
        } else if (publicPearl.notes.isNotBlank()) {
            PearlDetailSection(title = "Notes", body = publicPearl.notes, theme = theme, linkifyBody = false)
        }

        publicPearl.linkPreviewDescription?.takeIf { it.isNotBlank() }?.let {
            PearlDetailSection(title = "Preview", body = it, theme = theme, linkifyBody = false)
        }

        publicPearl.effectiveSourceReference.takeIf { it.isNotBlank() }?.let {
            PearlDetailSection(title = "Source", body = it, theme = theme)
        }

        if (publicPearl.tags.isNotEmpty()) {
            PearlDetailSection(
                title = "Tags",
                body = publicPearl.tags.joinToString(", "),
                theme = theme,
                linkifyBody = false,
            )
        }
    }
}
