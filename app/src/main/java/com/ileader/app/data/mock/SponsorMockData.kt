package com.ileader.app.data.mock

import androidx.compose.ui.graphics.Color

enum class SponsorshipType { TEAM, TOURNAMENT }

data class SponsorshipItem(
    val id: String,
    val type: SponsorshipType,
    val targetId: String,
    val targetName: String,
    val sportName: String,
    val amount: Long,
    val startDate: String,
    val endDate: String? = null,
    val locationCity: String? = null,
    val locationName: String? = null
)

data class OpenTournament(
    val id: String,
    val name: String,
    val sportName: String,
    val status: String,
    val startDate: String,
    val endDate: String,
    val locationCity: String,
    val locationName: String,
    val participantCount: Int,
    val maxParticipants: Int = 30
)

data class AvailableTeam(
    val id: String,
    val name: String,
    val sportName: String,
    val trainerName: String,
    val memberCount: Int
)

data class SponsorInvite(
    val id: String,
    val tournamentId: String,
    val tournamentName: String,
    val organizerName: String,
    val sportName: String,
    val status: String,
    val timeAgo: String,
    val contactPhone: String? = null,
    val responseMessage: String? = null
)

data class TeamMemberData(
    val id: String,
    val name: String,
    val role: String,
    val wins: Int,
    val tournaments: Int,
    val rating: Int
)

object SponsorMockData {

    val sponsorships = listOf(
        SponsorshipItem("sp-1", SponsorshipType.TEAM, "team-1", "Red Bull Racing KZ", "Картинг", 2_500_000, "15.01.2026", "31.12.2026", "Алматы"),
        SponsorshipItem("sp-2", SponsorshipType.TEAM, "team-2", "Astana Motors", "Картинг", 1_800_000, "01.02.2026", "31.12.2026", "Астана"),
        SponsorshipItem("sp-3", SponsorshipType.TOURNAMENT, "t-1", "Кубок Казахстана по Картингу 2026", "Картинг", 5_000_000, "15.03.2026", "17.03.2026", "Алматы", "Алматы Картинг Центр"),
        SponsorshipItem("sp-4", SponsorshipType.TOURNAMENT, "t-2", "Чемпионат по стрельбе", "Стрельба", 3_000_000, "10.04.2026", "12.04.2026", "Астана", "Астана Тир"),
        SponsorshipItem("sp-5", SponsorshipType.TEAM, "team-3", "Sharpshooters KZ", "Стрельба", 1_200_000, "01.01.2026", "30.06.2026", "Шымкент")
    )

    val openTournaments = listOf(
        OpenTournament("ot-1", "Гран-При Алматы 2026", "Картинг", "registration_open", "20.05.2026", "22.05.2026", "Алматы", "Алматы Картинг Центр", 18, 30),
        OpenTournament("ot-2", "Кубок Астаны по стрельбе", "Стрельба", "registration_open", "05.06.2026", "07.06.2026", "Астана", "Астана Тир", 12, 24),
        OpenTournament("ot-3", "Летний Чемпионат Караганды", "Картинг", "in_progress", "01.04.2026", "03.04.2026", "Караганда", "Караганда Арена", 24, 30)
    )

    val availableTeams = listOf(
        AvailableTeam("at-1", "Speed Demons KZ", "Картинг", "Иванов А.С.", 8),
        AvailableTeam("at-2", "Thunder Racing", "Картинг", "Петров Б.К.", 6),
        AvailableTeam("at-3", "Elite Shooters", "Стрельба", "Сидоров В.М.", 5),
        AvailableTeam("at-4", "Nomad Warriors", "Бокс", "Ахметов Д.Р.", 10),
        AvailableTeam("at-5", "Steppe Riders", "Картинг", "Касымов Е.Т.", 7)
    )

    val invites = listOf(
        SponsorInvite("inv-1", "ot-1", "Гран-При Алматы 2026", "Алматы Спорт", "Картинг", "pending", "2 дня назад"),
        SponsorInvite("inv-2", "ot-2", "Кубок Астаны по стрельбе", "Астана Стрелковый Клуб", "Стрельба", "pending", "3 дня назад"),
        SponsorInvite("inv-3", "t-1", "Кубок Казахстана по Картингу", "Федерация Картинга РК", "Картинг", "accepted", "2 недели назад", "+7 777 100 20 30", "Рады сотрудничать!"),
        SponsorInvite("inv-4", "t-3", "Зимний Кубок Шымкента", "Шымкент Спорт", "Картинг", "declined", "1 месяц назад", null, "Бюджет уже распределён.")
    )

