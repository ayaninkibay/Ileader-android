package com.ileader.app.data.mock

import com.ileader.app.data.models.*

object TrainerMockData {

    val profile = User(
        id = "trainer1",
        name = "Иван Сергеевич Козлов",
        email = "kozlov@ileader.ru",
        role = UserRole.TRAINER,
        phone = "+7 (999) 123-45-67",
        city = "Москва",
        country = "Россия",
        bio = "Профессиональный тренер по картингу и стрельбе. 15 лет опыта подготовки спортсменов к национальным и международным соревнованиям.",
        birthDate = "12 мая 1985",
        sportIds = listOf("s1", "s3"),
        verification = VerificationStatus.VERIFIED,
        status = UserStatus.ACTIVE,
        createdAt = "10 янв 2023",
        teamId = "team1"
    )

    val sports = listOf(
        "Картинг" to "s1",
        "Стрельба" to "s3"
    )

    // === TEAMS ===

    data class TrainerTeam(
        val id: String,
        val name: String,
        val sportId: String,
        val sportName: String,
        val description: String,
        val foundedYear: Int,
        val ageCategory: String,
        val members: List<TrainerAthlete>
    )

    data class TrainerAthlete(
        val id: String,
        val name: String,
        val email: String,
        val avatarUrl: String? = null,
        val age: Int,
        val joinedDate: String,
        val tournaments: Int,
        val wins: Int,
        val podiums: Int,
        val rating: Int,
        val bio: String = ""
    )

    data class TrainerNotification(
        val id: String,
        val type: String,
        val title: String,
        val message: String,
        val fromName: String,
        val teamName: String? = null,
        val status: InviteStatus = InviteStatus.PENDING,
        val createdAt: String
    )

    data class PendingInvite(
        val id: String,
        val athleteName: String,
        val athleteEmail: String,
        val teamId: String,
        val teamName: String,
        val sentAt: String,
        val status: InviteStatus = InviteStatus.PENDING
    )

    val teams = listOf(
        TrainerTeam(
            id = "team1",
            name = "Racing Legends",
            sportId = "s1",
            sportName = "Картинг",
            description = "Профессиональная команда по картингу. Выступаем на региональных и национальных соревнованиях.",
            foundedYear = 2021,
            ageCategory = "Взрослые",
            members = listOf(
                TrainerAthlete("a1", "Алексей Петров", "petrov@mail.ru", null, 22, "Мар 2023", 24, 8, 14, 1850, "Капитан команды, специализация — спринт"),
                TrainerAthlete("a2", "Дмитрий Волков", "volkov@mail.ru", null, 20, "Июн 2023", 18, 5, 9, 1720, "Стабильный пилот с хорошей техникой"),
                TrainerAthlete("a3", "Михаил Орлов", "orlov@mail.ru", null, 19, "Сен 2023", 15, 3, 7, 1580, "Молодой талант, быстро прогрессирует"),
                TrainerAthlete("a4", "Анна Соколова", "sokolova@mail.ru", null, 21, "Янв 2024", 12, 4, 8, 1690, "Сильный финишёр"),
                TrainerAthlete("a5", "Кирилл Новиков", "novikov@mail.ru", null, 18, "Авг 2024", 6, 1, 2, 1320, "Новичок с потенциалом")
            )
        ),
        TrainerTeam(
            id = "team2",
            name = "Eagle Shooters",
            sportId = "s3",
            sportName = "Стрельба",
            description = "Команда стрелков. Пулевая и стендовая стрельба.",
            foundedYear = 2022,
            ageCategory = "Взрослые",
            members = listOf(
                TrainerAthlete("a6", "Сергей Кузнецов", "kuznetsov@mail.ru", null, 25, "Фев 2022", 20, 7, 12, 1920, "Мастер спорта по пулевой стрельбе"),
                TrainerAthlete("a7", "Елена Морозова", "morozova@mail.ru", null, 23, "Апр 2022", 16, 4, 9, 1750, "Специализация — стендовая стрельба"),
                TrainerAthlete("a8", "Артём Белов", "belov@mail.ru", null, 21, "Окт 2022", 14, 3, 6, 1600, "Перспективный стрелок")
            )
        ),
        TrainerTeam(
            id = "team3",
            name = "Junior Racers",
            sportId = "s1",
            sportName = "Картинг",
            description = "Юношеская команда по картингу. Подготовка молодых пилотов.",
            foundedYear = 2023,
            ageCategory = "Юниоры",
            members = listOf(
                TrainerAthlete("a9", "Максим Иванов", "ivanov.m@mail.ru", null, 16, "Мар 2023", 10, 3, 5, 1450, "Лидер юниорской команды"),
                TrainerAthlete("a10", "Даниил Сидоров", "sidorov@mail.ru", null, 15, "Май 2023", 8, 2, 4, 1380, "Хорошая реакция и тактика"),
                TrainerAthlete("a11", "София Козлова", "kozlova.s@mail.ru", null, 14, "Авг 2023", 6, 1, 3, 1280, "Быстро учится"),
                TrainerAthlete("a12", "Тимур Алиев", "aliev@mail.ru", null, 15, "Ноя 2023", 5, 1, 2, 1220, "Смелый пилот"),
                TrainerAthlete("a13", "Ева Романова", "romanova@mail.ru", null, 14, "Фев 2024", 3, 0, 1, 1100, "Новичок, быстро адаптируется")
            )
        )
    )

