package com.ileader.app.ui.screens.trainer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.AvatarViewModel
import com.ileader.app.ui.viewmodels.TrainerProfileViewModel

@Composable
fun TrainerProfileScreen(
    user: User,
    onSignOut: () -> Unit
) {
    val viewModel: TrainerProfileViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    var showNotifications by remember { mutableStateOf(false) }

    if (showNotifications) {
        TrainerNotificationsScreen(user = user, onBack = { showNotifications = false })
        return
    }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> {
            val data = s.data
            val profile = data.profile
            val pendingCount = data.pendingNotificationsCount

            val avatarVM: AvatarViewModel = viewModel()
            val isUploading by avatarVM.isUploading.collectAsState()
            val uploadedUrl by avatarVM.uploadedUrl.collectAsState()

            LaunchedEffect(uploadedUrl) {
                if (uploadedUrl != null) {
                    viewModel.load(user.id)
                    avatarVM.clearUploadedUrl()
                }
            }

            var isEditing by remember { mutableStateOf(false) }
            var editName by remember { mutableStateOf(profile.name) }
            var editPhone by remember { mutableStateOf(profile.phone ?: "") }
            var editCity by remember { mutableStateOf(profile.city ?: "") }
            var editBio by remember { mutableStateOf(profile.bio ?: "") }

            var started by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { started = true }

            Box(Modifier.fillMaxSize()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        Spacer(Modifier.height(16.dp))

                        // ── HEADER ──
                        FadeIn(visible = started, delayMs = 0) {
                            Column {
                                Text(
                                    profile.displayName,
                                    fontSize = 14.sp,
                                    color = DarkTheme.TextMuted
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Профиль",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DarkTheme.TextPrimary,
                                    letterSpacing = (-0.8).sp
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // ── QUICK LINKS ──
                        FadeIn(visible = started, delayMs = 200) {
                            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                                QuickLink(Modifier.weight(1f), Icons.Default.Notifications, "Уведомления", pendingCount) { showNotifications = true }
                                QuickLink(Modifier.weight(1f), Icons.Default.Verified, "Верификация") { }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // ── PROFILE INFO ──
                        FadeIn(visible = started, delayMs = 350) {
                            DarkCard {
                                Column(Modifier.padding(16.dp)) {
                                    Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                        Text("Личная информация", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary, modifier = Modifier.weight(1f).padding(end = 12.dp))
                                        Button(
                                            onClick = {
                                                if (isEditing) {
                                                    viewModel.updateProfile(user.id, editName, editPhone, editCity, editBio)
                                                }
                                                isEditing = !isEditing
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(if (isEditing) Icons.Default.Check else Icons.Default.Edit, null, Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(if (isEditing) "Сохранить" else "Редактировать", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    if (isEditing) {
                                        ProfileEditField("Имя", editName) { editName = it }
                                        Spacer(Modifier.height(8.dp))
                                        ProfileEditField("О себе", editBio) { editBio = it }
                                        Spacer(Modifier.height(8.dp))
                                        ProfileEditField("Телефон", editPhone) { editPhone = it }
                                        Spacer(Modifier.height(8.dp))
                                        ProfileEditField("Город", editCity) { editCity = it }
                                        Spacer(Modifier.height(12.dp))
                                        OutlinedButton(
                                            onClick = { isEditing = false; editName = profile.name; editPhone = profile.phone ?: ""; editCity = profile.city ?: ""; editBio = profile.bio ?: "" },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTheme.TextSecondary),
                                            border = ButtonDefaults.outlinedButtonBorder(true).copy(
                                                brush = Brush.linearGradient(listOf(DarkTheme.CardBorder, DarkTheme.CardBorder))
                                            )
                                        ) { Text("Отмена") }
                                    } else {
                                        ProfileRow(Icons.Default.Email, "Email", profile.email)
                                        ProfileRow(Icons.Default.Phone, "Телефон", profile.phone ?: "Не указан")
                                        ProfileRow(Icons.Default.LocationCity, "Город", "${profile.city ?: "—"}, ${profile.country ?: "—"}")
                                        ProfileRow(Icons.Default.Cake, "Дата рождения", profile.birthDate ?: "Не указана")
                                        ProfileRow(Icons.Default.CalendarMonth, "Регистрация", profile.createdAt ?: "—")
                                        ProfileRow(Icons.Default.School, "Роль", profile.role.displayName)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // ── SPORTS ──
                        FadeIn(visible = started, delayMs = 500) {
                            Column {
                                DarkCard {
                                    Column(Modifier.padding(16.dp)) {
                                        Text("Виды спорта", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                                        Spacer(Modifier.height(10.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            data.sports.forEach { (name, _) ->
                                                Surface(shape = RoundedCornerShape(10.dp), color = DarkTheme.AccentSoft) {
                                                    Text(name, Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.Accent)
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                // ── EXPERIENCE ──
                                DarkCard {
                                    Column(Modifier.padding(16.dp)) {
                                        Text("Опыт и квалификация", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                                        Spacer(Modifier.height(10.dp))
                                        Text(profile.bio ?: "Не указано", fontSize = 14.sp, color = DarkTheme.TextSecondary, lineHeight = 20.sp)
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                // ── THEME SWITCHER ──
                                ThemeSwitcherCard()

                                Spacer(Modifier.height(12.dp))

                                // ── CHANGE PASSWORD ──
                                DarkCard {
                                    Row(
                                        Modifier.clickable { }.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        SoftIconBox(Icons.Default.Lock)
                                        Spacer(Modifier.width(12.dp))
                                        Text("Сменить пароль", Modifier.weight(1f), fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium, color = DarkTheme.TextPrimary)
                                        Icon(Icons.Default.ChevronRight, null, Modifier.size(20.dp), DarkTheme.TextMuted)
                                    }
                                }

                                Spacer(Modifier.height(24.dp))

                                // ── SIGN OUT ──
                                OutlinedButton(
                                    onClick = onSignOut,
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTheme.Accent),
                                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                        brush = Brush.linearGradient(listOf(DarkTheme.Accent.copy(alpha = 0.3f), DarkTheme.Accent.copy(alpha = 0.3f)))
                                    )
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Logout, null, Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Выйти из аккаунта", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickLink(modifier: Modifier, icon: ImageVector, label: String, badge: Int = 0, onClick: () -> Unit) {
    Surface(
        onClick = onClick, modifier = modifier,
        shape = RoundedCornerShape(16.dp), color = DarkTheme.CardBg
    ) {
        Column(
            Modifier
                .border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BadgedBox(badge = { if (badge > 0) Badge(containerColor = DarkTheme.Accent) { Text(badge.toString()) } }) {
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(DarkTheme.AccentSoft),
                    Alignment.Center
                ) {
                    Icon(icon, null, Modifier.size(22.dp), DarkTheme.Accent)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
        }
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(DarkTheme.CardBorder.copy(alpha = 0.5f)),
            Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(16.dp), DarkTheme.TextMuted)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = DarkTheme.TextMuted)
            Text(value, fontSize = 14.sp, color = DarkTheme.TextPrimary)
        }
    }
}

@Composable
private fun ProfileEditField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, fontSize = 12.sp, color = DarkTheme.TextSecondary)
        Spacer(Modifier.height(4.dp))
        Surface(Modifier.fillMaxWidth(), RoundedCornerShape(10.dp), DarkTheme.CardBg) {
            Row(
                Modifier.border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) Text(label, fontSize = 14.sp, color = DarkTheme.TextMuted)
                    BasicTextField(value, onValueChange,
                        textStyle = TextStyle(fontSize = 14.sp, color = DarkTheme.TextPrimary),
                        singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
