package com.ileader.app.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ileader.app.data.models.AthleteGoal
import com.ileader.app.data.models.User
import com.ileader.app.ui.screens.common.MyTicketsScreen
import com.ileader.app.ui.screens.common.NotificationsScreen

private sealed class ProfileNavState {
    data object Main : ProfileNavState()
    data object Edit : ProfileNavState()
    data object Tickets : ProfileNavState()
    data object Notifications : ProfileNavState()
    data class GoalDetail(val goal: AthleteGoal) : ProfileNavState()
    data object GoalCreate : ProfileNavState()
}

@Composable
fun ProfileTab(user: User, onSignOut: () -> Unit) {
    var navState by remember { mutableStateOf<ProfileNavState>(ProfileNavState.Main) }

    when (val state = navState) {
        is ProfileNavState.Main -> {
            ProfileScreen(
                user = user,
                onSignOut = onSignOut,
                onEditProfile = { navState = ProfileNavState.Edit },
                onTickets = { navState = ProfileNavState.Tickets },
                onNotifications = { navState = ProfileNavState.Notifications },
                onGoalClick = { goal -> navState = ProfileNavState.GoalDetail(goal) },
                onGoalCreate = { navState = ProfileNavState.GoalCreate }
            )
        }
        is ProfileNavState.Edit -> {
            EditProfileScreen(user = user, onBack = { navState = ProfileNavState.Main })
        }
        is ProfileNavState.Tickets -> {
            MyTicketsScreen(user = user, onBack = { navState = ProfileNavState.Main })
        }
        is ProfileNavState.Notifications -> {
            NotificationsScreen(user = user, onBack = { navState = ProfileNavState.Main })
        }
        is ProfileNavState.GoalDetail -> {
            GoalDetailScreen(goal = state.goal, onBack = { navState = ProfileNavState.Main }, onDeleted = { navState = ProfileNavState.Main })
        }
        is ProfileNavState.GoalCreate -> {
            GoalCreateScreen(userId = user.id, onBack = { navState = ProfileNavState.Main }, onCreated = { navState = ProfileNavState.Main })
        }
    }
}
