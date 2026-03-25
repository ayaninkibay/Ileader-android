package com.ileader.app.ui.screens.athlete

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.License
import com.ileader.app.data.models.User
import com.ileader.app.data.repository.AthleteRepository
import com.ileader.app.ui.components.*

@Composable
fun AthleteRacingLicenseScreen(
    user: User,
    onBack: () -> Unit
) {
    val repo = remember { AthleteRepository() }
    var license by remember { mutableStateOf<License?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user.id) {
        isLoading = true
        errorMessage = null
        try {
            license = repo.getLicense(user.id)
        } catch (e: Exception) {
            errorMessage = "Не удалось загрузить лицензию"
        } finally {
            isLoading = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ── BACK + TITLE ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    onClick = onBack, shape = CircleShape, color = DarkTheme.CardBg,
                    modifier = Modifier.size(40.dp).border(0.5.dp, DarkTheme.CardBorder, CircleShape)
                ) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", Modifier.size(20.dp), DarkTheme.TextPrimary)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text("Гоночная лицензия", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    color = DarkTheme.TextPrimary, letterSpacing = (-0.5).sp)
            }

            Spacer(Modifier.height(20.dp))

            when {
                isLoading -> {
                    Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), Alignment.Center) {
                        CircularProgressIndicator(color = DarkTheme.Accent)
                    }
                }
                errorMessage != null -> {
                    DarkCardPadded {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ErrorOutline, null, Modifier.size(48.dp), DarkTheme.TextMuted)
                            Spacer(Modifier.height(12.dp))
                            Text(errorMessage!!, fontSize = 15.sp, color = DarkTheme.TextSecondary, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    isLoading = true
                                    errorMessage = null
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)
                            ) {
                                Text("Повторить", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    // Re-trigger load when retry is pressed
                    if (isLoading) {
                        LaunchedEffect(Unit) {
                            try {
                                license = repo.getLicense(user.id)
                                errorMessage = null
                            } catch (e: Exception) {
                                errorMessage = "Не удалось загрузить лицензию"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                }
                license == null -> {
                    DarkCardPadded {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier.size(56.dp).clip(CircleShape).background(DarkTheme.CardBorder.copy(alpha = 0.3f)),
                                Alignment.Center
                            ) {
                                Icon(Icons.Default.Badge, null, Modifier.size(28.dp), DarkTheme.TextMuted)
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("Лицензия не найдена", fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text("У вас пока нет зарегистрированной лицензии", fontSize = 14.sp,
                                color = DarkTheme.TextMuted, textAlign = TextAlign.Center)
                        }
                    }
                }
                else -> {
                    val lic = license!!
                    LicenseContent(lic)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LicenseContent(license: License) {
    // ── LICENSE CARD ──
    Surface(
        Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), Color.Transparent
    ) {
        Box(
            Modifier.fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(listOf(DarkTheme.CardBg, DarkTheme.BgSecondary)),
                    shape = RoundedCornerShape(20.dp)
                )
                .border(0.5.dp, DarkTheme.CardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text("СПОРТИВНАЯ ЛИЦЕНЗИЯ", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f), letterSpacing = 2.sp)
                        Text(license.number.ifEmpty { "—" }, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    LicenseStatusBadge(license.status)
                }

                Spacer(Modifier.height(20.dp))

                Row(Modifier.fillMaxWidth()) {
                    LicenseField("Категория", license.category.ifEmpty { "—" }, Modifier.weight(1f))
                    LicenseField("Класс", license.className.ifEmpty { "—" }, Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth()) {
                    LicenseField("Выдана", formatDate(license.issueDate), Modifier.weight(1f))
                    LicenseField("Действует до", formatDate(license.expiryDate), Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                LicenseField("Федерация", license.federation.ifEmpty { "—" }, Modifier.fillMaxWidth())
            }
        }
    }

    Spacer(Modifier.height(20.dp))

    // ── MEDICAL CLEARANCE ──
    DarkCardPadded {
        Text("Медицинский допуск", fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
            color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)
        Spacer(Modifier.height(12.dp))

        val isMedicalValid = license.medicalCheckExpiry.isNotEmpty() && license.status != "expired"

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(
                    if (isMedicalValid) DarkTheme.AccentSoft else DarkTheme.TextMuted.copy(alpha = 0.15f)
                ),
                Alignment.Center
            ) {
                Icon(Icons.Default.HealthAndSafety, null, Modifier.size(24.dp),
                    if (isMedicalValid) DarkTheme.Accent else DarkTheme.TextMuted)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Медицинская справка", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkTheme.TextPrimary)
                Text(
                    if (isMedicalValid) "Действительна" else "Нет данных",
                    fontSize = 12.sp,
                    color = if (isMedicalValid) DarkTheme.Accent else DarkTheme.TextMuted
                )
            }
            if (isMedicalValid) {
                Icon(Icons.Default.CheckCircle, null, Modifier.size(24.dp), DarkTheme.Accent)
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(Modifier.fillMaxWidth()) {
            InfoItem("Дата осмотра", formatDate(license.medicalCheckDate), Modifier.weight(1f))
            InfoItem("Действует до", formatDate(license.medicalCheckExpiry), Modifier.weight(1f))
        }
    }

    Spacer(Modifier.height(12.dp))

    // ── CERTIFICATES ──
    DarkCardPadded {
        Text("Сертификаты", fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
            color = DarkTheme.TextPrimary, letterSpacing = (-0.3).sp)

        Spacer(Modifier.height(16.dp))

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(52.dp).clip(CircleShape).background(DarkTheme.CardBorder.copy(alpha = 0.3f)),
                Alignment.Center
            ) {
                Icon(Icons.Default.WorkspacePremium, null, Modifier.size(24.dp), DarkTheme.TextMuted)
            }
            Spacer(Modifier.height(12.dp))
            Text("Нет дополнительных сертификатов", fontSize = 14.sp,
                fontWeight = FontWeight.Medium, color = DarkTheme.TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text("Загрузите свои сертификаты и документы", fontSize = 13.sp,
                color = DarkTheme.TextMuted, textAlign = TextAlign.Center)
        }
    }

    Spacer(Modifier.height(16.dp))

    // ── UPLOAD BUTTON ──
    Button(
        onClick = { },
        Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DarkTheme.Accent)
    ) {
        Icon(Icons.Default.Upload, null, Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("Загрузить документ", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Helpers ──

private fun formatDate(isoDate: String): String {
    if (isoDate.isBlank()) return "—"
    return try {
        // ISO date "2026-03-15" or "2026-03-15T00:00:00" → "15.03.2026"
        val datePart = isoDate.take(10)
        val parts = datePart.split("-")
        if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}" else isoDate
    } catch (_: Exception) {
        isoDate
    }
}

// ── Screen-specific composables ──

@Composable
private fun LicenseStatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status) {
        "active" -> Triple(DarkTheme.Accent.copy(alpha = 0.2f), DarkTheme.Accent, "Активна")
        "expired" -> Triple(DarkTheme.TextMuted.copy(alpha = 0.2f), DarkTheme.TextMuted, "Истекла")
        "suspended" -> Triple(Color(0xFFFF9800).copy(alpha = 0.2f), Color(0xFFFF9800), "Приостановлена")
        "pending" -> Triple(DarkTheme.Accent.copy(alpha = 0.15f), DarkTheme.Accent, "На рассмотрении")
        else -> Triple(DarkTheme.TextMuted.copy(alpha = 0.2f), DarkTheme.TextMuted, status.ifEmpty { "—" })
    }
    Surface(shape = RoundedCornerShape(8.dp), color = bgColor) {
        Text(label, Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

@Composable
private fun LicenseField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
    }
}

@Composable
private fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, fontSize = 12.sp, color = DarkTheme.TextMuted)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = DarkTheme.TextPrimary)
    }
}
