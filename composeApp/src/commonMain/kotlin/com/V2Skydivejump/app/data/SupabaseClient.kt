package com.V2Skydivejump.app.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import com.russhwolf.settings.Settings
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.Json
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.annotations.SupabaseInternal
import io.ktor.client.plugins.HttpTimeout
import kotlin.time.Duration.Companion.seconds

object SupabaseManager {
    private var _client: io.github.jan.supabase.SupabaseClient? = null
    val client: io.github.jan.supabase.SupabaseClient
        get() = _client ?: throw IllegalStateException("SupabaseManager not initialized. Call init() first.")

    fun init(url: String, key: String) {
        if (_client != null) return
        println("SupabaseManager: Initializing with URL: $url")
        if (url.isBlank() || key.isBlank()) {
            println("SupabaseManager ERROR: URL or Key is blank!")
        }
        _client = createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = key
        ) {
            @OptIn(SupabaseInternal::class)
            httpConfig {
                install(HttpTimeout) {
                    requestTimeoutMillis = 30000
                    connectTimeoutMillis = 30000
                    socketTimeoutMillis = 30000
                }
            }
            install(Auth) {
                sessionManager = object : SessionManager {
                    private val settings = Settings()
                    private val sessionKey = "v2skydive_supabase_session"
                    private val json = Json { 
                        ignoreUnknownKeys = true
                        encodeDefaults = true
                    }

                    override suspend fun saveSession(session: UserSession) {
                        settings.putString(sessionKey, json.encodeToString(session))
                    }

                    override suspend fun loadSession(): UserSession {
                        val sessionString = settings.getStringOrNull(sessionKey)
                            ?: throw IllegalStateException("No session stored")
                        return json.decodeFromString(sessionString)
                    }

                    override suspend fun deleteSession() {
                        settings.remove(sessionKey)
                    }
                }
            }
            install(Postgrest) {
                serializer = KotlinXSerializer(Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                    encodeDefaults = true
                    explicitNulls = false
                })
            }
            install(Realtime)
            install(Storage)
        }
    }
}
