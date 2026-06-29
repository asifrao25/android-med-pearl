package com.knowledgepearls.app.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.knowledgepearls.app.R
import com.knowledgepearls.app.data.settings.AboutCreatorContent
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.components.PearlMaterialAlertDialog
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@Composable
fun AboutCreatorScreen(
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    embeddedInSheet: Boolean = false,
    viewModel: AboutCreatorViewModel = hiltViewModel(),
) {
    val theme = TabTheme.Settings
    val darkTheme = isPearlDarkTheme()
    val creatorUserId by viewModel.creatorUserId.collectAsState()
    var showCreationStoryAlert by remember { mutableStateOf(false) }

    if (showCreationStoryAlert) {
        PearlMaterialAlertDialog(
            onDismissRequest = { showCreationStoryAlert = false },
            title = { Text("Coming soon") },
            text = { Text("The app creation story page is on the way.") },
            confirmButton = {
                TextButton(onClick = { showCreationStoryAlert = false }) {
                    Text("OK")
                }
            },
        )
    }

    val shellModifier = Modifier
        .fillMaxSize()
        .then(if (embeddedInSheet) Modifier.navigationBarsPadding() else Modifier.statusBarsPadding().navigationBarsPadding())

    val bodyColumn: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit = {
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

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PearlLayout.screenHorizontalPadding)
                .padding(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            CreatorPortraitHero(theme = theme, darkTheme = darkTheme)

            SettingsMenuCard(theme = theme) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "About me",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PearlColors.heroPrimary(darkTheme),
                    )

                    Image(
                        painter = painterResource(R.drawable.creator_portrait),
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(14.dp),
                            ),
                    )

                    Text(
                        text = AboutCreatorContent.BIO,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 24.sp,
                            color = PearlColors.heroPrimary(darkTheme).copy(alpha = 0.92f),
                        ),
                    )

                    CreatorLinkButton(
                        label = "Read app creation story",
                        icon = Icons.AutoMirrored.Filled.OpenInNew,
                        theme = theme,
                        onClick = { showCreationStoryAlert = true },
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    CreatorLinkButton(
                        label = "Profile",
                        icon = Icons.Default.Person,
                        theme = theme,
                        onClick = { onOpenProfile(creatorUserId) },
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }

        if (embeddedInSheet) {
            Spacer(Modifier.height(PearlLayout.tabBarOverlayInset))
        }
    }

    if (embeddedInSheet) {
        Column(modifier = shellModifier, content = bodyColumn)
    } else {
        Box(Modifier.fillMaxSize()) {
            LiquidBackground(theme = theme, intensity = 0.55f)
            Column(modifier = shellModifier, content = bodyColumn)
        }
    }
}

@Composable
fun AboutCreatorSettingsRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    val borderColor = PearlColors.cardBorder(darkTheme)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.creator_portrait),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(14.dp)),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "About Creator",
                color = PearlColors.heroPrimary(darkTheme),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = AboutCreatorContent.SUBTITLE,
                color = PearlColors.heroSecondary(darkTheme),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 2,
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = PearlColors.heroSecondary(darkTheme),
        )
    }
}

@Composable
private fun CreatorPortraitHero(
    theme: TabTheme,
    darkTheme: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    spotColor = theme.primary.copy(alpha = 0.25f),
                )
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(listOf(theme.primary, theme.secondary)),
                    shape = CircleShape,
                )
                .padding(3.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.creator_portrait),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
            )
        }

        Text(
            text = AboutCreatorContent.NAME,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PearlColors.heroPrimary(darkTheme),
        )

        Text(
            text = AboutCreatorContent.ROLE,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = PearlColors.heroSecondary(darkTheme),
        )
    }
}

@Composable
private fun CreatorLinkButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    theme: TabTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = theme.primary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = theme.primary,
            modifier = Modifier.size(16.dp),
        )
    }
}
