package com.ileader.app.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.ArticleDto
import com.ileader.app.data.remote.dto.CommunityProfileDto
import com.ileader.app.data.models.AthleteGoal
import com.ileader.app.data.models.AthleteStats
import com.ileader.app.data.models.RefereeStats
import com.ileader.app.data.models.RefereeTournament
import com.ileader.app.data.models.Tournament
import com.ileader.app.data.remote.dto.ArticleStatsDto
import com.ileader.app.data.remote.dto.SponsorStats
import com.ileader.app.data.remote.dto.TournamentSponsorshipDto
import com.ileader.app.data.repository.AthleteRepository
import com.ileader.app.data.repository.MediaRepository
import com.ileader.app.data.repository.NotificationRepository
import com.ileader.app.data.repository.SponsorRepository
import com.ileader.app.data.repository.TrainerTeamData
import com.ileader.app.data.remote.dto.OrganizerStatsDto
import com.ileader.app.data.remote.dto.ParticipantDto
import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.remote.dto.TournamentDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto
import com.ileader.app.data.remote.dto.AdminStatsDto
import com.ileader.app.data.repository.AdminRepository
import com.ileader.app.data.repository.OrganizerRepository
import com.ileader.app.data.repository.RefereeRepository
import com.ileader.app.data.repository.TrainerRepository
import com.ileader.app.data.repository.ViewerRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class HomeState(
    val sports: List<SportDto> = emptyList(),
    val news: UiState<List<ArticleDto>> = UiState.Loading,
    val tournaments: UiState<List<TournamentWithCountsDto>> = UiState.Loading,
    val people: UiState<List<CommunityProfileDto>> = UiState.Loading
)

data class OrganizerDashboardState(
    val stats: OrganizerStatsDto? = null,
    val upcomingTournaments: List<TournamentDto> = emptyList(),
    val recentRegistrations: List<ParticipantDto> = emptyList(),
    val isLoaded: Boolean = false
)

data class RefereeDashboardState(
    val stats: RefereeStats? = null,
    val activeTournaments: List<RefereeTournament> = emptyList(),
    val isLoaded: Boolean = false
)

data class TrainerDashboardState(
    val teams: List<TrainerTeamData> = emptyList(),
    val totalAthletes: Int = 0,
    val upcomingTournaments: List<TournamentWithCountsDto> = emptyList(),
    val isLoaded: Boolean = false
)

data class AthleteDashboardState(
    val stats: AthleteStats? = null,
    val goals: List<AthleteGoal> = emptyList(),
    val upcomingTournaments: List<Tournament> = emptyList(),
    val isLoaded: Boolean = false
)

data class SponsorDashboardState(
    val stats: SponsorStats? = null,
    val sponsorships: List<TournamentSponsorshipDto> = emptyList(),
    val isLoaded: Boolean = false
)

data class AdminDashboardState(
    val stats: AdminStatsDto? = null,
    val isLoaded: Boolean = false
)

data class MediaDashboardState(
    val articleStats: ArticleStatsDto? = null,
    val accreditationsTotal: Int = 0,
    val accreditationsAccepted: Int = 0,
    val interviewsTotal: Int = 0,
    val interviewsScheduled: Int = 0,
    val recentArticles: List<ArticleDto> = emptyList(),
    val isLoaded: Boolean = false
)

class HomeViewModel : ViewModel() {
    private val repo = ViewerRepository()
    private val organizerRepo = OrganizerRepository()
    private val refereeRepo = RefereeRepository()
    private val trainerRepo = TrainerRepository()
    private val athleteRepo = AthleteRepository()
    private val sponsorRepo = SponsorRepository()
    private val mediaRepo = MediaRepository()
    private val adminRepo = AdminRepository()
    private val notificationRepo = NotificationRepository()

    var state by mutableStateOf(HomeState())
        private set

    var organizerDashboard by mutableStateOf(OrganizerDashboardState())
        private set

    var refereeDashboard by mutableStateOf(RefereeDashboardState())
        private set

