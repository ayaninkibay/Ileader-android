package com.ileader.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.remote.dto.PlatformSettingDto
import com.ileader.app.data.repository.AdminRepository
import com.ileader.app.ui.components.BackHeader
import com.ileader.app.ui.components.DarkFormField
import com.ileader.app.ui.components.DarkTheme
import kotlinx.coroutines.launch

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun AdminSettingsScreen(onBack: () -> Unit) {
    val repo = remember { AdminRepository() }
    val scope = rememberCoroutineScope()

    var settings by remember { mutableStateOf<List<PlatformSettingDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<PlatformSettingDto?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                settings = repo.getPlatformSettings()
            } catch (e: Exception) {
                error = e.message ?: "Ошибка загрузки"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    Column(
        Modifier.fillMaxSize().background(Bg).statusBarsPadding()
    ) {
        BackHeader("Настройки платформы", onBack)

        when {
            loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
            error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(error ?: "", color = TextMuted)
            }
            settings.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Настроек нет", color = TextMuted)
            }
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(settings, key = { it.key }) { s ->
                    Surface(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { editing = s },
                        shape = RoundedCornerShape(12.dp),
                        color = CardBg
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(s.key, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            s.description?.let {
                                Text(it, fontSize = 11.sp, color = TextMuted)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                s.value?.toString() ?: "—",
                                fontSize = 12.sp, color = Accent
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

    editing?.let { setting ->
        EditSettingDialog(
            setting = setting,
            onDismiss = { editing = null },
            onSave = { newValue ->
                scope.launch {
                    try {
                        repo.updatePlatformSettingString(setting.key, newValue)
                        editing = null
                        load()
                    } catch (_: Exception) {
                        editing = null
                    }
                }
            }
        )
    }
}

@Composable
private fun EditSettingDialog(
    setting: PlatformSettingDto,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    val initial = setting.value?.toString()?.trim('"') ?: ""
    var value by remember(setting.key) { mutableStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text(setting.key, color = TextPrimary, fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                setting.description?.let {
                    Text(it, fontSize = 12.sp, color = TextMuted)
                    Spacer(Modifier.height(8.dp))
                }
                DarkFormField(
                    label = "Значение",
                    value = value,
                    onValueChange = { value = it },
                    placeholder = ""
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(value) }) { Text("Сохранить", color = Accent) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = TextMuted) }
        }
    )
}
