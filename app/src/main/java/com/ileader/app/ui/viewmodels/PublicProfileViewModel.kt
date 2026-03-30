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

data class PublicProfileData(
    val profile: ProfileDto,
    val sports: List<UserSportDto>,
    val stats: List<UserSportStatsDto>,
    val results: List<ResultDto>,
    val membership: TeamMembershipDto?
)

class PublicProfileViewModel : ViewModel() {
    private val repo = ViewerRepository()

    var state by mutableStateOf<UiState<PublicProfileData>>(UiState.Loading)
        private set

    fun load(userId: String) {
        viewModelScope.launch {
            state = UiState.Loading
            try {
                val profileDef = async { repo.getPublicProfile(userId) }
                val sportsDef = async { repo.getUserSports(userId) }
                val statsDef = async { repo.getUserSportStats(userId) }
                val resultsDef = async { repo.getAthleteResults(userId, 10) }
                val memberDef = async { repo.getAthleteMembership(userId) }

                state = UiState.Success(
                    PublicProfileData(
                        profile = profileDef.await(),
                        sports = sportsDef.await(),
                        stats = statsDef.await(),
                        results = resultsDef.await(),
                        membership = memberDef.await()
                    )
                )
            } catch (e: Exception) {
                state = UiState.Error(e.message ?: "Ошибка загрузки профиля")
            }
        }
    }
}
