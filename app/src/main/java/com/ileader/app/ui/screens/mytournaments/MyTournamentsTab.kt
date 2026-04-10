package com.ileader.app.ui.screens.mytournaments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ileader.app.data.models.User
import com.ileader.app.ui.screens.common.ManualCheckInScreen
import com.ileader.app.ui.screens.common.QrScannerScreen
import com.ileader.app.ui.screens.detail.AthleteProfilePage
import com.ileader.app.ui.screens.detail.PublicProfileScreen
import com.ileader.app.ui.screens.detail.RefereeProfilePage
import com.ileader.app.ui.screens.detail.TrainerProfilePage
import com.ileader.app.ui.screens.detail.TeamDetailScreen
import com.ileader.app.ui.screens.detail.TournamentDetailScreen

private sealed class MyTournamentsNavState {
    data object List : MyTournamentsNavState()
    data object CreateTournament : MyTournamentsNavState()
    data object Locations : MyTournamentsNavState()
    data object TeamManagement : MyTournamentsNavState()
    data class TournamentDetail(val id: String) : MyTournamentsNavState()
    data class QrScanner(val tournamentId: String, val tournamentName: String = "") : MyTournamentsNavState()
    data class ManualCheckIn(val tournamentId: String, val tournamentName: String = "") : MyTournamentsNavState()
    data class TournamentEdit(val id: String) : MyTournamentsNavState()
    data class HelperManagement(val tournamentId: String, val tournamentName: String) : MyTournamentsNavState()
    data class RefereeManagement(val tournamentId: String, val tournamentName: String) : MyTournamentsNavState()
    data class InviteCodes(val tournamentId: String, val tournamentName: String) : MyTournamentsNavState()
    data class PublicProfile(val id: String) : MyTournamentsNavState()
    data class AthleteProfile(val id: String) : MyTournamentsNavState()
    data class RefereeProfile(val id: String) : MyTournamentsNavState()
    data class TrainerProfile(val id: String) : MyTournamentsNavState()
    data class TeamDetail(val id: String) : MyTournamentsNavState()
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
                onHelperManagement = { id, name -> navState = MyTournamentsNavState.HelperManagement(id, name) },
                onCreateTournament = { navState = MyTournamentsNavState.CreateTournament },
                onLocations = { navState = MyTournamentsNavState.Locations },
                onTeamManagement = { navState = MyTournamentsNavState.TeamManagement }
            )
        }
        is MyTournamentsNavState.CreateTournament -> {
            TournamentCreateScreen(
                userId = user.id,
                onBack = { navState = MyTournamentsNavState.List },
                onCreated = { id -> navState = MyTournamentsNavState.TournamentDetail(id) }
            )
        }
        is MyTournamentsNavState.Locations -> {
            LocationsScreen(
                userId = user.id,
                onBack = { navState = MyTournamentsNavState.List }
            )
        }
        is MyTournamentsNavState.TeamManagement -> {
            TeamManagementScreen(
                userId = user.id,
                onBack = { navState = MyTournamentsNavState.List }
            )
        }
        is MyTournamentsNavState.TournamentDetail -> {
            TournamentDetailScreen(
                tournamentId = state.id,
                user = user,
                onBack = { navState = MyTournamentsNavState.List },
                onEditTournament = { id -> navState = MyTournamentsNavState.TournamentEdit(id) },
                onQrScan = { id, name -> navState = MyTournamentsNavState.QrScanner(id, name) },
                onManualCheckIn = { id, name -> navState = MyTournamentsNavState.ManualCheckIn(id, name) },
                onHelperManagement = { id, name -> navState = MyTournamentsNavState.HelperManagement(id, name) },
                onRefereeManagement = { id, name -> navState = MyTournamentsNavState.RefereeManagement(id, name) },
                onInviteCodes = { id, name -> navState = MyTournamentsNavState.InviteCodes(id, name) },
                onProfileClick = { navState = MyTournamentsNavState.PublicProfile(it) },
                onAthleteProfileClick = { navState = MyTournamentsNavState.AthleteProfile(it) },
                onRefereeProfileClick = { navState = MyTournamentsNavState.RefereeProfile(it) },
                onTrainerProfileClick = { navState = MyTournamentsNavState.TrainerProfile(it) },
                onTeamClick = { navState = MyTournamentsNavState.TeamDetail(it) }
            )
        }
        is MyTournamentsNavState.PublicProfile -> {
            PublicProfileScreen(
                userId = state.id,
                onBack = { navState = MyTournamentsNavState.List }
            )
        }
        is MyTournamentsNavState.QrScanner -> {
            QrScannerScreen(
                tournamentId = state.tournamentId,
                tournamentName = state.tournamentName,
                userId = user.id,
                onBack = { navState = MyTournamentsNavState.List }
            )
        }
        is MyTournamentsNavState.ManualCheckIn -> {
            ManualCheckInScreen(
                tournamentId = state.tournamentId,
                tournamentName = state.tournamentName,
                userId = user.id,
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
        is MyTournamentsNavState.RefereeManagement -> {
            RefereeManagementScreen(
                tournamentId = state.tournamentId,
                tournamentName = state.tournamentName,
                onBack = { navState = MyTournamentsNavState.List }
            )
        }
        is MyTournamentsNavState.InviteCodes -> {
            InviteCodesScreen(
                tournamentId = state.tournamentId,
                tournamentName = state.tournamentName,
                userId = user.id,
                onBack = { navState = MyTournamentsNavState.List }
            )
        }
        is MyTournamentsNavState.AthleteProfile -> {
            AthleteProfilePage(
                athleteId = state.id,
                onBack = { navState = MyTournamentsNavState.List },
                onTournamentClick = { navState = MyTournamentsNavState.TournamentDetail(it) },
                onProfileClick = { navState = MyTournamentsNavState.PublicProfile(it) }
            )
        }
        is MyTournamentsNavState.RefereeProfile -> {
            RefereeProfilePage(
                refereeId = state.id,
                onBack = { navState = MyTournamentsNavState.List },
                onTournamentClick = { navState = MyTournamentsNavState.TournamentDetail(it) },
                onProfileClick = { navState = MyTournamentsNavState.PublicProfile(it) }
            )
        }
        is MyTournamentsNavState.TrainerProfile -> {
            TrainerProfilePage(
                trainerId = state.id,
                onBack = { navState = MyTournamentsNavState.List },
                onTournamentClick = { navState = MyTournamentsNavState.TournamentDetail(it) },
                onAthleteClick = { navState = MyTournamentsNavState.AthleteProfile(it) },
                onProfileClick = { navState = MyTournamentsNavState.PublicProfile(it) }
            )
        }
        is MyTournamentsNavState.TeamDetail -> {
            TeamDetailScreen(
                teamId = state.id,
                onBack = { navState = MyTournamentsNavState.List }
            )
        }
    }
}
