package com.ileader.app.ui.screens.viewer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.ViewerProfileViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentDark: Color @Composable get() = DarkTheme.AccentDark
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun ViewerProfileScreen(
    user: User,
    onSignOut: () -> Unit
) {
    val viewModel: ViewerProfileViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    LaunchedEffect(user.id) { viewModel.load(user.id) }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Use profile from DB if loaded, otherwise fall back to user
    val displayName = when (val s = state) {
        is UiState.Success -> s.data.name ?: user.displayName
        else -> user.displayName
    }
    val displayEmail = when (val s = state) {
        is UiState.Success -> s.data.email ?: user.email
        else -> user.email
    }
    val displayCity = when (val s = state) {
        is UiState.Success -> s.data.city
        else -> user.city
    }
    val displayPhone = when (val s = state) {
        is UiState.Success -> s.data.phone
        else -> user.phone
    }

    val accentColor = Accent
    Box(Modifier.fillMaxSize()) {
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
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            FadeIn(visible, 0) {
                Box(
                    Modifier.fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Accent.copy(alpha = 0.15f), Bg)))
                        .statusBarsPadding().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        InitialsAvatar(name = displayName, gradient = listOf(Accent, AccentDark), size = 80)
                        Spacer(Modifier.height(12.dp))
                        Text(displayName, fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextPrimary, letterSpacing = (-0.5).sp)
                        Spacer(Modifier.height(4.dp))
                        Text(displayEmail, fontSize = 14.sp, color = TextSecondary)
                        Spacer(Modifier.height(4.dp))
                        StatusBadge("Зритель")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            FadeIn(visible, 200) {
                SectionCard(title = "Личные данные", modifier = Modifier.padding(horizontal = 20.dp)) {
                    ProfileInfoRow(Icons.Default.Person, "Имя", displayName)
                    Spacer(Modifier.height(8.dp))
                    ProfileInfoRow(Icons.Default.Email, "Email", displayEmail)
                    if (displayCity != null) {
                        Spacer(Modifier.height(8.dp))
                        ProfileInfoRow(Icons.Default.LocationOn, "Город", displayCity)
                    }
                    if (displayPhone != null) {
                        Spacer(Modifier.height(8.dp))
                        ProfileInfoRow(Icons.Default.Phone, "Телефон", displayPhone)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            FadeIn(visible, 400) {
                SectionCard(title = "Настройки", modifier = Modifier.padding(horizontal = 20.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SettingsItem(Icons.Default.Notifications, "Уведомления")
                        SettingsItem(Icons.Default.Language, "Язык", "Русский")
                        SettingsItem(Icons.Default.DarkMode, "Тема", "Тёмная")
                        SettingsItem(Icons.Default.Info, "О приложении", "v1.0.0")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            FadeIn(visible, 600) {
                Button(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Выйти из аккаунта", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = CardBorder.copy(alpha = 0.3f)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(AccentSoft), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(label, fontSize = 11.sp, color = TextMuted)
                Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            }
        }
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, label: String, value: String? = null) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = CardBorder.copy(alpha = 0.3f)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(AccentSoft), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(label, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
            if (value != null) {
                Text(value, fontSize = 13.sp, color = TextSecondary)
                Spacer(Modifier.width(4.dp))
            }
            Icon(Icons.Default.ChevronRight, null, tint = TextMuted.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
        }
    }
}
