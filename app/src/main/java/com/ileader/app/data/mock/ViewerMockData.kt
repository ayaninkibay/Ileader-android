package com.ileader.app.data.mock

/**
 * Mock data for the Viewer (User) role screens.
 */
object ViewerMockData {

    // ── Platform stats ──────────────────────────────────────────────
    data class PlatformStats(
        val usersCount: Int,
        val tournamentsCount: Int,
        val sportsCount: Int
    )

    val platformStats = PlatformStats(
        usersCount = 1240,
        tournamentsCount = 86,
        sportsCount = 8
    )

    // ── Sports ──────────────────────────────────────────────────────
    data class SportItem(
        val id: String,
        val name: String,
        val description: String,
        val available: Boolean = true
    )

    val sports = listOf(
        SportItem("karting", "Картинг", "Гоночные соревнования"),
        SportItem("shooting", "Стрельба", "Стрелковый спорт"),
        SportItem("tennis", "Теннис", "Ракеточный спорт", available = false),
        SportItem("football", "Футбол", "Командная игра", available = false),
        SportItem("boxing", "Бокс", "Единоборства", available = false),
        SportItem("swimming", "Плавание", "Водный спорт", available = false)
    )

    // ── Tournaments ─────────────────────────────────────────────────
    data class MockTournament(
        val id: String,
        val name: String,
        val sportId: String,
        val sportName: String,
        val status: String,           // registration_open, in_progress, completed, cancelled
        val startDate: String,
        val endDate: String,
        val city: String,
        val location: String,
        val participants: Int,
        val maxParticipants: Int,
        val prize: String? = null,
        val description: String = "",
        val organizerName: String = "iLeader",
        val categories: List<String> = emptyList(),
        val schedule: List<ScheduleItem> = emptyList(),
        val prizes: List<String> = emptyList(),
        val requirements: List<String> = emptyList(),
        val results: List<MockResult> = emptyList()
    )

    data class ScheduleItem(
        val time: String,
        val title: String,
        val description: String? = null
    )

