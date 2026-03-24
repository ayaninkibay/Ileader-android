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
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.SignUpData
import com.ileader.app.data.models.UserRole
import com.ileader.app.ui.components.ILeaderButton
import com.ileader.app.ui.components.ILeaderInputField
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors

private enum class RegisterStep {
    CHOOSE_TYPE,
    VIEWER_FORM,
    PARTICIPANT_FORM
}

@Composable
fun RegisterScreen(
    state: AuthState,
    onSignUp: (SignUpData) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    onClearError: () -> Unit
) {
    val colors = LocalAppColors.current
    var currentStep by remember { mutableStateOf(RegisterStep.CHOOSE_TYPE) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
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
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        when (currentStep) {
                            RegisterStep.CHOOSE_TYPE -> onNavigateBack()
                            else -> {
                                currentStep = RegisterStep.CHOOSE_TYPE
                                onClearError()
                            }
                        }
                    }
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

                Spacer(modifier = Modifier.weight(1f))

                StepIndicator(
                    currentStep = if (currentStep == RegisterStep.CHOOSE_TYPE) 1 else 2,
                    totalSteps = 2
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState.ordinal > initialState.ordinal) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "stepTransition"
            ) { step ->
                when (step) {
                    RegisterStep.CHOOSE_TYPE -> ChooseTypeStep(
                        onChooseViewer = {
                            currentStep = RegisterStep.VIEWER_FORM
                            onClearError()
                        },
                        onChooseParticipant = {
                            currentStep = RegisterStep.PARTICIPANT_FORM
                            onClearError()
                        }
                    )
                    RegisterStep.VIEWER_FORM -> RegistrationForm(
                        title = "Регистрация зрителя",
                        subtitle = "Смотрите турниры и следите за результатами",
                        name = name, email = email, password = password,
                        phone = phone, city = city, passwordVisible = passwordVisible,
                        selectedRole = null, showRoleSelection = false, state = state,
                        onNameChange = { name = it },
                        onEmailChange = { email = it; onClearError() },
                        onPasswordChange = { password = it; onClearError() },
                        onPhoneChange = { phone = it },
                        onCityChange = { city = it },
                        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                        onRoleSelected = {},
                        onSubmit = {
                            focusManager.clearFocus()
                            onSignUp(SignUpData(
                                name = name.trim(), email = email.trim(),
                                password = password, phone = phone.trim(),
                                city = city.trim(), role = UserRole.USER
                            ))
                        },
                        focusManager = focusManager
                    )
                    RegisterStep.PARTICIPANT_FORM -> RegistrationForm(
                        title = "Регистрация участника",
                        subtitle = "Выберите свою роль на платформе",
                        name = name, email = email, password = password,
                        phone = phone, city = city, passwordVisible = passwordVisible,
                        selectedRole = selectedRole, showRoleSelection = true, state = state,
                        onNameChange = { name = it },
                        onEmailChange = { email = it; onClearError() },
                        onPasswordChange = { password = it; onClearError() },
                        onPhoneChange = { phone = it },
                        onCityChange = { city = it },
                        onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                        onRoleSelected = { selectedRole = it },
                        onSubmit = {
                            focusManager.clearFocus()
                            onSignUp(SignUpData(
                                name = name.trim(), email = email.trim(),
                                password = password, phone = phone.trim(),
                                city = city.trim(), role = selectedRole ?: UserRole.ATHLETE
                            ))
                        },
                        focusManager = focusManager
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Уже есть аккаунт? ",
                    color = colors.textSecondary,
                    fontSize = 14.sp
                )
                Text(
                    text = "Войти",
                    color = ILeaderColors.PrimaryRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    val colors = LocalAppColors.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index + 1 <= currentStep
            val isCurrent = index + 1 == currentStep
            Box(
                modifier = Modifier
                    .width(if (isCurrent) 28.dp else 10.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (isActive) Brush.horizontalGradient(
                            listOf(ILeaderColors.PrimaryRed, ILeaderColors.DarkRed)
                        ) else Brush.horizontalGradient(
                            listOf(colors.border, colors.border)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "$currentStep/$totalSteps",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun ChooseTypeStep(
    onChooseViewer: () -> Unit,
    onChooseParticipant: () -> Unit
) {
    val colors = LocalAppColors.current
    Column {
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
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                tint = ILeaderColors.PrimaryRed,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Создать аккаунт",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.textPrimary,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Выберите тип аккаунта",
            fontSize = 15.sp,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        AccountTypeCard(
            icon = Icons.Default.Visibility,
            title = "Зритель",
            description = "Смотрите турниры, следите за результатами и статистикой спортсменов",
            color = ILeaderColors.Info,
            onClick = onChooseViewer
        )

        Spacer(modifier = Modifier.height(12.dp))

        AccountTypeCard(
            icon = Icons.Default.Groups,
            title = "Участник",
            description = "Полный доступ: спортсмен, тренер, организатор, судья, спонсор или СМИ",
            color = ILeaderColors.PrimaryRed,
            onClick = onChooseParticipant
        )
    }
}

@Composable
private fun AccountTypeCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = colors.cardBg
    ) {
        Row(
            modifier = Modifier
                .border(
                    width = 0.5.dp,
                    color = colors.border.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.linearGradient(
                            listOf(color.copy(alpha = 0.2f), color.copy(alpha = 0.08f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun RegistrationForm(
    title: String,
    subtitle: String,
    name: String,
    email: String,
    password: String,
    phone: String,
    city: String,
    passwordVisible: Boolean,
    selectedRole: UserRole?,
    showRoleSelection: Boolean,
    state: AuthState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onRoleSelected: (UserRole) -> Unit,
    onSubmit: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    val colors = LocalAppColors.current
    Column {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.textPrimary,
            letterSpacing = (-0.3).sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Error
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

        // Role selection
        if (showRoleSelection) {
            Text(
                text = "Выберите роль",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            RoleSelectionGrid(
                selectedRole = selectedRole,
                onRoleSelected = onRoleSelected
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Form card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = colors.cardBg,
            border = ButtonDefaults.outlinedButtonBorder(true).copy(
                brush = Brush.linearGradient(
                    listOf(
                        colors.border.copy(alpha = 0.4f),
                        colors.border.copy(alpha = 0.15f)
                    )
                )
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ILeaderInputField(
                    value = name, onValueChange = onNameChange,
                    placeholder = "Имя и фамилия", leadingIcon = Icons.Default.Person,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                Spacer(modifier = Modifier.height(10.dp))
                ILeaderInputField(
                    value = email, onValueChange = onEmailChange,
                    placeholder = "Email", leadingIcon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                Spacer(modifier = Modifier.height(10.dp))
                ILeaderInputField(
                    value = password, onValueChange = onPasswordChange,
                    placeholder = "Пароль", leadingIcon = Icons.Default.Lock,
                    isPassword = true, passwordVisible = passwordVisible,
                    onPasswordVisibilityToggle = onPasswordVisibilityToggle,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                Spacer(modifier = Modifier.height(10.dp))
                ILeaderInputField(
                    value = phone, onValueChange = onPhoneChange,
                    placeholder = "Телефон", leadingIcon = Icons.Default.Phone,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                Spacer(modifier = Modifier.height(10.dp))
                ILeaderInputField(
                    value = city, onValueChange = onCityChange,
                    placeholder = "Город", leadingIcon = Icons.Default.LocationCity,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
            }
        }

        // Verification notice
        if (showRoleSelection && selectedRole != null && selectedRole.requiresVerification) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = ILeaderColors.Warning.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = ILeaderColors.Warning,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Роль «${selectedRole.displayName}» требует подтверждения администратором",
                        fontSize = 12.sp,
                        color = ILeaderColors.Warning,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val isFormValid = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() &&
                (!showRoleSelection || selectedRole != null)

        ILeaderButton(
            text = "Создать аккаунт",
            onClick = onSubmit,
            enabled = isFormValid,
            isLoading = state.isLoading,
            icon = Icons.Default.HowToReg
        )
    }
}

@Composable
private fun RoleSelectionGrid(
    selectedRole: UserRole?,
    onRoleSelected: (UserRole) -> Unit
) {
    val colors = LocalAppColors.current
    val roles = listOf(
        Triple(UserRole.ATHLETE, Icons.Default.SportsKabaddi, ILeaderColors.AthleteColor),
        Triple(UserRole.TRAINER, Icons.Default.School, ILeaderColors.TrainerColor),
        Triple(UserRole.ORGANIZER, Icons.AutoMirrored.Filled.EventNote, ILeaderColors.OrganizerColor),
        Triple(UserRole.REFEREE, Icons.Default.Gavel, ILeaderColors.RefereeColor),
        Triple(UserRole.SPONSOR, Icons.Default.Handshake, ILeaderColors.SponsorColor),
        Triple(UserRole.MEDIA, Icons.Default.Videocam, ILeaderColors.MediaColor)
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        roles.chunked(3).forEach { rowRoles ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowRoles.forEach { (role, icon, color) ->
                    val isSelected = selectedRole == role
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onRoleSelected(role) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) color.copy(alpha = 0.12f) else colors.cardBg
                    ) {
                        Column(
                            modifier = Modifier
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) color.copy(alpha = 0.7f) else colors.border.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(vertical = 14.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) color.copy(alpha = 0.2f)
                                        else colors.cardHover.copy(alpha = 0.5f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) color else colors.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = role.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) color else colors.textSecondary,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
                repeat(3 - rowRoles.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
