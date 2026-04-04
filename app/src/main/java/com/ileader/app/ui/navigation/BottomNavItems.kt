package com.ileader.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val shortLabel: String = label,
    val icon: ImageVector,
    val badge: Int = 0
)

fun getBottomNavItems(): List<BottomNavItem> = listOf(
    BottomNavItem("home", "Главная", "Главная", Icons.Default.Home),
    BottomNavItem("sport", "Спорт", "Спорт", Icons.Default.SportsScore),
    BottomNavItem("my_tournaments", "Турниры", "Турниры", Icons.Default.EmojiEvents),
    BottomNavItem("profile", "Профиль", "Профиль", Icons.Default.Person)
)
