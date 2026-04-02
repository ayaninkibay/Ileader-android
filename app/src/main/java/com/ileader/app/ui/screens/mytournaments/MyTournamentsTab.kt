package com.ileader.app.ui.screens.mytournaments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ileader.app.data.models.User
import com.ileader.app.ui.screens.common.ManualCheckInScreen
import com.ileader.app.ui.screens.common.QrScannerScreen
import com.ileader.app.ui.screens.detail.TournamentDetailScreen

private sealed class MyTournamentsNavState {
    data object List : MyTournamentsNavState()
    data class TournamentDetail(val id: String) : MyTournamentsNavState()
    data class QrScanner(val tournamentId: String, val tournamentName: String = "") : MyTournamentsNavState()
    data class ManualCheckIn(val tournamentId: String, val tournamentName: String = "") : MyTournamentsNavState()
    data class TournamentEdit(val id: String) : MyTournamentsNavState()
    data class HelperManagement(val tournamentId: String, val tournamentName: String) : MyTournamentsNavState()
}

@Composable
fun MyTournamentsTab(user: User, onSignOut: () -> Unit) {
    var navState by remember { mutableStateOf<MyTournamentsNavState>(MyTournamentsNavState.List) }

    when (val state = navState) {
        is MyTournamentsNavState.List -> {
            MyTournamentsScreen(
                user = user,
                onTournamentClick = { id -> navState = MyTournamentsNavState.TournamentDetail(id) },
                onQrScan = { id, name -> navState = MyTournamentsNavState.QrScanner(id, name) },
                onManualCheckIn = { id, name -> navState = MyTournamentsNavState.ManualCheckIn(id, name) },
                onEditTournament = { id -> navState = MyTournamentsNavState.TournamentEdit(id) },
                onHelperManagement = { id, name -> navState = MyTournamentsNavState.HelperManagement(id, name) }
            )
        }
        is MyTournamentsNavState.TournamentDetail -> {
            TournamentDetailScreen(
                tournamentId = state.id,
                user = user,
                onBack = { navState = MyTournamentsNavState.List },
                onEditTournament = { id -> navState = MyTournamentsNavState.TournamentEdit(id) }
            )
        }
        is MyTournamentsNavState.QrScanner -> {
            QrScannerScreen(
                tournamentId = state.tournamentId,
                tournamentName = state.tournamentName,
                onBack = { navState = MyTournamentsNavState.List }
            )
        }
        is MyTournamentsNavState.ManualCheckIn -> {
            ManualCheckInScreen(
                tournamentId = state.tournamentId,
                tournamentName = state.tournamentName,
                onBack = { navState = MyTournamentsNavState.List }
            )
        }
        is MyTournamentsNavState.TournamentEdit -> {
            TournamentEditScreen(
                tournamentId = state.id,
                onBack = { navState = MyTournamentsNavState.List }
            )
        }
        is MyTournamentsNavState.HelperManagement -> {
            HelperManagementScreen(
                tournamentId = state.tournamentId,
                tournamentName = state.tournamentName,
                userId = user.id,
                onBack = { navState = MyTournamentsNavState.List }
            )
        }
    }
}
