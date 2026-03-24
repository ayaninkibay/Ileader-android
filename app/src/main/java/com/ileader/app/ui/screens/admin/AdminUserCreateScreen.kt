package com.ileader.app.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.mock.AdminMockData
import com.ileader.app.data.models.AthleteSubtype
import com.ileader.app.data.models.UserRole
import com.ileader.app.ui.components.*
// TODO: When Edge Function for user creation is ready, add:
// import androidx.lifecycle.viewmodel.compose.viewModel
// import com.ileader.app.data.remote.UiState
// import com.ileader.app.data.remote.dto.AdminCreateUserRequest
// import com.ileader.app.ui.viewmodels.AdminUserCreateViewModel

// TODO: Создание пользователей требует Edge Function (admin-create-user).
//  Когда Edge Function будет готова, заменить mock-логику на вызов ViewModel.
@Composable
fun AdminUserCreateScreen(onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.ATHLETE) }
    var selectedSubtype by remember { mutableStateOf<AthleteSubtype?>(null) }
    var showSaved by remember { mutableStateOf(false) }
    var showUnavailable by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showUnavailable) {
        if (showUnavailable) {
            snackbarHostState.showSnackbar("Создание пользователей через БД пока не доступно")
            showUnavailable = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Bg
    ) { _ ->
    Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        BackHeader("Создание пользователя", onBack)

        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showSaved) {
                SuccessBanner("Пользователь создан")
            }

            DarkCard {
                Column(Modifier.padding(14.dp)) {
                    Text("Рекомендации", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Accent)
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "Укажите действующий email для приглашения",
                        "Для спортсменов выберите подтип (пилот, стрелок и т.д.)",
                        "Выберите виды спорта для пользователя",
                        "Письмо-приглашение будет отправлено автоматически"
                    ).forEach { tip ->
                        Row(Modifier.padding(vertical = 2.dp)) {
                            Text("•", color = Accent, fontSize = 13.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(tip, fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp)
                        }
                    }
                }
            }

            DarkCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    DarkFormField("Имя *", name, { name = it }, placeholder = "Введите имя")
                    DarkFormField("Email *", email, { email = it }, placeholder = "Введите email")
                    PasswordField(
                        label = "Пароль *",
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Введите пароль"
                    )
                    DarkFormField("Телефон", phone, { phone = it }, placeholder = "+7 777 000 0000")
                    DarkFormField("Город", city, { city = it }, placeholder = "Алматы")

                    FieldLabel("Роль *") {
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
                }
            }

            Button(
                onClick = {
                    // TODO: заменить на вызов ViewModel когда Edge Function будет готова
                    showUnavailable = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Создать пользователя", fontWeight = FontWeight.SemiBold)
            }
        }
    }
    } // Scaffold
}

@Composable
private fun PasswordField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = if (isFocused) Accent.copy(alpha = 0.5f) else CardBorder

    Column {
        Text(label, fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = CardBg) {
            BasicTextField(
                value = value, onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth()
                    .border(0.5.dp, borderColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .onFocusChanged { isFocused = it.isFocused },
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                cursorBrush = SolidColor(Accent),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) Text(placeholder, color = TextMuted, fontSize = 14.sp)
                        innerTextField()
                    }
                }
            )
        }
    }
}
