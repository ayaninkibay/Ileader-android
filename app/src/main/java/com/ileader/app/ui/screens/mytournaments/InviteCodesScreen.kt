package com.ileader.app.ui.screens.mytournaments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.remote.dto.InviteCodeDto
import com.ileader.app.data.remote.dto.InviteCodeInsertDto
import com.ileader.app.data.repository.OrganizerRepository
import com.ileader.app.ui.components.BackHeader
import com.ileader.app.ui.components.DarkFormField
import com.ileader.app.ui.components.DarkTheme
import kotlinx.coroutines.launch

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun InviteCodesScreen(
    tournamentId: String,
    tournamentName: String,
    userId: String,
    onBack: () -> Unit
) {
    val repo = remember { OrganizerRepository() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var codes by remember { mutableStateOf<List<InviteCodeDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showCreateDialog by remember { mutableStateOf(false) }

    fun loadCodes() {
        scope.launch {
            loading = true
            try {
                codes = repo.getInviteCodes(tournamentId).sortedByDescending { it.createdAt }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(e.message ?: "Ошибка загрузки")
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(tournamentId) { loadCodes() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(padding)
        ) {
            BackHeader("Инвайт-коды", onBack)

            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(12.dp))
                Text(tournamentName, fontSize = 13.sp, color = TextMuted)
                Spacer(Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .clickable { showCreateDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = Accent.copy(alpha = 0.1f)
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, null, tint = Accent, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Создать код", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Активные коды (${codes.count { it.isActive }})",
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))

                if (loading) {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Accent, modifier = Modifier.size(28.dp))
                    }
                } else if (codes.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Кодов нет", fontSize = 13.sp, color = TextMuted)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        codes.forEach { code ->
                            InviteCodeCard(
                                code = code,
                                onCopy = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("invite", code.code))
                                    Toast.makeText(context, "Код скопирован", Toast.LENGTH_SHORT).show()
                                },
                                onDeactivate = {
                                    scope.launch {
                                        try {
                                            repo.deactivateInviteCode(code.id ?: return@launch)
                                            snackbarHostState.showSnackbar("Код деактивирован")
                                            loadCodes()
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar(e.message ?: "Ошибка")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCodeDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { type, maxUses ->
                scope.launch {
                    try {
                        val randomCode = generateCode(type)
                        repo.createInviteCode(
                            InviteCodeInsertDto(
                                tournamentId = tournamentId,
                                code = randomCode,
                                type = type,
                                maxUses = maxUses,
                                createdBy = userId
                            )
                        )
                        snackbarHostState.showSnackbar("Код создан: $randomCode")
                        showCreateDialog = false
                        loadCodes()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(e.message ?: "Ошибка")
                    }
                }
            }
        )
    }
}

private fun generateCode(type: String): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    val random = (1..6).map { chars.random() }.joinToString("")
    val prefix = when (type) {
        "athlete" -> "ATH"
        "referee" -> "REF"
        "sponsor" -> "SPN"
        "media" -> "MED"
        "helper" -> "HLP"
        else -> "INV"
    }
    return "$prefix-$random"
}

@Composable
private fun InviteCodeCard(
    code: InviteCodeDto,
    onCopy: () -> Unit,
    onDeactivate: () -> Unit
) {
    val typeLabel = when (code.type) {
        "athlete" -> "Спортсмен"
        "referee" -> "Судья"
        "sponsor" -> "Спонсор"
        "media" -> "Медиа"
        "helper" -> "Помощник"
        else -> code.type ?: "Код"
    }
    val typeColor = when (code.type) {
        "athlete" -> Color(0xFFEF4444)
        "referee" -> Color(0xFFF59E0B)
        "sponsor" -> Color(0xFF8B5CF6)
        "media" -> Color(0xFF3B82F6)
        "helper" -> Color(0xFF22C55E)
        else -> TextMuted
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBg
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                        .background(typeColor.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.QrCode2, null, tint = typeColor, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        code.code,
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(4.dp), color = typeColor.copy(0.1f)) {
                            Text(
                                typeLabel,
                                Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                fontSize = 10.sp, color = typeColor, fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        val usesLabel = if (code.maxUses != null) "${code.usedCount}/${code.maxUses}"
                        else "${code.usedCount} использований"
                        Text(usesLabel, fontSize = 11.sp, color = TextMuted)
                    }
                }
                if (code.isActive) {
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onCopy() },
                        color = Accent.copy(0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.ContentCopy, null, tint = Accent,
                            modifier = Modifier.size(20.dp).padding(2.dp)
                        )
                    }
                } else {
                    Surface(shape = RoundedCornerShape(6.dp), color = TextMuted.copy(0.1f)) {
                        Text(
                            "Неактивен",
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp, color = TextMuted
                        )
                    }
                }
            }
            if (code.isActive) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                        .clickable { onDeactivate() },
                    color = Color(0xFFEF4444).copy(0.08f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Деактивировать",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        fontSize = 12.sp, color = Color(0xFFEF4444),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateCodeDialog(
    onDismiss: () -> Unit,
    onCreate: (type: String, maxUses: Int?) -> Unit
) {
    var type by remember { mutableStateOf("athlete") }
    var maxUses by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text("Создать инвайт-код", fontWeight = FontWeight.SemiBold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Тип кода", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                val types = listOf(
                    "athlete" to "Спортсмен",
                    "referee" to "Судья",
                    "sponsor" to "Спонсор",
                    "media" to "Медиа",
                    "helper" to "Помощник"
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    types.forEach { (key, label) ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .clickable { type = key },
                            color = if (type == key) Accent.copy(0.15f) else Bg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                label,
                                Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontSize = 13.sp,
                                color = if (type == key) Accent else TextPrimary,
                                fontWeight = if (type == key) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                DarkFormField(
                    label = "Макс. использований (опционально)",
                    value = maxUses,
                    onValueChange = { maxUses = it },
                    placeholder = "Без ограничения",
                    keyboardType = KeyboardType.Number
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onCreate(type, maxUses.toIntOrNull()) }) {
                Text("Создать", color = Accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = TextMuted) }
        }
    )
}
