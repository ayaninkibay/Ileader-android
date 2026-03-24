package com.ileader.app.data.mock

import com.ileader.app.data.models.*

object AthleteMockData {

    val stats = AthleteStats(
        totalTournaments = 24,
        wins = 8,
        rating = 1850,
        podiums = 14,
        points = 2340,
        accuracy = 58.3f
    )

    val upcomingTournaments = listOf(
        Tournament(
            id = "t1",
            name = "Кубок Скорости 2026",
            sportId = "s1",
            sportName = "Картинг",
            status = TournamentStatus.REGISTRATION_OPEN,
            startDate = "15 марта 2026",
            endDate = "16 марта 2026",
            location = "Автодром «Сокол», Москва",
            description = "Ежегодный турнир по картингу среди любителей и профессионалов. Трасса длиной 1.2 км с 12 поворотами.",
            format = "Квалификация + финальные заезды",
            maxParticipants = 32,
            currentParticipants = 24,
            prize = "150 000 ₽",
            organizerName = "Федерация картинга",
            requirements = listOf(
                "Возраст от 16 лет",
                "Действующая гоночная лицензия",
                "Медицинская справка",
                "Собственный шлем и комбинезон"
            ),
            schedule = listOf(
                ScheduleItem("09:00", "Регистрация участников", "Проверка документов и экипировки"),
                ScheduleItem("10:00", "Свободная практика", "30 минут на трассе"),
                ScheduleItem("11:00", "Квалификация", "Определение стартовых позиций"),
                ScheduleItem("13:00", "Обед"),
                ScheduleItem("14:00", "Финальные заезды", "3 заезда по 15 кругов"),
                ScheduleItem("17:00", "Награждение")
            ),
            prizes = listOf("1 место — 80 000 ₽", "2 место — 45 000 ₽", "3 место — 25 000 ₽")
        ),
        Tournament(
            id = "t2",
            name = "Чемпионат города по теннису",
            sportId = "s2",
            sportName = "Теннис",
            status = TournamentStatus.REGISTRATION_OPEN,
            startDate = "22 марта 2026",
            location = "ТК «Олимпийский», Москва",
            description = "Открытый чемпионат города среди мужчин и женщин.",
            format = "Олимпийская система (плей-офф)",
            maxParticipants = 64,
            currentParticipants = 41,
            prize = "200 000 ₽",
            organizerName = "Городская федерация тенниса"
        ),
        Tournament(
            id = "t3",
            name = "Весенний Кубок по стрельбе",
            sportId = "s3",
            sportName = "Стрельба",
            status = TournamentStatus.IN_PROGRESS,
            startDate = "10 марта 2026",
            endDate = "12 марта 2026",
            location = "Стрелковый клуб «Снайпер», СПб",
            description = "Соревнования по пулевой стрельбе из пневматического оружия.",
            format = "Групповой этап + финал",
            maxParticipants = 48,
            currentParticipants = 48,
            prize = "100 000 ₽",
            organizerName = "Стрелковый союз"
        ),
        Tournament(
            id = "t4",
            name = "Летний Гран-При",
            sportId = "s1",
            sportName = "Картинг",
            status = TournamentStatus.REGISTRATION_OPEN,
            startDate = "5 апреля 2026",
            location = "Трасса «Формула», Казань",
            description = "Международный турнир с участием спортсменов из 5 стран.",
            format = "3 этапа + суперфинал",
            maxParticipants = 40,
            currentParticipants = 18,
            prize = "300 000 ₽",
            organizerName = "Racing Club International"
        )
    )

