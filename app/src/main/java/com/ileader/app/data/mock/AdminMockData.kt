package com.ileader.app.data.mock

import com.ileader.app.data.models.*
import com.ileader.app.ui.screens.admin.AdminUtils
import com.ileader.app.ui.theme.ILeaderColors

object AdminMockData {

    // ─── Stats ───────────────────────────────────────────────
    data class AdminStats(
        val totalUsers: Int = 156,
        val activeUsers: Int = 142,
        val blockedUsers: Int = 14,
        val activeTournaments: Int = 8,
        val totalTournaments: Int = 45,
        val totalSports: Int = 8
    )

    val stats = AdminStats()

    // ─── Chart Data ──────────────────────────────────────────
    data class ChartPoint(val label: String, val value: Int)

    val userGrowth = listOf(
        ChartPoint("Авг", 45), ChartPoint("Сен", 62), ChartPoint("Окт", 78),
        ChartPoint("Ноя", 95), ChartPoint("Дек", 120), ChartPoint("Янв", 142),
        ChartPoint("Фев", 156)
    )

    val roleDistribution = listOf(
        ChartPoint("Спортсмены", 68), ChartPoint("Тренеры", 22),
        ChartPoint("Организаторы", 15), ChartPoint("Судьи", 12),
        ChartPoint("Спонсоры", 18), ChartPoint("СМИ", 8),
        ChartPoint("Зрители", 13)
    )

    val tournamentsBySport = listOf(
        ChartPoint("Картинг", 12), ChartPoint("Стрельба", 8),
        ChartPoint("Теннис", 6), ChartPoint("Футбол", 10),
        ChartPoint("Бокс", 5), ChartPoint("Плавание", 4)
    )

    // ─── Users ───────────────────────────────────────────────
    data class AdminUser(
        val id: String, val name: String, val email: String, val role: UserRole,
        val status: UserStatus = UserStatus.ACTIVE,
        val verification: VerificationStatus = VerificationStatus.NOT_REQUIRED,
        val phone: String? = null, val city: String? = null,
        val athleteSubtype: AthleteSubtype? = null,
        val sportIds: List<String> = emptyList(), val createdAt: String = "2026-01-15"
    )

    val users = listOf(
        AdminUser("1", "Алексей Петров", "athlete@demo.com", UserRole.ATHLETE, phone = "+7 777 123 4567", city = "Алматы", athleteSubtype = AthleteSubtype.PILOT, sportIds = listOf("karting")),
        AdminUser("2", "Иван Сергеев", "trainer@demo.com", UserRole.TRAINER, verification = VerificationStatus.VERIFIED, phone = "+7 777 234 5678", city = "Астана", sportIds = listOf("karting", "boxing")),
        AdminUser("3", "Мария Козлова", "organizer@demo.com", UserRole.ORGANIZER, verification = VerificationStatus.VERIFIED, phone = "+7 777 345 6789", city = "Алматы", sportIds = listOf("karting", "shooting")),
        AdminUser("4", "Дмитрий Волков", "referee@demo.com", UserRole.REFEREE, verification = VerificationStatus.VERIFIED, phone = "+7 777 456 7890", city = "Шымкент", sportIds = listOf("karting")),
        AdminUser("5", "Елена Новикова", "sponsor@demo.com", UserRole.SPONSOR, verification = VerificationStatus.VERIFIED, phone = "+7 777 567 8901", city = "Караганда"),
        AdminUser("6", "Анна Медведева", "media@demo.com", UserRole.MEDIA, verification = VerificationStatus.VERIFIED, phone = "+7 777 678 9012", city = "Алматы"),
        AdminUser("7", "Админ Системы", "admin@demo.com", UserRole.ADMIN, phone = "+7 777 789 0123", city = "Алматы"),
        AdminUser("8", "Зритель Иванов", "user@demo.com", UserRole.USER, city = "Актау"),
        AdminUser("9", "Сергей Картингист", "pilot2@demo.com", UserRole.ATHLETE, phone = "+7 777 111 2222", city = "Алматы", athleteSubtype = AthleteSubtype.PILOT, sportIds = listOf("karting"), createdAt = "2026-02-01"),
        AdminUser("10", "Ольга Стрелок", "shooter@demo.com", UserRole.ATHLETE, phone = "+7 777 222 3333", city = "Астана", athleteSubtype = AthleteSubtype.SHOOTER, sportIds = listOf("shooting"), createdAt = "2026-02-05"),
        AdminUser("11", "Никита Боксёр", "boxer@demo.com", UserRole.ATHLETE, phone = "+7 777 333 4444", city = "Шымкент", athleteSubtype = AthleteSubtype.BOXER, sportIds = listOf("boxing"), createdAt = "2026-02-10"),
        AdminUser("12", "Павел Заблокиров", "blocked@demo.com", UserRole.ATHLETE, status = UserStatus.BLOCKED, city = "Караганда", athleteSubtype = AthleteSubtype.GENERAL, createdAt = "2025-11-20"),
        AdminUser("13", "Тренер Кайрат", "trainer2@demo.com", UserRole.TRAINER, verification = VerificationStatus.PENDING, phone = "+7 777 444 5555", city = "Алматы", sportIds = listOf("football"), createdAt = "2026-02-15"),
        AdminUser("14", "Организатор Марат", "organizer2@demo.com", UserRole.ORGANIZER, verification = VerificationStatus.PENDING, phone = "+7 777 555 6666", city = "Астана", sportIds = listOf("tennis"), createdAt = "2026-02-18"),
        AdminUser("15", "Журналист Айгуль", "media2@demo.com", UserRole.MEDIA, verification = VerificationStatus.REJECTED, phone = "+7 777 666 7777", city = "Алматы", createdAt = "2026-01-20")
    )

