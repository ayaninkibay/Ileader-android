package com.ileader.app.ui.screens.mytournaments

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.remote.dto.TournamentHelperDto
import com.ileader.app.data.repository.HelperRepository
import com.ileader.app.ui.components.BackHeader
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.EmptyState
import com.ileader.app.ui.components.ErrorScreen
import com.ileader.app.ui.components.LoadingScreen
import com.ileader.app.ui.theme.LocalAppColors
import kotlinx.coroutines.launch

// ── Palette aliases ──
private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = LocalAppColors.current.border

@Composable
fun HelperManagementScreen(
    tournamentId: String,
    tournamentName: String,
    userId: String,
    onBack: () -> Unit
) {
    val repo = remember { HelperRepository() }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var helpers by remember { mutableStateOf<List<TournamentHelperDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var generatedCode by remember { mutableStateOf<String?>(null) }
    var isGeneratingCode by remember { mutableStateOf(false) }

    fun loadHelpers() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                helpers = repo.getTournamentHelpers(tournamentId)
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.message ?: "Ошибка загрузки"
                isLoading = false
            }
        }
    }

    LaunchedEffect(tournamentId) {
        loadHelpers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
    ) {
        BackHeader(title = "Управление помощниками", onBack = onBack)

        when {
            isLoading -> LoadingScreen()
            errorMessage != null -> ErrorScreen(
                message = errorMessage ?: "",
                onRetry = { loadHelpers() }
            )
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── Tournament name ──
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            tournamentName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Помощников: ${helpers.count { it.status == "active" }}",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }

                    // ── Generate invite code ──
                    item {
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    isGeneratingCode = true
                                    try {
                                        val code = repo.createHelperInviteCode(tournamentId, userId)
                                        generatedCode = code
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isGeneratingCode = false
                                    }
                                }
                            },
                            enabled = !isGeneratingCode,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Accent,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (isGeneratingCode) "Создание..." else "Пригласить помощника",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // ── Show generated code ──
                    if (generatedCode != null) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = Accent.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, Accent.copy(alpha = 0.3f))
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        "Код приглашения",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            generatedCode ?: "",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Accent,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(generatedCode ?: ""))
                                                Toast.makeText(context, "Скопировано", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = "Скопировать",
                                                tint = Accent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "Отправьте этот код помощнику для подключения",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }

                    // ── Helpers list ──
                    item {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Groups,
                                contentDescription = null,
                                tint = Accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Список помощников",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }

                    if (helpers.isEmpty()) {
                        item {
                            EmptyState(
                                title = "Нет помощников",
                                subtitle = "Создайте код приглашения для добавления помощников"
                            )
                        }
                    } else {
                        items(helpers, key = { it.id }) { helper ->
                            HelperCard(
                                helper = helper,
                                onRevoke = {
                                    scope.launch {
                                        try {
                                            repo.revokeHelper(helper.id)
                                            loadHelpers()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(100.dp)) }
                }
            }
        }
    }
}

@Composable
private fun HelperCard(
    helper: TournamentHelperDto,
    onRevoke: () -> Unit
) {
    val isActive = helper.status == "active"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) Accent.copy(alpha = 0.12f)
                        else Color.Gray.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isActive) Accent else Color.Gray,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Name and status
            Column(Modifier.weight(1f)) {
                Text(
                    helper.profiles?.name ?: "Помощник",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) TextPrimary else TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    helper.profiles?.email ?: "",
                    fontSize = 12.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Status badge
            HelperStatusBadge(status = helper.status)

            // Revoke button (only for active helpers)
            if (isActive) {
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onRevoke,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.PersonOff,
                        contentDescription = "Отозвать",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HelperStatusBadge(status: String) {
    val (label, bgColor, textColor) = when (status) {
        "active" -> Triple("Активен", Color(0xFF22C55E).copy(alpha = 0.15f), Color(0xFF22C55E))
        "revoked" -> Triple("Отозван", Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFEF4444))
        else -> Triple(status, Color.Gray.copy(alpha = 0.15f), Color.Gray)
    }

    Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
