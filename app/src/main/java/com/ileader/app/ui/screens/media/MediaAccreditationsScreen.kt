package com.ileader.app.ui.screens.media

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.MediaInviteFullDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.repository.AccreditationStats
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.viewmodels.MediaViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

private val MediaColor = Color(0xFF06B6D4)
private val MediaColorDark = Color(0xFF0891B2)

private enum class AccFilter(val label: String) {
    ALL("Все"), PENDING("Ожидают"), ACCEPTED("Одобренные"), DECLINED("Отклонённые")
}

@Composable
fun MediaAccreditationsScreen(
    user: User,
    onTournamentClick: (String) -> Unit,
    onBrowseTournaments: () -> Unit = {},
    onInterviewsClick: () -> Unit = {},
    vm: MediaViewModel = viewModel()
) {
    val invites by vm.invites.collectAsState()
    val stats by vm.accreditationStats.collectAsState()
    val accreditationMap by vm.accreditationMap.collectAsState()
    val actionState by vm.actionState.collectAsState()

    var started by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(AccFilter.ALL) }
    var showInviteCodeDialog by remember { mutableStateOf(false) }
    var showRequestDialog by remember { mutableStateOf(false) }
    var expandedInviteId by remember { mutableStateOf<String?>(null) }

    val snackbar = LocalSnackbarHost.current

    LaunchedEffect(user.id) {
        vm.loadAccreditations(user.id)
        vm.loadUpcomingTournaments()
        started = true
    }

    // Handle action results
    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is UiState.Success -> {
                snackbar.showSnackbar(s.data)
                vm.clearAction()
                showInviteCodeDialog = false
                showRequestDialog = false
            }
            is UiState.Error -> {
                snackbar.showSnackbar(s.message)
                vm.clearAction()
            }
            else -> {}
        }
    }

    val allInvites = if (invites is UiState.Success) (invites as UiState.Success).data else emptyList()
    val pending = allInvites.filter { it.status == "pending" }
    val accepted = allInvites.filter { it.status == "accepted" }
    val declined = allInvites.filter { it.status == "declined" }
    val filtered = when (selectedFilter) {
        AccFilter.ALL -> allInvites
        AccFilter.PENDING -> pending
        AccFilter.ACCEPTED -> accepted
        AccFilter.DECLINED -> declined
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Bg),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // ── 1. Hero ──
        item {
            FadeIn(visible = started, delayMs = 0) {
                AccreditationHero(stats = stats, pendingCount = pending.size)
            }
        }

        // ── 2. Quick Actions ──
        item {
            Spacer(Modifier.height(16.dp))
            FadeIn(visible = started, delayMs = 60) {
                QuickActions(
                    onBrowse = { showRequestDialog = true },
                    onInviteCode = { showInviteCodeDialog = true },
                    onInterviews = onInterviewsClick
                )
            }
        }

        when (invites) {
            is UiState.Loading -> {
                item {
                    Spacer(Modifier.height(20.dp))
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        LoadingScreen()
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Spacer(Modifier.height(20.dp))
                    Box(Modifier.padding(16.dp)) {
                        EmptyState(
                            title = "Ошибка загрузки",
                            subtitle = (invites as UiState.Error).message
                        )
                    }
                }
            }
            is UiState.Success -> {
                if (allInvites.isEmpty()) {
                    item {
                        Spacer(Modifier.height(40.dp))
                        EmptyState(
                            title = "Нет аккредитаций",
                            subtitle = "Запросите аккредитацию на турнир или используйте инвайт-код",
                            icon = Icons.Default.CameraAlt
                        )
                    }
                } else {
                    // ── 3. Pending invites (outgoing from organizers) ──
                    val outgoing = allInvites.filter { it.direction == "outgoing" && it.status == "pending" }
                    if (outgoing.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(20.dp))
                            FadeIn(visible = started, delayMs = 100) {
                                SectionHeader(title = "Входящие приглашения", action = "${outgoing.size}")
                            }
                        }
                        items(outgoing, key = { it.id }) { invite ->
                            FadeIn(visible = started, delayMs = 140) {
                                IncomingInviteCard(
                                    invite = invite,
                                    expanded = expandedInviteId == invite.id,
                                    onToggle = {
                                        expandedInviteId = if (expandedInviteId == invite.id) null else invite.id
                                    },
                                    onAccept = { phone, msg ->
                                        vm.acceptInvite(invite.id, user.id, phone, msg)
                                    },
                                    onDecline = { reason ->
                                        vm.declineInvite(invite.id, user.id, reason)
                                    },
                                    onTournamentClick = { onTournamentClick(invite.tournamentId) }
                                )
                            }
                        }
                    }

                    // ── 4. Filter chips ──
                    item {
                        Spacer(Modifier.height(16.dp))
                        FadeIn(visible = started, delayMs = 120) {
                            AccFilterChips(
                                selected = selectedFilter,
                                onSelect = { selectedFilter = it },
                                counts = mapOf(
                                    AccFilter.ALL to allInvites.size,
                                    AccFilter.PENDING to pending.size,
                                    AccFilter.ACCEPTED to accepted.size,
                                    AccFilter.DECLINED to declined.size
                                )
                            )
                        }
                    }

                    // ── 5. All accreditations ──
                    item {
                        Spacer(Modifier.height(8.dp))
                        SectionHeader(title = "Мои аккредитации")
                    }

                    if (filtered.isEmpty()) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            EmptyState(
                                title = "Нет аккредитаций",
                                subtitle = "В этой категории пока нет записей"
                            )
                        }
                    } else {
                        itemsIndexed(filtered, key = { _, it -> it.id }) { index, invite ->
                            val delay = (180 + index * 50).coerceAtMost(500)
                            FadeIn(visible = started, delayMs = delay) {
                                AccreditationCard(
                                    invite = invite,
                                    onClick = { onTournamentClick(invite.tournamentId) },
                                    onCancel = { vm.cancelAccreditation(user.id, invite.tournamentId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Dialogs ──
    if (showInviteCodeDialog) {
        InviteCodeDialog(
            onDismiss = { showInviteCodeDialog = false },
            onSubmit = { code -> vm.joinByInviteCode(code, user.id) },
            isLoading = actionState is UiState.Loading
        )
    }

    if (showRequestDialog) {
        RequestAccreditationDialog(
            vm = vm,
            userId = user.id,
            accreditationMap = accreditationMap,
            onDismiss = { showRequestDialog = false },
            isLoading = actionState is UiState.Loading
        )
    }
}

// ══════════════════════════════════════════════════════════
// Hero
// ══════════════════════════════════════════════════════════

@Composable
private fun AccreditationHero(stats: AccreditationStats, pendingCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
    ) {
        Box(
            Modifier.matchParentSize().background(
                Brush.linearGradient(listOf(Color(0xFF0E7490), MediaColor, Color(0xFF67E8F9)))
            )
        )
        Column(
            Modifier.statusBarsPadding().padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CameraAlt, null, tint = Color.White.copy(0.8f), modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "Аккредитации",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
            }
            Spacer(Modifier.height(4.dp))
            Text("Управляйте доступом к турнирам", fontSize = 14.sp, color = Color.White.copy(0.7f))
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroPill("Всего", stats.total, Color.White.copy(0.2f))
                if (stats.accepted > 0) HeroPill("Одобрено", stats.accepted, Color(0xFF10B981).copy(0.4f))
                if (pendingCount > 0) HeroPill("Ожидает", pendingCount, Color(0xFFF59E0B).copy(0.4f))
            }

            // Progress bar
            if (stats.total > 0) {
                Spacer(Modifier.height(14.dp))
                val progress = stats.accepted.toFloat() / stats.total
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Процент одобрения", fontSize = 11.sp, color = Color.White.copy(0.7f))
                        Text("${(progress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(0.2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroPill(label: String, value: Int, bg: Color) {
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("$value", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(label, fontSize = 11.sp, color = Color.White.copy(0.8f))
        }
    }
}

// ══════════════════════════════════════════════════════════
// Quick Actions
// ══════════════════════════════════════════════════════════

@Composable
private fun QuickActions(onBrowse: () -> Unit, onInviteCode: () -> Unit, onInterviews: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionCard(
                icon = Icons.Default.Search,
                title = "Запросить",
                subtitle = "Выберите турнир",
                color = MediaColor,
                onClick = onBrowse,
                modifier = Modifier.weight(1f)
            )
            ActionCard(
                icon = Icons.Default.QrCode,
                title = "Инвайт-код",
                subtitle = "Ввести код",
                color = Color(0xFF8B5CF6),
                onClick = onInviteCode,
                modifier = Modifier.weight(1f)
            )
        }
        ActionCard(
            icon = Icons.Default.Videocam,
            title = "Видеоинтервью",
            subtitle = "Интервью со спортсменами",
            color = Color(0xFFE11D48),
            onClick = onInterviews,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = DarkTheme.isDark
    val colors = LocalAppColors.current

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = if (isDark) DarkTheme.cardBorderStroke
        else androidx.compose.foundation.BorderStroke(0.5.dp, colors.border.copy(0.3f)),
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(
                    Brush.linearGradient(listOf(color, color.copy(0.6f)))
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(subtitle, fontSize = 11.sp, color = TextMuted)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Filter chips
// ══════════════════════════════════════════════════════════

@Composable
private fun AccFilterChips(
    selected: AccFilter,
    onSelect: (AccFilter) -> Unit,
    counts: Map<AccFilter, Int>
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(AccFilter.entries.toList()) { filter ->
            val isSelected = selected == filter
            val count = counts[filter] ?: 0
            Surface(
                shape = RoundedCornerShape(50),
                color = if (isSelected) MediaColor else CardBg,
                border = if (!isSelected && DarkTheme.isDark) DarkTheme.cardBorderStroke else null,
                modifier = Modifier.clickable { onSelect(filter) }
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        filter.label, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                    if (count > 0) {
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color.White.copy(0.25f) else TextMuted.copy(0.15f),
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(
                                    "$count", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Incoming Invite Card (from organizer)
// ══════════════════════════════════════════════════════════

@Composable
private fun IncomingInviteCard(
    invite: MediaInviteFullDto,
    expanded: Boolean,
    onToggle: () -> Unit,
    onAccept: (phone: String, message: String?) -> Unit,
    onDecline: (reason: String) -> Unit,
    onTournamentClick: () -> Unit
) {
    val isDark = DarkTheme.isDark
    val colors = LocalAppColors.current

    var phone by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var declineReason by remember { mutableStateOf("") }
    var showDecline by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, Color(0xFFF59E0B).copy(0.3f)
        ),
        shadowElevation = if (isDark) 0.dp else 3.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            // Header with pulse indicator
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF59E0B))
                )
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        invite.tournaments?.name ?: "Турнир",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        invite.tournaments?.sports?.name?.let { sport ->
                            Text(sport, fontSize = 12.sp, color = TextMuted)
                        }
                        invite.tournaments?.startDate?.let { date ->
                            Text(formatDateShort(date), fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF59E0B).copy(0.15f)
                ) {
                    Text(
                        "Приглашение",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF59E0B)
                    )
                }
            }

            invite.message?.let { msg ->
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDark) Color.White.copy(0.05f) else Color.Black.copy(0.03f)
                ) {
                    Text(
                        msg,
                        modifier = Modifier.padding(10.dp),
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Expanded actions
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(12.dp))

                    if (!showDecline) {
                        // Accept form
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Контактный телефон") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MediaColor,
                                unfocusedBorderColor = colors.border,
                                focusedLabelColor = MediaColor,
                                unfocusedLabelColor = TextMuted,
                                cursorColor = MediaColor,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text("Сообщение (необязательно)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MediaColor,
                                unfocusedBorderColor = colors.border,
                                focusedLabelColor = MediaColor,
                                unfocusedLabelColor = TextMuted,
                                cursorColor = MediaColor,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onAccept(phone, message.ifBlank { null }) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Принять", fontWeight = FontWeight.SemiBold)
                            }
                            OutlinedButton(
                                onClick = { showDecline = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(0.5f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Отклонить", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        // Decline form
                        OutlinedTextField(
                            value = declineReason,
                            onValueChange = { declineReason = it },
                            label = { Text("Причина отказа") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFEF4444),
                                unfocusedBorderColor = colors.border,
                                focusedLabelColor = Color(0xFFEF4444),
                                unfocusedLabelColor = TextMuted,
                                cursorColor = Color(0xFFEF4444),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showDecline = false },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) { Text("Назад") }
                            Button(
                                onClick = { if (declineReason.isNotBlank()) onDecline(declineReason) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                                enabled = declineReason.isNotBlank()
                            ) { Text("Отклонить", fontWeight = FontWeight.SemiBold) }
                        }
                    }

                    // View tournament link
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onTournamentClick) {
                        Text("Посмотреть турнир →", color = MediaColor, fontSize = 13.sp)
                    }
                }
            }

            if (!expanded) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Нажмите для ответа",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.clickable(onClick = onToggle)
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Accreditation Card
// ══════════════════════════════════════════════════════════

@Composable
private fun AccreditationCard(
    invite: MediaInviteFullDto,
    onClick: () -> Unit,
    onCancel: () -> Unit
) {
    val isDark = DarkTheme.isDark
    val colors = LocalAppColors.current
    val statusColor = getAccStatusColor(invite.status)
    val statusLabel = getAccStatusLabel(invite.status)

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = if (isDark) DarkTheme.cardBorderStroke
        else androidx.compose.foundation.BorderStroke(0.5.dp, colors.border.copy(0.3f)),
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Column {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                // Status indicator
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(
                        Brush.linearGradient(listOf(statusColor.copy(0.8f), statusColor.copy(0.4f)))
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        when (invite.status) {
                            "accepted" -> Icons.Default.CheckCircle
                            "declined" -> Icons.Default.Cancel
                            else -> Icons.Default.HourglassTop
                        },
                        null, tint = Color.White, modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        invite.tournaments?.name ?: "Турнир",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        invite.tournaments?.sports?.name?.let { sport ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(sportIcon(sport), null, tint = TextMuted, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(sport, fontSize = 12.sp, color = TextMuted)
                            }
                        }
                        invite.tournaments?.startDate?.let { date ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(formatDateShort(date), fontSize = 12.sp, color = TextMuted)
                            }
                        }
                    }
                    invite.tournaments?.locations?.let { loc ->
                        val locText = listOfNotNull(loc.name, loc.city).joinToString(", ")
                        if (locText.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(locText, fontSize = 12.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }

                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(0.15f)) {
                    Text(
                        statusLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }
            }

            // Cancel action for pending
            if (invite.status == "pending" && invite.direction != "outgoing") {
                Row(
                    Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Отменить заявку", fontSize = 12.sp)
                    }
                }
            }

            // Response message
            invite.responseMessage?.let { msg ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 10.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDark) Color.White.copy(0.05f) else Color.Black.copy(0.03f)
                ) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.ChatBubbleOutline, null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(msg, fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// Invite Code Dialog
// ══════════════════════════════════════════════════════════

@Composable
private fun InviteCodeDialog(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
    isLoading: Boolean
) {
    var code by remember { mutableStateOf("") }
    val colors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.QrCode, null, tint = Color(0xFF8B5CF6))
                Spacer(Modifier.width(8.dp))
                Text("Инвайт-код", color = TextPrimary)
            }
        },
        text = {
            Column {
                Text("Введите код приглашения от организатора", fontSize = 13.sp, color = TextSecondary)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    placeholder = { Text("Например: ABC123") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Color(0xFF8B5CF6)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (code.isNotBlank()) onSubmit(code) },
                enabled = code.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Применить")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextMuted)
            }
        }
    )
}

// ══════════════════════════════════════════════════════════
// Request Accreditation Dialog
// ══════════════════════════════════════════════════════════

@Composable
private fun RequestAccreditationDialog(
    vm: MediaViewModel,
    userId: String,
    accreditationMap: Map<String, String>,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    val tournaments by vm.upcomingTournaments.collectAsState()
    var selectedTournamentId by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    val colors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null, tint = MediaColor)
                Spacer(Modifier.width(8.dp))
                Text("Запрос аккредитации", color = TextPrimary, fontSize = 18.sp)
            }
        },
        text = {
            Column(Modifier.heightIn(max = 400.dp)) {
                Text("Выберите турнир для аккредитации", fontSize = 13.sp, color = TextSecondary)
                Spacer(Modifier.height(12.dp))

                when (tournaments) {
                    is UiState.Loading -> {
                        Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MediaColor, modifier = Modifier.size(24.dp))
                        }
                    }
                    is UiState.Error -> {
                        Text("Ошибка загрузки", color = Color(0xFFEF4444), fontSize = 13.sp)
                    }
                    is UiState.Success -> {
                        val list = (tournaments as UiState.Success).data
                        val available = list.filter { it.id !in accreditationMap }
                        if (available.isEmpty()) {
                            Text("Нет доступных турниров", fontSize = 13.sp, color = TextMuted)
                        } else {
                            LazyColumn(Modifier.heightIn(max = 250.dp)) {
                                items(available, key = { it.id }) { t ->
                                    val isSelected = selectedTournamentId == t.id
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .clickable { selectedTournamentId = t.id },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MediaColor.copy(0.15f) else Color.Transparent,
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MediaColor.copy(0.5f)) else null
                                    ) {
                                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            if (isSelected) {
                                                Icon(Icons.Default.CheckCircle, null, tint = MediaColor, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                            }
                                            Column(Modifier.weight(1f)) {
                                                Text(t.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    t.sportName?.let { Text(it, fontSize = 11.sp, color = TextMuted) }
                                                    t.startDate?.let { Text(formatDateShort(it), fontSize = 11.sp, color = TextMuted) }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = message,
                                onValueChange = { message = it },
                                placeholder = { Text("Сообщение организатору (необязательно)", fontSize = 13.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MediaColor,
                                    unfocusedBorderColor = colors.border,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = MediaColor
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedTournamentId?.let { tid ->
                        vm.requestAccreditation(userId, tid, message.ifBlank { null })
                    }
                },
                enabled = selectedTournamentId != null && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MediaColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Отправить заявку")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextMuted)
            }
        }
    )
}

// ══════════════════════════════════════════════════════════
// Helpers
// ══════════════════════════════════════════════════════════

private fun formatDateShort(dateStr: String): String {
    val parts = dateStr.split("T")[0].split("-")
    if (parts.size < 3) return dateStr
    val day = parts[2].toIntOrNull() ?: return dateStr
    val m = listOf("", "янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")
    val month = parts[1].toIntOrNull() ?: return dateStr
    return "$day ${m.getOrElse(month) { "" }}"
}

private fun getAccStatusColor(status: String?): Color = when (status) {
    "accepted" -> Color(0xFF10B981)
    "declined" -> Color(0xFFEF4444)
    "pending" -> Color(0xFFF59E0B)
    else -> Color(0xFF8E8E93)
}

private fun getAccStatusLabel(status: String?): String = when (status) {
    "accepted" -> "Одобрено"
    "declined" -> "Отклонено"
    "pending" -> "Ожидает"
    else -> "Неизвестно"
}
