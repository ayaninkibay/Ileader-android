package com.ileader.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.ileader.app.data.models.UserRole

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val badge: Int = 0
)

fun getBottomNavItems(role: UserRole, teamId: String? = null, unreadNotifications: Int = 0): List<BottomNavItem> {
    return when (role) {
        UserRole.ATHLETE -> buildList {
            add(BottomNavItem("athlete/dashboard", "Главная", Icons.Default.Home))
            add(BottomNavItem("athlete/tournaments", "Турниры", Icons.Default.EmojiEvents))
            if (teamId != null) {
                add(BottomNavItem("athlete/team", "Команда", Icons.Default.Groups))
            } else {
                add(BottomNavItem("athlete/results", "Результаты", Icons.Default.Leaderboard))
            }
            add(BottomNavItem("athlete/goals", "Цели", Icons.Default.Flag))
            add(BottomNavItem("athlete/profile", "Профиль", Icons.Default.Person))
        }
        UserRole.TRAINER -> listOf(
            BottomNavItem("trainer/dashboard", "Главная", Icons.Default.Home),
            BottomNavItem("trainer/team", "Команда", Icons.Default.Groups),
            BottomNavItem("trainer/tournaments", "Турниры", Icons.Default.EmojiEvents),
            BottomNavItem("trainer/statistics", "Статистика", Icons.Default.BarChart),
            BottomNavItem("trainer/profile", "Профиль", Icons.Default.Person)
        )
        UserRole.ORGANIZER -> listOf(
            BottomNavItem("organizer/dashboard", "Главная", Icons.Default.Home),
            BottomNavItem("organizer/tournaments", "Турниры", Icons.Default.EmojiEvents),
            BottomNavItem("organizer/notifications", "Уведомления", Icons.Default.Notifications, badge = unreadNotifications),
            BottomNavItem("organizer/profile", "Профиль", Icons.Default.Person)
        )
        UserRole.REFEREE -> listOf(
            BottomNavItem("referee/dashboard", "Главная", Icons.Default.Home),
            BottomNavItem("referee/tournaments", "Турниры", Icons.Default.EmojiEvents),
            BottomNavItem("referee/requests", "Заявки", Icons.Default.Inbox),
            BottomNavItem("referee/history", "История", Icons.Default.History),
            BottomNavItem("referee/profile", "Профиль", Icons.Default.Person)
        )
        UserRole.SPONSOR -> listOf(
            BottomNavItem("sponsor/dashboard", "Главная", Icons.Default.Home),
            BottomNavItem("sponsor/tournaments", "Турниры", Icons.Default.EmojiEvents),
            BottomNavItem("sponsor/notifications", "Уведомления", Icons.Default.Notifications, badge = unreadNotifications),
            BottomNavItem("sponsor/profile", "Профиль", Icons.Default.Person)
        )
        UserRole.MEDIA -> listOf(
            BottomNavItem("media/dashboard", "Главная", Icons.Default.Home),
            BottomNavItem("media/tournaments", "Турниры", Icons.Default.EmojiEvents),
            BottomNavItem("media/content", "Контент", Icons.AutoMirrored.Filled.Article),
            BottomNavItem("media/notifications", "Уведомления", Icons.Default.Notifications, badge = unreadNotifications),
            BottomNavItem("media/profile", "Профиль", Icons.Default.Person)
        )
        UserRole.ADMIN -> listOf(
            BottomNavItem("admin/dashboard", "Главная", Icons.Default.Home),
            BottomNavItem("admin/users", "Юзеры", Icons.Default.People),
            BottomNavItem("admin/tournaments", "Турниры", Icons.Default.EmojiEvents),
            BottomNavItem("admin/requests", "Заявки", Icons.Default.Inbox),
            BottomNavItem("admin/settings", "Настройки", Icons.Default.Settings)
        )
        UserRole.USER -> listOf(
            BottomNavItem("viewer/home", "Главная", Icons.Default.Home),
            BottomNavItem("viewer/tournaments", "Турниры", Icons.Default.EmojiEvents),
            BottomNavItem("viewer/news", "Новости", Icons.Default.Newspaper),
            BottomNavItem("viewer/community", "Сообщество", Icons.Default.People),
            BottomNavItem("viewer/profile", "Профиль", Icons.Default.Person)
        )
    }
}
