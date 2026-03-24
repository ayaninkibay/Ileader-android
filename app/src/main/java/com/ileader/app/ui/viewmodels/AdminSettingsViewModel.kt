package com.ileader.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.ileader.app.data.mock.AdminMockData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Settings ViewModel — пока на MockData.
 * Таблица platform_settings будет добавлена позже.
 */
class AdminSettingsViewModel : ViewModel() {

    private val _settings = MutableStateFlow(AdminMockData.defaultSettings)
    val settings: StateFlow<AdminMockData.PlatformSettings> = _settings

    private val _showSaved = MutableStateFlow(false)
    val showSaved: StateFlow<Boolean> = _showSaved

    fun updateSettings(settings: AdminMockData.PlatformSettings) {
        _settings.value = settings
    }

    fun save() {
        // TODO: Сохранение в БД когда таблица platform_settings будет создана
        _showSaved.value = true
    }

    fun dismissSaved() {
        _showSaved.value = false
    }
}
