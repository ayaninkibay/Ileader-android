package com.ileader.app.data.mock

import com.ileader.app.data.models.*

object OrganizerMockData {

    // ─── Inner data classes ─────────────────────────────────────

    data class OrganizerStats(
        val totalTournaments: Int,
        val activeTournaments: Int,
        val totalLocations: Int,
        val totalParticipants: Int
    )

    data class OrganizerProfile(
        val organizationName: String,
        val phone: String,
        val bio: String,
        val experience: Int,
        val joinedDate: String,
        val tournamentsOrganized: Int,
        val totalParticipants: Int,
        val website: String,
        val rating: Float
    )

    data class MockParticipant(
        val id: String,
        val name: String,
        val team: String = "",
        val rating: Float = 0f,
        val status: String = "confirmed", // confirmed, pending, cancelled
        val position: Int? = null
    )

    data class MockResultRow(
        val athleteId: String,
        val athleteName: String,
        val number: String,
        val team: String = "",
        val position: Int,
        val time: String = "",
        val points: Int = 0,
        val penalty: String? = null,
        val notes: String? = null
    )

    // ─── Stats ──────────────────────────────────────────────────

    val stats = OrganizerStats(
        totalTournaments = 24,
        activeTournaments = 5,
        totalLocations = 8,
        totalParticipants = 342
    )

    // ─── Profile ────────────────────────────────────────────────

    val profile = OrganizerProfile(
        organizationName = "Турниры Про",
        phone = "+7 (777) 456-78-90",
        bio = "Профессиональный организатор спортивных мероприятий с 8-летним опытом. Провели более 120 турниров по всему Казахстану. Специализируемся на картинге, стрельбе и командных видах спорта.",
        experience = 8,
        joinedDate = "Июнь 2018",
        tournamentsOrganized = 124,
        totalParticipants = 4850,
        website = "https://turniry.pro",
        rating = 4.7f
    )

    // ─── Locations ──────────────────────────────────────────────

    val locations = listOf(
        AdminMockData.AdminLocation("loc-1", "Автодром \"Сокол\"", "karting",
            "ул. Жандосова, 180", "Алматы", 500, 4.8f,
            listOf("Парковка", "Кафе", "Трибуны", "Раздевалки", "Медпункт", "Wi-Fi"),
            32, "Профессиональный автодром с трассой международного уровня. Длина трассы — 1200 метров, 14 поворотов.",
            "+7 (727) 123-45-67", "info@sokol-track.kz", "https://sokol-track.kz", "2024-01-15"),
        AdminMockData.AdminLocation("loc-2", "Тир \"Снайпер\"", "shooting",
            "пр. Стрелковый, 42", "Астана", 80, 4.5f,
            listOf("Закрытый тир", "Открытый тир", "Оружейная", "Класс"),
            18, "Современный тир для пулевой и стендовой стрельбы.",
            "+7 (717) 234-56-78", "sniper@example.com", null, "2024-03-20"),
        AdminMockData.AdminLocation("loc-3", "Картинг-центр \"Адреналин\"", "karting",
            "ул. Скоростная, 8", "Шымкент", 150, 4.2f,
            listOf("Трибуны", "Боксы", "Прокат", "Кафе"),
            12, "Картинг-центр с двумя трассами разного уровня сложности.",
            "+7 (725) 345-67-89", "adrenalin@karting.kz", null, "2024-06-10"),
        AdminMockData.AdminLocation("loc-4", "Автодром \"Северный\"", "karting",
            "ул. Автодромная, 1", "Караганда", 120, 4.9f,
            listOf("Трибуны", "Боксы", "Парковка"),
            24, "Автодром для проведения соревнований по картингу.",
            "+7 (721) 456-78-90", "north@track.kz", null, "2024-02-01"),
        AdminMockData.AdminLocation("loc-5", "Трасса \"Форсаж\"", "karting",
            "ул. Морская, 22", "Актау", 100, 4.3f,
            listOf("Трибуны", "Боксы", "Кафе"),
            15, "Картинг-трасса для проведения соревнований.",
            "+7 (729) 567-89-01", "forsage@track.kz", null, "2024-05-15"),
        AdminMockData.AdminLocation("loc-6", "Картинг-центр \"Старт\"", "karting",
            "ул. Нефтяников, 5", "Атырау", 80, 4.6f,
            listOf("Трибуны", "Прокат"),
            8, "Картинг-центр для проведения соревнований.",
            "+7 (712) 678-90-12", "start@karting.kz", null, "2024-08-01"),
        AdminMockData.AdminLocation("loc-7", "Дворец спорта \"Жекпе-жек\"", "arena",
            "пр. Кабанбай Батыра, 1", "Астана", 5000, 4.1f,
            listOf("Парковка", "VIP-ложи", "Медпункт", "Пресс-центр"),
            6, "Дворец спорта для проведения турниров по боксу, теннису и другим видам.",
            "+7 (717) 789-01-23", "arena@sport-astana.kz", "https://sport-astana.kz", "2024-04-01"),
        AdminMockData.AdminLocation("loc-8", "Спорткомплекс \"Каспий\"", "other",
            "пр. Нурсултана, 12", "Актау", 200, 3.9f,
            listOf("Бассейн", "Парковка", "Душевые", "Раздевалка"),
            4, "Многофункциональный спортивный комплекс для различных мероприятий.",
            "+7 (729) 890-12-34", "kaspiy@sport.kz", null, "2024-07-01")
    )

