package com.ileader.app.ui.screens.organizer

import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ProfileDto
import com.ileader.app.data.remote.dto.ProfileUpdateDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.viewmodels.AvatarViewModel
import com.ileader.app.ui.viewmodels.OrganizerProfileViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentDark: Color @Composable get() = DarkTheme.AccentDark

@Composable
fun OrganizerProfileScreen(
    user: User,
    onSignOut: () -> Unit
) {
    val vm: OrganizerProfileViewModel = viewModel()
    val profileState by vm.state.collectAsState()
    val saveState by vm.saveState.collectAsState()

    LaunchedEffect(user.id) { vm.load(user.id) }

    when (val s = profileState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { vm.load(user.id) }
        is UiState.Success -> ProfileContent(
            user = user,
            profile = s.data,
            vm = vm,
            saveState = saveState,
            onSignOut = onSignOut
        )
    }
}

@Composable
private fun ProfileContent(
    user: User,
    profile: ProfileDto,
    vm: OrganizerProfileViewModel,
    saveState: UiState<Boolean>?,
    onSignOut: () -> Unit
) {
    val avatarVM: AvatarViewModel = viewModel()
    val isUploading by avatarVM.isUploading.collectAsState()
    val uploadedUrl by avatarVM.uploadedUrl.collectAsState()

    LaunchedEffect(uploadedUrl) {
        if (uploadedUrl != null) {
            vm.load(user.id)
            avatarVM.clearUploadedUrl()
        }
    }

    var isEditing by remember { mutableStateOf(false) }
    var name by remember(profile) { mutableStateOf(profile.name ?: user.name) }
    var phone by remember(profile) { mutableStateOf(profile.phone ?: "") }
    var bio by remember(profile) { mutableStateOf(profile.bio ?: "") }
    var city by remember(profile) { mutableStateOf(profile.city ?: user.city ?: "Алматы") }

    // Handle save success
    LaunchedEffect(saveState) {
        if (saveState is UiState.Success) {
            isEditing = false
        }
    }

    val isSaving = saveState is UiState.Loading

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Format createdAt date for display
    val memberSince = profile.createdAt?.take(10) ?: ""

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Header
            FadeIn(visible, 0) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Профиль", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.5).sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Настройки аккаунта", fontSize = 14.sp, color = TextSecondary)
                    }
                    if (isEditing) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    isEditing = false
                                    name = profile.name ?: user.name
                                    phone = profile.phone ?: ""
                                    bio = profile.bio ?: ""
                                    city = profile.city ?: user.city ?: "Алматы"
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = DarkTheme.cardBorderStroke
                            ) {
                                Text("Отмена", fontSize = 13.sp, color = TextSecondary)
                            }
                            Button(
                                onClick = {
                                    vm.updateProfile(
                                        user.id,
                                        ProfileUpdateDto(
                                            name = name,
                                            phone = phone.ifEmpty { null },
                                            city = city.ifEmpty { null },
                                            bio = bio.ifEmpty { null }
                                        )
                                    )
                                },
                                enabled = !isSaving,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Accent)
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Сохранить", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { isEditing = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Accent),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Редактировать", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Avatar + Name card
            FadeIn(visible, 200) {
                DarkCard {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            EditableAvatar(
                                avatarUrl = profile.avatarUrl,
                                displayName = profile.name ?: user.displayName,
                                size = 72.dp,
                                isUploading = isUploading,
                                onImageSelected = { bytes -> avatarVM.uploadAvatar(user.id, bytes) }
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                if (isEditing) {
                                    ProfileDarkEditField("ФИО", name, "Введите ФИО") { name = it }
                                } else {
                                    Text(name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                                Spacer(Modifier.height(8.dp))
                                StatusBadge("Организатор", ILeaderColors.OrganizerColor)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Statistics card
            FadeIn(visible, 350) {
                SectionHeader("Статистика")
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatItem(Modifier.weight(1f), Icons.Default.CalendarMonth, memberSince, "В системе с")
                        StatItem(Modifier.weight(1f), Icons.Default.LocationOn, city, "Город")
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Contact info card
            FadeIn(visible, 500) {
                SectionHeader("Контактная информация")
                Spacer(Modifier.height(10.dp))
                DarkCard {
                    Column(Modifier.padding(20.dp)) {
                        ProfileInfoRow(Icons.Default.Email, "Email", profile.email ?: user.email)
                        Spacer(Modifier.height(14.dp))

                        if (isEditing) {
                            ProfileDarkEditFieldWithIcon(Icons.Default.Phone, "Телефон", phone, "+7 (___) ___-__-__") { phone = it }
                        } else {
                            ProfileInfoRow(Icons.Default.Phone, "Телефон", phone.ifEmpty { "Не указан" })
                        }
                        Spacer(Modifier.height(14.dp))

                        if (isEditing) {
                            ProfileDarkEditFieldWithIcon(Icons.Default.LocationOn, "Город", city, "Город") { city = it }
                        } else {
                            ProfileInfoRow(Icons.Default.LocationOn, "Город", city.ifEmpty { "Не указан" })
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // About card
            FadeIn(visible, 600) {
                SectionHeader("О себе")
                Spacer(Modifier.height(10.dp))
                DarkCard {
                    Column(Modifier.padding(20.dp)) {
                        if (isEditing) {
                            ProfileDarkEditField(null, bio, "Расскажите о себе", singleLine = false) { bio = it }
                        } else {
                            Text(bio.ifEmpty { "Не указано" }, fontSize = 14.sp, color = if (bio.isEmpty()) TextMuted else TextSecondary, lineHeight = 20.sp)
                        }
                    }
                }
            }

            // Save error
            if (saveState is UiState.Error) {
                Spacer(Modifier.height(8.dp))
                Text(saveState.message, color = Color.Red, fontSize = 13.sp)
            }

            Spacer(Modifier.height(28.dp))

            // Sign out
            FadeIn(visible, 700) {
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Accent.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Accent)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Accent, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Выйти из аккаунта", color = Accent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextMuted)
            Text(value, fontSize = 14.sp, color = if (value == "Не указан") TextMuted else TextPrimary)
        }
    }
}

@Composable
private fun ProfileDarkEditField(
    label: String?,
    value: String,
    placeholder: String,
    singleLine: Boolean = true,
    onValueChange: (String) -> Unit
) {
    Column {
        if (label != null) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextMuted)
            Spacer(Modifier.height(6.dp))
        }
        Surface(shape = RoundedCornerShape(12.dp), color = Bg) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = TextPrimary),
                singleLine = singleLine,
                cursorBrush = SolidColor(Accent),
                decorationBox = { inner ->
                    if (value.isEmpty()) Text(placeholder, fontSize = 14.sp, color = TextMuted)
                    inner()
                }
            )
        }
    }
}

@Composable
private fun ProfileDarkEditFieldWithIcon(
    icon: ImageVector,
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(18.dp).offset(y = 14.dp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextMuted)
            Spacer(Modifier.height(6.dp))
            Surface(shape = RoundedCornerShape(12.dp), color = Bg) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, CardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = TextPrimary),
                    singleLine = true,
                    cursorBrush = SolidColor(Accent),
                    decorationBox = { inner ->
                        if (value.isEmpty()) Text(placeholder, fontSize = 14.sp, color = TextMuted)
                        inner()
                    }
                )
            }
        }
    }
}
