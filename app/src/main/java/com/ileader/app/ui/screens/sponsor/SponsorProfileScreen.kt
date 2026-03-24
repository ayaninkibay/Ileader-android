package com.ileader.app.ui.screens.sponsor

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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ProfileDto
import com.ileader.app.ui.components.DarkCardPadded
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.EditableAvatar
import com.ileader.app.ui.components.ErrorScreen
import com.ileader.app.ui.components.FadeIn
import com.ileader.app.ui.components.LoadingScreen
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.viewmodels.AvatarViewModel
import com.ileader.app.ui.viewmodels.SponsorProfileViewModel

private val SponsorBadge = ILeaderColors.SponsorColor

@Composable
fun SponsorProfileScreen(user: User, onSignOut: () -> Unit) {
    val viewModel: SponsorProfileViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(user.id) { viewModel.load(user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(user.id) }
        is UiState.Success -> ProfileContent(s.data, user, viewModel, onSignOut)
    }
}

@Composable
private fun ProfileContent(profile: ProfileDto, user: User, viewModel: SponsorProfileViewModel, onSignOut: () -> Unit) {
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
    var name by remember(profile) { mutableStateOf(profile.name ?: "") }
    var phone by remember(profile) { mutableStateOf(profile.phone ?: "") }
    var bio by remember(profile) { mutableStateOf(profile.bio ?: "Компания-спонсор спортивных мероприятий и команд.") }
    var city by remember(profile) { mutableStateOf(profile.city ?: "") }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize().background(DarkTheme.Bg)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            FadeIn(visible, 0) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text("Профиль", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                            color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
                        Spacer(Modifier.height(4.dp))
                        Text(profile.name ?: user.displayName, fontSize = 14.sp, color = DarkTheme.TextSecondary)
                    }
                    if (isEditing) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                isEditing = false
                                name = profile.name ?: ""
                                phone = profile.phone ?: ""
                                bio = profile.bio ?: "Компания-спонсор спортивных мероприятий и команд."
                                city = profile.city ?: ""
                            }, shape = RoundedCornerShape(10.dp),
                                border = ButtonDefaults.outlinedButtonBorder(true).copy(brush = SolidColor(DarkTheme.CardBorder))) {
                                Text("Отмена", fontSize = 13.sp, color = DarkTheme.TextSecondary)
                            }
                            Button(onClick = {
                                viewModel.saveProfile(user.id, name, phone, city, bio)
                                isEditing = false
                            }, shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)) {
                                Text("Сохранить", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        Button(onClick = { isEditing = true }, shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Редактировать", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            FadeIn(visible, 200) {
                DarkCardPadded(padding = 20.dp) {
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
                                ProfileDarkTextField(value = name, onValueChange = { name = it }, placeholder = "Название компании",
                                    textStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary))
                            } else {
                                Text(name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkTheme.TextPrimary)
                            }
                            Spacer(Modifier.height(6.dp))
                            Surface(shape = RoundedCornerShape(8.dp), color = SponsorBadge.copy(alpha = 0.10f)) {
                                Text("Спонсор", Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SponsorBadge)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Text("О компании", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    if (isEditing) {
                        ProfileDarkTextField(value = bio, onValueChange = { bio = it }, placeholder = "Расскажите о компании...",
                            singleLine = false, minHeight = 80.dp)
                    } else {
                        Text(bio, fontSize = 14.sp, color = DarkTheme.TextSecondary, lineHeight = 20.sp)
                    }

                    Spacer(Modifier.height(24.dp))

                    Text("Контактная информация", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                    Spacer(Modifier.height(12.dp))

                    ProfileInfoRow(Icons.Default.Email, "Email", profile.email ?: user.email)
                    Spacer(Modifier.height(12.dp))

                    if (isEditing) {
                        ProfileEditRow(Icons.Default.Phone, "Телефон", phone) { phone = it }
                    } else {
                        ProfileInfoRow(Icons.Default.Phone, "Телефон", phone.ifEmpty { "Не указан" })
                    }
                    Spacer(Modifier.height(12.dp))

                    if (isEditing) {
                        ProfileEditRow(Icons.Default.LocationOn, "Город", city) { city = it }
                    } else {
                        val cityDisplay = if (city.isNotEmpty()) {
                            val country = profile.country ?: ""
                            if (country.isNotEmpty()) "$city, $country" else city
                        } else "Не указан"
                        ProfileInfoRow(Icons.Default.LocationOn, "Город", cityDisplay)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            FadeIn(visible, 400) {
                DarkCardPadded {
                    TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = DarkTheme.Accent, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Выйти из аккаунта", color = DarkTheme.Accent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(DarkTheme.AccentSoft), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = DarkTheme.Accent, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = DarkTheme.TextMuted)
            Text(value, fontSize = 14.sp, color = if (value == "Не указан") DarkTheme.TextMuted else DarkTheme.TextPrimary)
        }
    }
}

@Composable
private fun ProfileEditRow(icon: ImageVector, label: String, value: String, onValueChange: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(DarkTheme.AccentSoft), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = DarkTheme.Accent, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = DarkTheme.TextMuted)
            Spacer(Modifier.height(4.dp))
            ProfileDarkTextField(value = value, onValueChange = onValueChange, placeholder = label)
        }
    }
}

@Composable
private fun ProfileDarkTextField(value: String, onValueChange: (String) -> Unit, placeholder: String,
                                 singleLine: Boolean = true, minHeight: Dp = 0.dp,
                                 textStyle: TextStyle = TextStyle(fontSize = 14.sp, color = DarkTheme.TextPrimary)) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = DarkTheme.CardBg) {
        Box(Modifier.border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .then(if (minHeight > 0.dp) Modifier.heightIn(min = minHeight) else Modifier)) {
            if (value.isEmpty()) Text(placeholder, fontSize = textStyle.fontSize, color = DarkTheme.TextMuted)
            BasicTextField(value = value, onValueChange = onValueChange, textStyle = textStyle,
                singleLine = singleLine, cursorBrush = SolidColor(DarkTheme.Accent), modifier = Modifier.fillMaxWidth())
        }
    }
}
