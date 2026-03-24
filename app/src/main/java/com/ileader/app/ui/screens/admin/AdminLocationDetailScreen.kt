package com.ileader.app.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.mock.AdminMockData
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.viewmodels.AdminLocationsViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminLocationDetailScreen(
    locationId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val viewModel: AdminLocationsViewModel = viewModel()
    val state by viewModel.detailState.collectAsState()

    LaunchedEffect(locationId) { viewModel.loadDetail(locationId) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = s.message, onRetry = { viewModel.loadDetail(locationId) })
        is UiState.Success -> {
            val location = s.data.location
            val locationTournaments = s.data.tournaments
            var showDeleteDialog by remember { mutableStateOf(false) }
            val typeColor = AdminMockData.locationTypeColor(location.type ?: "")

            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                BackHeader("Локация", onBack)

                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp).padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DarkCard {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadge(text = AdminMockData.locationTypeLabel(location.type ?: ""), color = typeColor)
                                if (location.rating != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = ILeaderColors.Warning.copy(alpha = 0.12f)
                                    ) {
                                        Row(
                                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Star, null, tint = ILeaderColors.Warning, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(2.dp))
                                            Text("${location.rating}", fontSize = 15.sp, fontWeight = FontWeight.Bold,
                                                color = ILeaderColors.Warning)
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(location.name, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                                color = TextPrimary, letterSpacing = (-0.3).sp)
                            Spacer(Modifier.height(6.dp))
                            Text("${location.address ?: ""}, ${location.city ?: ""}", fontSize = 14.sp, color = TextSecondary)

                            Spacer(Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onEdit,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Редактировать")
                                }
                                OutlinedButton(
                                    onClick = { showDeleteDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                                    border = BorderStroke(0.5.dp, CardBorder)
                                ) {
                                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = TextMuted)
                                }
                            }
                        }
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatItem(modifier = Modifier.weight(1f), icon = Icons.Default.People,
                            value = "${location.capacity ?: 0}", label = "Вместимость")
                        StatItem(modifier = Modifier.weight(1f), icon = Icons.Default.EmojiEvents,
                            value = "${locationTournaments.size}", label = "Турниров")
                    }

                    if ((location.description ?: "").isNotEmpty()) {
                        DarkCard {
                            Column(Modifier.padding(16.dp)) {
                                AdminSectionTitle("Описание")
                                Spacer(Modifier.height(8.dp))
                                Text(location.description ?: "", fontSize = 14.sp, color = TextPrimary, lineHeight = 20.sp)
                            }
                        }
                    }

                    if ((location.facilities ?: emptyList()).isNotEmpty()) {
                        DarkCard {
                            Column(Modifier.padding(16.dp)) {
                                AdminSectionTitle("Удобства и услуги")
                                Spacer(Modifier.height(12.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    (location.facilities ?: emptyList()).forEach { facility ->
                                        Surface(shape = RoundedCornerShape(8.dp), color = CardBorder.copy(alpha = 0.5f)) {
                                            Text(facility, Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                fontSize = 13.sp, color = TextPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (location.phone != null || location.email != null || location.website != null) {
                        DarkCard {
                            Column(Modifier.padding(16.dp)) {
                                AdminSectionTitle("Контактная информация")
                                Spacer(Modifier.height(12.dp))
                                if (location.phone != null) ContactRow(Icons.Default.Phone, "Телефон", location.phone)
                                if (location.email != null) ContactRow(Icons.Default.Email, "Email", location.email)
                                if (location.website != null) ContactRow(Icons.Default.Language, "Сайт", location.website)
                            }
                        }
                    }

                    if (locationTournaments.isNotEmpty()) {
                        DarkCard {
                            Column(Modifier.padding(16.dp)) {
                                AdminSectionTitle("Турниры на этой локации")
                                Spacer(Modifier.height(12.dp))
                                locationTournaments.forEachIndexed { index, t ->
                                    Row(
                                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(t.name, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                                            Text("${t.sportName ?: ""} • ${formatShortDate(t.startDate)} — ${formatShortDate(t.endDate)}",
                                                fontSize = 12.sp, color = TextSecondary)
                                        }
                                        StatusBadge(
                                            text = AdminMockData.statusLabel(t.status ?: ""),
                                            color = AdminMockData.statusColor(t.status ?: "")
                                        )
                                    }
                                    if (index < locationTournaments.lastIndex) {
                                        Spacer(Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    containerColor = CardBg,
                    titleContentColor = TextPrimary,
                    textContentColor = TextSecondary,
                    title = { Text("Удалить локацию?") },
                    text = { Text("Это действие нельзя отменить.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false
                            viewModel.deleteLocation(locationId)
                            onBack()
                        }) { Text("Удалить", color = Accent) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена", color = TextSecondary) }
                    }
                )
            }
        }
    }
}

@Composable
private fun ContactRow(icon: ImageVector, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                .background(CardBorder.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 12.sp, color = TextMuted)
            Text(value, fontSize = 14.sp, color = TextPrimary)
        }
    }
}
