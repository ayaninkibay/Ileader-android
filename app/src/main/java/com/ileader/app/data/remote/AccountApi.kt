package com.ileader.app.data.remote

import com.ileader.app.BuildConfig
import io.github.jan.supabase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Самоудаление аккаунта через Supabase Edge Function `delete-account`.
 *
 * Требование сторов: Apple 5.1.1(v) и Google Play обязывают приложение
 * с регистрацией давать удалить аккаунт прямо из приложения. Серверная
 * функция по JWT вызывающего удаляет строку profiles (каскадом — все
 * прикладные данные) и auth-пользователя через admin API.
 *
 * Functions-плагин supabase-kt не подключён — функция дёргается обычным
 * Ktor-запросом по образцу [AuthApi].
 */
object AccountApi {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(HttpTimeout) {
                requestTimeoutMillis = 20_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 20_000
            }
            expectSuccess = false
        }
    }

    @Serializable
    private data class DeleteResponse(
        val success: Boolean? = null,
        val error: String? = null
    )

    /** Бросает исключение с человекочитаемым сообщением при любой неудаче. */
    suspend fun deleteAccount() {
        val accessToken = SupabaseModule.client.auth.currentSessionOrNull()?.accessToken
            ?: throw IllegalStateException("Сессия не найдена — войдите заново")

        val response = client.post("${BuildConfig.SUPABASE_URL}/functions/v1/delete-account") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            header("apikey", BuildConfig.SUPABASE_ANON_KEY)
        }
        val body = response.bodyAsText()
        val parsed = runCatching {
            json.decodeFromString(DeleteResponse.serializer(), body)
        }.getOrNull()

        if (response.status.value !in 200..299 || parsed?.success != true) {
            throw RuntimeException(
                parsed?.error ?: "Не удалось удалить аккаунт (${response.status.value})"
            )
        }
    }
}
