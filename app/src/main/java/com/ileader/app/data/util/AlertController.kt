package com.ileader.app.data.util

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Тип пользовательского алерта.
 */
enum class AlertKind { ERROR, INFO, SUCCESS }

data class UiAlert(
    val text: String,
    val kind: AlertKind = AlertKind.ERROR
)

/**
 * Единый канал пользовательских уведомлений (snackbar/toast).
 *
 * Любой слой (Repository, ViewModel, Composable) может вызвать
 * [Alerts.error] / [Alerts.info] / [Alerts.success] — корневой
 * [com.ileader.app.ui.components.AlertHost] подпишется и покажет.
 *
 * Why SharedFlow с replay=0 и BufferOverflow.DROP_OLDEST:
 *  - replay=0: новый подписчик не получит старые алерты (логично — они уже неактуальны)
 *  - extraBufferCapacity=8: если много алертов придёт одновременно (например при логауте
 *    несколько фоновых запросов отвалятся), не теряем emit но и не блокируем продьюсера
 *  - DROP_OLDEST: если буфер переполнен, отбрасываем самые старые
 */
object AlertController {
    private val _alerts = MutableSharedFlow<UiAlert>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val alerts: SharedFlow<UiAlert> = _alerts.asSharedFlow()

    fun emit(alert: UiAlert) {
        _alerts.tryEmit(alert)
    }
}

/**
 * Удобный API. Вызывается из любого места:
 * ```
 * Alerts.error("Не удалось загрузить турниры")
 * Alerts.success("Профиль сохранён")
 * ```
 */
object Alerts {
    fun error(text: String) = AlertController.emit(UiAlert(text, AlertKind.ERROR))
    fun info(text: String) = AlertController.emit(UiAlert(text, AlertKind.INFO))
    fun success(text: String) = AlertController.emit(UiAlert(text, AlertKind.SUCCESS))
}
