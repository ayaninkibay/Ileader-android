package com.ileader.app.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.AdminSettingsViewModel

@Composable
fun AdminSettingsScreen(user: User) {
    val viewModel: AdminSettingsViewModel = viewModel()
    val vmSettings by viewModel.settings.collectAsState()
    val showSaved by viewModel.showSaved.collectAsState()
    var settings by remember(vmSettings) { mutableStateOf(vmSettings) }
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Настройки", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = TextPrimary, letterSpacing = (-0.3).sp)
            Button(
                onClick = { viewModel.updateSettings(settings); viewModel.save() },
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Сохранить")
            }
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showSaved) {
                SuccessBanner("Настройки сохранены")
                LaunchedEffect(showSaved) {
                    kotlinx.coroutines.delay(2000)
                    viewModel.dismissSaved()
                }
            }

            FadeIn(visible = started, delayMs = 0) {
                SettingsSection(icon = Icons.Default.Public, title = "Общие настройки") {
                    DarkFormField("Название платформы", settings.platformName,
                        { settings = settings.copy(platformName = it) })
                    Spacer(Modifier.height(12.dp))
                    DarkFormField("Описание платформы", settings.platformDescription,
                        { settings = settings.copy(platformDescription = it) }, singleLine = false)
                    Spacer(Modifier.height(12.dp))
                    DarkFormField("Email поддержки", settings.supportEmail,
                        { settings = settings.copy(supportEmail = it) })
                    Spacer(Modifier.height(12.dp))
                    DarkFormField("Телефон поддержки", settings.supportPhone,
                        { settings = settings.copy(supportPhone = it) })
                }
            }

            FadeIn(visible = started, delayMs = 150) {
                SettingsSection(icon = Icons.Default.PersonAdd, title = "Настройки регистрации") {
                    SettingsToggle("Разрешить самостоятельную регистрацию", settings.selfRegistration,
                        { settings = settings.copy(selfRegistration = it) })
                    SettingsToggle("Требовать подтверждение email", settings.emailConfirmation,
                        { settings = settings.copy(emailConfirmation = it) })
                    Spacer(Modifier.height(8.dp))
                    Text("Доступные роли при регистрации", fontWeight = FontWeight.Medium,
                        fontSize = 14.sp, color = TextPrimary)
                    Spacer(Modifier.height(4.dp))
                    val roleLabels = mapOf(
                        "athlete" to "Спортсмен", "trainer" to "Тренер",
                        "organizer" to "Организатор", "referee" to "Судья",
                        "sponsor" to "Спонсор", "media" to "СМИ"
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        roleLabels.forEach { (key, label) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = settings.availableRoles[key] == true,
                                    onCheckedChange = { checked ->
                                        settings = settings.copy(
                                            availableRoles = settings.availableRoles.toMutableMap().apply {
                                                this[key] = checked
                                            }
                                        )
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Accent,
                                        uncheckedColor = TextMuted,
                                        checkmarkColor = Color.White
                                    )
                                )
                                Text(label, fontSize = 14.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }

            FadeIn(visible = started, delayMs = 300) {
                SettingsSection(icon = Icons.Default.EmojiEvents, title = "Настройки турниров") {
                    DarkFormField("Макс. участников по умолчанию", settings.defaultMaxParticipants.toString(),
                        { settings = settings.copy(defaultMaxParticipants = it.toIntOrNull() ?: 32) })
                    Spacer(Modifier.height(12.dp))
                    DarkFormField("Мин. рейтинг для участия", settings.minRatingForParticipation.toString(),
                        { settings = settings.copy(minRatingForParticipation = it.toIntOrNull() ?: 0) })
                    Spacer(Modifier.height(8.dp))
                    SettingsToggle("Автоматическая публикация результатов", settings.autoPublishResults,
                        { settings = settings.copy(autoPublishResults = it) })
                }
            }

            FadeIn(visible = started, delayMs = 450) {
                SettingsSection(icon = Icons.Default.Notifications, title = "Уведомления") {
                    SettingsToggle("Email-уведомления", settings.emailNotifications,
                        { settings = settings.copy(emailNotifications = it) })
                    SettingsToggle("Push-уведомления", settings.pushNotifications,
                        { settings = settings.copy(pushNotifications = it) })
                    SettingsToggle("Новые пользователи", settings.notifyNewUsers,
                        { settings = settings.copy(notifyNewUsers = it) })
                    SettingsToggle("Новые турниры", settings.notifyNewTournaments,
                        { settings = settings.copy(notifyNewTournaments = it) })
                }
            }

            FadeIn(visible = started, delayMs = 600) {
                Button(
                    onClick = { viewModel.updateSettings(settings); viewModel.save() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text("Сохранить настройки", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    DarkCard {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(AccentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Accent, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun SettingsToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Accent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = CardBorder
            )
        )
    }
}
