package com.ileader.app.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.ui.components.ILeaderButton
import com.ileader.app.ui.components.ILeaderInputField
import com.ileader.app.ui.components.ILeaderOutlinedButton
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors

@Composable
fun ForgotPasswordScreen(
    state: AuthState,
    onResetPassword: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onClearError: () -> Unit
) {
    val colors = LocalAppColors.current
    var email by remember { mutableStateOf("") }
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
                .padding(horizontal = 24.dp)
        ) {
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

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedContent(
                targetState = state.passwordResetSent,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.95f) togetherWith
                            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 1.05f)
                },
                label = "resetState"
            ) { isSent ->
                if (isSent) {
                    SuccessContent(onNavigateToLogin = onNavigateToLogin)
                } else {
                    FormContent(
                        email = email,
                        state = state,
                        onEmailChange = { email = it; onClearError() },
                        onResetPassword = {
                            focusManager.clearFocus()
                            onResetPassword(email.trim())
                        },
                        focusManager = focusManager
                    )
                }
            }
        }
    }
}

@Composable
private fun FormContent(
    email: String,
    state: AuthState,
    onEmailChange: (String) -> Unit,
    onResetPassword: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    val colors = LocalAppColors.current
    Column {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
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
                imageVector = Icons.Default.LockReset,
                contentDescription = null,
                tint = ILeaderColors.PrimaryRed,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Восстановление\nпароля",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.textPrimary,
            lineHeight = 34.sp,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Введите email, привязанный к вашему аккаунту. Мы отправим ссылку для сброса пароля.",
            fontSize = 14.sp,
            color = colors.textSecondary,
            lineHeight = 21.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

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
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Email",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                )

                ILeaderInputField(
                    value = email,
                    onValueChange = onEmailChange,
                    placeholder = "your@email.com",
                    leadingIcon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (email.isNotBlank()) onResetPassword()
                        }
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                ILeaderButton(
                    text = "Отправить ссылку",
                    onClick = onResetPassword,
                    enabled = email.isNotBlank(),
                    isLoading = state.isLoading,
                    icon = Icons.AutoMirrored.Filled.Send
                )
            }
        }
    }
}

@Composable
private fun SuccessContent(
    onNavigateToLogin: () -> Unit
) {
    val colors = LocalAppColors.current
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "successScale"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .size(88.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            ILeaderColors.Success.copy(alpha = 0.2f),
                            ILeaderColors.SuccessLight.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MarkEmailRead,
                contentDescription = null,
                tint = ILeaderColors.Success,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Ссылка отправлена!",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.3).sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Проверьте вашу почту и перейдите\nпо ссылке для сброса пароля",
            fontSize = 14.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        ILeaderOutlinedButton(
            text = "Вернуться к входу",
            onClick = onNavigateToLogin,
            icon = Icons.AutoMirrored.Filled.ArrowBack
        )
    }
}
