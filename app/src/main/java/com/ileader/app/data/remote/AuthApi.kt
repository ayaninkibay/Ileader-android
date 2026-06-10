package com.ileader.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * HTTP-клиент к боевым Next.js-эндпоинтам ileader.kz для OTP-флоу регистрации.
 *
 * Веб-проект использует кастомный OTP (таблица `email_otps`, 6-значный код по Resend)
 * вместо встроенного Supabase email-confirm. Поэтому мобилка дёргает те же routes,
 * чтобы единый user-experience по подтверждению email и одни и те же письма.
 *
 * Эндпоинты:
 *   POST /api/auth/send-otp    { email }                  → { success, devCode? }
 *   POST /api/auth/verify-otp  { email, code }            → { success, userId? } | { error }
 */
object AuthApi {

    private const val BASE_URL = "https://ileader.kz"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 15_000
            }
            expectSuccess = false
        }
    }

    @Serializable
    private data class SendOtpRequest(val email: String)

    @Serializable
    private data class VerifyOtpRequest(val email: String, val code: String)

    @Serializable
    private data class OtpResponse(
        val success: Boolean? = null,
        val error: String? = null,
        @SerialName("devCode") val devCode: String? = null,
        val userId: String? = null
    )

    /**
     * Шлёт 6-значный код на email. Backend хранит хеш в `email_otps`, TTL 10 мин.
     *
     * Возвращает success=true даже если письмо не доставилось (best-effort).
     * Если Resend не настроен в проде — придёт `devCode` в ответе (только для dev).
     */
    suspend fun sendOtp(email: String): Result<String?> = runCatching {
        val response = client.post("$BASE_URL/api/auth/send-otp") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(SendOtpRequest.serializer(), SendOtpRequest(email)))
        }
        val body = response.bodyAsText()
        val parsed = json.decodeFromString(OtpResponse.serializer(), body)
        if (response.status.value !in 200..299 || parsed.success == false) {
            throw RuntimeException(parsed.error ?: "Не удалось отправить код (${response.status.value})")
        }
        parsed.devCode
    }

    /**
     * Проверяет 6-значный код. На успех — backend через admin-API ставит
     * `email_confirm: true` для пользователя в Supabase auth.users.
     *
     * После этого юзер уже может signIn'иться через обычный пароль.
     */
    suspend fun verifyOtp(email: String, code: String): Result<Unit> = runCatching {
        val response = client.post("$BASE_URL/api/auth/verify-otp") {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(VerifyOtpRequest.serializer(), VerifyOtpRequest(email, code)))
        }
        val body = response.bodyAsText()
        val parsed = json.decodeFromString(OtpResponse.serializer(), body)
        if (response.status != HttpStatusCode.OK || parsed.success != true) {
            throw RuntimeException(parsed.error ?: "Не удалось подтвердить код")
        }
    }
}
