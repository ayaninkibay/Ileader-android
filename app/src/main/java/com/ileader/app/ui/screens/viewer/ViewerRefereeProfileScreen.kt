package com.ileader.app.ui.screens.viewer

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.DarkTheme
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.ViewerPublicProfileViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentDark: Color @Composable get() = DarkTheme.AccentDark
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun ViewerRefereeProfileScreen(
    refereeId: String,
    user: User,
    onBack: () -> Unit = {}
) {
    val viewModel: ViewerPublicProfileViewModel = viewModel()
    val state by viewModel.refereeState.collectAsState()
    LaunchedEffect(refereeId) { viewModel.loadReferee(refereeId) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.loadReferee(refereeId) }
        is UiState.Success -> {
            val data = s.data
            val profile = data.profile
            val sportName = data.sports.firstOrNull()?.sports?.name ?: ""

            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            ) {
                Box(
                    Modifier.fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Accent.copy(alpha = 0.15f), Bg)))
                        .statusBarsPadding().padding(16.dp)
                ) {
                    Column {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = TextPrimary)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            InitialsAvatar(name = profile.name ?: "?", gradient = listOf(Accent, AccentDark), size = 72)
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(profile.name ?: "", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary, letterSpacing = (-0.5).sp)
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (sportName.isNotBlank()) {
                                        Surface(shape = RoundedCornerShape(8.dp), color = CardBorder.copy(alpha = 0.5f)) {
                                            Text(sportName, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                        }
                                    }
                                    Surface(shape = RoundedCornerShape(8.dp), color = AccentSoft) {
                                        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Default.Shield, null, tint = Accent, modifier = Modifier.size(12.dp))
                                            Text("Судья", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Accent)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(profile.city ?: "", fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                if (!profile.bio.isNullOrBlank()) {
                    SectionCard(title = "О судье", modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(profile.bio, fontSize = 14.sp, color = TextSecondary, lineHeight = 20.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                }

                SectionCard(title = "Виды спорта", modifier = Modifier.padding(horizontal = 20.dp)) {
                    if (sportName.isNotBlank()) {
                        Surface(shape = RoundedCornerShape(8.dp), color = CardBorder.copy(alpha = 0.5f)) {
                            Text(sportName, Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                    } else {
                        Text("Не указано", fontSize = 13.sp, color = TextSecondary)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