    // ─── Tournaments ────────────────────────────────────────────

    val tournaments = listOf(
        // DRAFT (2)
        Tournament(
            id = "ot-1", name = "Весенний Кубок по плаванию",
            sportId = "swimming", sportName = "Плавание",
            status = TournamentStatus.DRAFT,
            startDate = "2026-05-10", endDate = "2026-05-12",
            location = "Спорткомплекс \"Каспий\"", city = "Актау",
            description = "Черновик весеннего кубка по плаванию. Дисциплины: вольный стиль, брасс, баттерфляй.",
            format = "Круговая система", maxParticipants = 60, currentParticipants = 0,
            prize = "200 000 ₸", organizerId = "4", locationId = "loc-8",
            ageCategory = "youth", categories = listOf("Вольный стиль 50м", "Брасс 100м", "Баттерфляй 100м"),
            requirements = listOf("Спортивный разряд не ниже 3-го", "Медицинская справка")
        ),
        Tournament(
            id = "ot-2", name = "Открытый чемпионат по теннису",
            sportId = "tennis", sportName = "Теннис",
            status = TournamentStatus.DRAFT,
            startDate = "2026-06-01", endDate = "2026-06-05",
            location = "Дворец спорта \"Жекпе-жек\"", city = "Астана",
            description = "Черновик. Открытый чемпионат Астаны по теннису.",
            format = "Одиночное выбывание", maxParticipants = 32, currentParticipants = 0,
            prize = "250 000 ₸", organizerId = "4", locationId = "loc-7",
            ageCategory = "adult", categories = listOf("Мужской одиночный", "Женский одиночный")
        ),

        // REGISTRATION_OPEN (3)
        Tournament(
            id = "ot-3", name = "Чемпионат Алматы по картингу",
            sportId = "karting", sportName = "Картинг",
            status = TournamentStatus.REGISTRATION_OPEN,
            startDate = "2026-03-15", endDate = "2026-03-17",
            location = "Автодром \"Сокол\"", city = "Алматы",
            description = "Главный картинговый турнир Алматы. Участвуют пилоты со всего Казахстана.",
            format = "Одиночное выбывание", maxParticipants = 64, currentParticipants = 45,
            prize = "500 000 ₸", organizerId = "4", locationId = "loc-1",
            ageCategory = "adult",
            categories = listOf("KZ2", "Mini (6-10 лет)", "Junior (11-15 лет)", "Senior (16+)"),
            prizes = listOf("1 место — 200 000 ₸", "2 место — 150 000 ₸", "3 место — 100 000 ₸"),
            schedule = listOf(
                ScheduleItem("08:00", "Регистрация и техосмотр", "Проверка экипировки и картов"),
                ScheduleItem("09:30", "Свободные заезды", "Ознакомление с трассой"),
                ScheduleItem("11:00", "Квалификация", "Определение стартовых позиций"),
                ScheduleItem("13:00", "Обеденный перерыв"),
                ScheduleItem("14:00", "Предварительные заезды", "Хиты 1 и 2"),
                ScheduleItem("16:00", "Финал", "Финальный заезд"),
                ScheduleItem("17:30", "Награждение", "Церемония награждения")
            ),
            requirements = listOf(
                "Возраст от 14 лет", "Гоночная лицензия категории B или выше",
                "Медицинская справка (не старше 6 месяцев)", "Личная экипировка"
            )
        ),
        Tournament(
            id = "ot-4", name = "Открытый турнир по стрельбе",
            sportId = "shooting", sportName = "Стрельба",
            status = TournamentStatus.REGISTRATION_OPEN,
            startDate = "2026-03-20", endDate = "2026-03-22",
            location = "Тир \"Снайпер\"", city = "Астана",
            description = "Открытый турнир по пулевой стрельбе.",
            format = "Круговая система", maxParticipants = 50, currentParticipants = 32,
            prize = "300 000 ₸", organizerId = "4", locationId = "loc-2",
            ageCategory = "youth",
            categories = listOf("Пистолет 10м", "Винтовка 50м"),
            requirements = listOf("Разрешение на оружие", "Медицинская справка")
        ),
        Tournament(
            id = "ot-5", name = "Детский картинг-турнир",
            sportId = "karting", sportName = "Картинг",
            status = TournamentStatus.REGISTRATION_OPEN,
            startDate = "2026-04-05", endDate = "2026-04-05",
            location = "Картинг-центр \"Адреналин\"", city = "Шымкент",
            description = "Турнир для юных пилотов.",
            format = "Одиночное выбывание", maxParticipants = 20, currentParticipants = 14,
            prize = "50 000 ₸", organizerId = "4", locationId = "loc-3",
            ageCategory = "children"
        ),

        // REGISTRATION_CLOSED (1)
        Tournament(
            id = "ot-6", name = "Кубок Караганды по картингу",
            sportId = "karting", sportName = "Картинг",
            status = TournamentStatus.REGISTRATION_CLOSED,
            startDate = "2026-03-10", endDate = "2026-03-11",
            location = "Автодром \"Северный\"", city = "Караганда",
            description = "Региональный кубок по картингу.",
            format = "Одиночное выбывание", maxParticipants = 32, currentParticipants = 32,
            prize = "200 000 ₸", organizerId = "4", locationId = "loc-4",
            ageCategory = "adult"
        ),

        // CHECK_IN (1)
        Tournament(
            id = "ot-7", name = "Весенний Гран-При Актау",
            sportId = "karting", sportName = "Картинг",
            status = TournamentStatus.CHECK_IN,
            startDate = "2026-03-08", endDate = "2026-03-09",
            location = "Трасса \"Форсаж\"", city = "Актау",
            description = "Весенний Гран-При по картингу на побережье Каспия.",
            format = "Двойное выбывание", maxParticipants = 24, currentParticipants = 22,
            prize = "150 000 ₸", organizerId = "4", locationId = "loc-5",
            ageCategory = "adult"
        ),

        // IN_PROGRESS (2)
        Tournament(
            id = "ot-8", name = "Чемпионат Атырау по картингу",
            sportId = "karting", sportName = "Картинг",
            status = TournamentStatus.IN_PROGRESS,
            startDate = "2026-03-01", endDate = "2026-03-02",
            location = "Картинг-центр \"Старт\"", city = "Атырау",
            description = "Чемпионат города по картингу.",
            format = "Круговая система", maxParticipants = 16, currentParticipants = 16,
            prize = "100 000 ₸", organizerId = "4", locationId = "loc-6",
            ageCategory = "adult"
        ),
        Tournament(
            id = "ot-9", name = "Кубок столицы по боксу",
            sportId = "boxing", sportName = "Бокс",
            status = TournamentStatus.IN_PROGRESS,
            startDate = "2026-02-28", endDate = "2026-03-02",
            location = "Дворец спорта \"Жекпе-жек\"", city = "Астана",
            description = "Боксёрский турнир в нескольких весовых категориях.",
            format = "Одиночное выбывание", maxParticipants = 32, currentParticipants = 28,
            prize = "400 000 ₸", organizerId = "4", locationId = "loc-7",
            ageCategory = "adult"
        ),

        // COMPLETED (2)
        Tournament(
            id = "ot-10", name = "Зимний Кубок по картингу 2025",
            sportId = "karting", sportName = "Картинг",
            status = TournamentStatus.COMPLETED,
            startDate = "2025-12-15", endDate = "2025-12-17",
            location = "Автодром \"Сокол\"", city = "Алматы",
            description = "Завершённый зимний кубок.",
            format = "Одиночное выбывание", maxParticipants = 48, currentParticipants = 44,
            prize = "350 000 ₸", organizerId = "4", locationId = "loc-1",
            ageCategory = "adult"
        ),
        Tournament(
            id = "ot-11", name = "Осенний чемпионат по стрельбе",
            sportId = "shooting", sportName = "Стрельба",
            status = TournamentStatus.COMPLETED,
            startDate = "2025-10-20", endDate = "2025-10-22",
            location = "Тир \"Снайпер\"", city = "Астана",
            description = "Завершённый осенний чемпионат.",
            format = "Круговая система", maxParticipants = 40, currentParticipants = 38,
            prize = "250 000 ₸", organizerId = "4", locationId = "loc-2",
            ageCategory = "adult"
        ),

        // CANCELLED (1)
        Tournament(
            id = "ot-12", name = "Летний турнир по футболу",
            sportId = "football", sportName = "Футбол",
            status = TournamentStatus.CANCELLED,
            startDate = "2026-06-15", endDate = "2026-06-20",
            location = "Дворец спорта \"Жекпе-жек\"", city = "Астана",
            description = "Отменён из-за ремонта площадки.",
            format = "Группы + Плей-офф", maxParticipants = 64, currentParticipants = 12,
            prize = "500 000 ₸", organizerId = "4", locationId = "loc-7",
            ageCategory = "youth"
        )
    )

