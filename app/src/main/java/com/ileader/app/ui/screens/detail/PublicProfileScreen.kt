package com.ileader.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.PublicProfileData
import com.ileader.app.ui.viewmodels.PublicProfileViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent

@Composable
fun PublicProfileScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: PublicProfileViewModel = viewModel()
) {
    LaunchedEffect(userId) { viewModel.load(userId) }

    when (val state = viewModel.state) {
        is UiState.Loading -> {
            Column(Modifier.fillMaxSize().background(Bg)) {
                BackHeader("Профиль", onBack)
                LoadingScreen()
            }
        }
        is UiState.Error -> {
            Column(Modifier.fillMaxSize().background(Bg)) {
                BackHeader("Профиль", onBack)
                ErrorScreen(state.message, onRetry = { viewModel.load(userId) })
            }
        }
        is UiState.Success -> ProfileContent(data = state.data, onBack = onBack)
    }
}

@Composable
private fun ProfileContent(data: PublicProfileData, onBack: () -> Unit) {
    val profile = data.profile
    val user = profile.toDomain()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
    ) {
        // ══════════════════════════════════════
        // HERO with neutral background
        // ══════════════════════════════════════
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                Modifier.fillMaxWidth().height(200.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(Brush.horizontalGradient(listOf(Accent.copy(0.9f), Accent.copy(0.4f))))
            )

            // Back button
            Box(
                Modifier.statusBarsPadding().padding(16.dp).size(40.dp)
                    .clip(CircleShape).background(Color.Black.copy(0.3f))
                    .then(Modifier.padding(0.dp)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            // Avatar + name overlay
            Column(
                Modifier.fillMaxWidth().statusBarsPadding().padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with accent gradient border
                val borderColors = listOf(Accent, Accent.copy(0.5f), Accent.copy(0.8f), Accent)
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(112.dp).background(Brush.sweepGradient(borderColors), CircleShape))
                    Box(Modifier.size(106.dp).clip(CircleShape).background(Bg))
                    Box(
                        modifier = Modifier.size(100.dp).clip(CircleShape).background(CardBg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profile.avatarUrl.isNullOrEmpty()) {
                            AsyncImage(profile.avatarUrl, null, Modifier.size(100.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                            Box(Modifier.size(100.dp).clip(CircleShape).background(Accent), contentAlignment = Alignment.Center) {
                                Text((profile.name ?: "?").take(1).uppercase(), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(profile.name ?: "Пользователь", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoleBadge(role = user.role)
                    if (!profile.city.isNullOrEmpty()) {
                        Text("·", fontSize = 14.sp, color = TextMuted)
                        Text(profile.city, fontSize = 13.sp, color = TextMuted)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ══════════════════════════════════════
        // BIO
        // ══════════════════════════════════════
        if (!profile.bio.isNullOrEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = CardBg, shadowElevation = 0.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("О себе", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text(profile.bio, fontSize = 14.sp, color = TextSecondary, lineHeight = 21.sp)
                }
            }
            
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(80.dp))
    }
}


