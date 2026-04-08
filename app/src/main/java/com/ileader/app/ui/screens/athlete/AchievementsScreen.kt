package com.ileader.app.ui.screens.athlete

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.MilitaryTech
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.data.models.AchievementItem
import com.ileader.app.data.models.AchievementRarity
import com.ileader.app.data.repository.AthleteRepository
import com.ileader.app.ui.components.*
import kotlinx.coroutines.launch

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted

private fun rarityColor(r: AchievementRarity): Color = when (r) {
    AchievementRarity.LEGENDARY -> Color(0xFFFFD700)
    AchievementRarity.EPIC -> Color(0xFFA855F7)
    AchievementRarity.RARE -> Color(0xFF3B82F6)
    AchievementRarity.COMMON -> Color(0xFF94A3B8)
}

@Composable
fun AchievementsScreen(userId: String, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val repo = remember { AthleteRepository() }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<AchievementItem>>(emptyList()) }

    fun load() {
        scope.launch {
            loading = true; error = null
            try { items = repo.getAchievements(userId) }
            catch (e: Exception) { error = e.message ?: "Ошибка загрузки" }
            finally { loading = false }
        }
    }
    LaunchedEffect(userId) { load() }

    Column(Modifier.fillMaxSize().background(Bg)) {
        BackHeader("Достижения", onBack)
        when {
            loading -> LoadingScreen()
            error != null -> ErrorScreen(error!!) { load() }
            items.isEmpty() -> Box(Modifier.fillMaxSize().padding(16.dp)) {
                EmptyState(title = "Нет достижений", subtitle = "Достижения появятся после побед", icon = Icons.Outlined.MilitaryTech)
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { AchievementCard(it) }
            }
        }
    }
}

@Composable
private fun AchievementCard(a: AchievementItem) {
    val color = rarityColor(a.rarity)
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBg
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(color.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.WorkspacePremium, null, tint = color, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(a.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 2)
            if (a.description.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(a.description, fontSize = 11.sp, color = TextMuted, maxLines = 3)
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {
                Surface(shape = RoundedCornerShape(50), color = color.copy(0.15f)) {
                    Text(a.rarity.displayName, Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
                }
                if (a.date.isNotBlank()) Text(a.date, fontSize = 10.sp, color = TextMuted)
            }
        }
    }
}