    // === TEAM STATISTICS ===

    data class TeamStats(
        val athleteCount: Int,
        val totalTournaments: Int,
        val totalWins: Int,
        val totalPodiums: Int,
        val avgRating: Int,
        val winRate: Float
    )

    fun getTeamStats(teamId: String): TeamStats {
        val team = teams.find { it.id == teamId } ?: return TeamStats(0, 0, 0, 0, 0, 0f)
        val members = team.members
        val totalT = members.sumOf { it.tournaments }
        val totalW = members.sumOf { it.wins }
        val totalP = members.sumOf { it.podiums }
        val avgR = if (members.isNotEmpty()) members.sumOf { it.rating } / members.size else 0
        val winR = if (totalT > 0) totalW.toFloat() / totalT * 100 else 0f
        return TeamStats(members.size, totalT, totalW, totalP, avgR, winR)
    }

    // === TOURNAMENTS ===

    val tournaments = listOf(
        Tournament(
            id = "tt1",
            name = "Кубок Скорости 2026",
            sportId = "s1",
            sportName = "Картинг",
            status = TournamentStatus.REGISTRATION_OPEN,
            startDate = "15 марта 2026",
            endDate = "16 марта 2026",
            location = "Автодром «Сокол», Москва",
            description = "Ежегодный турнир по картингу",
            format = "Квалификация + финалы",
            maxParticipants = 32,
            currentParticipants = 24,
            prize = "150 000 ₽",
            organizerName = "Федерация картинга",
            ageCategory = "Взрослые"
        ),
        Tournament(
            id = "tt2",
            name = "Весенний Кубок по стрельбе",
            sportId = "s3",
            sportName = "Стрельба",
            status = TournamentStatus.REGISTRATION_OPEN,
            startDate = "22 марта 2026",
            location = "Стрелковый клуб «Снайпер», СПб",
            description = "Соревнования по пулевой стрельбе",
            format = "Групповой этап + финал",
            maxParticipants = 48,
            currentParticipants = 30,
            prize = "100 000 ₽",
            organizerName = "Стрелковый союз",
            ageCategory = "Взрослые"
        ),
        Tournament(
            id = "tt3",
            name = "Юниорский Гран-При",
            sportId = "s1",
            sportName = "Картинг",
            status = TournamentStatus.REGISTRATION_OPEN,
            startDate = "5 апреля 2026",
            location = "Трасса «Формула», Казань",
            description = "Турнир для юниоров",
            format = "3 этапа + суперфинал",
            maxParticipants = 24,
            currentParticipants = 12,
            prize = "80 000 ₽",
            organizerName = "Racing Club",
            ageCategory = "Юниоры"
        ),
        Tournament(
            id = "tt4",
            name = "Летний Гран-При",
            sportId = "s1",
            sportName = "Картинг",
            status = TournamentStatus.IN_PROGRESS,
            startDate = "28 февраля 2026",
            endDate = "1 марта 2026",
            location = "Автодром «Сокол», Москва",
            description = "Международный турнир",
            format = "Квалификация + финалы",
            maxParticipants = 40,
            currentParticipants = 38,
            prize = "300 000 ₽",
            organizerName = "Racing International",
            ageCategory = "Взрослые"
        ),
        Tournament(
            id = "tt5",
            name = "Зимний Кубок 2025",
            sportId = "s1",
            sportName = "Картинг",
            status = TournamentStatus.COMPLETED,
            startDate = "15 декабря 2025",
            location = "Автодром «Сокол», Москва",
            format = "Квалификация + финалы",
            maxParticipants = 32,
            currentParticipants = 32,
            prize = "120 000 ₽",
            organizerName = "Федерация картинга",
            ageCategory = "Взрослые"
        ),
        Tournament(
            id = "tt6",
            name = "Осенний чемпионат по стрельбе",
            sportId = "s3",
            sportName = "Стрельба",
            status = TournamentStatus.COMPLETED,
            startDate = "20 октября 2025",
            location = "Тир «Олимп», Москва",
            format = "Круговая система",
            maxParticipants = 24,
            currentParticipants = 24,
            prize = "90 000 ₽",
            organizerName = "Стрелковый союз",
            ageCategory = "Взрослые"
        )
    )