    val allTournaments = upcomingTournaments + listOf(
        Tournament(
            id = "t5",
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
            organizerName = "Федерация картинга"
        ),
        Tournament(
            id = "t6",
            name = "Осенний чемпионат",
            sportId = "s2",
            sportName = "Теннис",
            status = TournamentStatus.COMPLETED,
            startDate = "20 октября 2025",
            location = "ТК «Олимпийский», Москва",
            format = "Круговая система",
            maxParticipants = 16,
            currentParticipants = 16,
            prize = "80 000 ₽",
            organizerName = "Городская федерация тенниса"
        ),
        Tournament(
            id = "t7",
            name = "Кубок Новичков",
            sportId = "s1",
            sportName = "Картинг",
            status = TournamentStatus.CANCELLED,
            startDate = "1 февраля 2026",
            location = "Автодром «Старт», Сочи",
            format = "Открытый формат",
            maxParticipants = 24,
            currentParticipants = 5,
            prize = "50 000 ₽",
            organizerName = "Сочи Racing"
        )
    )

    val results = listOf(
        TournamentResult("r1", "t5", "Зимний Кубок 2025", "15 дек 2025", 1, 100, 32, "s1", "Картинг"),
        TournamentResult("r2", "t6", "Осенний чемпионат", "20 окт 2025", 3, 60, 16, "s2", "Теннис"),
        TournamentResult("r3", "t10", "Кубок Дружбы", "5 сен 2025", 2, 80, 24, "s1", "Картинг"),
        TournamentResult("r4", "t11", "Летние соревнования", "12 июл 2025", 1, 100, 28, "s3", "Стрельба"),
        TournamentResult("r5", "t12", "Весенний турнир", "20 апр 2025", 5, 35, 40, "s1", "Картинг"),
        TournamentResult("r6", "t13", "Мартовский Кубок", "10 мар 2025", 1, 100, 20, "s1", "Картинг"),
        TournamentResult("r7", "t14", "Зимний Гран-При 2024", "15 дек 2024", 4, 45, 36, "s1", "Картинг"),
        TournamentResult("r8", "t15", "Открытый чемпионат", "28 ноя 2024", 2, 80, 32, "s2", "Теннис"),
        TournamentResult("r9", "t16", "Кубок Мастеров", "10 окт 2024", 1, 100, 24, "s3", "Стрельба"),
        TournamentResult("r10", "t17", "Осенний заезд", "5 сен 2024", 6, 25, 30, "s1", "Картинг"),
        TournamentResult("r11", "t18", "Летний Кубок", "20 июл 2024", 1, 100, 20, "s1", "Картинг"),
        TournamentResult("r12", "t19", "Кубок Города", "15 июн 2024", 3, 60, 48, "s2", "Теннис")
    )

    val goals = listOf(
        AthleteGoal(
            id = "g1",
            type = GoalType.RATING,
            title = "Достичь рейтинга 2000",
            description = "Повысить рейтинг до 2000 очков к концу сезона",
            deadline = "31 дек 2026",
            status = GoalStatus.ACTIVE,
            targetValue = 2000,
            currentValue = 1850,
            createdAt = "1 янв 2026"
        ),
        AthleteGoal(
            id = "g2",
            type = GoalType.TOURNAMENT,
            title = "Выиграть 5 турниров",
            description = "Одержать победу минимум в 5 турнирах за сезон",
            deadline = "31 дек 2026",
            status = GoalStatus.ACTIVE,
            targetValue = 5,
            currentValue = 2,
            createdAt = "1 янв 2026"
        ),
        AthleteGoal(
            id = "g3",
            type = GoalType.POINTS,
            title = "Набрать 500 очков за квартал",
            description = "Набрать 500 очков в рейтинговых турнирах за Q1 2026",
            deadline = "31 мар 2026",
            status = GoalStatus.ACTIVE,
            targetValue = 500,
            currentValue = 340,
            createdAt = "1 янв 2026"
        ),
        AthleteGoal(
            id = "g4",
            type = GoalType.TOURNAMENT,
            title = "Участие в 10 турнирах",
            description = "Принять участие минимум в 10 турнирах за 2025 год",
            deadline = "31 дек 2025",
            status = GoalStatus.COMPLETED,
            targetValue = 10,
            currentValue = 12,
            createdAt = "1 янв 2025"
        ),
        AthleteGoal(
            id = "g5",
            type = GoalType.RATING,
            title = "Войти в топ-10 рейтинга",
            description = "Занять место в десятке лучших спортсменов региона",
            deadline = "30 июн 2025",
            status = GoalStatus.FAILED,
            targetValue = 10,
            currentValue = 15,
            createdAt = "1 янв 2025"
        )
    )

