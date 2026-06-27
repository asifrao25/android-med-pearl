package com.knowledgepearls.app.data.repository

import com.knowledgepearls.app.data.model.UserProfile
import com.knowledgepearls.app.data.remote.SupabaseConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

sealed class AccountAuthException(message: String) : Exception(message) {
    class EmailVerificationRequired(val email: String) :
        AccountAuthException("Enter the 6-digit code we sent to your email to finish creating your account.")

    class InvalidVerificationCode :
        AccountAuthException("Enter the 6-digit code from your email.")

    class VerificationIncomplete :
        AccountAuthException("Verification didn't complete. Try again or request a new code.")

    class RateLimited :
        AccountAuthException("Please wait about a minute before requesting another code.")

    class EmailAlreadyRegistered :
        AccountAuthException("This email already has an account. Sign in instead.")
}

enum class EmailSignUpOutcome {
    SignedIn,
    EmailVerificationRequired,
}

@Singleton
class AccountRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    suspend fun currentUserId(): String? =
        supabase.auth.currentSessionOrNull()?.user?.id

    suspend fun currentUserEmail(): String? =
        supabase.auth.currentSessionOrNull()?.user?.email

    suspend fun restoreSession(): Boolean {
        return try {
            supabase.auth.currentSessionOrNull()?.user != null
        } catch (_: Exception) {
            false
        }
    }

    suspend fun signIn(email: String, password: String) {
        val normalizedEmail = normalizeEmail(email)
        try {
            supabase.auth.signInWith(Email) {
                this.email = normalizedEmail
                this.password = password
            }
            val user = supabase.auth.currentUserOrNull()
            if (user?.emailConfirmedAt == null) {
                supabase.auth.resendEmail(OtpType.Email.SIGNUP, normalizedEmail)
                throw AccountAuthException.EmailVerificationRequired(normalizedEmail)
            }
        } catch (error: AccountAuthException) {
            throw error
        } catch (error: Exception) {
            if (isEmailNotConfirmed(error)) {
                runCatching { supabase.auth.resendEmail(OtpType.Email.SIGNUP, normalizedEmail) }
                throw AccountAuthException.EmailVerificationRequired(normalizedEmail)
            }
            throw error
        }
    }

    suspend fun signUp(email: String, password: String): EmailSignUpOutcome {
        val normalizedEmail = normalizeEmail(email)
        try {
            // No redirect URL — Android Auth config defaults to the OAuth deeplink, which makes
            // GoTrue send a confirmation link instead of the 6-digit OTP the app expects.
            supabase.auth.signUpWith(Email, redirectUrl = null) {
                this.email = normalizedEmail
                this.password = password
            }
        } catch (error: AuthRestException) {
            if (error.errorCode == AuthErrorCode.UserAlreadyExists) {
                runCatching { supabase.auth.resendEmail(OtpType.Email.SIGNUP, normalizedEmail) }
                return EmailSignUpOutcome.EmailVerificationRequired
            }
            throw mapAuthException(error)
        }
        val user = supabase.auth.currentUserOrNull()
        if (user?.emailConfirmedAt != null && supabase.auth.currentSessionOrNull() != null) {
            return EmailSignUpOutcome.SignedIn
        }
        return EmailSignUpOutcome.EmailVerificationRequired
    }

    suspend fun verifySignupEmailCode(email: String, code: String) {
        val normalizedEmail = normalizeEmail(email)
        val token = code.trim()
        if (token.length != 6 || token.any { !it.isDigit() }) {
            throw AccountAuthException.InvalidVerificationCode()
        }
        supabase.auth.verifyEmailOtp(
            type = OtpType.Email.SIGNUP,
            email = normalizedEmail,
            token = token,
        )
        if (supabase.auth.currentSessionOrNull() == null) {
            throw AccountAuthException.VerificationIncomplete()
        }
    }

    suspend fun resendSignupVerificationCode(email: String) {
        try {
            supabase.auth.resendEmail(OtpType.Email.SIGNUP, normalizeEmail(email))
        } catch (error: AuthRestException) {
            throw mapAuthException(error)
        }
    }

    suspend fun signInWithGoogleIdToken(idToken: String) {
        supabase.auth.signInWith(IDToken) {
            this.idToken = idToken
            provider = Google
        }
    }

    suspend fun signInWithGoogleOAuth() {
        supabase.auth.signInWith(Google)
    }

    suspend fun signOut() {
        supabase.auth.signOut()
    }

    suspend fun fetchProfile(userId: String): UserProfile? {
        val profiles = supabase.from("profiles").select {
            filter { eq("id", userId) }
        }.decodeList<UserProfile>()
        return profiles.firstOrNull()
    }

    suspend fun createProfile(
        userId: String,
        name: String,
        bio: String,
        deanery: String,
        specialty: String,
        grade: String,
        allowMessages: Boolean,
        showEmail: Boolean,
        publicEmail: String,
        allowPearlShares: Boolean,
        notifyPearlSharesEmail: Boolean,
        avatarUrl: String?,
    ) {
        val fields = ProfileFields(
            name = name,
            bio = bio.ifBlank { null },
            deanery = deanery,
            specialty = specialty,
            grade = grade,
            allowMessages = allowMessages,
            showEmail = showEmail,
            publicEmail = publicEmail.ifBlank { null },
            allowPearlShares = allowPearlShares,
            notifyPearlSharesEmail = notifyPearlSharesEmail,
            avatarUrl = avatarUrl,
        )

        supabase.from("profiles").update(fields) {
            filter { eq("id", userId) }
        }

        val updated = fetchProfile(userId)
        if (updated?.name != name) {
            val safeUsername = "user_${userId.lowercase().replace("-", "")}"
            supabase.from("profiles").insert(
                ProfileInsert(
                    id = userId,
                    username = safeUsername,
                    name = name,
                    bio = bio.ifBlank { null },
                    deanery = deanery,
                    specialty = specialty,
                    grade = grade,
                    allowMessages = allowMessages,
                    showEmail = showEmail,
                    publicEmail = publicEmail.ifBlank { null },
                    avatarUrl = avatarUrl,
                    allowPearlShares = allowPearlShares,
                    notifyPearlSharesEmail = notifyPearlSharesEmail,
                ),
            )
        }
    }

    suspend fun updateProfile(
        userId: String,
        name: String,
        bio: String,
        deanery: String,
        specialty: String,
        grade: String,
        allowMessages: Boolean,
        showEmail: Boolean,
        publicEmail: String,
        allowPearlShares: Boolean,
        notifyPearlSharesEmail: Boolean,
        avatarUrl: String?,
    ) {
        supabase.from("profiles").update(
            ProfileFields(
                name = name,
                bio = bio.ifBlank { null },
                deanery = deanery,
                specialty = specialty,
                grade = grade,
                allowMessages = allowMessages,
                showEmail = showEmail,
                publicEmail = publicEmail.ifBlank { null },
                allowPearlShares = allowPearlShares,
                notifyPearlSharesEmail = notifyPearlSharesEmail,
                avatarUrl = avatarUrl,
            ),
        ) {
            filter { eq("id", userId) }
        }
    }

    suspend fun uploadAvatar(userId: String, jpegBytes: ByteArray): String {
        val path = "${userId.lowercase()}.jpg"
        supabase.storage.from(SupabaseConfig.AVATARS_BUCKET).upload(path, jpegBytes) {
            upsert = true
        }
        val publicUrl = supabase.storage.from(SupabaseConfig.AVATARS_BUCKET).publicUrl(path)
        return versionAvatarUrl(publicUrl)
    }

    suspend fun patchAvatarUrl(userId: String, avatarUrl: String) {
        supabase.from("profiles").update(AvatarPatch(avatarUrl)) {
            filter { eq("id", userId) }
        }
    }

    fun isAuthenticated(): Boolean =
        supabase.auth.currentSessionOrNull() != null

    private fun normalizeEmail(email: String): String =
        email.trim().lowercase()

    private fun isEmailNotConfirmed(error: Exception): Boolean {
        if (error is AuthRestException && error.errorCode == AuthErrorCode.EmailNotConfirmed) {
            return true
        }
        val message = error.message?.lowercase().orEmpty()
        return message.contains("email not confirmed") ||
            message.contains("not verified") ||
            message.contains("confirm your email")
    }

    private fun mapAuthException(error: AuthRestException): Exception =
        when (error.errorCode) {
            AuthErrorCode.OverEmailSendRateLimit, AuthErrorCode.OverRequestRateLimit ->
                AccountAuthException.RateLimited()
            AuthErrorCode.UserAlreadyExists ->
                AccountAuthException.EmailAlreadyRegistered()
            else -> error
        }

    private fun versionAvatarUrl(url: String): String {
        val separator = if (url.contains("?")) "&" else "?"
        return "$url${separator}v=${System.currentTimeMillis()}"
    }

    @Serializable
    private data class ProfileFields(
        val name: String,
        val bio: String? = null,
        val deanery: String,
        val specialty: String,
        val grade: String,
        @SerialName("allow_messages") val allowMessages: Boolean,
        @SerialName("show_email") val showEmail: Boolean,
        @SerialName("public_email") val publicEmail: String? = null,
        @SerialName("allow_pearl_shares") val allowPearlShares: Boolean = true,
        @SerialName("notify_pearl_shares_email") val notifyPearlSharesEmail: Boolean = true,
        @SerialName("avatar_url") val avatarUrl: String? = null,
    )

    @Serializable
    private data class ProfileInsert(
        val id: String,
        val username: String,
        val name: String,
        val bio: String? = null,
        val deanery: String,
        val specialty: String,
        val grade: String,
        @SerialName("allow_messages") val allowMessages: Boolean,
        @SerialName("show_email") val showEmail: Boolean,
        @SerialName("public_email") val publicEmail: String? = null,
        @SerialName("avatar_url") val avatarUrl: String? = null,
        @SerialName("allow_pearl_shares") val allowPearlShares: Boolean = true,
        @SerialName("notify_pearl_shares_email") val notifyPearlSharesEmail: Boolean = true,
    )

    @Serializable
    private data class AvatarPatch(@SerialName("avatar_url") val avatarUrl: String)
}