    // ─── Sports ──────────────────────────────────────────────
    data class AdminSport(
        val id: String, val name: String, val slug: String, val description: String,
        val isActive: Boolean = true, val athleteCount: Int, val tournamentCount: Int,
        val createdAt: String = "2025-06-01"
    )

    val sports = listOf(
        AdminSport("1", "Картинг", "karting", "Гонки на картах по специализированным трассам", athleteCount = 45, tournamentCount = 12),
        AdminSport("2", "Стрельба", "shooting", "Стрелковый спорт из различных видов оружия", athleteCount = 28, tournamentCount = 8),
        AdminSport("3", "Теннис", "tennis", "Большой теннис — одиночные и парные соревнования", athleteCount = 32, tournamentCount = 6),
        AdminSport("4", "Футбол", "football", "Командный вид спорта с мячом", athleteCount = 64, tournamentCount = 10),
        AdminSport("5", "Бокс", "boxing", "Контактный вид спорта — поединки на ринге", athleteCount = 22, tournamentCount = 5),
        AdminSport("6", "Плавание", "swimming", "Водные виды спорта и соревнования", athleteCount = 18, tournamentCount = 4),
        AdminSport("7", "Лёгкая атлетика", "athletics", "Беговые, прыжковые и метательные дисциплины", athleteCount = 38, tournamentCount = 7),
        AdminSport("8", "Гребля", "rowing", "Гребной спорт на байдарках и каноэ", isActive = false, athleteCount = 8, tournamentCount = 2)
    )

    val sportNames = mapOf(
        "karting" to "Картинг", "shooting" to "Стрельба", "tennis" to "Теннис",
        "football" to "Футбол", "boxing" to "Бокс", "swimming" to "Плавание",
        "athletics" to "Лёгкая атлетика", "rowing" to "Гребля"
    )

    // ─── Tournaments ─────────────────────────────────────────
    data class AdminTournament(
        val id: String, val name: String, val sportSlug: String, val sportName: String,
        val locationName: String, val locationCity: String,
        val startDate: String, val endDate: String, val status: String,
        val participantCount: Int, val maxParticipants: Int?,
        val organizerName: String, val ageCategory: String? = null
    )

    val tournaments = listOf(
        AdminTournament("1", "Кубок Алматы по картингу 2026", "karting", "Картинг", "Алматы Картодром", "Алматы", "2026-03-15", "2026-03-16", "registration_open", 24, 32, "Мария Козлова", "adult"),
        AdminTournament("2", "Чемпионат РК по стрельбе", "shooting", "Стрельба", "Тир «Мишень»", "Астана", "2026-04-10", "2026-04-12", "registration_open", 16, 40, "Мария Козлова", "adult"),
        AdminTournament("3", "Детский турнир по картингу", "karting", "Картинг", "Алматы Картодром", "Алматы", "2026-03-20", "2026-03-20", "in_progress", 18, 20, "Организатор Марат", "children"),
        AdminTournament("4", "Весенний теннисный турнир", "tennis", "Теннис", "Теннис-центр Mega", "Алматы", "2026-04-01", "2026-04-03", "registration_open", 8, 16, "Мария Козлова", "youth"),
        AdminTournament("5", "Бокс Кубок Столицы", "boxing", "Бокс", "Спорт-Арена", "Астана", "2026-02-20", "2026-02-22", "completed", 32, 32, "Организатор Марат", "adult"),
        AdminTournament("6", "Зимний чемпионат по плаванию", "swimming", "Плавание", "Бассейн Олимпик", "Караганда", "2026-01-15", "2026-01-17", "completed", 28, 50, "Мария Козлова"),
        AdminTournament("7", "Марафон Алматы 2026", "athletics", "Лёгкая атлетика", "Стадион Центральный", "Алматы", "2026-05-01", "2026-05-01", "draft", 0, 500, "Организатор Марат"),
        AdminTournament("8", "Футбольный турнир «Кожаный мяч»", "football", "Футбол", "Стадион Центральный", "Алматы", "2026-03-25", "2026-03-30", "in_progress", 48, 64, "Мария Козлова", "children")
    )

    // ─── Locations ───────────────────────────────────────────
    data class AdminLocation(
        val id: String, val name: String, val type: String, val address: String,
        val city: String, val capacity: Int, val rating: Float? = null,
        val facilities: List<String> = emptyList(), val tournamentsCount: Int = 0,
        val description: String = "", val phone: String? = null,
        val email: String? = null, val website: String? = null,
        val createdAt: String = "2025-06-01"
    )

