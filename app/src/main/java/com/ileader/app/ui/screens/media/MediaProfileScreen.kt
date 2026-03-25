package com.ileader.app.ui.screens.media

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.ileader.app.ui.viewmodels.MediaProfileData
import com.ileader.app.ui.viewmodels.MediaProfileViewModel

@Composable
fun MediaProfileScreen(
    user: User,
    onSignOut: () -> Unit
) {
    val viewModel: MediaProfileViewModel = viewModel()
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
    data: MediaProfileData,
    viewModel: MediaProfileViewModel,
    onSignOut: () -> Unit
) {
    val profile = data.profile

    val avatarVM: AvatarViewModel = viewModel()
    val isUploading by avatarVM.isUploading.collectAsState()
    val uploadedUrl by avatarVM.uploadedUrl.collectAsState()

    LaunchedEffect(uploadedUrl) {
        if (uploadedUrl != null) {
            viewModel.load(profile.id)
            avatarVM.clearUploadedUrl()
        }
    }

    var isEditing by remember { mutableStateOf(false) }
    var name by remember(profile) { mutableStateOf(profile.name) }
    var email by remember(profile) { mutableStateOf(profile.email) }
    var phone by remember(profile) { mutableStateOf(profile.phone ?: data.mediaProfile.phone) }
    var city by remember(profile) { mutableStateOf(profile.city ?: "Алматы") }
    var mediaName by remember { mutableStateOf(data.mediaProfile.mediaName) }
    var mediaType by remember { mutableStateOf(data.mediaProfile.mediaType) }
    var website by remember { mutableStateOf(data.mediaProfile.website) }
    var description by remember { mutableStateOf(data.mediaProfile.description) }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── HEADER ──
            FadeIn(visible, 0) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text("Профиль СМИ", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                            color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                        Spacer(Modifier.height(4.dp))
                        Text(profile.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                    }
                    if (!isEditing) {
                        Button(
                            onClick = { isEditing = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Редактировать", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Edit / Save buttons
            if (isEditing) {
                Spacer(Modifier.height(12.dp))
                FadeIn(visible, 0) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                viewModel.updateProfile(name, phone, city, description)
                                isEditing = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Сохранить", fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = {
                                name = profile.name
                                email = profile.email
                                phone = profile.phone ?: data.mediaProfile.phone
                                city = profile.city ?: "Алматы"
                                mediaName = data.mediaProfile.mediaName
                                mediaType = data.mediaProfile.mediaType
                                website = data.mediaProfile.website
                                description = data.mediaProfile.description
                                isEditing = false
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTheme.TextSecondary),
                            border = ButtonDefaults.outlinedButtonBorder(true).copy(
                                brush = Brush.linearGradient(listOf(DarkTheme.CardBorder, DarkTheme.CardBorder))
                            )
                        ) {
                            Text("Отменить", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── PROFILE CARD ──
            FadeIn(visible, 200) {
                DarkCardPadded {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        EditableAvatar(
                            avatarUrl = profile.avatarUrl,
                            displayName = mediaName,
                            size = 72.dp,
                            isUploading = isUploading,
                            onImageSelected = { bytes -> avatarVM.uploadAvatar(profile.id, bytes) }
                        )

                        Column(Modifier.weight(1f)) {
                            if (isEditing) {
                                ProfileEditField(value = mediaName,
                                    onValueChange = { mediaName = it },
                                    placeholder = "Название СМИ")
                                Spacer(Modifier.height(8.dp))
                                ProfileEditField(value = mediaType,
                                    onValueChange = { mediaType = it },
                                    placeholder = "Тип СМИ")
                            } else {
                                Text(mediaName, fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary)
                                Text(mediaType, fontSize = 14.sp, color = DarkTheme.TextSecondary,
                                    modifier = Modifier.padding(top = 2.dp))
                                Text(name, fontSize = 13.sp, color = DarkTheme.TextMuted,
                                    modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text("О нас", fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp, color = DarkTheme.TextPrimary)
                    Spacer(Modifier.height(6.dp))
                    if (isEditing) {
                        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DarkTheme.CardBg) {
                            BasicTextField(
                                value = description,
                                onValueChange = { description = it },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp)
                                    .border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                textStyle = TextStyle(fontSize = 14.sp, color = DarkTheme.TextPrimary),
                                cursorBrush = SolidColor(DarkTheme.Accent)
                            )
                        }
                    } else {
                        Text(description, fontSize = 13.sp, color = DarkTheme.TextSecondary, lineHeight = 20.sp)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Contact info
                    ContactInfoItem(Icons.Default.Language, "Веб-сайт", website, isEditing) { website = it }
                    Spacer(Modifier.height(8.dp))
                    ContactInfoItem(Icons.Default.Email, "Email", email, false) {}
                    Spacer(Modifier.height(8.dp))
                    ContactInfoItem(Icons.Default.Phone, "Телефон", phone, isEditing) { phone = it }
                    Spacer(Modifier.height(8.dp))
                    ContactInfoItem(Icons.Default.LocationOn, "Город", city, isEditing) { city = it }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── COVERAGE AREAS (mock — no DB table) ──
            FadeIn(visible, 350) {
                Text("Области освещения", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)

                Spacer(Modifier.height(12.dp))

                DarkCardPadded {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        data.coverageAreas.forEach { area ->
                            StatusBadge(area, DarkTheme.Accent)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── STATS ──
            FadeIn(visible, 500) {
                Text("Статистика", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatItem(Modifier.weight(1f), Icons.Default.Badge,
                        "${data.accreditationStats.total}", "Аккредитаций")
                    StatItem(Modifier.weight(1f), Icons.Default.Description,
                        "${data.publishedArticles}", "Статей")
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatItem(Modifier.weight(1f), Icons.Default.Visibility,
                        "${data.totalViews}", "Просмотров")
                    StatItem(Modifier.weight(1f), Icons.Default.North,
                        "+18%", "Рост")
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── ACHIEVEMENTS (mock — no DB table) ──
            FadeIn(visible, 650) {
                Text("Награды и достижения", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)

                Spacer(Modifier.height(12.dp))

                DarkCardPadded {
                    data.achievements.forEach { achievement ->
                        Row(
                            Modifier.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                Modifier.size(32.dp).clip(CircleShape)
                                    .background(DarkTheme.AccentSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.EmojiEvents, null,
                                    tint = DarkTheme.Accent, modifier = Modifier.size(16.dp))
                            }
                            Text(achievement, fontSize = 13.sp, color = DarkTheme.TextSecondary,
                                lineHeight = 18.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            ThemeSwitcherCard()

            Spacer(Modifier.height(12.dp))

            // ── SIGN OUT ──
            FadeIn(visible, 800) {
                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkTheme.Accent),
                    border = ButtonDefaults.outlinedButtonBorder(true).copy(
                        brush = Brush.linearGradient(listOf(DarkTheme.Accent.copy(alpha = 0.5f), DarkTheme.Accent.copy(alpha = 0.2f)))
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Выйти из аккаунта", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileEditField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DarkTheme.CardBg) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
                .border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            textStyle = TextStyle(fontSize = 14.sp, color = DarkTheme.TextPrimary),
            singleLine = true,
            cursorBrush = SolidColor(DarkTheme.Accent),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(placeholder, fontSize = 14.sp, color = DarkTheme.TextMuted)
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun ContactInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit
) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = DarkTheme.CardBg) {
        Row(
            Modifier.border(0.5.dp, DarkTheme.CardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SoftIconBox(icon, size = 36.dp, iconSize = 18.dp)
            Column(Modifier.weight(1f)) {
                Text(label, fontSize = 12.sp, color = DarkTheme.TextMuted)
                if (isEditing) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        textStyle = TextStyle(fontSize = 13.sp, color = DarkTheme.TextPrimary),
                        singleLine = true,
                        cursorBrush = SolidColor(DarkTheme.Accent)
                    )
                } else {
                    Text(
                        value.ifEmpty { "Не указан" }, fontSize = 13.sp,
                        color = if (value.isEmpty()) DarkTheme.TextMuted else DarkTheme.TextPrimary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
