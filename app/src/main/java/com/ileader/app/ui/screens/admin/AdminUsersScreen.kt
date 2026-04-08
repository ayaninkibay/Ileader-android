package com.ileader.app.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ileader.app.data.models.UserRole
import com.ileader.app.data.remote.dto.AdminUserDto
import com.ileader.app.data.repository.AdminRepository
import com.ileader.app.ui.components.BackHeader
import com.ileader.app.ui.components.DarkFilterChip
import com.ileader.app.ui.components.DarkFormField
import com.ileader.app.ui.components.DarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

private val ROLE_FILTERS = listOf(
    null to "Все",
    "athlete" to "Спортсмены",
    "trainer" to "Тренеры",
    "organizer" to "Организаторы",
    "referee" to "Судьи",
    "sponsor" to "Спонсоры",
    "media" to "СМИ",
    "content_manager" to "Контент",
    "admin" to "Админы",
    "user" to "Зрители"
)

private val CHANGEABLE_ROLES = listOf(
    "user", "athlete", "trainer", "organizer",
    "referee", "sponsor", "media", "content_manager", "admin"
)

@Composable
fun AdminUsersScreen(onBack: () -> Unit) {
    val repo = remember { AdminRepository() }
    val scope = rememberCoroutineScope()

    var users by remember { mutableStateOf<List<AdminUserDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var roleDialogUser by remember { mutableStateOf<AdminUserDto?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                users = repo.getAllUsers(selectedRole, query.trim())
            } catch (e: Exception) {
                error = e.message ?: "Ошибка загрузки"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(selectedRole) { load() }
    LaunchedEffect(query) {
        delay(350)
        load()
    }

    Column(
        Modifier.fillMaxSize().background(Bg).statusBarsPadding()
    ) {
        BackHeader("Пользователи", onBack)

        Column(Modifier.padding(horizontal = 16.dp)) {
            DarkFormField(
                label = "",
                value = query,
                onValueChange = { query = it },
                placeholder = "Поиск по имени"
            )
            Spacer(Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ROLE_FILTERS) { (key, label) ->
                    DarkFilterChip(
                        text = label,
                        selected = selectedRole == key,
                        onClick = { selectedRole = key }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
            error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(error ?: "", color = TextMuted)
            }
            users.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Никого не найдено", color = TextMuted)
            }
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(users, key = { it.id }) { user ->
                    AdminUserCard(
                        user = user,
                        onChangeRole = { roleDialogUser = user },
                        onToggleBlock = {
                            scope.launch {
                                try {
                                    if (user.status == "blocked") repo.unblockUser(user.id)
                                    else repo.blockUser(user.id)
                                    load()
                                } catch (_: Exception) {}
                            }
                        }
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }

    roleDialogUser?.let { target ->
        RoleChangeDialog(
            user = target,
            onDismiss = { roleDialogUser = null },
            onSelect = { newRole ->
                scope.launch {
                    try {
                        repo.updateUserRole(target.id, newRole)
                        roleDialogUser = null
                        load()
                    } catch (_: Exception) {
                        roleDialogUser = null
                    }
                }
            }
        )
    }
}

@Composable
private fun AdminUserCard(
    user: AdminUserDto,
    onChangeRole: () -> Unit,
    onToggleBlock: () -> Unit
) {
    val isBlocked = user.status == "blocked"
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBg
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (user.avatarUrl != null) {
                AsyncImage(
                    model = user.avatarUrl, contentDescription = null,
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                )
            } else {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(Accent.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, null, tint = Accent, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    user.name ?: "—",
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = if (isBlocked) TextMuted else TextPrimary
                )
                Text(
                    user.email ?: "",
                    fontSize = 11.sp, color = TextMuted
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val roleName = user.roles?.name ?: "—"
                    val roleLabel = roleNameRu(roleName)
                    Surface(
                        modifier = Modifier.clickable { onChangeRole() },
                        shape = RoundedCornerShape(8.dp),
                        color = Accent.copy(alpha = 0.12f)
                    ) {
                        Text(
                            roleLabel,
                            Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp, color = Accent, fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (isBlocked) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.12f)
                        ) {
                            Text(
                                "Заблокирован",
                                Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onToggleBlock() },
                shape = RoundedCornerShape(8.dp),
                color = if (isBlocked) Color(0xFF22C55E).copy(0.12f) else Color(0xFFEF4444).copy(0.12f)
            ) {
                Icon(
                    if (isBlocked) Icons.Filled.LockOpen else Icons.Filled.Block,
                    null,
                    tint = if (isBlocked) Color(0xFF22C55E) else Color(0xFFEF4444),
                    modifier = Modifier.size(28.dp).padding(6.dp)
                )
            }
        }
    }
}

@Composable
private fun RoleChangeDialog(
    user: AdminUserDto,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text("Сменить роль", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(user.name ?: "—", fontSize = 12.sp, color = TextMuted)
                Spacer(Modifier.height(4.dp))
                CHANGEABLE_ROLES.forEach { role ->
                    Surface(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(role) },
                        shape = RoundedCornerShape(8.dp),
                        color = Bg
                    ) {
                        Text(
                            roleNameRu(role),
                            Modifier.padding(10.dp),
                            fontSize = 13.sp, color = TextPrimary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = Accent) }
        }
    )
}

private fun roleNameRu(name: String): String = when (name) {
    "athlete" -> UserRole.ATHLETE.displayName
    "trainer" -> UserRole.TRAINER.displayName
    "organizer" -> UserRole.ORGANIZER.displayName
    "referee" -> UserRole.REFEREE.displayName
    "sponsor" -> UserRole.SPONSOR.displayName
    "media" -> UserRole.MEDIA.displayName
    "content_manager" -> UserRole.CONTENT_MANAGER.displayName
    "admin" -> UserRole.ADMIN.displayName
    "user" -> UserRole.USER.displayName
    else -> name
}
