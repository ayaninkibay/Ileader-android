package com.ileader.app.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.mock.AdminMockData
import com.ileader.app.data.models.AthleteSubtype
import com.ileader.app.data.models.UserRole
import com.ileader.app.data.models.UserStatus
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.AdminUserUpdateDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.viewmodels.AdminUserEditViewModel

@Composable
fun AdminUserEditScreen(userId: String, onBack: () -> Unit) {
    val viewModel: AdminUserEditViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(userId) { viewModel.load(userId) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(userId) }
        is UiState.Success -> {
            val user = s.data.user

            var name by remember(user) { mutableStateOf(user.name) }
            var email by remember(user) { mutableStateOf(user.email) }
            var phone by remember(user) { mutableStateOf(user.phone ?: "") }
            var city by remember(user) { mutableStateOf(user.city ?: "") }
            var selectedRole by remember(user) { mutableStateOf(user.role) }
            var selectedSubtype by remember(user) { mutableStateOf(user.athleteSubtype) }
            var status by remember(user) { mutableStateOf(user.status) }

            Column(Modifier.fillMaxSize().statusBarsPadding()) {
                BackHeader("Редактирование пользователя", onBack)

                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp).padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (saveState is AdminUserEditViewModel.SaveState.Success) {
                        SuccessBanner("Пользователь обновлён")
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
                            DarkFormField("Имя", name, { name = it })
                            DarkFormField("Email", email, { email = it })
                            DarkFormField("Телефон", phone, { phone = it }, placeholder = "+7 777 000 0000")
                            DarkFormField("Город", city, { city = it }, placeholder = "Алматы")

                            FieldLabel("Роль") {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    UserRole.entries.chunked(2).forEach { row ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            row.forEach { role ->
                                                val rColor = AdminMockData.roleColor(role)
                                                FilterChip(
                                                    selected = selectedRole == role,
                                                    onClick = { selectedRole = role },
                                                    label = { Text(role.displayName, fontSize = 13.sp) },
                                                    modifier = Modifier.weight(1f),
                                                    colors = FilterChipDefaults.filterChipColors(
                                                        selectedContainerColor = rColor,
                                                        selectedLabelColor = Color.White,
                                                        containerColor = CardBg,
                                                        labelColor = TextSecondary
                                                    ),
                                                    border = FilterChipDefaults.filterChipBorder(
                                                        borderColor = CardBorder,
                                                        selectedBorderColor = Color.Transparent,
                                                        enabled = true, selected = selectedRole == role
                                                    ),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                            }
                                            if (row.size == 1) Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            if (selectedRole == UserRole.ATHLETE) {
                                FieldLabel("Тип спортсмена") {
                                    Row(
                                        Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        AthleteSubtype.entries.forEach { subtype ->
                                            DarkFilterChip(
                                                text = subtype.displayName,
                                                selected = selectedSubtype == subtype,
                                                onClick = { selectedSubtype = subtype }
                                            )
                                        }
                                    }
                                }
                            }

                            FieldLabel("Статус") {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = status == UserStatus.ACTIVE,
                                        onClick = { status = UserStatus.ACTIVE },
                                        label = { Text("Активен") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Accent,
                                            selectedLabelColor = Color.White,
                                            containerColor = CardBg,
                                            labelColor = TextSecondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = CardBorder,
                                            selectedBorderColor = Color.Transparent,
                                            enabled = true, selected = status == UserStatus.ACTIVE
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    FilterChip(
                                        selected = status == UserStatus.BLOCKED,
                                        onClick = { status = UserStatus.BLOCKED },
                                        label = { Text("Заблокирован") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TextMuted,
                                            selectedLabelColor = Color.White,
                                            containerColor = CardBg,
                                            labelColor = TextSecondary
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = CardBorder,
                                            selectedBorderColor = Color.Transparent,
                                            enabled = true, selected = status == UserStatus.BLOCKED
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.save(
                                userId,
                                AdminUserUpdateDto(
                                    name = name,
                                    email = email,
                                    phone = phone.ifBlank { null },
                                    city = city.ifBlank { null },
                                    status = status.name.lowercase(),
                                    athleteSubtype = selectedSubtype?.name?.lowercase()
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
