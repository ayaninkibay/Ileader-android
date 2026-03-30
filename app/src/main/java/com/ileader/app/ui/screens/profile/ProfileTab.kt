package com.ileader.app.ui.screens.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ileader.app.data.models.User
import com.ileader.app.ui.screens.common.MyTicketsScreen
import com.ileader.app.ui.screens.common.NotificationsScreen

private sealed class ProfileNavState {
    data object Main : ProfileNavState()
    data object Edit : ProfileNavState()
    data object Tickets : ProfileNavState()
    data object Notifications : ProfileNavState()
}

@Composable
fun ProfileTab(user: User, onSignOut: () -> Unit) {
    var navState by remember { mutableStateOf<ProfileNavState>(ProfileNavState.Main) }

    when (navState) {
        is ProfileNavState.Main -> {
            ProfileScreen(
                user = user,
                onSignOut = onSignOut,
                onEditProfile = { navState = ProfileNavState.Edit },
                onTickets = { navState = ProfileNavState.Tickets },
                onNotifications = { navState = ProfileNavState.Notifications }
            )
        }
        is ProfileNavState.Edit -> {
            EditProfileScreen(
                user = user,
                onBack = { navState = ProfileNavState.Main }
            )
        }
        is ProfileNavState.Tickets -> {
            MyTicketsScreen(user = user, onBack = { navState = ProfileNavState.Main })
        }
        is ProfileNavState.Notifications -> {
            NotificationsScreen(user = user, onBack = { navState = ProfileNavState.Main })
        }
    }
}
