package com.knowledgepearls.app.ui.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knowledgepearls.app.R
import com.knowledgepearls.app.ui.theme.PearlColors
import com.knowledgepearls.app.ui.theme.isPearlDarkTheme

private val AuthTeal = Color(0xFF14B8A6)

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF747775)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF1F1F1F),
            disabledContainerColor = Color.White.copy(alpha = 0.72f),
            disabledContentColor = Color(0xFF1F1F1F).copy(alpha = 0.38f),
        ),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color(0xFF4285F4),
                strokeWidth = 2.dp,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_google),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Color.Unspecified,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Continue with Google",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F1F1F),
                )
            }
        }
    }
}

@Composable
fun InlineAuthPanel(
    uiState: AccountUiState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onVerifyCode: (String, String) -> Unit,
    onResendCode: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val darkTheme = isPearlDarkTheme()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var pendingAuthMethod by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GoogleSignInButton(
            onClick = {
                pendingAuthMethod = "google"
                onGoogleSignIn()
            },
            enabled = !uiState.isLoading,
            isLoading = uiState.isLoading && pendingAuthMethod == "google",
        )

        Text(
            text = "or sign in with email",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = PearlColors.heroSecondary(darkTheme),
        )

        if (uiState.pendingVerificationEmail != null) {
            val pendingEmail = uiState.pendingVerificationEmail
            EmailVerificationSection(
                email = pendingEmail,
                code = verificationCode,
                onCodeChange = { verificationCode = it.filter(Char::isDigit).take(6) },
                isLoading = uiState.isLoading,
                errorMessage = uiState.errorMessage,
                infoMessage = uiState.verificationCodeSentMessage,
                onVerify = { onVerifyCode(pendingEmail, verificationCode) },
                onResend = { onResendCode(pendingEmail) },
            )
        } else {
            AccountGlassTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email,
            )
            AccountGlassTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                isPassword = true,
            )

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color(0xFFFF6B6B),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Button(
                onClick = {
                    pendingAuthMethod = "email"
                    if (isSignUp) onSignUp(email, password) else onSignIn(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.isLoading && email.isNotBlank() && password.length >= 6,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AuthTeal),
            ) {
                if (uiState.isLoading && pendingAuthMethod == "email") {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                } else {
                    Text(
                        text = if (isSignUp) "Create account" else "Sign in",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            TextButton(
                onClick = { isSignUp = !isSignUp },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = if (isSignUp) "Already have an account? Sign in" else "Need an account? Sign up",
                    color = PearlColors.heroSecondary(darkTheme),
                )
            }
        }
    }
}

@Composable
internal fun EmailVerificationSection(
    email: String,
    code: String,
    onCodeChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    infoMessage: String?,
    onVerify: () -> Unit,
    onResend: () -> Unit,
) {
    val darkTheme = isPearlDarkTheme()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "We sent a 6-digit code to $email. In Gmail, check Spam and Promotions, or search for \"Med Pearls verification\".",
            color = PearlColors.heroSecondary(darkTheme),
            style = MaterialTheme.typography.bodyMedium,
        )
        AccountGlassTextField(
            value = code,
            onValueChange = onCodeChange,
            label = "Verification code",
            keyboardType = KeyboardType.Number,
        )
        errorMessage?.let {
            Text(it, color = Color(0xFFFF6B6B), style = MaterialTheme.typography.bodySmall)
        }
        infoMessage?.let {
            Text(it, color = AuthTeal, style = MaterialTheme.typography.bodySmall)
        }
        Button(
            onClick = onVerify,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && code.length == 6,
            colors = ButtonDefaults.buttonColors(containerColor = AuthTeal),
        ) {
            Text("Verify email")
        }
        TextButton(onClick = onResend, enabled = !isLoading) {
            Text("Resend code", color = PearlColors.heroSecondary(darkTheme))
        }
    }
}