    // Which tournaments each team is registered for
    val registeredTournaments = mutableMapOf(
        "team1" to mutableListOf("tt1", "tt4", "tt5"),
        "team2" to mutableListOf("tt2", "tt6"),
        "team3" to mutableListOf("tt3")
    )

    // === RATING PROGRESS ===

    val ratingProgressByTeam = mapOf(
        "team1" to listOf("Сен" to 1580, "Окт" to 1620, "Ноя" to 1660, "Дек" to 1700, "Янв" to 1740, "Фев" to 1770),
        "team2" to listOf("Сен" to 1650, "Окт" to 1690, "Ноя" to 1710, "Дек" to 1730, "Янв" to 1760, "Фев" to 1790),
        "team3" to listOf("Сен" to 1100, "Окт" to 1180, "Ноя" to 1220, "Дек" to 1280, "Янв" to 1320, "Фев" to 1370)
    )

    // === RESULTS DISTRIBUTION ===

    data class ResultDistribution(
        val label: String,
        val value: Int,
        val color: Long // ARGB hex
    )

    val resultsDistributionByTeam = mapOf(
        "team1" to listOf(
            ResultDistribution("1 место", 8, 0xFFFFD700),
            ResultDistribution("2 место", 6, 0xFFC0C0C0),
            ResultDistribution("3 место", 5, 0xFFCD7F32),
            ResultDistribution("Другое", 12, 0xFF6B7280)
        ),
        "team2" to listOf(
            ResultDistribution("1 место", 7, 0xFFFFD700),
            ResultDistribution("2 место", 5, 0xFFC0C0C0),
            ResultDistribution("3 место", 4, 0xFFCD7F32),
            ResultDistribution("Другое", 8, 0xFF6B7280)
        ),
        "team3" to listOf(
            ResultDistribution("1 место", 3, 0xFFFFD700),
            ResultDistribution("2 место", 3, 0xFFC0C0C0),
            ResultDistribution("3 место", 4, 0xFFCD7F32),
            ResultDistribution("Другое", 10, 0xFF6B7280)
        )
    )

    // === PENDING INVITES ===