    val tournaments = listOf(
        MockTournament(
            id = "t1",
            name = "Grand Prix Алматы 2026",
            sportId = "karting",
            sportName = "Картинг",
            status = "registration_open",
            startDate = "2026-03-15",
            endDate = "2026-03-16",
            city = "Алматы",
            location = "Алматы Картинг Центр",
            participants = 18,
            maxParticipants = 32,
            prize = "500 000 ₸",
            description = "Главный этап чемпионата по картингу в Алматы. Участвуют лучшие пилоты Казахстана.",
            organizerName = "Федерация Картинга РК",
            categories = listOf("Мини (8-12 лет)", "Кадет (12-15 лет)", "Юниор (15-18 лет)", "Взрослые"),
            schedule = listOf(
                ScheduleItem("08:00", "Регистрация", "Проверка документов и техосмотр"),
                ScheduleItem("09:30", "Свободные заезды"),
                ScheduleItem("11:00", "Квалификация"),
                ScheduleItem("13:00", "Обеденный перерыв"),
                ScheduleItem("14:00", "Финальные заезды"),
                ScheduleItem("17:00", "Награждение")
            ),
            prizes = listOf("250 000 ₸ + кубок", "150 000 ₸ + медаль", "100 000 ₸ + медаль"),
            requirements = listOf(
                "Действующая спортивная лицензия",
                "Медицинская справка",
                "Шлем и экипировка по стандартам FIA",
                "Возраст от 8 лет"
            )
        ),
        MockTournament(
            id = "t2",
            name = "Кубок Астаны по картингу",
            sportId = "karting",
            sportName = "Картинг",
            status = "in_progress",
            startDate = "2026-02-28",
            endDate = "2026-03-01",
            city = "Астана",
            location = "Astana Karting Club",
            participants = 24,
            maxParticipants = 24,
            prize = "300 000 ₸",
            description = "Этап кубка столицы по картингу. Соревнования проходят в закрытом комплексе.",
            organizerName = "Astana Karting Club",
            categories = listOf("Юниор", "Взрослые"),
            results = listOf(
                MockResult("r1", 1, "Алмаз Ибрагимов", "1:23.456", 25),
                MockResult("r2", 2, "Данияр Касымов", "1:24.012", 18),
                MockResult("r3", 3, "Арман Жумабеков", "1:24.578", 15),
                MockResult("r4", 4, "Тимур Сагинтаев", "1:25.101", 12),
                MockResult("r5", 5, "Ерлан Муратов", "1:25.890", 10),
                MockResult("r6", 6, "Бауржан Ахметов", "1:26.234", 8),
                MockResult("r7", 7, "Нурсултан Ержан", "1:26.789", 6),
                MockResult("r8", 8, "Максим Волков", "1:27.100", 4)
            )
        ),
        MockTournament(
            id = "t3",
            name = "Открытый Кубок по стрельбе",
            sportId = "shooting",
            sportName = "Стрельба",
            status = "registration_open",
            startDate = "2026-04-10",
            endDate = "2026-04-12",
            city = "Шымкент",
            location = "Стрелковый клуб «Мерген»",
            participants = 12,
            maxParticipants = 40,
            prize = "200 000 ₸",
            description = "Открытое первенство по практической стрельбе. Приглашаются стрелки всех уровней подготовки.",
            organizerName = "Клуб «Мерген»"
        ),
        MockTournament(
            id = "t4",
            name = "Зимний Чемпионат Картинга",
            sportId = "karting",
            sportName = "Картинг",
            status = "completed",
            startDate = "2026-01-20",
            endDate = "2026-01-21",
            city = "Алматы",
            location = "Алматы Картинг Центр",
            participants = 28,
            maxParticipants = 28,
            prize = "400 000 ₸",
            description = "Завершённый зимний чемпионат по картингу.",
            organizerName = "Федерация Картинга РК",
            results = listOf(
                MockResult("r10", 1, "Данияр Касымов", "1:21.345", 25),
                MockResult("r11", 2, "Алмаз Ибрагимов", "1:21.890", 18),
                MockResult("r12", 3, "Тимур Сагинтаев", "1:22.456", 15),
                MockResult("r13", 4, "Арман Жумабеков", "1:23.012", 12),
                MockResult("r14", 5, "Ерлан Муратов", "1:23.567", 10)
            )
        ),
        MockTournament(
            id = "t5",
            name = "Spring Cup 2026",
            sportId = "karting",
            sportName = "Картинг",
            status = "registration_open",
            startDate = "2026-04-05",
            endDate = "2026-04-06",
            city = "Караганда",
            location = "Караганда Speedway",
            participants = 8,
            maxParticipants = 20,
            prize = "150 000 ₸",
            description = "Весенний этап региональных соревнований по картингу.",
            organizerName = "KaragandaMotorSport"
        ),
        MockTournament(
            id = "t6",
            name = "Чемпионат РК по стрельбе",
            sportId = "shooting",
            sportName = "Стрельба",
            status = "completed",
            startDate = "2025-12-10",
            endDate = "2025-12-12",
            city = "Астана",
            location = "Национальный стрелковый центр",
            participants = 36,
            maxParticipants = 40,
            prize = "600 000 ₸",
            description = "Национальный чемпионат по стрельбе из пневматического оружия.",
            organizerName = "Федерация стрелкового спорта РК",
            results = listOf(
                MockResult("r20", 1, "Серик Байжанов", null, 100, "490 из 500"),
                MockResult("r21", 2, "Нурлан Токтаров", null, 85, "485 из 500"),
                MockResult("r22", 3, "Асхат Маратов", null, 70, "478 из 500")
            )
        )
    )

    // ── Results ─────────────────────────────────────────────────────
    data class MockResult(
        val id: String,
        val position: Int,
        val athleteName: String,
        val time: String? = null,
        val points: Int = 0,
        val score: String? = null,
        val penalty: String? = null,
        val category: String? = null
    )

    // ── News / Articles ─────────────────────────────────────────────
    data class NewsArticle(
        val id: String,
        val title: String,
        val category: String,
        val date: String,
        val summary: String,
        val content: String,
        val authorName: String
    )