    val team = Team(
        id = "team1",
        name = "Speed Demons",
        sportId = "s1",
        sportName = "Картинг",
        trainerId = "trainer1",
        trainerName = "Иван Сергеевич Козлов",
        sponsorName = "AutoParts Pro",
        foundedDate = "Март 2023",
        description = "Команда профессиональных картингистов. Выступаем на региональных и национальных соревнованиях с 2023 года.",
        members = listOf(
            TeamMember("m1", "Алексей Петров", "Капитан", tournaments = 24, wins = 8, podiums = 14),
            TeamMember("m2", "Дмитрий Волков", "Пилот", tournaments = 18, wins = 5, podiums = 9),
            TeamMember("m3", "Михаил Орлов", "Пилот", tournaments = 15, wins = 3, podiums = 7),
            TeamMember("m4", "Анна Соколова", "Пилот", tournaments = 12, wins = 4, podiums = 8),
            TeamMember("m5", "Кирилл Новиков", "Пилот-новичок", tournaments = 6, wins = 1, podiums = 2)
        )
    )

    val tournamentInvites = listOf(
        TournamentInvite(
            id = "inv1",
            tournamentId = "t1",
            tournamentName = "Кубок Скорости 2026",
            status = InviteStatus.PENDING,
            message = "Приглашаем вас принять участие в ежегодном Кубке Скорости!",
            createdAt = "25 фев 2026"
        ),
        TournamentInvite(
            id = "inv2",
            tournamentId = "t4",
            tournamentName = "Летний Гран-При",
            status = InviteStatus.PENDING,
            message = "Ваши результаты впечатляют! Ждём вас на Гран-При.",
            createdAt = "20 фев 2026"
        ),
        TournamentInvite(
            id = "inv3",
            tournamentId = "t5",
            tournamentName = "Зимний Кубок 2025",
            status = InviteStatus.ACCEPTED,
            message = "Приглашение на Зимний Кубок",
            createdAt = "1 дек 2025"
        )
    )

    val teamRequests = listOf(
        TeamRequest(
            id = "tr1",
            teamId = "team2",
            teamName = "Racing Stars",
            status = InviteStatus.PENDING,
            message = "Хотел бы присоединиться к вашей команде",
            createdAt = "22 фев 2026"
        ),
        TeamRequest(
            id = "tr2",
            teamId = "team3",
            teamName = "Thunder Racing",
            status = InviteStatus.DECLINED,
            message = "Заявка на вступление",
            responseMessage = "К сожалению, все места заняты",
            createdAt = "10 янв 2026"
        )
    )

    val license = License(
        number = "RUS-KRT-2025-1847",
        category = "Национальная",
        issueDate = "15 янв 2025",
        expiryDate = "15 янв 2026",
        status = "active",
        className = "Класс B",
        federation = "Российская автомобильная федерация",
        medicalCheckDate = "10 янв 2025",
        medicalCheckExpiry = "10 янв 2026"
    )

    val sports = listOf(
        "Картинг" to "s1",
        "Теннис" to "s2",
        "Стрельба" to "s3"
    )

    val ratingHistory = listOf(
        "Сен" to 1620,
        "Окт" to 1700,
        "Ноя" to 1680,
        "Дек" to 1780,
        "Янв" to 1820,
        "Фев" to 1850
    )

    val leaderboard = listOf(
        Triple("Максим Иванов", 2450, 1),
        Triple("Сергей Кузнецов", 2380, 2),
        Triple("Артём Морозов", 2210, 3),
        Triple("Денис Попов", 2100, 4),
        Triple("Алексей Петров", 1850, 5)
    )
}
