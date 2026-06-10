package com.ileader.app.ui.providers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import com.ileader.app.data.models.User
import com.ileader.app.data.session.UserSession

/**
 * CompositionLocal с текущим пользователем.
 * Используется в любом @Composable: `val user = LocalCurrentUser.current`.
 *
 * Источник — [UserSession]. Сюда подписывается через [UserProvider]
 * в корне UI (MainActivity), пересборка происходит автоматически
 * при обновлении сессии.
 */
val LocalCurrentUser = compositionLocalOf<User?> { null }

/**
 * Оборачивает дерево Compose, делая текущего пользователя доступным
 * через [LocalCurrentUser]. Подписывается на [UserSession.currentUser].
 */
@Composable
fun UserProvider(content: @Composable () -> Unit) {
    val user: State<User?> = UserSession.currentUser.collectAsState()
    CompositionLocalProvider(LocalCurrentUser provides user.value) {
        content()
    }
}
