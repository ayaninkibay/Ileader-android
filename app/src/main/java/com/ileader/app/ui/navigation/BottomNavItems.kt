package com.ileader.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val badge: Int = 0
)

fun getBottomNavItems(): List<BottomNavItem> = listOf(
    BottomNavItem("home", "Главная", Icons.Default.Home),
    BottomNavItem("sport", "Спорт", Icons.Default.Search),
    BottomNavItem("my_tournaments", "Мои турниры", Icons.Default.EmojiEvents),
    BottomNavItem("profile", "Профиль", Icons.Default.Person)
)
