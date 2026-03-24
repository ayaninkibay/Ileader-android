package com.ileader.app.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.SportUpdateDto
import com.ileader.app.data.remote.dto.SportWithCountsDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.viewmodels.AdminSportsViewModel
import com.ileader.app.ui.viewmodels.AdminUserEditViewModel

@Composable
fun AdminSportEditScreen(sportId: String, onBack: () -> Unit) {
    val viewModel: AdminSportsViewModel = viewModel()
    val state by viewModel.sportDetail.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(sportId) { viewModel.loadSportDetail(sportId) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.loadSportDetail(sportId) }
        is UiState.Success -> {
            val sport = s.data

            var name by remember(sport) { mutableStateOf(sport.name) }
            var slug by remember(sport) { mutableStateOf(sport.slug ?: "") }
            var description by remember(sport) { mutableStateOf(sport.description ?: "") }
            var isActive by remember(sport) { mutableStateOf(sport.isActive) }

            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                BackHeader("Редактирование: ${sport.name}", onBack)

                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp).padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (saveState is AdminUserEditViewModel.SaveState.Success) {
                        SuccessBanner("Вид спорта обновлён")
                    }
                    if (saveState is AdminUserEditViewModel.SaveState.Error) {
                        Surface(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = ILeaderColors.LightRed.copy(alpha = 0.12f)
                        ) {
                            Text(
                                (saveState as AdminUserEditViewModel.SaveState.Error).message,
                                modifier = Modifier.padding(12.dp),
                                color = ILeaderColors.LightRed,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }

                    DarkCard {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            DarkFormField("Название", name, { name = it })
                            DarkFormField("Slug (URL)", slug, { slug = it })
                            DarkFormField("Описание", description, { description = it }, singleLine = false, minLines = 3)
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Активен", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                                Switch(
                                    checked = isActive,
                                    onCheckedChange = { isActive = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Accent,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = CardBorder
                                    )
                                )
                            }
                        }
                    }

                    DarkCard {
                        Column(Modifier.padding(16.dp)) {
                            AdminSectionTitle("Статистика")
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                Column {
                                    Text("${sport.athleteCount}", fontWeight = FontWeight.Bold, fontSize = 20.sp,
                                        color = Accent, letterSpacing = (-0.3).sp)
                                    Text("Спортсменов", fontSize = 12.sp, color = TextSecondary)
                                }
                                Column {
                                    Text("${sport.tournamentCount}", fontWeight = FontWeight.Bold, fontSize = 20.sp,
                                        color = Accent, letterSpacing = (-0.3).sp)
                                    Text("Турниров", fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.updateSport(
                                sportId,
                                SportUpdateDto(
                                    name = name,
                                    athleteLabel = null,
                                    isActive = isActive
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = saveState !is AdminUserEditViewModel.SaveState.Saving,
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        if (saveState is AdminUserEditViewModel.SaveState.Saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Сохранить изменения", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