    val locations = listOf(
        AdminLocation("1", "Алматы Картодром", "karting", "ул. Картинговая 15", "Алматы", 30, 4.8f, listOf("Парковка", "Кафе", "Раздевалка", "Прокат экипировки"), 12, "Профессиональный картодром с трассой 800м", "+7 727 123 4567", "info@almaty-karting.kz", "almaty-karting.kz"),
        AdminLocation("2", "Тир «Мишень»", "shooting", "пр. Стрелковый 42", "Астана", 20, 4.5f, listOf("Парковка", "Оружейная комната", "Магазин"), 8, "Современный стрелковый комплекс", "+7 717 234 5678", "tir@mishen.kz"),
        AdminLocation("3", "Теннис-центр Mega", "arena", "ул. Розыбакиева 263", "Алматы", 200, 4.3f, listOf("Парковка", "Кафе", "Душевые", "Тренажёрный зал"), 6, "4 крытых корта с профессиональным покрытием"),
        AdminLocation("4", "Спорт-Арена", "arena", "пр. Кабанбай Батыра 1", "Астана", 5000, 4.7f, listOf("Парковка", "VIP-ложи", "Медпункт", "Пресс-центр"), 15, "Многофункциональная спортивная арена", "+7 717 345 6789", "arena@sport-astana.kz", "sport-astana.kz"),
        AdminLocation("5", "Стадион Центральный", "stadium", "ул. Абая 48", "Алматы", 15000, 4.6f, listOf("Парковка", "Раздевалка", "Медпункт", "Пресс-центр", "Кафе"), 17, "Главный стадион города"),
        AdminLocation("6", "Бассейн Олимпик", "other", "пр. Нурсултана 12", "Караганда", 100, 4.2f, listOf("Парковка", "Душевые", "Раздевалка"), 4, "50-метровый бассейн олимпийского стандарта")
    )

    // ─── Requests ────────────────────────────────────────────
    data class AdminInvite(
        val id: String, val userName: String, val userRole: UserRole,
        val tournamentName: String, val message: String?,
        val status: String, val createdAt: String, val isIncoming: Boolean = true
    )

    data class AdminTeamRequest(
        val id: String, val userName: String, val teamName: String,
        val message: String?, val status: String, val createdAt: String
    )

    val invites = listOf(
        AdminInvite("1", "Дмитрий Волков", UserRole.REFEREE, "Кубок Алматы по картингу 2026", "Приглашаем вас судить турнир", "pending", "2026-02-20"),
        AdminInvite("2", "Елена Новикова", UserRole.SPONSOR, "Чемпионат РК по стрельбе", "Предложение спонсорства", "accepted", "2026-02-15"),
        AdminInvite("3", "Анна Медведева", UserRole.MEDIA, "Детский турнир по картингу", "Аккредитация СМИ", "pending", "2026-02-22"),
        AdminInvite("4", "Дмитрий Волков", UserRole.REFEREE, "Бокс Кубок Столицы", "Приглашение на судейство", "declined", "2026-02-10")
    )

    val teamRequests = listOf(
        AdminTeamRequest("1", "Сергей Картингист", "Team Speed", "Хочу присоединиться к команде", "pending", "2026-02-18"),
        AdminTeamRequest("2", "Никита Боксёр", "Boxing Club", "Подаю заявку", "accepted", "2026-02-12"),
        AdminTeamRequest("3", "Ольга Стрелок", "Снайперы", "Прошу рассмотреть заявку", "pending", "2026-02-20")
    )

    // ─── Settings ────────────────────────────────────────────
    data class PlatformSettings(
        val platformName: String = "iLeader",
        val platformDescription: String = "Спортивная платформа для организации и управления турнирами",
        val supportEmail: String = "support@ileader.kz",
        val supportPhone: String = "+7 727 123 45 67",
        val selfRegistration: Boolean = true,
        val emailConfirmation: Boolean = true,
        val availableRoles: Map<String, Boolean> = mapOf(
            "athlete" to true, "trainer" to true, "organizer" to true,
            "referee" to true, "sponsor" to true, "media" to true
        ),
        val defaultMaxParticipants: Int = 32,
        val minRatingForParticipation: Int = 0,
        val autoPublishResults: Boolean = false,
        val emailNotifications: Boolean = true,
        val pushNotifications: Boolean = true,
        val notifyNewUsers: Boolean = true,
        val notifyNewTournaments: Boolean = true
    )

    val defaultSettings = PlatformSettings()

    // ─── Helpers (delegated to AdminUtils) ────────────────────

    fun roleColor(role: UserRole) = AdminUtils.roleColor(role)
    fun statusLabel(status: String) = AdminUtils.statusLabel(status)
    fun statusColor(status: String) = AdminUtils.statusColor(status)
    fun locationTypeLabel(type: String) = AdminUtils.locationTypeLabel(type)
    fun locationTypeColor(type: String) = AdminUtils.locationTypeColor(type)
    fun ageCategoryLabel(category: String?) = AdminUtils.ageCategoryLabel(category)
}
