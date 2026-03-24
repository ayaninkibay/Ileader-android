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
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.LocationInsertDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.AdminLocationsViewModel
import com.ileader.app.ui.viewmodels.AdminUserEditViewModel

@Composable
fun AdminLocationEditScreen(locationId: String, onBack: () -> Unit) {
    val viewModel: AdminLocationsViewModel = viewModel()
    val state by viewModel.detailState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    LaunchedEffect(locationId) { viewModel.loadDetail(locationId) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = s.message, onRetry = { viewModel.loadDetail(locationId) })
        is UiState.Success -> {
            val location = s.data.location

            var name by remember { mutableStateOf(location.name) }
            var type by remember { mutableStateOf(location.type ?: "") }
            var address by remember { mutableStateOf(location.address ?: "") }
            var city by remember { mutableStateOf(location.city ?: "") }
            var capacity by remember { mutableStateOf((location.capacity ?: 0).toString()) }
            var description by remember { mutableStateOf(location.description ?: "") }
            var phone by remember { mutableStateOf(location.phone ?: "") }
            var email by remember { mutableStateOf(location.email ?: "") }
            var website by remember { mutableStateOf(location.website ?: "") }
            var showSaved by remember { mutableStateOf(false) }

            LaunchedEffect(saveState) {
                if (saveState is AdminUserEditViewModel.SaveState.Success) {
                    showSaved = true
                    viewModel.resetSaveState()
                }
            }

            Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
                BackHeader("Редактирование локации", onBack)

                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp).padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (showSaved) {
                        SuccessBanner("Локация обновлена")
                    }

                    DarkCard {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            DarkFormField("Название", name, { name = it })

                            FieldLabel("Тип") {
                                Row(
                                    Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        "karting" to "Картодром", "shooting" to "Тир",
                                        "stadium" to "Стадион", "arena" to "Арена", "other" to "Другое"
                                    ).forEach { (key, label) ->
                                        val tColor = AdminMockData.locationTypeColor(key)
                                        FilterChip(
                                            selected = type == key,
                                            onClick = { type = key },
                                            label = { Text(label, fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = tColor,
                                                selectedLabelColor = Color.White,
                                                containerColor = CardBg,
                                                labelColor = TextSecondary
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                borderColor = CardBorder,
                                                selectedBorderColor = Color.Transparent,
                                                enabled = true,
                                                selected = type == key
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }
                            }

                            DarkFormField("Адрес", address, { address = it })
                            DarkFormField("Город", city, { city = it })
                            DarkFormField("Вместимость", capacity, { capacity = it })
                            DarkFormField("Описание", description, { description = it }, singleLine = false, minLines = 3)
                        }
                    }

                    DarkCard {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            AdminSectionTitle("Контактная информация")
                            DarkFormField("Телефон", phone, { phone = it }, placeholder = "+7 727 000 00 00")
                            DarkFormField("Email", email, { email = it }, placeholder = "email@example.com")
                            DarkFormField("Сайт", website, { website = it }, placeholder = "example.kz")
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.updateLocation(locationId, LocationInsertDto(
                                name = name,
                                type = type,
                                address = address.ifBlank { null },
                                city = city.ifBlank { null },
                                capacity = capacity.toIntOrNull(),
                                description = description.ifBlank { null },
                                ownerId = location.ownerId ?: "",
                                phone = phone.ifBlank { null },
                                email = email.ifBlank { null },
                                website = website.ifBlank { null }
                            ))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text("Сохранить изменения", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