    private val teamMembers = mapOf(
        "team-1" to listOf(
            TeamMemberData("m1", "Алексей Иванов", "Пилот", 5, 12, 1850),
            TeamMemberData("m2", "Дмитрий Петров", "Пилот", 3, 10, 1720),
            TeamMemberData("m3", "Сергей Ким", "Пилот", 7, 15, 1950),
            TeamMemberData("m4", "Артём Касымов", "Пилот", 2, 8, 1580)
        ),
        "team-2" to listOf(
            TeamMemberData("m5", "Руслан Ахметов", "Пилот", 4, 11, 1800),
            TeamMemberData("m6", "Максим Тен", "Пилот", 6, 14, 1900),
            TeamMemberData("m7", "Нурлан Жумабеков", "Пилот", 1, 6, 1450)
        ),
        "team-3" to listOf(
            TeamMemberData("m8", "Данияр Сулейменов", "Стрелок", 8, 16, 2010),
            TeamMemberData("m9", "Айдар Нурланов", "Стрелок", 3, 9, 1650),
            TeamMemberData("m10", "Тимур Оспанов", "Стрелок", 5, 12, 1780)
        )
    )

    data class TournamentDetailData(
        val id: String,
        val name: String,
        val status: String,
        val sportName: String,
        val organizerName: String,
        val description: String,
        val startDate: String,
        val endDate: String,
        val locationCity: String,
        val locationName: String,
        val participantCount: Int,
        val maxParticipants: Int,
        val refereeCount: Int,
        val format: String,
        val ageCategory: String,
        val sponsorshipAmount: Long? = null
    )

    val tournamentDetails = listOf(
        TournamentDetailData("t-1", "Кубок Казахстана по Картингу 2026", "registration_open", "Картинг", "Федерация Картинга РК", "Главный кубок Казахстана по картингу. Лучшие пилоты страны соревнуются за звание чемпиона.", "15.03.2026", "17.03.2026", "Алматы", "Алматы Картинг Центр", 22, 30, 4, "Квалификация + Финал", "16+", 5_000_000),
        TournamentDetailData("t-2", "Чемпионат по стрельбе", "in_progress", "Стрельба", "Астана Стрелковый Клуб", "Ежегодный чемпионат по стрельбе среди лучших стрелков Казахстана.", "10.04.2026", "12.04.2026", "Астана", "Астана Тир", 16, 24, 3, "Олимпийская система", "18+", 3_000_000),
        TournamentDetailData("ot-1", "Гран-При Алматы 2026", "registration_open", "Картинг", "Алматы Спорт", "Престижный Гран-При города Алматы.", "20.05.2026", "22.05.2026", "Алматы", "Алматы Картинг Центр", 18, 30, 5, "Гонка", "14+"),
        TournamentDetailData("ot-2", "Кубок Астаны по стрельбе", "registration_open", "Стрельба", "Астана Стрелковый Клуб", "Кубок города Астаны по стрельбе.", "05.06.2026", "07.06.2026", "Астана", "Астана Тир", 12, 24, 3, "Олимпийская система", "16+"),
        TournamentDetailData("ot-3", "Летний Чемпионат Караганды", "in_progress", "Картинг", "Караганда Мотоспорт", "Летний чемпионат по картингу в Караганде.", "01.04.2026", "03.04.2026", "Караганда", "Караганда Арена", 24, 30, 4, "Гонка", "12+")
    )

    fun getTournamentDetail(id: String): TournamentDetailData? = tournamentDetails.find { it.id == id }

    fun getTeamMembers(teamId: String): List<TeamMemberData> = teamMembers[teamId] ?: emptyList()

    fun getTeamSponsorship(teamId: String): SponsorshipItem? =
        sponsorships.find { it.type == SponsorshipType.TEAM && it.targetId == teamId }

    val totalInvested: Long get() = sponsorships.sumOf { it.amount }
    val teamCount: Int get() = sponsorships.count { it.type == SponsorshipType.TEAM }
    val tournamentCount: Int get() = sponsorships.count { it.type == SponsorshipType.TOURNAMENT }
    val pendingInviteCount: Int get() = invites.count { it.status == "pending" }

    fun formatAmount(amount: Long): String = when {
        amount >= 1_000_000 -> {
            val m = amount.toDouble() / 1_000_000
            if (m == m.toLong().toDouble()) "${m.toLong()}M ₸" else "${"%.1f".format(m)}M ₸"
        }
        amount >= 1_000 -> "${amount / 1_000}K ₸"
        else -> "$amount ₸"
    }

    fun getStatusLabel(status: String): String = when (status) {
        "registration_open" -> "Регистрация"
        "registration_closed" -> "Рег. закрыта"
        "check_in" -> "Check-in"
        "in_progress" -> "Идёт"
        "completed" -> "Завершён"
        else -> status
    }

    fun getStatusColor(status: String): Color = when (status) {
        "registration_open", "in_progress" -> Color(0xFFE53535)
        else -> Color(0xFF5A5A6E)
    }
}
