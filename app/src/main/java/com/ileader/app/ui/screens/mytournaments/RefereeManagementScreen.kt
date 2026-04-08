package com.ileader.app.ui.screens.mytournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ileader.app.data.remote.dto.ProfileDto
import com.ileader.app.data.remote.dto.RefereeAssignmentDto
import com.ileader.app.data.repository.OrganizerRepository
import com.ileader.app.ui.components.BackHeader
import com.ileader.app.ui.components.DarkFormField
import com.ileader.app.ui.components.DarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun RefereeManagementScreen(
    tournamentId: String,
    tournamentName: String,
    onBack: () -> Unit
) {
    val repo = remember { OrganizerRepository() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var assignments by remember { mutableStateOf<List<RefereeAssignmentDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var processingId by remember { mutableStateOf<String?>(null) }

    fun loadAssignments() {
        scope.launch {
            loading = true
            try {
                assignments = repo.getReferees(tournamentId)
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(e.message ?: "Ошибка загрузки")
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(tournamentId) { loadAssignments() }

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
            BackHeader("Судьи турнира", onBack)

            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(12.dp))
                Text(tournamentName, fontSize = 13.sp, color = TextMuted)
                Spacer(Modifier.height(12.dp))

                // ── Add referee button ──
                Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .clickable { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = Accent.copy(alpha = 0.1f)
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, null, tint = Accent, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Добавить судью", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Назначенные судьи (${assignments.size})",
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))

                if (loading) {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Accent, modifier = Modifier.size(28.dp))
                    }
                } else if (assignments.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Судьи ещё не назначены", fontSize = 13.sp, color = TextMuted)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        assignments.forEach { ref ->
                            RefereeCard(
                                assignment = ref,
                                isProcessing = processingId == ref.refereeId,
                                onRemove = {
                                    val rid = ref.refereeId ?: return@RefereeCard
                                    scope.launch {
                                        processingId = rid
                                        try {
                                            repo.removeReferee(tournamentId, rid)
                                            snackbarHostState.showSnackbar("Судья удалён")
                                            loadAssignments()
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar(e.message ?: "Ошибка")
                                        } finally {
                                            processingId = null
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

    // ── Add referee dialog ──
    if (showAddDialog) {
        AddRefereeDialog(
            onDismiss = { showAddDialog = false },
            onSelect = { profile ->
                val rid = profile.id
                scope.launch {
                    try {
                        repo.assignReferee(tournamentId, rid)
                        snackbarHostState.showSnackbar("Судья назначен")
                        showAddDialog = false
                        loadAssignments()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(e.message ?: "Ошибка назначения")
                    }
                }
            }
        )
    }
}

@Composable
private fun RefereeCard(
    assignment: RefereeAssignmentDto,
    isProcessing: Boolean,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBg
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            val avatarUrl = assignment.profiles?.avatarUrl
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl, contentDescription = null,
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                )
            } else {
                Box(
                    Modifier.size(40.dp).clip(CircleShape)
                        .background(Accent.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (assignment.profiles?.name ?: "?").take(1).uppercase(),
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Accent
                    )
                }
            }
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    assignment.profiles?.name ?: "—",
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary
                )
                Text(
                    when (assignment.role) {
                        "main" -> "Главный судья"
                        "assistant" -> "Помощник судьи"
                        "secretary" -> "Секретарь"
                        else -> assignment.role ?: "Судья"
                    },
                    fontSize = 12.sp, color = TextMuted
                )
            }

            // Remove button
            if (isProcessing) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFFEF4444))
            } else {
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onRemove() },
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.1f)
                ) {
                    Icon(
                        Icons.Filled.Close, null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp).padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddRefereeDialog(
    onDismiss: () -> Unit,
    onSelect: (ProfileDto) -> Unit
) {
    val repo = remember { OrganizerRepository() }
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<ProfileDto>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }

    // Debounced search
    LaunchedEffect(query) {
        delay(300)
        searching = true
        try {
            results = repo.searchReferees(query.trim())
        } catch (_: Exception) {
            results = emptyList()
        } finally {
            searching = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text("Найти судью", fontWeight = FontWeight.SemiBold, color = TextPrimary) },
        text = {
            Column {
                DarkFormField(
                    label = "",
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Поиск по имени"
                )
                Spacer(Modifier.height(12.dp))
                if (searching) {
                    Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Accent)
                    }
                } else if (results.isEmpty()) {
                    Text(
                        if (query.isBlank()) "Введите имя для поиска" else "Никого не найдено",
                        fontSize = 12.sp, color = TextMuted
                    )
                } else {
                    Column(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        results.forEach { profile ->
                            Surface(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelect(profile) },
                                shape = RoundedCornerShape(8.dp),
                                color = Bg
                            ) {
                                Row(
                                    Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (profile.avatarUrl != null) {
                                        AsyncImage(
                                            model = profile.avatarUrl, contentDescription = null,
                                            modifier = Modifier.size(32.dp).clip(CircleShape)
                                        )
                                    } else {
                                        Box(
                                            Modifier.size(32.dp).clip(CircleShape)
                                                .background(Accent.copy(0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Person, null, tint = Accent, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(profile.name ?: "—", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                        profile.city?.let {
                                            Text(it, fontSize = 11.sp, color = TextMuted)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть", color = Accent) }
        }
    )
}
