package com.ileader.app.ui.screens.athlete

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CardMembership
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.License
import com.ileader.app.data.remote.dto.LicenseUpsertDto
import com.ileader.app.data.repository.AthleteRepository
import com.ileader.app.data.util.Alerts
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import kotlinx.coroutines.launch

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun RacingLicenseScreen(userId: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val repo = remember { AthleteRepository() }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var license by remember { mutableStateOf<License?>(null) }
    var editing by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    fun load() {
        scope.launch {
            loading = true; error = null
            try {
                license = repo.getLicense(userId)
                if (license == null) editing = true
            } catch (e: Exception) { error = e.message ?: "РћС€РёР±РєР° Р·Р°РіСЂСѓР·РєРё" }
            finally { loading = false }
        }
    }
    LaunchedEffect(userId) { load() }

    Scaffold(
        containerColor = Bg,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(Bg)) {
            BackHeader("Гоночная лицензия", onBack)
            when {
                loading -> LoadingScreen()
                error != null -> ErrorScreen(error!!) { load() }
                editing -> LicenseEditor(
                    initial = license,
                    onCancel = { if (license != null) editing = false else onBack() },
                    onSave = { upsert ->
                        scope.launch {
                            try {
                                repo.upsertLicense(upsert.copy(userId = userId))
                                Alerts.success("Р›РёС†РµРЅР·РёСЏ СЃРѕС…СЂР°РЅРµРЅР°")
                                snackbar.showSnackbar("РЎРѕС…СЂР°РЅРµРЅРѕ")
                                editing = false
                                load()
                            } catch (e: Exception) {
                                Alerts.error("РќРµ СѓРґР°Р»РѕСЃСЊ СЃРѕС…СЂР°РЅРёС‚СЊ Р»РёС†РµРЅР·РёСЋ")
                                snackbar.showSnackbar(e.message ?: "РћС€РёР±РєР°")
                            }
                        }
                    }
                )
                license != null -> LicenseView(license!!, onEdit = { editing = true })
                else -> Box(Modifier.fillMaxSize().padding(16.dp)) {
                    EmptyState(
                        title = "Р›РёС†РµРЅР·РёРё РЅРµС‚",
                        subtitle = "Р”РѕР±Р°РІСЊС‚Рµ РґР°РЅРЅС‹Рµ РІР°С€РµР№ Р»РёС†РµРЅР·РёРё",
                        icon = Icons.Outlined.CardMembership,
                        actionLabel = "Р”РѕР±Р°РІРёС‚СЊ",
                        onAction = { editing = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun LicenseView(l: License, onEdit: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = CardBg) {
            Column(Modifier.padding(20.dp)) {
                Text("Гоночная лицензия", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(l.number.ifBlank { "вЂ”" }, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column { Text("РљР°С‚РµРіРѕСЂРёСЏ", fontSize = 11.sp, color = TextMuted); Text(l.category.ifBlank { "вЂ”" }, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold) }
                    Column(horizontalAlignment = Alignment.End) { Text("РљР»Р°СЃСЃ", fontSize = 11.sp, color = TextMuted); Text(l.className.ifBlank { "вЂ”" }, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold) }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column { Text("Р¤РµРґРµСЂР°С†РёСЏ", fontSize = 11.sp, color = TextMuted); Text(l.federation.ifBlank { "вЂ”" }, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold) }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("РЎС‚Р°С‚СѓСЃ", fontSize = 11.sp, color = TextMuted)
                        val active = l.status == "active"
                        Surface(shape = RoundedCornerShape(50), color = (if (active) ILeaderColors.Success else ILeaderColors.Error).copy(0.12f)) {
                            Text(if (active) "РђРєС‚РёРІРЅР°" else (l.status.ifBlank { "вЂ”" }),
                                Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                color = if (active) ILeaderColors.Success else ILeaderColors.Error)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        InfoRow("Р”Р°С‚Р° РІС‹РґР°С‡Рё", l.issueDate.ifBlank { "вЂ”" })
        InfoRow("Р”РµР№СЃС‚РІРёС‚РµР»СЊРЅР° РґРѕ", l.expiryDate.ifBlank { "вЂ”" })
        InfoRow("РњРµРґРѕСЃРјРѕС‚СЂ", l.medicalCheckDate.ifBlank { "вЂ”" })
        InfoRow("РњРµРґРѕСЃРјРѕС‚СЂ РґРµР№СЃС‚РІРёС‚РµР»РµРЅ РґРѕ", l.medicalCheckExpiry.ifBlank { "вЂ”" })
        Spacer(Modifier.height(20.dp))
        GradientButton(text = "Р РµРґР°РєС‚РёСЂРѕРІР°С‚СЊ", onClick = onEdit, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Surface(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), color = CardBg) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 13.sp, color = TextMuted)
            Text(value, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun LicenseEditor(
    initial: License?,
    onCancel: () -> Unit,
    onSave: (LicenseUpsertDto) -> Unit
) {
    var number by remember { mutableStateOf(initial?.number ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "") }
    var cls by remember { mutableStateOf(initial?.className ?: "") }
    var federation by remember { mutableStateOf(initial?.federation ?: "") }
    var issueDate by remember { mutableStateOf(initial?.issueDate ?: "") }
    var expiryDate by remember { mutableStateOf(initial?.expiryDate ?: "") }
    var medCheck by remember { mutableStateOf(initial?.medicalCheckDate ?: "") }
    var medExpiry by remember { mutableStateOf(initial?.medicalCheckExpiry ?: "") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        DarkFormField("РќРѕРјРµСЂ Р»РёС†РµРЅР·РёРё", number, { number = it }, placeholder = "KZ-2026-0001")
        Spacer(Modifier.height(12.dp))
        DarkFormField("РљР°С‚РµРіРѕСЂРёСЏ", category, { category = it }, placeholder = "A / B / C")
        Spacer(Modifier.height(12.dp))
        DarkFormField("РљР»Р°СЃСЃ", cls, { cls = it }, placeholder = "Junior / Senior")
        Spacer(Modifier.height(12.dp))
        DarkFormField("Р¤РµРґРµСЂР°С†РёСЏ", federation, { federation = it }, placeholder = "KAF")
        Spacer(Modifier.height(12.dp))
        DarkFormField("Р”Р°С‚Р° РІС‹РґР°С‡Рё", issueDate, { issueDate = it }, placeholder = "YYYY-MM-DD")
        Spacer(Modifier.height(12.dp))
        DarkFormField("Р”РµР№СЃС‚РІРёС‚РµР»СЊРЅР° РґРѕ", expiryDate, { expiryDate = it }, placeholder = "YYYY-MM-DD")
        Spacer(Modifier.height(12.dp))
        DarkFormField("РњРµРґРѕСЃРјРѕС‚СЂ", medCheck, { medCheck = it }, placeholder = "YYYY-MM-DD")
        Spacer(Modifier.height(12.dp))
        DarkFormField("РњРµРґРѕСЃРјРѕС‚СЂ РґРѕ", medExpiry, { medExpiry = it }, placeholder = "YYYY-MM-DD")
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("РћС‚РјРµРЅР°") }
            GradientButton(
                text = "РЎРѕС…СЂР°РЅРёС‚СЊ",
                onClick = {
                    onSave(
                        LicenseUpsertDto(
                            id = initial?.id,
                            userId = "",
                            number = number.trim(),
                            category = category.trim().ifEmpty { null },
                            licenseClass = cls.trim().ifEmpty { null },
                            federation = federation.trim().ifEmpty { null },
                            status = "active",
                            issueDate = issueDate.trim().ifEmpty { null },
                            expiryDate = expiryDate.trim().ifEmpty { null },
                            medicalCheckDate = medCheck.trim().ifEmpty { null },
                            medicalCheckExpiry = medExpiry.trim().ifEmpty { null }
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = number.isNotBlank()
            )
        }
        Spacer(Modifier.height(32.dp))
    }
}
