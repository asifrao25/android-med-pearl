package com.knowledgepearls.app.ui.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.ui.account.AccountUiState
import com.knowledgepearls.app.ui.components.GlassSurface
import com.knowledgepearls.app.ui.components.TabScreenHeader
import com.knowledgepearls.app.ui.theme.LiquidBackground
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.PearlLayout
import com.knowledgepearls.app.ui.theme.TabTheme
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    visible: Boolean,
    accountState: AccountUiState,
    onDismiss: () -> Unit,
    onSignIn: () -> Unit,
    onEditProfile: () -> Unit,
    onSignOut: () -> Unit,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val darkTheme = isPearlDarkTheme()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PearlColors.canvasDark,
        dragHandle = null,
    ) {
        Box(Modifier.fillMaxSize()) {
            LiquidBackground(theme = TabTheme.Settings)
            Column(Modifier.statusBarsPadding()) {
                TabScreenHeader(
                    title = "Settings",
                    subtitle = "Account & app",
                    theme = TabTheme.Settings,
                    showsSettingsButton = false,
                )

                Column(
                    modifier = Modifier.padding(PearlLayout.screenHorizontalPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Account",
                        fontWeight = FontWeight.Bold,
                        color = PearlColors.heroPrimary(darkTheme),
                    )

                    if (accountState.isSignedIn) {
                        Text(
                            text = accountState.userProfile?.name?.ifBlank { accountState.userEmail }
                                ?: accountState.userEmail.orEmpty(),
                            color = PearlColors.heroSecondary(darkTheme),
                        )
                        Button(onClick = onEditProfile, modifier = Modifier.fillMaxWidth()) {
                            Text("Edit profile")
                        }
                        OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                            Text("Sign out")
                        }
                    } else {
                        Text(
                            text = "Sign in to sync public pearls and use community features.",
                            color = PearlColors.heroSecondary(darkTheme),
                        )
                        Button(onClick = {
                            onDismiss()
                            onSignIn()
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Sign in")
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "More settings coming in Stage 10",
                        color = PearlColors.heroSecondary(darkTheme),
                    )
                }
            }
        }
    }
}

@Composable
fun FolderMenuOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    val darkTheme = isPearlDarkTheme()

    Box(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PearlColors.scrim(darkTheme, 0.38f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )

        GlassSurface(
            modifier = Modifier
                .padding(
                    top = 120.dp,
                    end = PearlLayout.screenHorizontalPadding,
                )
                .fillMaxWidth(0.72f)
                .align(androidx.compose.ui.Alignment.TopEnd)
                .clip(RoundedCornerShape(18.dp)),
            cornerRadius = 18.dp,
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Folders",
                    fontWeight = FontWeight.Bold,
                    color = PearlColors.heroPrimary(darkTheme),
                )
                Text(
                    text = "Folder menu coming in Stage 7",
                    modifier = Modifier.padding(top = 8.dp),
                    color = PearlColors.heroSecondary(darkTheme),
                )
            }
        }
    }
}
