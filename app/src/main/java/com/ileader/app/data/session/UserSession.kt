package com.ileader.app.data.session

import com.ileader.app.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Единый источник правды о текущем пользователе.
 *
 * Why: ViewModel'ы и не-композайбл сервисы не должны лазить в Supabase auth
 * за userId или повторно загружать профиль. AuthViewModel пишет сюда после
 * входа, чистит на выходе — все остальные подписываются.
 */
object UserSession {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    /**
     * Becomes true once AuthViewModel finishes its initial session-restore
     * attempt (regardless of outcome). Splash screen uses this to know when
     * it can safely dismiss without the welcome→home flicker.
     */
    private val _restored = MutableStateFlow(false)
    val restored: StateFlow<Boolean> = _restored.asStateFlow()

    val userId: String? get() = _currentUser.value?.id
    val isAuthenticated: Boolean get() = _currentUser.value != null

    fun setUser(user: User?) {
        _currentUser.value = user
    }

    fun update(transform: (User) -> User) {
        _currentUser.value = _currentUser.value?.let(transform)
    }

    fun markRestored() {
        _restored.value = true
    }

    fun clear() {
        _currentUser.value = null
    }
}
