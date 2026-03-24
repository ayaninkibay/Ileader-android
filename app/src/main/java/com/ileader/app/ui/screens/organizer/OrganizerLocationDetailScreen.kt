package com.ileader.app.ui.screens.organizer

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.OrgLocationDetailData
import com.ileader.app.ui.viewmodels.OrganizerLocationDetailViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun OrganizerLocationDetailScreen(
    locationId: String,
    userId: String,
    onBack: () -> Unit,
    onEditClick: () -> Unit
) {
    val vm: OrganizerLocationDetailViewModel = viewModel()
    val state by vm.state.collectAsState()

    LaunchedEffect(locationId, userId) { vm.load(locationId, userId) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { vm.load(locationId, userId) }
        is UiState.Success -> LocationDetailContent(s.data, onBack, onEditClick)
    }
}

@Composable
private fun LocationDetailContent(
    data: OrgLocationDetailData,
    onBack: () -> Unit,
    onEditClick: () -> Unit
) {
    val location = data.location
    val tournamentsAtLocation = data.tournamentsAtLocation
    val facilities = location.facilities ?: emptyList()

    Box(Modifier.fillMaxSize().background(Bg)) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Back + Title
            BackHeader(location.name, onBack)

            // Subtitle
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(locationTypeLabel(location.type), Accent)
                Spacer(Modifier.width(8.dp))
                Text(location.city ?: "", color = TextSecondary, fontSize = 13.sp)
            }

            Spacer(Modifier.height(16.dp))

            // Edit button
            Button(
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Редактировать")
            }

            Spacer(Modifier.height(28.dp))

            // Info card
            DarkCardPadded {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SoftIconBox(Icons.Default.Info)
                    Spacer(Modifier.width(12.dp))
                    Text("Информация", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.3).sp)
                }
                Spacer(Modifier.height(16.dp))
                InfoRow(Icons.Default.LocationOn, "Адрес", "${location.address ?: ""}, ${location.city ?: ""}")
                if ((location.capacity ?: 0) > 0) {
                    InfoRow(Icons.Default.Groups, "Вместимость", "${location.capacity} чел.")
                }
                if (location.rating != null) {
                    InfoRow(Icons.Default.Star, "Рейтинг", "${location.rating}")
                }
                val description = location.description ?: ""
                if (description.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(description, fontSize = 14.sp, color = TextSecondary, lineHeight = 20.sp)
                }
            }

            Spacer(Modifier.height(28.dp))

            // Contacts card
            if (location.phone != null || location.email != null || location.website != null) {
                DarkCardPadded {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SoftIconBox(Icons.Default.ContactPhone)
                        Spacer(Modifier.width(12.dp))
                        Text("Контакты", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.3).sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    if (location.phone != null) {
                        InfoRow(Icons.Default.Phone, "Телефон", location.phone)
                    }
                    if (location.email != null) {
                        InfoRow(Icons.Default.Email, "Email", location.email)
                    }
                    if (location.website != null) {
                        InfoRow(Icons.Default.Language, "Сайт", location.website)
                    }
                }
                Spacer(Modifier.height(28.dp))
            }

            // Facilities
            if (facilities.isNotEmpty()) {
                DarkCardPadded {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SoftIconBox(Icons.Default.CheckCircle)
                        Spacer(Modifier.width(12.dp))
                        Text("Удобства", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.3).sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        facilities.forEach { facility ->
                            StatusBadge(facility, Accent)
                        }
                    }
                }
                Spacer(Modifier.height(28.dp))
            }

            // Statistics
            val tournamentCount = tournamentsAtLocation.size
            DarkCardPadded {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SoftIconBox(Icons.Default.BarChart)
                    Spacer(Modifier.width(12.dp))
                    Text("Статистика", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.3).sp)
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(tournamentCount.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Accent)
                        Text("турниров", fontSize = 12.sp, color = TextSecondary)
                    }
                    if (location.rating != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${location.rating}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Accent)
                            Text("рейтинг", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // Tournaments at this location
            if (tournamentsAtLocation.isNotEmpty()) {
                Spacer(Modifier.height(28.dp))
                DarkCardPadded {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AccentIconBox(Icons.Default.EmojiEvents)
                        Spacer(Modifier.width(12.dp))
                        Text("Турниры на локации", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.3).sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    tournamentsAtLocation.forEach { tournament ->
                        Row(
                            modifier = Modifier.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SoftIconBox(Icons.Default.EmojiEvents, size = 36.dp, iconSize = 18.dp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(tournament.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                Text("${tournament.sportName ?: ""} \u2022 ${tournament.startDate ?: ""}", fontSize = 11.sp, color = TextSecondary)
                            }
                            val isActive = isActiveStatus(tournament.status)
                            StatusBadge(statusLabel(tournament.status), if (isActive) Accent else TextMuted)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text(value, fontSize = 14.sp, color = TextPrimary)
        }
    }
}
