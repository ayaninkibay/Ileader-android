package com.ileader.app.ui.screens.mytournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
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
import com.ileader.app.data.remote.dto.ProfileMinimalDto
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.remote.dto.TeamInsertDto
import com.ileader.app.data.repository.OrganizerRepository
import com.ileader.app.data.repository.TrainerRepository
import com.ileader.app.data.repository.TrainerTeamData
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
fun TeamManagementScreen(
    userId: String,
    onBack: () -> Unit
) {
    val repo = remember { TrainerRepository() }
    val orgRepo = remember { OrganizerRepository() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var teams by remember { mutableStateOf<List<TrainerTeamData>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var managingTeam by remember { mutableStateOf<TrainerTeamData?>(null) }
    var deleteConfirmId by remember { mutableStateOf<String?>(null) }

    fun loadTeams() {
        scope.launch {
            loading = true
            try {
                teams = repo.getMyTeams(userId)
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(e.message ?: "Ошибка загрузки")
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(userId) { loadTeams() }

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
            BackHeader("Мои команды", onBack)

            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .clickable { showCreateDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = Accent.copy(alpha = 0.1f)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Add, null, tint = Accent, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Создать команду", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Команды (${teams.size})",
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))

                if (loading) {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Accent, modifier = Modifier.size(28.dp))
                    }
                } else if (teams.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Команд нет", fontSize = 13.sp, color = TextMuted)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        teams.forEach { team ->
                            TeamCard(
                                team = team,
                                onManage = { managingTeam = team },
                                onDelete = { deleteConfirmId = team.id }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }

    if (showCreateDialog) {
        CreateTeamDialog(
            userId = userId,
            onDismiss = { showCreateDialog = false },
            onCreate = { name, sportId, description, city ->
                scope.launch {
                    try {
                        repo.createTeam(
                            TeamInsertDto(
                                name = name,
                                sportId = sportId,
                                ownerId = userId,
                                description = description.ifBlank { null },
                                city = city.ifBlank { null }
                            )
                        )
                        snackbarHostState.showSnackbar("Команда создана")
                        showCreateDialog = false
                        loadTeams()
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(e.message ?: "Ошибка создания")
                    }
                }
            }
        )
    }

    managingTeam?.let { team ->
        ManageTeamMembersDialog(
            team = team,
            onDismiss = { managingTeam = null },
            onChanged = { loadTeams() }
        )
    }

    deleteConfirmId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteConfirmId = null },
            containerColor = CardBg,
            title = { Text("Удалить команду?", fontWeight = FontWeight.SemiBold, color = TextPrimary) },
            text = { Text("Это действие необратимо.", fontSize = 14.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            repo.deleteTeam(id)
                            snackbarHostState.showSnackbar("Команда удалена")
                            loadTeams()
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar(e.message ?: "Ошибка")
                        } finally {
                            deleteConfirmId = null
                        }
                    }
                }) { Text("Удалить", color = Color(0xFFEF4444)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmId = null }) { Text("Отмена", color = TextMuted) }
            }
        )
    }
}

@Composable
private fun TeamCard(
    team: TrainerTeamData,
    onManage: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBg
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                        .background(Accent.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Groups, null, tint = Accent, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(team.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("${team.sportName} • ${team.members.size} атлетов", fontSize = 12.sp, color = TextMuted)
                }
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onManage() },
                    color = Accent.copy(0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.PersonAdd, null, tint = Accent, modifier = Modifier.size(20.dp).padding(4.dp))
                }
                Spacer(Modifier.width(6.dp))
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onDelete() },
                    color = Color(0xFFEF4444).copy(0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Delete, null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp).padding(4.dp))
                }
            }
        }
    }
}

