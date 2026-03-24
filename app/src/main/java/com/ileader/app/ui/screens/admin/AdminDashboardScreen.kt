package com.ileader.app.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.mock.AdminMockData
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.AdminDashboardViewModel


@Composable
fun AdminDashboardScreen(user: User) {
    var subScreen by remember { mutableStateOf<String?>(null) }

    when {
        subScreen == null -> DashboardContent(user) { subScreen = it }
        subScreen == "sports" -> AdminSportsScreen(
            onBack = { subScreen = null },
            onEditSport = { subScreen = "sport_edit:$it" }
        )
        subScreen?.startsWith("sport_edit:") == true -> {
            val id = subScreen?.removePrefix("sport_edit:") ?: return
            AdminSportEditScreen(sportId = id, onBack = { subScreen = "sports" })
        }
        subScreen == "locations" -> AdminLocationsScreen(
            onBack = { subScreen = null },
            onViewLocation = { subScreen = "location:$it" },
            onEditLocation = { subScreen = "location_edit:$it" }
        )
        subScreen?.startsWith("location:") == true -> {
            val id = subScreen?.removePrefix("location:") ?: return
            AdminLocationDetailScreen(
                locationId = id,
                onBack = { subScreen = "locations" },
                onEdit = { subScreen = "location_edit:$id" }
            )
        }
        subScreen?.startsWith("location_edit:") == true -> {
            val id = subScreen?.removePrefix("location_edit:") ?: return
            AdminLocationEditScreen(locationId = id, onBack = { subScreen = "locations" })
        }
        subScreen == "applications" -> AdminApplicationsScreen(onBack = { subScreen = null })
    }
}

@Composable
private fun DashboardContent(user: User, onNavigate: (String) -> Unit) {
    val viewModel: AdminDashboardViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewModel.load(); visible = true }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load() }
        is UiState.Success -> {
            val stats = s.data.stats

            val accentColor = Accent
            Box(Modifier.fillMaxSize().background(Bg)) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(accentColor.copy(alpha = 0.06f), Color.Transparent),
                            center = Offset(size.width * 0.85f, size.height * 0.03f),
                            radius = 280.dp.toPx()
                        ),
                        radius = 280.dp.toPx(),
                        center = Offset(size.width * 0.85f, size.height * 0.03f)
                    )
                }

                Column(
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(16.dp))

                    // Header
                    FadeIn(visible, 0) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            ILeaderBrandHeader(role = user.role)
                            UserAvatar(avatarUrl = user.avatarUrl, displayName = user.displayName)
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Stats 2x2
                    FadeIn(visible, 200) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatItem(Modifier.weight(1f), Icons.Default.People, "${stats.totalUsers}", "Пользователей")
                            StatItem(Modifier.weight(1f), Icons.Default.EmojiEvents, "${stats.activeTournaments}", "Турниров")
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            StatItem(Modifier.weight(1f), Icons.Default.SportsSoccer, "${stats.totalSports}", "Видов спорта")
                            StatItem(Modifier.weight(1f), Icons.Default.Block, "${stats.blockedUsers}", "Заблокировано")
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // User growth chart
                    FadeIn(visible, 400) {
                        SectionHeader("Рост пользователей")
                        Spacer(Modifier.height(12.dp))
                        DarkCard {
                            SimpleBarChart(
                                data = AdminMockData.userGrowth,
                                modifier = Modifier.fillMaxWidth().height(160.dp).padding(16.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Role distribution
                    FadeIn(visible, 500) {
                        SectionHeader("Распределение по ролям")
                        Spacer(Modifier.height(12.dp))
                        DarkCard {
                            Column(Modifier.padding(16.dp)) {
                                val roleDistribution = s.data.roleDistribution
                                val total = roleDistribution.sumOf { it.second }
                                roleDistribution.forEach { pair ->
                                    Row(
                                        Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(pair.first, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.weight(1f))
                                        Text("${pair.second}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                        Spacer(Modifier.width(12.dp))
                                        Box(
                                            Modifier.width(80.dp).height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)).background(CardBorder)
                                        ) {
                                            Box(
                                                Modifier.fillMaxHeight()
                                                    .fillMaxWidth(fraction = if (total > 0) pair.second.toFloat() / total else 0f)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(Brush.horizontalGradient(listOf(Accent, AccentDark)))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Tournaments by sport
                    FadeIn(visible, 600) {
                        SectionHeader("Турниры по видам спорта")
                        Spacer(Modifier.height(12.dp))
                        DarkCard {
                            SimpleBarChart(
                                data = AdminMockData.tournamentsBySport,
                                modifier = Modifier.fillMaxWidth().height(160.dp).padding(16.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    // Quick actions
                    FadeIn(visible, 700) {
                        SectionHeader("Быстрые действия")
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            QuickAction(Modifier.weight(1f), Icons.AutoMirrored.Filled.Assignment, "Заявки") { onNavigate("applications") }
                            QuickAction(Modifier.weight(1f), Icons.Default.SportsSoccer, "Виды спорта") { onNavigate("sports") }
                            QuickAction(Modifier.weight(1f), Icons.Default.LocationOn, "Локации") { onNavigate("locations") }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickAction(modifier: Modifier, icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(14.dp)).clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = CardBg
    ) {
        Column(
            Modifier.border(0.5.dp, CardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(Accent, AccentDark))),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun SimpleBarChart(data: List<AdminMockData.ChartPoint>, modifier: Modifier = Modifier) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
        data.forEach { point ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text("${point.value}", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier.width(28.dp)
                        .fillMaxHeight(fraction = (point.value.toFloat() / maxValue).coerceIn(0.05f, 1f))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(Brush.verticalGradient(listOf(Accent, AccentDark)))
                )
                Spacer(Modifier.height(4.dp))
                Text(point.label, fontSize = 10.sp, color = TextMuted, textAlign = TextAlign.Center)
            }
        }
    }
}
