package com.ileader.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.BuildConfig
import com.ileader.app.data.models.*
import com.ileader.app.data.notifications.FcmTokenManager
import com.ileader.app.data.remote.AuthApi
import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.session.UserSession
import com.ileader.app.data.util.Alerts
import com.ileader.app.data.util.AppLogger
import com.ileader.app.data.remote.dto.ProfileDto
import com.ileader.app.data.remote.dto.RoleDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val currentUser: User? = null,
    val errorMessage: String? = null,
    val passwordResetSent: Boolean = false,
    // OTP-flow регистрации (как на сайте ileader.kz):
    // после signUp выставляется awaitingEmailConfirmation=true, NavGraph уводит
    // на VerifyCodeScreen, юзер вводит 6-значный код → verifyOtp → signIn.
    // pendingEmail/pendingPassword хранятся в памяти ровно на время этого флоу,
    // чтобы после verify не просить юзера повторно ввести пароль.
    val awaitingEmailConfirmation: Boolean = false,
    val pendingEmail: String? = null,
    val pendingPassword: String? = null,
    val devOtpCode: String? = null,
    val otpResendCooldown: Int = 0
)

class AuthViewModel : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val client = SupabaseModule.client

    init {
        // Restore session on app restart
        viewModelScope.launch {
            try {
                val session = client.auth.currentSessionOrNull()
                if (session != null) {
                    val user = loadCurrentUser()
                    if (user != null) {
                        UserSession.setUser(user)
                        FcmTokenManager.saveTokenForUser(user.id)
                        _state.value = _state.value.copy(
                            isAuthenticated = true,
                            currentUser = user
                        )
                    }
                }
            } catch (e: Exception) {
                AppLogger.w("Session restore failed", e)
            } finally {
                // Tell the splash screen it's safe to dismiss — either the user
                // is signed in, or we've confirmed no valid session exists.
                UserSession.markRestored()
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                val user = loadCurrentUser()
                if (user != null) {
                    UserSession.setUser(user)
                    FcmTokenManager.saveTokenForUser(user.id)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = user
                    )
                } else {
                    Alerts.error("Не удалось загрузить профиль")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Не удалось загрузить профиль"
                    )
                }
            } catch (e: Exception) {
                // Don't log the email — it's PII and ends up in logcat
                // which is readable by any installed debugger on the device.
                AppLogger.e("Sign-in failed", e)
                Alerts.error(parseAuthError(e))
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = parseAuthError(e)
                )
            }
        }
    }

    fun signUp(data: SignUpData) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            // Client-side validation
            if (data.name.isBlank()) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Введите имя")
                return@launch
            }
            if (data.email.isBlank() || !data.email.contains("@")) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Введите корректный email")
                return@launch
            }
            if (data.password.length < 6) {
                _state.value = _state.value.copy(isLoading = false, errorMessage = "Пароль должен быть не менее 6 символов")
                return@launch
            }

            try {
                // 1) Регистрируем юзера в Supabase auth.users.
                //    Триггер `handle_new_user()` сам создаёт строку в profiles
                //    из user_metadata — поэтому никаких последующих UPDATE'ов
                //    делать НЕ нужно (и нельзя — RLS не пустит до подтверждения).
                client.auth.signUpWith(Email) {
                    this.email = data.email
                    this.password = data.password
                    this.data = buildJsonObject {
                        put("name", data.name)
                        put("role", data.role.name.lowercase())
                        put("phone", data.phone)
                        put("city", data.city)
                        put("country", data.country)
                        if (data.athleteSubtype != null) {
                            put("athlete_subtype", data.athleteSubtype.name.lowercase())
                        }
                        if (data.sportIds != null && data.sportIds.isNotEmpty()) {
                            put("sport_ids", kotlinx.serialization.json.buildJsonArray {
                                data.sportIds.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) }
                            })
                        }
                    }
                }

                // 2) НЕ входим в систему — email ещё не подтверждён, signIn упадёт
                //    с "Email not confirmed". Гасим сессию и переходим к OTP-флоу.
                runCatching { client.auth.signOut() }

                // 3) Запрашиваем 6-значный код через тот же бэкенд что и веб.
                val sendResult = AuthApi.sendOtp(data.email)
                val devCode = sendResult.getOrNull()
                if (sendResult.isFailure) {
                    AppLogger.w("OTP send failed (non-fatal)", sendResult.exceptionOrNull())
                    // Не валим регистрацию — юзер сможет нажать "Отправить повторно"
                    // на экране ввода кода.
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    awaitingEmailConfirmation = true,
                    pendingEmail = data.email,
                    pendingPassword = data.password,
                    devOtpCode = devCode,
                    otpResendCooldown = 60
                )
            } catch (e: Exception) {
                AppLogger.e("Sign-up failed", e)
                Alerts.error(parseAuthError(e))
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = parseAuthError(e)
                )
            }
        }
    }

    /**
     * Подтверждение email 6-значным кодом из письма.
     * После успеха автоматически выполняем signIn с сохранённым паролем —
     * пользователь сразу попадает в приложение.
     */
    fun verifyOtp(code: String) {
        val email = _state.value.pendingEmail
        val password = _state.value.pendingPassword
        if (email == null || password == null) {
            Alerts.error("Сессия регистрации потеряна. Войдите заново.")
            _state.value = AuthState()
            return
        }
        if (!code.matches(Regex("^\\d{6}$"))) {
            _state.value = _state.value.copy(errorMessage = "Введите 6-значный код")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                AuthApi.verifyOtp(email, code).getOrThrow()

                // Email confirmed — теперь можно входить.
                client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                val user = loadCurrentUser()
                if (user != null) {
                    UserSession.setUser(user)
                    FcmTokenManager.saveTokenForUser(user.id)
                    _state.value = AuthState(
                        isAuthenticated = true,
                        currentUser = user
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Не удалось загрузить профиль"
                    )
                    Alerts.error("Не удалось загрузить профиль")
                }
            } catch (e: Exception) {
                AppLogger.e("OTP verify failed", e)
                val msg = e.message?.takeIf { it.isNotBlank() } ?: "Не удалось подтвердить код"
                Alerts.error(msg)
                _state.value = _state.value.copy(isLoading = false, errorMessage = msg)
            }
        }
    }

    fun resendOtp() {
        val email = _state.value.pendingEmail ?: return
        if (_state.value.otpResendCooldown > 0) return
        viewModelScope.launch {
            val result = AuthApi.sendOtp(email)
            if (result.isSuccess) {
                Alerts.success("Код отправлен повторно")
                _state.value = _state.value.copy(
                    devOtpCode = result.getOrNull(),
                    otpResendCooldown = 60
                )
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Не удалось отправить код"
                Alerts.error(msg)
            }
        }
    }

    /** Уменьшает счётчик cooldown на 1 (вызывается из экрана раз в секунду). */
    fun tickResendCooldown() {
        val cur = _state.value.otpResendCooldown
        if (cur > 0) {
            _state.value = _state.value.copy(otpResendCooldown = cur - 1)
        }
    }

    /** Выйти из OTP-флоу (юзер нажал "Назад" / "Войти"). */
    fun cancelEmailConfirmation() {
        _state.value = _state.value.copy(
            awaitingEmailConfirmation = false,
            pendingEmail = null,
            pendingPassword = null,
            devOtpCode = null,
            otpResendCooldown = 0,
            errorMessage = null
        )
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null, passwordResetSent = false)

            if (email.isBlank() || !email.contains("@")) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Введите корректный email"
                )
                return@launch
            }

            try {
                client.auth.resetPasswordForEmail(email)
                Alerts.success("Письмо для сброса пароля отправлено")
                _state.value = _state.value.copy(
                    isLoading = false,
                    passwordResetSent = true
                )
            } catch (e: Exception) {
                AppLogger.e("Password reset failed", e)
                Alerts.error(parseAuthError(e))
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = parseAuthError(e)
                )
            }
        }
    }

    fun demoLogin(role: UserRole) {
        val demoEmails = mapOf(
            UserRole.ATHLETE to "athlete@demo.com",
            UserRole.TRAINER to "trainer@demo.com",
            UserRole.ORGANIZER to "organizer@demo.com",
            UserRole.REFEREE to "referee@demo.com",
            UserRole.SPONSOR to "sponsor@demo.com",
            UserRole.MEDIA to "media@demo.com",
            UserRole.ADMIN to "admin@mail.ru",
            UserRole.USER to "user@demo.com"
        )
        val email = demoEmails[role] ?: return
        val password = BuildConfig.DEMO_PASSWORD
        val adminPassword = BuildConfig.DEMO_ADMIN_PASSWORD
        signIn(email, if (role == UserRole.ADMIN) adminPassword else password)
    }

    fun signOut() {
        viewModelScope.launch {
            // Clear FCM token before signing out — auth still valid for the update.
            val userId = client.auth.currentUserOrNull()?.id
            if (userId != null) {
                try {
                    FcmTokenManager.clearTokenForUser(userId)
                } catch (e: Exception) {
                    AppLogger.w("FCM token clear failed (non-critical)", e)
                }
            }
            try {
                client.auth.signOut()
            } catch (e: Exception) {
                AppLogger.w("Sign-out error (non-critical)", e)
            }
            // Drop every in-memory cache entry. The next user may see different
            // data (privacy) and different server responses (session change).
            com.ileader.app.data.util.MemoryCache.clear()
            UserSession.clear()
            _state.value = AuthState()
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun clearPasswordResetSent() {
        _state.value = _state.value.copy(passwordResetSent = false)
    }

    /**
     * Load current user profile from Supabase.
     * Joins profiles with roles to determine UserRole.
     */
    private suspend fun loadCurrentUser(): User? {
        val session = client.auth.currentSessionOrNull()
        val userId = session?.user?.id ?: return null

        return try {
            // All three reads share userId; run in parallel.
            coroutineScope {
                val profileDef = async {
                    client.from("profiles")
                        .select(Columns.raw("*, roles!primary_role_id(id, name)"))
                        { filter { eq("id", userId) } }
                        .decodeSingle<ProfileDto>()
                }
                val teamIdDef = async {
                    try {
                        client.from("team_members")
                            .select(Columns.raw("team_id"))
                            { filter { eq("user_id", userId) } }
                            .decodeList<TeamIdDto>()
                            .firstOrNull()?.teamId
                    } catch (_: Exception) { null }
                }
                val sportIdsDef = async {
                    try {
                        client.from("user_sports")
                            .select(Columns.raw("sport_id"))
                            { filter { eq("user_id", userId) } }
                            .decodeList<SportIdDto>()
                            .map { it.sportId }
                    } catch (_: Exception) { emptyList() }
                }

                val user = profileDef.await().toDomain()
                user.copy(
                    teamId = teamIdDef.await(),
                    sportIds = sportIdsDef.await().ifEmpty { null }
                )
            }
        } catch (e: Exception) {
            AppLogger.e("loadCurrentUser failed", e)
            null
        }
    }

    private fun parseAuthError(e: Exception): String {
        val msg = e.message?.lowercase() ?: ""
        return when {
            "invalid login credentials" in msg -> "Неверный email или пароль"
            "email not confirmed" in msg -> "Email не подтверждён. Проверьте почту"
            "user already registered" in msg -> "Этот email уже зарегистрирован"
            "password" in msg && "weak" in msg -> "Пароль слишком слабый"
            "rate limit" in msg -> "Слишком много попыток. Подождите"
            "network" in msg || "unable to resolve host" in msg -> "Нет подключения к интернету"
            "timeout" in msg -> "Сервер не отвечает. Проверьте интернет-соединение"
            else -> e.message ?: "Произошла ошибка"
        }
    }
}

@kotlinx.serialization.Serializable
private data class TeamIdDto(
    @kotlinx.serialization.SerialName("team_id") val teamId: String
)

@kotlinx.serialization.Serializable
private data class SportIdDto(
    @kotlinx.serialization.SerialName("sport_id") val sportId: String
)