    var trainerDashboard by mutableStateOf(TrainerDashboardState())
        private set

    var athleteDashboard by mutableStateOf(AthleteDashboardState())
        private set

    var sponsorDashboard by mutableStateOf(SponsorDashboardState())
        private set

    var mediaDashboard by mutableStateOf(MediaDashboardState())
        private set

    var adminDashboard by mutableStateOf(AdminDashboardState())
        private set

    fun loadAdminDashboard() {
        if (adminDashboard.isLoaded) return
        viewModelScope.launch {
            try {
                val stats = adminRepo.getStats()
                adminDashboard = AdminDashboardState(stats = stats, isLoaded = true)
            } catch (_: Exception) {
                adminDashboard = adminDashboard.copy(isLoaded = true)
            }
        }
    }

    var unreadNotifications by mutableStateOf(0)
        private set

    fun loadUnreadCount(userId: String) {
        viewModelScope.launch {
            try {
                unreadNotifications = notificationRepo.getUnreadCount(userId)
            } catch (_: Exception) {}
        }
    }

    init {
        viewModelScope.launch {
            try {
                val sports = repo.getSports()
                state = state.copy(sports = sports)
            } catch (_: Exception) {}
        }
    }

    fun load() {
        viewModelScope.launch {
            val currentSports = state.sports

            val sportsDeferred = async {
                if (currentSports.isNotEmpty()) currentSports
                else try { repo.getSports() } catch (_: Exception) { emptyList() }
            }

            val newsDeferred = async {
                try { UiState.Success(repo.getRecentArticles(10)) }
                catch (e: Exception) { UiState.Error(e.message ?: "Ошибка") }
            }

            val tournamentsDeferred = async {
                try { UiState.Success(repo.getUpcomingTournaments(10)) }
                catch (e: Exception) { UiState.Error(e.message ?: "Ошибка") }
            }

            val peopleDeferred = async {
                try { UiState.Success(repo.getAthletes().take(10)) }
                catch (e: Exception) { UiState.Error(e.message ?: "Ошибка") }
            }

            state = HomeState(
                sports = sportsDeferred.await(),
                news = newsDeferred.await(),
                tournaments = tournamentsDeferred.await(),
                people = peopleDeferred.await()
            )
        }
    }

    fun loadOrganizerDashboard(userId: String) {
        if (organizerDashboard.isLoaded) return
        viewModelScope.launch {
            try {
                val statsDeferred = async { organizerRepo.getStats(userId) }
                val upcomingDeferred = async { organizerRepo.getUpcomingTournaments(userId) }
                val registrationsDeferred = async { organizerRepo.getRecentRegistrations(userId) }

                organizerDashboard = OrganizerDashboardState(
                    stats = statsDeferred.await(),
                    upcomingTournaments = upcomingDeferred.await(),
                    recentRegistrations = registrationsDeferred.await(),
                    isLoaded = true
                )
            } catch (_: Exception) {
                organizerDashboard = organizerDashboard.copy(isLoaded = true)
            }
        }
    }

    fun loadRefereeDashboard(userId: String) {
        if (refereeDashboard.isLoaded) return
        viewModelScope.launch {
            try {
                val statsDeferred = async { refereeRepo.getStats(userId) }
                val tournamentsDeferred = async { refereeRepo.getAssignedTournaments(userId) }

                val tournaments = tournamentsDeferred.await()
                refereeDashboard = RefereeDashboardState(
                    stats = statsDeferred.await(),
                    activeTournaments = tournaments.filter {
                        it.status.name in listOf("REGISTRATION_OPEN", "REGISTRATION_CLOSED", "CHECK_IN", "IN_PROGRESS")
                    }.take(5),
                    isLoaded = true
                )
            } catch (_: Exception) {
                refereeDashboard = refereeDashboard.copy(isLoaded = true)
            }
        }
    }

