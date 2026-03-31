package com.ileader.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ileader.app.data.DeepLinkHandler
import com.ileader.app.data.DeepLinkTarget
import com.ileader.app.data.notifications.NotificationHelper
import com.ileader.app.data.preferences.AppLanguage
import com.ileader.app.data.preferences.LanguagePreference
import com.ileader.app.data.preferences.ThemePreference
import com.ileader.app.ui.navigation.NavGraph
import com.ileader.app.ui.theme.ILeaderTheme
import com.ileader.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val _deepLinkTarget = MutableStateFlow<DeepLinkTarget?>(null)
    val deepLinkTarget: StateFlow<DeepLinkTarget?> = _deepLinkTarget

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Создаём каналы уведомлений (Android 8+)
        NotificationHelper.createNotificationChannels(this)

        // Обработка deep link при запуске
        _deepLinkTarget.value = DeepLinkHandler.parse(intent)

        val themePreference = ThemePreference(this)
        val languagePreference = LanguagePreference(this)

        setContent {
            val themeMode by themePreference.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val appLanguage by languagePreference.language.collectAsState(initial = AppLanguage.RUSSIAN)
            val deepLink by _deepLinkTarget.collectAsState()

            // Apply locale
            val locale = Locale(appLanguage.code)
            val config = resources.configuration
            if (config.locales[0] != locale) {
                Locale.setDefault(locale)
                config.setLocale(locale)
                @Suppress("DEPRECATION")
                resources.updateConfiguration(config, resources.displayMetrics)
            }

            ILeaderTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        deepLinkTarget = deepLink,
                        onDeepLinkConsumed = { _deepLinkTarget.value = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Обработка deep link когда приложение уже открыто
        _deepLinkTarget.value = DeepLinkHandler.parse(intent)
    }

    fun consumeDeepLink() {
        _deepLinkTarget.value = null
    }
}