    // ─── Participants ───────────────────────────────────────────

    val participants = listOf(
        MockParticipant("p-1", "Иван Петров", "Racing Legends", 9.2f, "confirmed", 1),
        MockParticipant("p-2", "Анна Сидорова", "Racing Legends", 8.5f, "confirmed", 2),
        MockParticipant("p-3", "Дмитрий Козлов", "Молния", 7.8f, "confirmed", 3),
        MockParticipant("p-4", "Мария Иванова", "Старт", 8.9f, "confirmed", 4),
        MockParticipant("p-5", "Алексей Смирнов", "", 7.2f, "pending"),
        MockParticipant("p-6", "Елена Волкова", "Racing Legends", 8.1f, "confirmed", 5),
        MockParticipant("p-7", "Сергей Новиков", "Молния", 9.0f, "confirmed", 6),
        MockParticipant("p-8", "Ольга Кузнецова", "", 7.5f, "cancelled")
    )

    // ─── Tournament Results ─────────────────────────────────────

    val tournamentResults = listOf(
        MockResultRow("p-1", "Иван Петров", "23", "Racing Legends", 1, "01:12.340", 250, notes = "Лучший круг"),
        MockResultRow("p-7", "Сергей Новиков", "07", "Молния", 2, "01:12.890", 220),
        MockResultRow("p-4", "Мария Иванова", "44", "Старт", 3, "01:13.120", 180),
        MockResultRow("p-2", "Анна Сидорова", "11", "Racing Legends", 4, "01:13.450", 150),
        MockResultRow("p-6", "Елена Волкова", "18", "Racing Legends", 5, "01:14.020", 120),
        MockResultRow("p-3", "Дмитрий Козлов", "33", "Молния", 6, "01:14.560", 100, "+5 сек", "Фальстарт"),
        MockResultRow("p-5", "Алексей Смирнов", "05", "", 7, "01:15.100", 80),
        MockResultRow("p-8", "Ольга Кузнецова", "28", "", 8, "01:16.230", 60)
    )

    // ─── Helpers ────────────────────────────────────────────────

    fun getLocationById(id: String) = locations.find { it.id == id }

    fun getTournamentById(id: String) = tournaments.find { it.id == id }

    fun getTournamentsByLocationId(locationId: String) = tournaments.filter { it.locationId == locationId }

    fun getUpcomingTournaments() = tournaments.filter {
        it.status == TournamentStatus.REGISTRATION_OPEN || it.status == TournamentStatus.DRAFT
    }

    fun getActiveTournaments() = tournaments.filter {
        it.status == TournamentStatus.IN_PROGRESS || it.status == TournamentStatus.CHECK_IN
    }
}
