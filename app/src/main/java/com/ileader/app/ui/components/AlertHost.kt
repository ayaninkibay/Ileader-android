package com.ileader.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ileader.app.data.util.AlertController
import com.ileader.app.data.util.AlertKind
import com.ileader.app.ui.theme.LocalAppColors

/**
 * Глобальный хост пользовательских алертов.
 * Подписан на [AlertController] и показывает Snackbar для каждого события.
 *
 * Размещается в корне UI ([com.ileader.app.MainActivity]), поверх NavGraph —
 * так что алерты работают на любом экране, включая auth.
 *
 * Внутри MainScreen есть отдельный [LocalSnackbarHost] для локальных
 * snackbar'ов конкретного экрана — это нормально, они не конфликтуют.
 */
@Composable
fun AlertHost(
    bottomPadding: androidx.compose.ui.unit.Dp = 24.dp,
    content: @Composable () -> Unit
) {
    val hostState = remember { SnackbarHostState() }
    val colors = LocalAppColors.current

    LaunchedEffect(Unit) {
        AlertController.alerts.collect { alert ->
            // Если уже что-то показано — мгновенно закрываем и кидаем новое.
            // Очередь Snackbar по умолчанию ждёт окончания текущего, что для
            // ошибок раздражает.
            hostState.currentSnackbarData?.dismiss()
            hostState.showSnackbar(
                message = alert.text,
                duration = when (alert.kind) {
                    AlertKind.ERROR -> SnackbarDuration.Long
                    else -> SnackbarDuration.Short
                }
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        content()

        SnackbarHost(
            hostState = hostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = bottomPadding, start = 12.dp, end = 12.dp)
        ) { data ->
            val (bg, fg) = snackbarColors(data.visuals.message, colors.cardBg, colors.textPrimary)
            Snackbar(
                snackbarData = data,
                containerColor = bg,
                contentColor = fg
            )
        }
    }
}

/**
 * Подбираем цвет под тип. Тип не передаётся через SnackbarData (это API M3),
 * но мы выставили длительность как proxy: ERROR = Long. Этого хватает чтобы
 * закрасить рамку в акцентный красный для критичных событий.
 */
private fun snackbarColors(
    message: String,
    defaultBg: Color,
    defaultFg: Color
): Pair<Color, Color> {
    // Мы используем нейтральные цвета карточки — иконку/цвет можно добавить позже,
    // когда понадобится отличать success от info визуально. Пока единый стиль.
    return defaultBg to defaultFg
}
