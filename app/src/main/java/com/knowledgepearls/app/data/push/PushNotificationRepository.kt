package com.knowledgepearls.app.data.push

import com.knowledgepearls.app.AnalyticsPlatform
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Singleton
class PushNotificationRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    suspend fun uploadToken(userId: String, token: String) {
        supabase.from("push_tokens").upsert(
            PushTokenRow(
                userId = userId.lowercase(),
                token = token,
                platform = AnalyticsPlatform.VALUE,
                updatedAt = Instant.now().toString(),
            ),
        ) {
            onConflict = "user_id,token"
        }
    }

    suspend fun removeToken(userId: String, token: String) {
        supabase.from("push_tokens").delete {
            filter {
                eq("user_id", userId.lowercase())
                eq("token", token)
            }
        }
    }

    @Serializable
    private data class PushTokenRow(
        @SerialName("user_id") val userId: String,
        val token: String,
        val platform: String,
        @SerialName("updated_at") val updatedAt: String,
    )
}