@Composable
private fun CreateTeamDialog(
    userId: String,
    onDismiss: () -> Unit,
    onCreate: (name: String, sportId: String, description: String, city: String) -> Unit
) {
    val orgRepo = remember { OrganizerRepository() }
    var sports by remember { mutableStateOf<List<SportDto>>(emptyList()) }
    var name by remember { mutableStateOf("") }
    var sportId by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try { sports = orgRepo.getSports() } catch (_: Exception) {}
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text("Новая команда", fontWeight = FontWeight.SemiBold, color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DarkFormField(label = "Название *", value = name, onValueChange = { name = it }, placeholder = "Спартак")

                Text("Вид спорта *", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    sports.forEach { sport ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .clickable { sportId = sport.id },
                            color = if (sportId == sport.id) Accent.copy(0.15f) else Bg,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                sport.name,
                                Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontSize = 13.sp,
                                color = if (sportId == sport.id) Accent else TextPrimary,
                                fontWeight = if (sportId == sport.id) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                DarkFormField(label = "Город", value = city, onValueChange = { city = it }, placeholder = "Алматы")
                DarkFormField(
                    label = "Описание",
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "О команде",
                    singleLine = false,
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, sportId, description, city) },
                enabled = name.isNotBlank() && sportId.isNotBlank()
            ) { Text("Создать", color = Accent) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = TextMuted) }
        }
    )
}

@Composable
private fun ManageTeamMembersDialog(
    team: TrainerTeamData,
    onDismiss: () -> Unit,
    onChanged: () -> Unit
) {
    val repo = remember { TrainerRepository() }
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<ProfileMinimalDto>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    var members by remember { mutableStateOf(team.members) }
    var processingId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(query) {
        delay(300)
        if (query.isNotBlank()) {
            searching = true
            try {
                results = repo.searchAthletes(query.trim())
                    .filter { p -> p.id !in members.map { m -> m.id } }
            } catch (_: Exception) {
                results = emptyList()
            } finally {
                searching = false
            }
        } else {
            results = emptyList()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text("Состав: ${team.name}", fontWeight = FontWeight.SemiBold, color = TextPrimary) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Current members
                if (members.isNotEmpty()) {
                    Text("Текущий состав (${members.size})",
                        fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                    Column(
                        modifier = Modifier.heightIn(max = 180.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        members.forEach { member ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Bg
                            ) {
                                Row(
                                    Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        Modifier.size(28.dp).clip(CircleShape).background(Accent.copy(0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Person, null, tint = Accent, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(member.name, fontSize = 13.sp, color = TextPrimary,
                                        modifier = Modifier.weight(1f))
                                    if (processingId == member.id) {
                                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFFEF4444))
                                    } else {
                                        Icon(
                                            Icons.Filled.Close, null,
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .clickable {
                                                    scope.launch {
                                                        processingId = member.id
                                                        try {
                                                            repo.removeTeamMember(team.id, member.id)
                                                            members = members.filter { it.id != member.id }
                                                            onChanged()
                                                        } catch (_: Exception) {
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

                Text("Добавить атлета", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                DarkFormField(label = "", value = query, onValueChange = { query = it }, placeholder = "Поиск по имени")

                if (searching) {
                    Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Accent)
                    }
                } else if (results.isNotEmpty()) {
                    Column(
                        modifier = Modifier.heightIn(max = 180.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        results.forEach { profile ->
                            val pid = profile.id ?: return@forEach
                            Surface(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        scope.launch {
                                            processingId = pid
                                            try {
                                                repo.addTeamMember(team.id, pid)
                                                // Refresh members optimistically
                                                onChanged()
                                                results = results.filter { it.id != pid }
                                            } catch (_: Exception) {
                                            } finally {
                                                processingId = null
                                            }
                                        }
                                    },
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
                                            modifier = Modifier.size(28.dp).clip(CircleShape)
                                        )
                                    } else {
                                        Box(
                                            Modifier.size(28.dp).clip(CircleShape).background(Accent.copy(0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Person, null, tint = Accent, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(profile.name ?: "—", fontSize = 13.sp, color = TextPrimary)
                                        profile.city?.let {
                                            Text(it, fontSize = 11.sp, color = TextMuted)
                                        }
                                    }
                                    if (processingId == pid) {
                                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Accent)
                                    } else {
                                        Icon(Icons.Filled.Add, null, tint = Accent, modifier = Modifier.size(18.dp))
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