    fun loadAthleteDashboard(userId: String) {
        if (athleteDashboard.isLoaded) return
        viewModelScope.launch {
            try {
                val statsDeferred = async { athleteRepo.getStats(userId) }
                val goalsDeferred = async {
                    try { athleteRepo.getGoals(userId) } catch (_: Exception) { emptyList() }
                }
                val tournamentsDeferred = async {
                    try { athleteRepo.getMyTournaments(userId) } catch (_: Exception) { emptyList() }
                }

                val tournaments = tournamentsDeferred.await()
                    .filter { it.status.name !in listOf("COMPLETED", "CANCELLED") }
                    .sortedBy { it.startDate }
                    .take(5)

                athleteDashboard = AthleteDashboardState(
                    stats = statsDeferred.await(),
                    goals = goalsDeferred.await().filter { it.status == com.ileader.app.data.models.GoalStatus.ACTIVE }.take(3),
                    upcomingTournaments = tournaments,
                    isLoaded = true
                )
            } catch (_: Exception) {
                athleteDashboard = athleteDashboard.copy(isLoaded = true)
            }
        }
    }

    fun loadSponsorDashboard(userId: String) {
        if (sponsorDashboard.isLoaded) return
        viewModelScope.launch {
            try {
                val itemsDeferred = async { sponsorRepo.getMySponsorships(userId) }
                val statsDeferred = async { sponsorRepo.getStats(userId) }
                val items = itemsDeferred.await()
                sponsorDashboard = SponsorDashboardState(
                    stats = statsDeferred.await(),
                    sponsorships = items.take(5),
                    isLoaded = true
                )
            } catch (_: Exception) {
                sponsorDashboard = sponsorDashboard.copy(isLoaded = true)
            }
        }
    }

    fun loadMediaDashboard(userId: String) {
        if (mediaDashboard.isLoaded) return
        viewModelScope.launch {
            try {
                val articleStatsDeferred = async {
                    try { mediaRepo.getArticleStats(userId) } catch (_: Exception) { null }
                }
                val accStatsDeferred = async {
                    try { mediaRepo.getAccreditationStats(userId) } catch (_: Exception) { null }
                }
                val intStatsDeferred = async {
                    try { mediaRepo.getInterviewStats(userId) } catch (_: Exception) { null }
                }
                val recentDeferred = async {
                    try { mediaRepo.getMyArticles(userId).take(5) } catch (_: Exception) { emptyList() }
                }

                val accStats = accStatsDeferred.await()
                val intStats = intStatsDeferred.await()

                mediaDashboard = MediaDashboardState(
                    articleStats = articleStatsDeferred.await(),
                    accreditationsTotal = accStats?.total ?: 0,
                    accreditationsAccepted = accStats?.accepted ?: 0,
                    interviewsTotal = intStats?.total ?: 0,
                    interviewsScheduled = intStats?.scheduled ?: 0,
                    recentArticles = recentDeferred.await(),
                    isLoaded = true
                )
            } catch (_: Exception) {
                mediaDashboard = mediaDashboard.copy(isLoaded = true)
            }
        }
    }

    fun loadTrainerDashboard(userId: String) {
        if (trainerDashboard.isLoaded) return
        viewModelScope.launch {
            try {
                val teamsDeferred = async { trainerRepo.getMyTeams(userId) }
                val tournamentsDeferred = async { trainerRepo.getMyTeamsTournaments(userId) }

                val teams = teamsDeferred.await()
                val totalAthletes = teams.sumOf { it.members.size }
                val tournaments = tournamentsDeferred.await()
                    .filter { it.status != "completed" && it.status != "cancelled" }
                    .sortedBy { it.startDate ?: "" }
                    .take(5)

                trainerDashboard = TrainerDashboardState(
                    teams = teams,
                    totalAthletes = totalAthletes,
                    upcomingTournaments = tournaments,
                    isLoaded = true
                )
            } catch (_: Exception) {
                trainerDashboard = trainerDashboard.copy(isLoaded = true)
            }
        }
    }
}