    val pendingInvites = listOf(
        PendingInvite("pi1", "Олег Васильев", "vasiliev@mail.ru", "team1", "Racing Legends", "25 фев 2026"),
        PendingInvite("pi2", "Марина Козлова", "kozlova.m@mail.ru", "team2", "Eagle Shooters", "22 фев 2026")
    )

    // === NOTIFICATIONS ===

    val notifications = listOf(
        TrainerNotification(
            id = "n1",
            type = "join_request",
            title = "Запрос на вступление",
            message = "Хочу присоединиться к вашей команде Racing Legends",
            fromName = "Виктор Смирнов",
            teamName = "Racing Legends",
            status = InviteStatus.PENDING,
            createdAt = "27 фев 2026"
        ),
        TrainerNotification(
            id = "n2",
            type = "join_request",
            title = "Запрос на вступление",
            message = "Занимаюсь стрельбой 3 года, хотел бы тренироваться у вас",
            fromName = "Павел Григорьев",
            teamName = "Eagle Shooters",
            status = InviteStatus.PENDING,
            createdAt = "26 фев 2026"
        ),
        TrainerNotification(
            id = "n3",
            type = "tournament_result",
            title = "Результаты турнира",
            message = "Зимний Кубок 2025 завершён. Ваша команда заняла 2 место!",
            fromName = "Федерация картинга",
            createdAt = "16 дек 2025"
        ),
        TrainerNotification(
            id = "n4",
            type = "sponsor_offer",
            title = "Предложение спонсорства",
            message = "Предлагаем спонсорский контракт для Racing Legends на сезон 2026",
            fromName = "AutoParts Pro",
            teamName = "Racing Legends",
            status = InviteStatus.PENDING,
            createdAt = "20 фев 2026"
        ),
        TrainerNotification(
            id = "n5",
            type = "join_request",
            title = "Запрос на вступление",
            message = "Мне 15 лет, хочу в юниорскую команду",
            fromName = "Артём Власов",
            teamName = "Junior Racers",
            status = InviteStatus.ACCEPTED,
            createdAt = "10 фев 2026"
        ),
        TrainerNotification(
            id = "n6",
            type = "join_request",
            title = "Запрос на вступление",
            message = "Заявка на вступление",
            fromName = "Ольга Петрова",
            teamName = "Racing Legends",
            status = InviteStatus.DECLINED,
            createdAt = "5 фев 2026"
        )
    )

    // === ATHLETE RESULTS (for detail screen) ===

    fun getAthleteResults(athleteId: String): List<TournamentResult> {
        val athlete = teams.flatMap { it.members }.find { it.id == athleteId }
        if (athlete == null) return emptyList()
        // Generate mock results based on athlete stats
        return listOf(
            TournamentResult("ar1", "tt5", "Зимний Кубок 2025", "15 дек 2025", 1, 100, 32, "s1", "Картинг"),
            TournamentResult("ar2", "tt6", "Осенний чемпионат", "20 окт 2025", 3, 60, 24, "s3", "Стрельба"),
            TournamentResult("ar3", "tt10", "Кубок Дружбы", "5 сен 2025", 2, 80, 20, "s1", "Картинг"),
            TournamentResult("ar4", "tt11", "Летние соревнования", "12 июл 2025", 5, 35, 28, "s1", "Картинг"),
            TournamentResult("ar5", "tt12", "Весенний турнир", "20 апр 2025", 1, 100, 16, "s1", "Картинг")
        )
    }

    fun getAthleteRatingHistory(athleteId: String): List<Pair<String, Int>> {
        val athlete = teams.flatMap { it.members }.find { it.id == athleteId }
        val base = athlete?.rating ?: 1500
        return listOf(
            "Сен" to (base - 230),
            "Окт" to (base - 180),
            "Ноя" to (base - 140),
            "Дек" to (base - 80),
            "Янв" to (base - 30),
            "Фев" to base
        )
    }
}
