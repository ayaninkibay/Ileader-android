package com.ileader.app.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.ui.components.ILeaderButton
import com.ileader.app.ui.components.ILeaderInputField
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors

@Composable
fun LoginScreen(
    state: AuthState,
    onSignIn: (String, String) -> Unit,
    onDemoLogin: ((com.ileader.app.data.models.UserRole) -> Unit)? = null,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateBack: () -> Unit,
    onClearError: () -> Unit
) {
    val colors = LocalAppColors.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Back button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.cardBg)
                        .border(0.5.dp, colors.border.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Header
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            brush = Brush.linearGradient(
                                listOf(
                                    ILeaderColors.PrimaryRed.copy(alpha = 0.2f),
                                    ILeaderColors.DarkRed.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = ILeaderColors.PrimaryRed,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Вход в аккаунт",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Добро пожаловать в iLeader",
                    fontSize = 15.sp,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Form card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = colors.cardBg,
                border = ButtonDefaults.outlinedButtonBorder(true).copy(
                    brush = Brush.linearGradient(
                        listOf(
                            colors.border.copy(alpha = 0.5f),
                            colors.border.copy(alpha = 0.2f)
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Error message
                    AnimatedVisibility(
                        visible = state.errorMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = ILeaderColors.Error.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = ILeaderColors.Error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = state.errorMessage ?: "",
                                    color = ILeaderColors.Error,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Email
                    Text(
                        text = "Email",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                    )

                    ILeaderInputField(
                        value = email,
                        onValueChange = { email = it; onClearError() },
                        placeholder = "your@email.com",
                        leadingIcon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password
                    Text(
                        text = "Пароль",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                    )

                    ILeaderInputField(
                        value = password,
                        onValueChange = { password = it; onClearError() },
                        placeholder = "Введите пароль",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (email.isNotBlank() && password.isNotBlank()) {
                                    onSignIn(email.trim(), password)
                                }
                            }
                        )
                    )

                    // Forgot password
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Забыли пароль?",
                            color = ILeaderColors.PrimaryRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onNavigateToForgotPassword() }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sign in button
                    ILeaderButton(
                        text = "Войти",
                        onClick = {
                            focusManager.clearFocus()
                            onSignIn(email.trim(), password)
                        },
                        enabled = email.isNotBlank() && password.isNotBlank(),
                        isLoading = state.isLoading,
                        icon = Icons.AutoMirrored.Filled.Login
                    )
                }
            }

            // === DEMO ACCOUNTS ===
            if (onDemoLogin != null) {
                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Быстрый вход (демо)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(start = 2.dp, bottom = 10.dp)
                )

                val demoRoles = listOf(
                    Triple(com.ileader.app.data.models.UserRole.ATHLETE, "Спортсмен", ILeaderColors.AthleteColor),
                    Triple(com.ileader.app.data.models.UserRole.TRAINER, "Тренер", ILeaderColors.TrainerColor),
                    Triple(com.ileader.app.data.models.UserRole.ORGANIZER, "Организатор", ILeaderColors.OrganizerColor),
                    Triple(com.ileader.app.data.models.UserRole.REFEREE, "Судья", ILeaderColors.RefereeColor),
                    Triple(com.ileader.app.data.models.UserRole.SPONSOR, "Спонсор", ILeaderColors.SponsorColor),
                    Triple(com.ileader.app.data.models.UserRole.MEDIA, "СМИ", ILeaderColors.MediaColor),
                    Triple(com.ileader.app.data.models.UserRole.ADMIN, "Админ", ILeaderColors.AdminColor),
                    Triple(com.ileader.app.data.models.UserRole.USER, "Зритель", ILeaderColors.ViewerColor),
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (row in demoRoles.chunked(4)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { (role, label, color) ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onDemoLogin(role) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = color.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = color,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Register link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Нет аккаунта? ",
                    color = colors.textSecondary,
                    fontSize = 14.sp
                )
                Text(
                    text = "Зарегистрироваться",
                    color = ILeaderColors.PrimaryRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }
}
