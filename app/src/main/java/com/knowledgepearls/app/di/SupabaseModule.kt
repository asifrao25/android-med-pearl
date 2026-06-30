package com.knowledgepearls.app.di

import com.knowledgepearls.app.data.auth.EncryptedSupabaseSessionManager
import com.knowledgepearls.app.data.remote.SupabaseConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {
    private val supabaseJson = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideSupabaseClient(sessionManager: EncryptedSupabaseSessionManager): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = SupabaseConfig.URL,
            supabaseKey = SupabaseConfig.ANON_KEY,
        ) {
            defaultSerializer = KotlinXSerializer(supabaseJson)
            install(Auth) {
                scheme = SupabaseConfig.AUTH_SCHEME
                host = SupabaseConfig.AUTH_HOST
                flowType = FlowType.PKCE
                this.sessionManager = sessionManager
            }
            install(Postgrest)
            install(Storage)
        }
}
