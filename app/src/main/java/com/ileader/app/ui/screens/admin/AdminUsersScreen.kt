package com.ileader.app.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.mock.AdminMockData
import com.ileader.app.data.models.User
import com.ileader.app.data.models.UserRole
import com.ileader.app.data.models.UserStatus
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.AdminUsersViewModel

@Composable
fun AdminUsersScreen(user: User) {
    var subScreen by remember { mutableStateOf<String?>(null) }

    when {
        subScreen == null -> UsersListContent(
            onEditUser = { subScreen = "edit:$it" },
            onCreateUser = { subScreen = "create" }
        )
        subScreen == "create" -> AdminUserCreateScreen(onBack = { subScreen = null })
        subScreen?.startsWith("edit:") == true -> {
            val userId = subScreen?.removePrefix("edit:") ?: return
            AdminUserEditScreen(userId = userId, onBack = { subScreen = null })
        }
    }
}

@Composable
private fun UsersListContent(onEditUser: (String) -> Unit, onCreateUser: () -> Unit) {
    val viewModel: AdminUsersViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = s.message, onRetry = { viewModel.load() })
        is UiState.Success -> UsersListSuccessContent(
            users = s.data,
            viewModel = viewModel,
            onEditUser = onEditUser,
            onCreateUser = onCreateUser
        )
    }
}

@Composable
private fun UsersListSuccessContent(
    users: List<User>,
    viewModel: AdminUsersViewModel,
    onEditUser: (String) -> Unit,
    onCreateUser: () -> Unit
) {
    var searchTerm by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("all") }
    var selectedStatus by remember { mutableStateOf("all") }
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val activeCount = users.count { it.status == UserStatus.ACTIVE }
    val blockedCount = users.count { it.status == UserStatus.BLOCKED }
    val newThisMonth = users.count { u ->
        u.createdAt?.let { it >= "2026-02-01" } == true
    }

    val filteredUsers = users.filter { u ->
        val matchSearch = searchTerm.isEmpty() ||
                u.name.contains(searchTerm, ignoreCase = true) ||
                u.email.contains(searchTerm, ignoreCase = true)
        val matchRole = selectedRole == "all" || u.role.name.lowercase() == selectedRole
        val matchStatus = selectedStatus == "all" ||
                (selectedStatus == "active" && u.status == UserStatus.ACTIVE) ||
                (selectedStatus == "blocked" && u.status == UserStatus.BLOCKED)
        matchSearch && matchRole && matchStatus
    }

    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            Arrangement.SpaceBetween, Alignment.CenterVertically
        ) {
            Text("Пользователи", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                color = TextPrimary, letterSpacing = (-0.5).sp)
            Button(
                onClick = onCreateUser,
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Добавить", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FadeIn(visible = started, delayMs = 0) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStat("Всего", "${users.size}", modifier = Modifier.weight(1f))
                    MiniStat("Активных", "$activeCount", modifier = Modifier.weight(1f))
                    MiniStat("Заблок.", "$blockedCount", modifier = Modifier.weight(1f))
                    MiniStat("Новых", "$newThisMonth", modifier = Modifier.weight(1f))
                }
            }

            FadeIn(visible = started, delayMs = 150) {
                DarkSearchField(value = searchTerm, onValueChange = { searchTerm = it }, placeholder = "Поиск по имени или email")
            }

            FadeIn(visible = started, delayMs = 300) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val roles = listOf("all" to "Все роли") +
                                UserRole.entries.map { it.name.lowercase() to it.displayName }
                        roles.forEach { (key, label) ->
                            DarkFilterChip(text = label, selected = selectedRole == key, onClick = { selectedRole = key })
                        }
                    }

                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("all" to "Все", "active" to "Активные", "blocked" to "Заблокированные").forEach { (key, label) ->
                            DarkFilterChip(text = label, selected = selectedStatus == key, onClick = { selectedStatus = key })
                        }
                    }
                }
            }

            FadeIn(visible = started, delayMs = 450) {
                Text("Показано ${filteredUsers.size} из ${users.size}", fontSize = 13.sp, color = TextMuted)
            }

            FadeIn(visible = started, delayMs = 600) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    filteredUsers.forEach { u ->
                        UserCard(
                            user = u,
                            onEdit = { onEditUser(u.id) },
                            onToggleBlock = {
                                if (u.status == UserStatus.ACTIVE) {
                                    viewModel.blockUser(u.id)
                                } else {
                                    viewModel.unblockUser(u.id)
                                }
                            },
                            onDelete = { showDeleteDialog = u.id }
                        )
                    }

                    if (filteredUsers.isEmpty()) {
                        EmptyState("Пользователи не найдены")
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { userId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            containerColor = CardBg,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Удалить пользователя?") },
            text = { Text("Это действие нельзя отменить. Вы уверены?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteUser(userId)
                    showDeleteDialog = null
                }) { Text("Удалить", color = Accent) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Отмена", color = TextSecondary) }
            }
        )
    }
}

@Composable
private fun UserCard(
    user: User,
    onEdit: () -> Unit,
    onToggleBlock: () -> Unit,
    onDelete: () -> Unit
) {
    val roleColor = AdminMockData.roleColor(user.role)

    DarkCard {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(roleColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(user.name.take(1), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = roleColor)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(user.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (user.role == UserRole.ADMIN) {
                            Spacer(Modifier.width(6.dp))
                            Box(
                                Modifier.size(20.dp).clip(RoundedCornerShape(6.dp)).background(AccentSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Shield, null, tint = Accent, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                    Text(user.email, fontSize = 12.sp, color = TextSecondary,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                StatusBadge(
                    text = if (user.status == UserStatus.ACTIVE) "Активен" else "Заблок.",
                    color = if (user.status == UserStatus.ACTIVE) Accent else TextMuted
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RoleBadge(user.role)
                if (user.athleteSubtype != null) {
                    Text(user.athleteSubtype.displayName, fontSize = 11.sp, color = TextMuted)
                }
                if (user.sportIds?.isNotEmpty() == true) {
                    Text(user.sportIds.joinToString(", "),
                        fontSize = 11.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            if (user.city != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    buildString {
                        append(user.city)
                        if (user.createdAt != null) {
                            append(" · ")
                            append(user.createdAt)
                        }
                    },
                    fontSize = 12.sp, color = TextMuted
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = Accent)
                    Spacer(Modifier.width(4.dp))
                    Text("Изменить", fontSize = 12.sp, color = Accent)
                }
                TextButton(onClick = onToggleBlock, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Icon(
                        if (user.status == UserStatus.ACTIVE) Icons.Default.Block else Icons.Default.CheckCircle,
                        null, modifier = Modifier.size(16.dp), tint = TextSecondary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (user.status == UserStatus.ACTIVE) "Блок." else "Разблок.",
                        fontSize = 12.sp, color = TextSecondary
                    )
                }
                TextButton(onClick = onDelete, contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = TextMuted)
                    Spacer(Modifier.width(4.dp))
                    Text("Удалить", fontSize = 12.sp, color = TextMuted)
                }
            }
        }
    }
}
