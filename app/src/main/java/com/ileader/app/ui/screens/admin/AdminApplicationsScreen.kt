package com.ileader.app.ui.screens.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.mock.AdminMockData
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.viewmodels.AdminRequestsViewModel

@Composable
fun AdminApplicationsScreen(onBack: () -> Unit) {
    val viewModel: AdminRequestsViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = s.message, onRetry = { viewModel.load() })
        is UiState.Success -> {
            val registrations = s.data.verifications

            Column(Modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
                BackHeader("Заявки на регистрацию", onBack)

                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp).padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoBanner(
                        "Автоматическая регистрация — все пользователи могут зарегистрироваться " +
                                "самостоятельно. Подтверждение администратора не требуется."
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MiniStat("Ожидают", "${registrations.size}", modifier = Modifier.weight(1f))
                        MiniStat("Статус", "Авто", modifier = Modifier.weight(1f))
                    }

                    if (registrations.isNotEmpty()) {
                        AdminSectionTitle("Ожидают верификации (${registrations.size})")
                        registrations.forEach { user ->
                            RegistrationCard(
                                user = user,
                                onApprove = { viewModel.approveVerification(user.id) },
                                onDecline = { viewModel.declineVerification(user.id) }
                            )
                        }
                    }

                    if (registrations.isEmpty()) {
                        EmptyState("Нет заявок на верификацию")
                    }
                }
            }
        }
    }
}

@Composable
private fun RegistrationCard(
    user: User,
    onApprove: () -> Unit,
    onDecline: () -> Unit
) {
    val roleColor = AdminMockData.roleColor(user.role)

    DarkCard {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(44.dp).clip(CircleShape).background(roleColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(user.name.take(1), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = roleColor)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(user.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(user.email, fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RoleBadge(user.role)
                        if (user.phone != null) {
                            Text(user.phone, fontSize = 11.sp, color = TextMuted)
                        }
                        if (user.city != null) {
                            Text(user.city, fontSize = 11.sp, color = TextMuted)
                        }
                        if (user.createdAt != null) {
                            Text(user.createdAt, fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
                StatusBadge(text = "Ожидает", color = ILeaderColors.Warning)
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Подтвердить", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = BorderStroke(0.5.dp, CardBorder),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Отклонить", fontSize = 13.sp)
                }
            }
        }
    }
}
