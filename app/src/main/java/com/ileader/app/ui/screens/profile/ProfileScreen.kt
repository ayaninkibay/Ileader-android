package com.ileader.app.ui.screens.profile

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.models.User
import com.ileader.app.data.models.UserRole
import com.ileader.app.data.preferences.SportPreference
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.ErrorScreen
import com.ileader.app.ui.components.LoadingScreen
import com.ileader.app.ui.components.RoleBadge
import com.ileader.app.ui.components.ThemeSwitcherCard
import com.ileader.app.ui.viewmodels.ProfileViewModel
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.launch

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val Border: Color @Composable get() = com.ileader.app.ui.theme.LocalAppColors.current.border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    onSignOut: () -> Unit,
    onEditProfile: () -> Unit,
    onTickets: () -> Unit,
    onNotifications: () -> Unit
) {
    val vm: ProfileViewModel = viewModel()
    val profileState by vm.profile.collectAsState()
    val stats by vm.stats.collectAsState()
    val userSports by vm.userSports.collectAsState()

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showSportSheet by remember { mutableStateOf(false) }
    var started by remember { mutableStateOf(false) }

    LaunchedEffect(user.id) { vm.load(user.id) }
    LaunchedEffect(Unit) { started = true }

    // Avatar scale-in animation
    val avatarScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.7f,
        animationSpec = tween(500, delayMillis = 100, easing = EaseOutBack),
        label = "avatarScale"
    )
    val avatarAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(400, delayMillis = 100),
        label = "avatarAlpha"
    )
    // Stats card animation
    val statsAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(400, delayMillis = 250),
        label = "statsAlpha"
    )
    val statsOffset by animateFloatAsState(
        targetValue = if (started) 0f else 40f,
        animationSpec = tween(400, delayMillis = 250, easing = EaseOutBack),
        label = "statsOffset"
    )
    // Quick actions animation
    val actionsAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(400, delayMillis = 400),
        label = "actionsAlpha"
    )
    val actionsOffset by animateFloatAsState(
        targetValue = if (started) 0f else 40f,
        animationSpec = tween(400, delayMillis = 400, easing = EaseOutBack),
        label = "actionsOffset"
    )
    // Theme + sign out animation
    val bottomAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(400, delayMillis = 550),
        label = "bottomAlpha"
    )
    val bottomOffset by animateFloatAsState(
        targetValue = if (started) 0f else 30f,
        animationSpec = tween(400, delayMillis = 550, easing = EaseOutBack),
        label = "bottomOffset"
    )

    when (val state = profileState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = state.message, onRetry = { vm.load(user.id) })
        is UiState.Success -> {
            val profile = state.data

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Bg)
                    .verticalScroll(rememberScrollState())
            ) {
                // ── Gradient header with overlapping avatar ──
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Gradient background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFE53535), Color(0xFFFF6B6B))
                                ),
                                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                            )
                            .statusBarsPadding()
                    )

                    // Avatar overlapping
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(top = 60.dp)
                            .graphicsLayer {
                                scaleX = avatarScale
                                scaleY = avatarScale
                                alpha = avatarAlpha
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar with white ring
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(CardBg)
                                .border(4.dp, CardBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profile.avatarUrl != null) {
                                AsyncImage(
                                    model = profile.avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(92.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(92.dp)
                                        .clip(CircleShape)
                                        .background(Accent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (profile.name ?: user.name).take(2).uppercase(),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Name
                        Text(
                            text = profile.name ?: user.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(6.dp))

                        // Role + city
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RoleBadge(user.role)
                            if (profile.city != null) {
                                Text(profile.city, fontSize = 13.sp, color = TextMuted)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Stats card ──
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha = statsAlpha
                            translationY = statsOffset
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = CardBg,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val primaryStats = stats.firstOrNull()
                        when (user.role) {
                            UserRole.ATHLETE -> {
                                StatItem("Турниры", "${primaryStats?.tournaments ?: 0}")
                                VerticalDivider()
                                StatItem("Победы", "${primaryStats?.wins ?: 0}")
                                VerticalDivider()
                                StatItem("Рейтинг", "${primaryStats?.rating ?: 1000}")
                            }
                            UserRole.TRAINER -> {
                                StatItem("Турниры", "${primaryStats?.tournaments ?: 0}")
                                VerticalDivider()
                                StatItem("Рейтинг", "${primaryStats?.rating ?: 1000}")
                            }
                            UserRole.REFEREE, UserRole.ORGANIZER -> {
                                StatItem("Турниры", "${primaryStats?.tournaments ?: 0}")
                            }
                            else -> {
                                StatItem("Виды спорта", "${userSports.size}")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Quick Actions ──
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha = actionsAlpha
                            translationY = actionsOffset
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = CardBg,
                    shadowElevation = 0.dp
                ) {
                    Column {
                        ActionRow(Icons.Default.Edit, "Редактировать профиль", onClick = onEditProfile)
                        ActionDivider()
                        ActionRow(Icons.Default.ConfirmationNumber, "Мои билеты", onClick = onTickets)
                        ActionDivider()
                        ActionRow(Icons.Default.Notifications, "Уведомления", onClick = onNotifications)
                        ActionDivider()
                        ActionRow(Icons.Default.SportsScore, "Виды спорта", onClick = { showSportSheet = true })
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Theme switcher
                Box(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha = bottomAlpha
                            translationY = bottomOffset
                        }
                ) {
                    ThemeSwitcherCard()
                }

                Spacer(Modifier.height(12.dp))

                // Sign out
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            alpha = bottomAlpha
                            translationY = bottomOffset
                        }
                        .clickable { showSignOutDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    color = CardBg,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(
                            "Выйти",
                            fontSize = 15.sp,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    }

    // ── Dialogs ──
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Выход", color = TextPrimary) },
            text = { Text("Вы уверены, что хотите выйти?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { showSignOutDialog = false; onSignOut() }) {
                    Text("Выйти", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Отмена", color = TextMuted)
                }
            }
        )
    }

    if (showSportSheet) {
        SportSelectionSheet(userId = user.id, onDismiss = { showSportSheet = false })
    }
}

// ══════════════════════════════════════════════════════════
// Components
// ══════════════════════════════════════════════════════════

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 12.sp, color = TextMuted)
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(Border)
    )
}

@Composable
private fun ActionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ActionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = Border
    )
}

// ══════════════════════════════════════════════════════════
// Sport selection bottom sheet
// ══════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SportSelectionSheet(userId: String, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val sportPref = remember { SportPreference(context) }

    var allSports by remember { mutableStateOf<List<SportDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val selectedIds = remember { mutableStateListOf<String>() }
    val currentIds by sportPref.selectedSportIds.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        try {
            allSports = ViewerRepository().getSports()
            selectedIds.addAll(currentIds)
        } catch (_: Exception) {}
        loading = false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text("Виды спорта", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Выберите 1-3 вида спорта", fontSize = 13.sp, color = TextMuted)
            Spacer(Modifier.height(16.dp))

            if (loading) {
                LoadingScreen()
            } else {
                allSports.forEach { sport ->
                    val isSelected = selectedIds.contains(sport.id)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable {
                                if (isSelected) selectedIds.remove(sport.id)
                                else if (selectedIds.size < 3) selectedIds.add(sport.id)
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Accent.copy(alpha = 0.1f) else Color.Transparent
                    ) {
                        Text(
                            sport.name,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            fontSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Accent else TextPrimary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = selectedIds.isNotEmpty()) {
                            scope.launch {
                                val selected = allSports.filter { selectedIds.contains(it.id) }
                                if (selected.isNotEmpty()) {
                                    sportPref.setSports(selected.map { it.id }, selected.map { it.name })
                                }
                                onDismiss()
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedIds.isNotEmpty()) Accent else TextMuted
                ) {
                    Text(
                        "Сохранить",
                        modifier = Modifier.padding(vertical = 14.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