    val newsArticles = listOf(
        NewsArticle(
            id = "n1",
            title = "Grand Prix Алматы 2026: открыта регистрация",
            category = "Турниры",
            date = "2026-02-25",
            summary = "Начата регистрация на главный этап чемпионата по картингу в Алматы. Участвуйте!",
            content = "Федерация Картинга Республики Казахстан объявляет о начале регистрации на Grand Prix Алматы 2026. " +
                "Турнир пройдёт 15-16 марта на трассе Алматы Картинг Центр. " +
                "Ожидается участие более 30 пилотов из всех регионов Казахстана. " +
                "Призовой фонд составляет 500 000 тенге. " +
                "Регистрация открыта до 10 марта.",
            authorName = "Редакция iLeader"
        ),
        NewsArticle(
            id = "n2",
            title = "Итоги Зимнего Чемпионата Картинга",
            category = "Результаты",
            date = "2026-01-22",
            summary = "Данияр Касымов стал чемпионом зимнего сезона, обойдя Алмаза Ибрагимова на финише.",
            content = "Завершился Зимний Чемпионат Картинга 2026 в Алматы. Победу одержал Данияр Касымов, " +
                "показавший лучшее время 1:21.345 в финальном заезде. Второе место занял Алмаз Ибрагимов (1:21.890), " +
                "третье — Тимур Сагинтаев (1:22.456). В турнире приняли участие 28 пилотов.",
            authorName = "Спортивный отдел"
        ),
        NewsArticle(
            id = "n3",
            title = "Новый стрелковый клуб открылся в Шымкенте",
            category = "Новости",
            date = "2026-02-10",
            summary = "Стрелковый клуб «Мерген» приглашает всех желающих. Открытый кубок в апреле.",
            content = "В Шымкенте открылся современный стрелковый клуб «Мерген». " +
                "Клуб оборудован по международным стандартам и предлагает обучение стрельбе. " +
                "В апреле клуб проведёт Открытый Кубок по стрельбе с призовым фондом 200 000 тенге.",
            authorName = "Редакция iLeader"
        ),
        NewsArticle(
            id = "n4",
            title = "iLeader запускает мобильное приложение",
            category = "Платформа",
            date = "2026-02-20",
            summary = "Спортивная платформа iLeader теперь доступна на Android. Скачайте и следите за турнирами!",
            content = "Команда iLeader рада объявить о запуске мобильного приложения для Android. " +
                "Теперь вы можете следить за турнирами, результатами и новостями спорта прямо с телефона. " +
                "Приложение доступно для скачивания в Google Play.",
            authorName = "Команда iLeader"
        ),
        NewsArticle(
            id = "n5",
            title = "Серик Байжанов — чемпион РК по стрельбе",
            category = "Результаты",
            date = "2025-12-13",
            summary = "Серик Байжанов завоевал золото Чемпионата РК, набрав 490 из 500 очков.",
            content = "На Национальном чемпионате по стрельбе в Астане золотую медаль завоевал Серик Байжанов. " +
                "Спортсмен набрал 490 из 500 возможных очков, установив новый рекорд. " +
                "Серебро досталось Нурлану Токтарову (485), бронза — Асхату Маратову (478).",
            authorName = "Спортивный отдел"
        )
    )

    val newsCategories = listOf("Все", "Турниры", "Результаты", "Новости", "Платформа")

    // ── Community Profiles ──────────────────────────────────────────
    data class CommunityProfile(
        val id: String,
        val name: String,
        val role: String,      // "athlete", "trainer", "referee"
        val city: String,
        val sportName: String,
        val rating: Int = 0,
        val wins: Int = 0,
        val tournaments: Int = 0,
        val podiums: Int = 0,
        val experience: Int = 0,        // лет опыта (для тренеров/судей)
        val athletesTrained: Int = 0,   // для тренеров
        val tournamentsJudged: Int = 0, // для судей
        val bio: String? = null,
        val nickname: String? = null,
        val subtypeLabel: String? = null
    )

