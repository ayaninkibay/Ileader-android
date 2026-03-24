package com.ileader.app.ui.screens.referee

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.viewmodels.AvatarViewModel
import com.ileader.app.ui.viewmodels.RefereeProfileViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentDark: Color @Composable get() = DarkTheme.AccentDark
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun RefereeProfileScreen(
    user: User,
    onSignOut: () -> Unit
) {
    val viewModel: RefereeProfileViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> ProfileContent(s.data, viewModel, onSignOut)
    }
}

@Composable
private fun ProfileContent(
    data: com.ileader.app.ui.viewmodels.RefereeProfileData,
    viewModel: RefereeProfileViewModel,
    onSignOut: () -> Unit
) {
    val profile = data.profile
    val sports = data.sports

    val avatarVM: AvatarViewModel = viewModel()
    val isUploading by avatarVM.isUploading.collectAsState()
    val uploadedUrl by avatarVM.uploadedUrl.collectAsState()

    LaunchedEffect(uploadedUrl) {
        if (uploadedUrl != null) {
            viewModel.load(data.profile.id)
            avatarVM.clearUploadedUrl()
        }
    }

    var isEditing by remember { mutableStateOf(false) }
    var name by remember(profile) { mutableStateOf(profile.name) }
    var phone by remember(profile) { mutableStateOf(profile.phone ?: "") }
    var city by remember(profile) { mutableStateOf(profile.city ?: "") }
    var bio by remember(profile) { mutableStateOf(profile.bio ?: "") }

    var showPasswordChange by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val sportsText = if (sports.isNotEmpty()) {
        sports.joinToString(", ") { it.first }
    } else {
        "Не указаны"
    }

    val accentColor = Accent
    Box(Modifier.fillMaxSize().background(Bg)) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(accentColor.copy(alpha = 0.06f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.03f),
                    radius = 280.dp.toPx()
                ),
                radius = 280.dp.toPx(),
                center = Offset(size.width * 0.85f, size.height * 0.03f)
            )
        }

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── HEADER ──
            FadeIn(visible, 0) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text("Профиль судьи", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                            color = TextPrimary, letterSpacing = (-0.5).sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Личная информация", fontSize = 14.sp, color = TextSecondary)
                    }
                    if (isEditing) {
                        Button(
                            onClick = {
                                viewModel.updateProfile(name, phone, city, bio)
                                isEditing = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Accent),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Save, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Сохранить", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { isEditing = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Редактировать", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── PROFILE CARD ──
            FadeIn(visible, 200) {
                DarkCardPadded(padding = 20.dp) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        // Avatar
                        EditableAvatar(
                            avatarUrl = profile.avatarUrl,
                            displayName = name,
                            size = 96.dp,
                            isUploading = isUploading,
                            onImageSelected = { bytes -> avatarVM.uploadAvatar(profile.id, bytes) }
                        )

                        Spacer(Modifier.height(12.dp))

                        if (isEditing) {
                            DarkInput(value = name, onValueChange = { name = it }, placeholder = "Имя")
                        } else {
                            Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProfileInfoRow(Icons.Default.Email, "Email", profile.email)
                        if (isEditing) {
                            DarkInput(value = phone, onValueChange = { phone = it }, placeholder = "Телефон", icon = Icons.Default.Phone)
                            DarkInput(value = city, onValueChange = { city = it }, placeholder = "Город", icon = Icons.Default.LocationOn)
                        } else {
                            ProfileInfoRow(Icons.Default.Phone, "Телефон", phone.ifEmpty { "Не указан" })
                            ProfileInfoRow(Icons.Default.LocationOn, "Город", city.ifEmpty { "Не указан" })
                        }
                        ProfileInfoRow(Icons.Default.DateRange, "На платформе с", profile.createdAt?.take(10) ?: "")
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("О себе", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary,
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    if (isEditing) {
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                            color = CardBorder.copy(alpha = 0.4f)) {
                            BasicTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp)
                                    .border(0.5.dp, CardBorder, RoundedCornerShape(10.dp)).padding(12.dp),
                                textStyle = TextStyle(fontSize = 14.sp, color = TextPrimary),
                                cursorBrush = SolidColor(Accent),
                                maxLines = 4,
                                decorationBox = { inner ->
                                    if (bio.isEmpty()) Text("Расскажите о себе...", fontSize = 14.sp, color = TextMuted)
                                    inner()
                                }
                            )
                        }
                    } else {
                        Text(bio.ifEmpty { "Не указано" }, fontSize = 14.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── ROLE BADGE ──
            FadeIn(visible, 300) {
                DarkCardPadded(padding = 20.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SoftIconBox(Icons.Default.Gavel, size = 44.dp, iconSize = 22.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Роль: ", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Surface(shape = RoundedCornerShape(6.dp), color = ILeaderColors.RefereeColor.copy(alpha = 0.10f)) {
                                    Text("Судья", Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ILeaderColors.RefereeColor)
                                }
                            }
                            Text(sportsText, fontSize = 13.sp, color = TextSecondary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── PASSWORD ──
            FadeIn(visible, 400) {
                DarkCardPadded(padding = 20.dp) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SoftIconBox(Icons.Default.Lock, size = 36.dp, iconSize = 18.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Сменить пароль", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                        }
                        IconButton(onClick = { showPasswordChange = !showPasswordChange }) {
                            Icon(if (showPasswordChange) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                null, tint = TextSecondary)
                        }
                    }

                    if (showPasswordChange) {
                        Spacer(Modifier.height(12.dp))
                        PasswordInput(currentPassword, { currentPassword = it }, "Текущий пароль")
                        Spacer(Modifier.height(8.dp))
                        PasswordInput(newPassword, { newPassword = it }, "Новый пароль")
                        Spacer(Modifier.height(8.dp))
                        PasswordInput(confirmPassword, { confirmPassword = it }, "Подтвердите пароль")
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                showPasswordChange = false
                                currentPassword = ""; newPassword = ""; confirmPassword = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Accent),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text("Обновить пароль", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── SIGN OUT ──
            FadeIn(visible, 500) {
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Accent),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Выйти из аккаунта", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        SoftIconBox(icon, size = 32.dp, iconSize = 16.dp)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextMuted)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}

@Composable
private fun DarkInput(value: String, onValueChange: (String) -> Unit, placeholder: String, icon: ImageVector? = null) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = CardBorder.copy(alpha = 0.4f)) {
        Row(
            Modifier.border(0.5.dp, CardBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, null, Modifier.size(18.dp), TextMuted)
                Spacer(Modifier.width(10.dp))
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 14.sp, color = TextPrimary),
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

@Composable
private fun PasswordInput(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = CardBorder.copy(alpha = 0.4f)) {
        Row(
            Modifier.border(0.5.dp, CardBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lock, null, Modifier.size(18.dp), TextMuted)
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 14.sp, color = TextPrimary),
                singleLine = true,
                cursorBrush = SolidColor(Accent),
                visualTransformation = PasswordVisualTransformation(),
                decorationBox = { inner ->
                    if (value.isEmpty()) Text(placeholder, fontSize = 14.sp, color = TextMuted)
                    inner()
                }
            )
        }
    }
}
