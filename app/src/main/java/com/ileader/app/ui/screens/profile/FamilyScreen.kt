package com.ileader.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.FamilyLinkDto
import com.ileader.app.data.remote.dto.ParentalApprovalDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.FamilyData
import com.ileader.app.ui.viewmodels.FamilyViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = LocalAppColors.current.border

@Composable
fun FamilyScreen(
    user: User,
    onBack: () -> Unit,
    viewModel: FamilyViewModel = viewModel()
) {
    LaunchedEffect(user.id) { viewModel.load(user.id) }

    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val actionState = viewModel.actionState

    LaunchedEffect(actionState) {
        when (actionState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Готово")
                viewModel.clearAction()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(actionState.message)
                viewModel.clearAction()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Bg
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).background(Bg)
        ) {
            BackHeader("Семья", onBack)

            when (val state = viewModel.state) {
                is UiState.Loading -> LoadingScreen()
                is UiState.Error -> ErrorScreen(state.message) { viewModel.load(user.id) }
                is UiState.Success -> FamilyContent(
                    data = state.data,
                    userId = user.id,
                    onAddChild = { showAddDialog = true },
                    onConfirm = { viewModel.confirmLink(it, user.id) },
                    onRemove = { viewModel.removeLink(it, user.id) },
                    onApprove = { viewModel.respondToApproval(it, true, user.id) },
                    onReject = { viewModel.respondToApproval(it, false, user.id) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddChildDialog(
            onDismiss = { showAddDialog = false },
            onSubmit = { email ->
                viewModel.linkChild(user.id, email)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun FamilyContent(
    data: FamilyData,
    userId: String,
    onAddChild: () -> Unit,
    onConfirm: (String) -> Unit,
    onRemove: (String) -> Unit,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    val children = data.childLinks(userId)
    val parents = data.parentLinks(userId)
    val approvals = data.pendingApprovals

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // ── Pending approvals ──
        if (approvals.isNotEmpty()) {
            Text("Ожидают одобрения", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            approvals.forEach { approval ->
                ApprovalCard(approval, onApprove = { onApprove(approval.id) }, onReject = { onReject(approval.id) })
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── My children ──
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Мои дети", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Surface(
                shape = RoundedCornerShape(50),
                color = Accent,
                modifier = Modifier.clickable { onAddChild() }
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Добавить", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (children.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.FamilyRestroom,
                title = "Нет привязанных детей",
                subtitle = "Добавьте ребёнка по email"
            )
        } else {
            children.forEach { link ->
                FamilyLinkCard(
                    name = link.child?.name ?: "—",
                    email = link.child?.email ?: "",
                    birthDate = link.child?.birthDate,
                    status = link.status,
                    isChild = true,
                    onConfirm = if (link.status == "pending") {{ onConfirm(link.id) }} else null,
                    onRemove = { onRemove(link.id) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        // ── My parents ──
        if (parents.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text("Мои родители", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            parents.forEach { link ->
                FamilyLinkCard(
                    name = link.parent?.name ?: "—",
                    email = link.parent?.email ?: "",
                    birthDate = null,
                    status = link.status,
                    isChild = false,
                    onConfirm = if (link.status == "pending") {{ onConfirm(link.id) }} else null,
                    onRemove = { onRemove(link.id) }
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun FamilyLinkCard(
    name: String,
    email: String,
    birthDate: String?,
    status: String,
    isChild: Boolean,
    onConfirm: (() -> Unit)?,
    onRemove: () -> Unit
) {
    val statusColor = when (status) {
        "active" -> Color(0xFF22C55E)
        "pending" -> Color(0xFFF59E0B)
        "removed" -> Color(0xFFEF4444)
        else -> TextMuted
    }
    val statusLabel = when (status) {
        "active" -> "Активна"
        "pending" -> "Ожидает"
        "removed" -> "Удалена"
        else -> status
    }

    DarkCardPadded {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).clip(CircleShape)
                    .background(if (isChild) Color(0xFF3B82F6).copy(0.15f) else Color(0xFF8B5CF6).copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isChild) Icons.Outlined.ChildCare else Icons.Outlined.Person,
                    null,
                    tint = if (isChild) Color(0xFF3B82F6) else Color(0xFF8B5CF6),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                if (email.isNotBlank()) {
                    Text(email, fontSize = 12.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (birthDate != null) {
                    Text("Дата рождения: ${birthDate.take(10)}", fontSize = 12.sp, color = TextMuted)
                }
            }
            Surface(shape = RoundedCornerShape(50), color = statusColor.copy(0.12f)) {
                Text(statusLabel, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = statusColor)
            }
        }

        if (onConfirm != null || status == "active" || status == "pending") {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (onConfirm != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF22C55E),
                        modifier = Modifier.clickable { onConfirm() }
                    ) {
                        Text("Подтвердить", Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEF4444).copy(0.12f),
                    modifier = Modifier.clickable { onRemove() }
                ) {
                    Text("Удалить", Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF4444))
                }
            }
        }
    }
}

@Composable
private fun ApprovalCard(
    approval: ParentalApprovalDto,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val actionLabel = when (approval.actionType) {
        "tournament_registration" -> "Регистрация на турнир"
        "team_join" -> "Вступление в команду"
        "league_registration" -> "Регистрация в лигу"
        else -> approval.actionType
    }
    val childName = approval.familyLink?.child?.name ?: "Ребёнок"

    DarkCardPadded {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF59E0B).copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Approval, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(actionLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("$childName · ${approval.referenceName ?: ""}", fontSize = 12.sp, color = TextMuted)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Surface(
                shape = RoundedCornerShape(8.dp), color = Color(0xFF22C55E),
                modifier = Modifier.clickable { onApprove() }
            ) {
                Text("Одобрить", Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp), color = Color(0xFFEF4444).copy(0.12f),
                modifier = Modifier.clickable { onReject() }
            ) {
                Text("Отклонить", Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEF4444))
            }
        }
    }
}

@Composable
private fun AddChildDialog(onDismiss: () -> Unit, onSubmit: (String) -> Unit) {
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text("Добавить ребёнка", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column {
                Text("Введите email аккаунта ребёнка на iLeader", fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email ребёнка") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (email.isNotBlank()) onSubmit(email.trim()) },
                enabled = email.isNotBlank()
            ) {
                Text("Привязать", color = Accent, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextMuted)
            }
        }
    )
}