    val athletes = listOf(
        CommunityProfile(
            id = "a1", name = "Алмаз Ибрагимов", role = "athlete",
            city = "Алматы", sportName = "Картинг", rating = 1850,
            wins = 12, tournaments = 28, podiums = 18, nickname = "Flash",
            subtypeLabel = "Пилот",
            bio = "Профессиональный картинг-пилот с 5-летним опытом. Многократный призёр чемпионатов Казахстана."
        ),
        CommunityProfile(
            id = "a2", name = "Данияр Касымов", role = "athlete",
            city = "Астана", sportName = "Картинг", rating = 1920,
            wins = 15, tournaments = 30, podiums = 22, nickname = "Speedy",
            subtypeLabel = "Пилот",
            bio = "Чемпион зимнего сезона 2026. Начал карьеру в 10 лет."
        ),
        CommunityProfile(
            id = "a3", name = "Серик Байжанов", role = "athlete",
            city = "Астана", sportName = "Стрельба", rating = 2100,
            wins = 8, tournaments = 15, podiums = 12,
            subtypeLabel = "Стрелок",
            bio = "Чемпион РК по практической стрельбе. Рекордсмен национального чемпионата."
        ),
        CommunityProfile(
            id = "a4", name = "Арман Жумабеков", role = "athlete",
            city = "Алматы", sportName = "Картинг", rating = 1680,
            wins = 5, tournaments = 20, podiums = 9,
            subtypeLabel = "Пилот"
        ),
        CommunityProfile(
            id = "a5", name = "Тимур Сагинтаев", role = "athlete",
            city = "Караганда", sportName = "Картинг", rating = 1750,
            wins = 8, tournaments = 22, podiums = 14,
            subtypeLabel = "Пилот"
        ),
        CommunityProfile(
            id = "a6", name = "Нурлан Токтаров", role = "athlete",
            city = "Астана", sportName = "Стрельба", rating = 1980,
            wins = 6, tournaments = 12, podiums = 10,
            subtypeLabel = "Стрелок"
        )
    )

    val trainers = listOf(
        CommunityProfile(
            id = "tr1", name = "Мурат Байсеитов", role = "trainer",
            city = "Алматы", sportName = "Картинг", rating = 48,
            experience = 12, athletesTrained = 35,
            bio = "Заслуженный тренер РК по автоспорту. Подготовил 5 чемпионов страны."
        ),
        CommunityProfile(
            id = "tr2", name = "Кайрат Нургалиев", role = "trainer",
            city = "Астана", sportName = "Картинг", rating = 45,
            experience = 8, athletesTrained = 20,
            bio = "Тренер молодёжной сборной по картингу."
        ),
        CommunityProfile(
            id = "tr3", name = "Оксана Ли", role = "trainer",
            city = "Алматы", sportName = "Стрельба", rating = 47,
            experience = 15, athletesTrained = 40,
            bio = "Мастер спорта международного класса по стрельбе. Тренер высшей категории."
        )
    )

    val referees = listOf(
        CommunityProfile(
            id = "ref1", name = "Ержан Омаров", role = "referee",
            city = "Алматы", sportName = "Картинг",
            experience = 10, tournamentsJudged = 45
        ),
        CommunityProfile(
            id = "ref2", name = "Бахыт Сулейменов", role = "referee",
            city = "Астана", sportName = "Стрельба",
            experience = 7, tournamentsJudged = 28
        )
    )

    data class MockTeam(
        val id: String,
        val name: String,
        val city: String,
        val sportName: String,
        val wins: Int,
        val memberCount: Int,
        val rating: Int,
        val trainerName: String? = null
    )

    val teams = listOf(
        MockTeam(
            id = "team1", name = "Almaty Racing", city = "Алматы",
            sportName = "Картинг", wins = 22, memberCount = 8, rating = 1850,
            trainerName = "Мурат Байсеитов"
        ),
        MockTeam(
            id = "team2", name = "Astana Speed", city = "Астана",
            sportName = "Картинг", wins = 18, memberCount = 6, rating = 1780,
            trainerName = "Кайрат Нургалиев"
        ),
        MockTeam(
            id = "team3", name = "Мерген Шымкент", city = "Шымкент",
            sportName = "Стрельба", wins = 10, memberCount = 5, rating = 1600,
            trainerName = "Оксана Ли"
        )
    )

    // ── Features ────────────────────────────────────────────────────
    data class FeatureItem(
        val title: String,
        val description: String
    )

    val features = listOf(
        FeatureItem("Рейтинги", "Честная система подсчёта"),
        FeatureItem("Календарь", "Все турниры в одном месте"),
        FeatureItem("Академия", "Обучение от профи"),
        FeatureItem("Статистика", "Анализ выступлений")
    )
}
