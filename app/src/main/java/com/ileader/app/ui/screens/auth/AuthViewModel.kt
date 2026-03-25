package com.ileader.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.BuildConfig
import com.ileader.app.data.models.*
import com.ileader.app.data.remote.SupabaseModule
import com.ileader.app.data.util.AppLogger
import com.ileader.app.data.remote.dto.ProfileDto
import com.ileader.app.data.remote.dto.RoleDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
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
    val passwordResetSent: Boolean = false
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
                        _state.value = _state.value.copy(
                            isAuthenticated = true,
                            currentUser = user
                        )
                    }
                }
            } catch (e: Exception) {
                AppLogger.w("Session restore failed", e)
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
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        currentUser = user
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Не удалось загрузить профиль"
                    )
                }
            } catch (e: Exception) {
                AppLogger.e("Sign-in failed for $email", e)
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
                client.auth.signUpWith(Email) {
                    this.email = data.email
                    this.password = data.password
                    this.data = buildJsonObject {
                        put("name", data.name)
                        put("role", data.role.name.lowercase())
                        put("phone", data.phone)
                        put("city", data.city)
                    }
                }

                // After signup, sign in to get session
                client.auth.signInWith(Email) {
                    this.email = data.email
                    this.password = data.password
                }

                // Update profile with additional data
                val session = client.auth.currentSessionOrNull()
                val userId = session?.user?.id
                if (userId != null) {
                    // Find role ID
                    val roleDto = client.from("roles")
                        .select { filter { eq("name", data.role.name.lowercase()) } }
                        .decodeSingleOrNull<RoleDto>()

                    // Update profile
                    client.from("profiles").update({
                        set("name", data.name)
                        set("phone", data.phone)
                        set("city", data.city)
                        if (roleDto != null) {
                            set("primary_role_id", roleDto.id)
                        }
                        if (data.athleteSubtype != null) {
                            set("athlete_subtype", data.athleteSubtype.name.lowercase())
                        }
                    }) {
                        filter { eq("id", userId) }
                    }
                }

                val user = loadCurrentUser()
                _state.value = _state.value.copy(
                    isLoading = false,
                    isAuthenticated = user != null,
                    currentUser = user
                )
            } catch (e: Exception) {
                AppLogger.e("Sign-up failed for ${data.email}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = parseAuthError(e)
                )
            }
        }
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
                _state.value = _state.value.copy(
                    isLoading = false,
                    passwordResetSent = true
                )
            } catch (e: Exception) {
                AppLogger.e("Password reset failed for $email", e)
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
            try {
                client.auth.signOut()
            } catch (e: Exception) {
                AppLogger.w("Sign-out error (non-critical)", e)
            }
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
            val profile = client.from("profiles")
                .select(Columns.raw("*, roles!primary_role_id(id, name)"))
                { filter { eq("id", userId) } }
                .decodeSingle<ProfileDto>()

            // Determine teamId from team_members
            val teamId = try {
                val membership = client.from("team_members")
                    .select(Columns.raw("team_id"))
                    { filter { eq("user_id", userId) } }
                    .decodeList<TeamIdDto>()
                membership.firstOrNull()?.teamId
            } catch (_: Exception) {
                null
            }

            // Get sport IDs
            val sportIds = try {
                client.from("user_sports")
                    .select(Columns.raw("sport_id"))
                    { filter { eq("user_id", userId) } }
                    .decodeList<SportIdDto>()
                    .map { it.sportId }
            } catch (_: Exception) {
                emptyList()
            }

            val user = profile.toDomain()
            user.copy(
                teamId = teamId,
                sportIds = sportIds.ifEmpty { null }
            )
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
