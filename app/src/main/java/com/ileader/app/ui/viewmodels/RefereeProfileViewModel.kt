package com.ileader.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.*
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class RefereeProfileData(
    val profile: ProfileDto,
    val sports: List<UserSportDto>,
    val stats: List<UserSportStatsDto>,
    val license: LicenseDto?,
    val assignments: List<RefereeAssignmentDto>
) {
    val activeTournaments: List<RefereeAssignmentDto>
        get() = assignments.filter {
            val s = it.tournaments?.status
            s == "in_progress" || s == "check_in" || s == "registration_open" || s == "registration_closed"
        }

    val upcomingTournaments: List<RefereeAssignmentDto>
        get() = assignments.filter {
            it.tournaments?.status == "registration_open"
        }

    val historyTournaments: List<RefereeAssignmentDto>
        get() = assignments.filter {
            it.tournaments?.status == "completed" || it.tournaments?.status == "cancelled"
        }

    val totalJudged: Int get() = assignments.size
    val completed: Int get() = historyTournaments.size
    val active: Int get() = activeTournaments.size
    val primarySportName: String
        get() = sports.firstOrNull { it.isPrimary }?.sports?.name
            ?: sports.firstOrNull()?.sports?.name ?: ""
}

class RefereeProfileViewModel : ViewModel() {
    private val repo = ViewerRepository()

    var state by mutableStateOf<UiState<RefereeProfileData>>(UiState.Loading)
        private set

    fun load(refereeId: String) {
        viewModelScope.launch {
            state = UiState.Loading
            try {
                val profileDef = async { repo.getPublicProfile(refereeId) }
                val sportsDef = async { repo.getUserSports(refereeId) }
                val statsDef = async { repo.getUserSportStats(refereeId) }
                val licenseDef = async { repo.getUserLicense(refereeId) }
                val assignmentsDef = async { repo.getRefereeAssignmentsFull(refereeId) }

                state = UiState.Success(
                    RefereeProfileData(
                        profile = profileDef.await(),
                        sports = sportsDef.await(),
                        stats = statsDef.await(),
                        license = licenseDef.await(),
                        assignments = assignmentsDef.await()
                    )
                )
            } catch (e: Exception) {
                state = UiState.Error(e.message ?: "Ошибка загрузки профиля судьи")
            }
        }
    }
}
