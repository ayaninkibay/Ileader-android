package com.ileader.app.data.mock

/**
 * Моковые данные для роли Referee (Судья)
 */
object RefereeMockData {

    // ── Enums ──────────────────────────────────────────────────────

    enum class TournamentStatus(val label: String) {
        REGISTRATION_OPEN("Регистрация"),
        IN_PROGRESS("В процессе"),
        PENDING_RESULTS("Ожидает результаты"),
        COMPLETED("Завершён")
    }

    enum class ViolationSeverity(val label: String) {
        WARNING("Предупреждение"),
        PENALTY("Штраф"),
        DISQUALIFICATION("Дисквалификация")
    }

    enum class ViolationCategory(val label: String) {
        FALSE_START("Фальстарт"),
        DANGEROUS_DRIVING("Опасное вождение"),
        TRACK_LIMITS("Выход за пределы трассы"),
        UNSPORTSMANLIKE("Неспортивное поведение"),
        EQUIPMENT("Нарушение экипировки"),
        SAFETY("Нарушение безопасности"),
        RULES("Нарушение правил"),
        CONTACT("Запрещённый контакт"),
        DELAY("Задержка / неявка"),
        OTHER("Прочее")
    }

    enum class MatchStatus(val label: String) {
        COMPLETED("Завершён"),
        IN_PROGRESS("Идёт сейчас"),
        SCHEDULED("Ожидает")
    }

    enum class InviteStatus(val label: String) {
        PENDING("Ожидает"),
        ACCEPTED("Принято"),
        DECLINED("Отклонено")
    }

    enum class RefereeRole(val label: String) {
        HEAD_REFEREE("Главный судья"),
        ASSISTANT("Помощник"),
        REFEREE("Судья")
    }

    // ── Data classes ───────────────────────────────────────────────

    data class RefereeTournament(
        val id: String,
        val name: String,
        val sport: String,
        val sportId: String = "",
        val location: String,
        val date: String,
        val time: String = "",
        val status: TournamentStatus,
        val participants: Int,
        val matchesTotal: Int = 0,
        val matchesCompleted: Int = 0,
        val description: String = "",
        val organizer: String = "",
        val prizeFund: String = "",
        val categories: List<String> = emptyList(),
        val documents: List<TournamentDocument> = emptyList(),
        val rating: Float? = null,
        val feedback: String? = null,
        val refereeRole: RefereeRole = RefereeRole.HEAD_REFEREE
    )

    data class TournamentDocument(
        val id: String,
        val name: String,
        val fileName: String,
        val fileSize: Long,
        val fileType: String
    )

    data class RefereeMatch(
        val id: String,
        val number: Int,
        val time: String,
        val category: String,
        val status: MatchStatus,
        val participants: Int
    )

    data class Participant(
        val id: String,
        val name: String,
        val number: String,
        val team: String,
        val category: String
    )

    data class Violation(
        val id: String,
        val participantId: String,
        val participantName: String,
        val tournamentId: String,
        val tournamentName: String,
        val sport: String,
        val date: String,
        val severity: ViolationSeverity,
        val category: ViolationCategory,
        val description: String,
        val matchNumber: Int? = null,
        val time: String? = null,
        val penaltyApplied: String? = null
    )

    data class RefereeInvite(
        val id: String,
        val tournamentId: String,
        val tournamentName: String,
        val sportName: String = "",
        val role: RefereeRole,
        val status: InviteStatus,
        val locationCity: String = "",
        val startDate: String = "",
        val createdAt: String,
        val contactPhone: String? = null,
        val responseMessage: String? = null
    )

    data class MonthlyStats(
        val month: String,
        val tournaments: Int,
        val avgRating: Float,
        val violations: Int
    )

    data class RefereeStats(
        val totalTournaments: Int,
        val thisMonth: Int,
        val pendingResults: Int,
        val totalParticipants: Int,
        val totalViolations: Int
    )

    // ── Participants ───────────────────────────────────────────────

    val participants = listOf(
        Participant("p-1", "Алексей Петров", "23", "Racing Team Pro", "Профи"),
        Participant("p-2", "Дмитрий Сидоров", "17", "Speed Masters", "Профи"),
        Participant("p-3", "Иван Кузнецов", "05", "Turbo Pilots", "Взрослые"),
        Participant("p-4", "Сергей Смирнов", "31", "Fast Track", "Профи"),
        Participant("p-5", "Андрей Попов", "42", "Racing Team Pro", "Взрослые"),
        Participant("p-6", "Михаил Васильев", "19", "Speed Masters", "Юниоры"),
        Participant("p-7", "Павел Новиков", "08", "Turbo Pilots", "Профи"),
        Participant("p-8", "Николай Морозов", "14", "Fast Track", "Взрослые")
    )

    // ── Current tournament (detail page) ──────────────────────────

    val currentTournament = RefereeTournament(
        id = "t-7",
        name = "Зимний Кубок по картингу",
        sport = "Картинг",
        sportId = "karting",
        location = "Автодром \"Сокол\", Алматы",
        date = "2026-02-10",
        time = "10:00",
        status = TournamentStatus.IN_PROGRESS,
        participants = 38,
        matchesTotal = 12,
        matchesCompleted = 3,
        description = "Зимний кубок — серия гонок на протяжении недели. 5 этапов, общий зачёт по очкам.",
        organizer = "Турниры Про",
        prizeFund = "350 000 ₸",
        categories = listOf("KZ2", "Junior", "Senior"),
        documents = listOf(
            TournamentDocument("doc-4", "Регламент Зимнего Кубка", "reglament_winter_cup_2026.pdf", 1843200, "application/pdf"),
            TournamentDocument("doc-5", "Правила безопасности", "winter_safety.pdf", 921600, "application/pdf")
        )
    )

    // ── Current matches ───────────────────────────────────────────

    val currentMatches = listOf(
        RefereeMatch("m-1", 1, "10:00", "Профи", MatchStatus.COMPLETED, 8),
        RefereeMatch("m-2", 2, "11:00", "Взрослые", MatchStatus.COMPLETED, 8),
        RefereeMatch("m-3", 3, "12:00", "Юниоры", MatchStatus.COMPLETED, 8),
        RefereeMatch("m-4", 4, "13:00", "Профи", MatchStatus.IN_PROGRESS, 8),
        RefereeMatch("m-5", 5, "14:00", "Взрослые", MatchStatus.SCHEDULED, 8),
        RefereeMatch("m-6", 6, "15:00", "Юниоры", MatchStatus.SCHEDULED, 8)
    )

    // ── Tournament history ────────────────────────────────────────

    val tournamentHistory = listOf(
        RefereeTournament("ref-h-1", "Зимняя серия - Этап 3", "Картинг", "karting", "Крытый картодром \"Форсаж\"", "2026-01-18", "10:00", TournamentStatus.COMPLETED, 18, 6, 6, rating = 4.9f, feedback = "Четкое судейство"),
        RefereeTournament("ref-h-2", "Кубок Казахстана - Четвертьфинал", "Стрельба", "shooting", "Стрелковый центр \"Динамо\"", "2026-01-10", "09:00", TournamentStatus.COMPLETED, 22, 8, 8, rating = 4.8f, feedback = "Профессионально"),
        RefereeTournament("ref-h-3", "Финал сезона по стрельбе 2025", "Стрельба", "shooting", "Стрелковый центр \"Динамо\"", "2025-12-20", "10:00", TournamentStatus.COMPLETED, 30, 10, 10, rating = 4.9f, feedback = "Высший класс"),
        RefereeTournament("ref-h-4", "Зимняя серия - Этап 2", "Картинг", "karting", "Автодром \"Сокол\", Алматы", "2025-12-10", "10:00", TournamentStatus.COMPLETED, 24, 8, 8, rating = 4.8f, feedback = "Отличная работа"),
        RefereeTournament("t-13", "Новогодний турнир по картингу", "Картинг", "karting", "Автодром \"Северный\", Караганда", "2025-12-28", "10:00", TournamentStatus.COMPLETED, 30, 10, 10, rating = 5.0f, feedback = "Идеальная организация"),
        RefereeTournament("t-11", "Кубок Независимости по стрельбе", "Стрельба", "shooting", "Тир \"Снайпер\", Алматы", "2025-12-14", "09:00", TournamentStatus.COMPLETED, 40, 10, 10, rating = 4.8f, feedback = "Профессиональный подход"),
        RefereeTournament("t-10", "Осенний Гран-При по картингу", "Картинг", "karting", "Автодром \"Сокол\", Алматы", "2025-10-15", "10:00", TournamentStatus.COMPLETED, 56, 12, 12, rating = 5.0f, feedback = "Отличная работа, четкое судейство"),
        RefereeTournament("t-14", "Региональный чемпионат по стрельбе", "Стрельба", "shooting", "Тир \"Снайпер\", Алматы", "2025-09-05", "09:00", TournamentStatus.COMPLETED, 45, 12, 12, rating = 4.9f, feedback = "Справедливое судейство"),
        RefereeTournament("t-15", "Гран-При Алматы по картингу 2025", "Картинг", "karting", "Автодром \"Сокол\", Алматы", "2025-08-20", "10:00", TournamentStatus.COMPLETED, 60, 15, 15, rating = 4.7f, feedback = "Хорошая работа"),
        RefereeTournament("t-12", "Летний кубок по футболу", "Футбол", "football", "Спорткомплекс \"Каспий\", Актау", "2025-07-01", "10:00", TournamentStatus.COMPLETED, 128, 20, 20, rating = 4.9f, feedback = "Четкое судейство")
    )

    // ── Assigned tournaments (all in one list for tournaments page) ─

    val assignedTournaments = listOf(
        // Upcoming
        RefereeTournament("t-3", "Чемпионат Алматы по картингу", "Картинг", "karting", "Автодром \"Сокол\", Алматы", "2026-03-15", "10:00", TournamentStatus.REGISTRATION_OPEN, 45, 12, 0, refereeRole = RefereeRole.HEAD_REFEREE),
        RefereeTournament("t-4", "Открытый турнир по стрельбе", "Стрельба", "shooting", "Тир \"Снайпер\", Алматы", "2026-03-20", "09:00", TournamentStatus.REGISTRATION_OPEN, 32, 16, 0, refereeRole = RefereeRole.REFEREE),
        RefereeTournament("t-5", "Кубок столицы по картингу", "Картинг", "karting", "Картинг-центр \"Адреналин\", Астана", "2026-04-05", "12:00", TournamentStatus.REGISTRATION_OPEN, 28, 9, 0, refereeRole = RefereeRole.ASSISTANT),
        RefereeTournament("t-6", "Первенство Шымкента по теннису", "Теннис", "tennis", "Дворец спорта \"Жекпе-жек\", Шымкент", "2026-04-20", "10:00", TournamentStatus.REGISTRATION_OPEN, 16, 8, 0, refereeRole = RefereeRole.HEAD_REFEREE),
        // Active
        currentTournament.copy(refereeRole = RefereeRole.HEAD_REFEREE),
        // Completed (last 2 from history)
        tournamentHistory[0].copy(matchesTotal = 6, matchesCompleted = 6, refereeRole = RefereeRole.HEAD_REFEREE),
        tournamentHistory[1].copy(matchesTotal = 8, matchesCompleted = 8, refereeRole = RefereeRole.REFEREE)
    )

    // ── Violations ────────────────────────────────────────────────

    val violations = listOf(
        // t-7: Зимний Кубок по картингу
        Violation("v-1", "p-1", "Алексей Петров", "t-7", "Зимний Кубок по картингу", "Картинг", "2026-02-12", ViolationSeverity.WARNING, ViolationCategory.TRACK_LIMITS, "Выезд за пределы трассы в повороте 3 во время хита 2", 2, "11:34"),
        Violation("v-2", "p-2", "Дмитрий Сидоров", "t-7", "Зимний Кубок по картингу", "Картинг", "2026-02-12", ViolationSeverity.PENALTY, ViolationCategory.DANGEROUS_DRIVING, "Агрессивный обгон с контактом на входе в S-поворот", 3, "12:15", "+5 секунд"),
        Violation("v-3", "p-4", "Сергей Смирнов", "t-7", "Зимний Кубок по картингу", "Картинг", "2026-02-11", ViolationSeverity.WARNING, ViolationCategory.FALSE_START, "Движение до сигнала старта в хите 1", 1, "10:01"),
        Violation("v-4", "p-1", "Алексей Петров", "t-7", "Зимний Кубок по картингу", "Картинг", "2026-02-10", ViolationSeverity.PENALTY, ViolationCategory.DANGEROUS_DRIVING, "Намеренное вытеснение соперника с трассы на прямой", 2, "14:23", "+10 секунд"),
        Violation("v-5", "p-7", "Павел Новиков", "t-7", "Зимний Кубок по картингу", "Картинг", "2026-02-10", ViolationSeverity.WARNING, ViolationCategory.EQUIPMENT, "Несоответствие защитного оборудования стандартам (шлем)", time = "09:30"),
        // t-10: Осенний Гран-При
        Violation("v-6", "p-2", "Дмитрий Сидоров", "t-10", "Осенний Гран-При по картингу", "Картинг", "2025-10-16", ViolationSeverity.WARNING, ViolationCategory.UNSPORTSMANLIKE, "Неспортивное поведение после финиша: жест в сторону соперника", 4, "15:45"),
        Violation("v-7", "p-3", "Иван Кузнецов", "t-10", "Осенний Гран-При по картингу", "Картинг", "2025-10-16", ViolationSeverity.DISQUALIFICATION, ViolationCategory.CONTACT, "Грубый контакт с соперником, приведший к аварии", 3, "14:12", "DSQ (дисквалификация из заезда)"),
        Violation("v-8", "p-5", "Андрей Попов", "t-10", "Осенний Гран-При по картингу", "Картинг", "2025-10-15", ViolationSeverity.WARNING, ViolationCategory.TRACK_LIMITS, "Систематический срез поворотов (3 раза за хит)", 2, "13:20"),
        // t-11: Кубок Независимости
        Violation("v-11", "p-1", "Алексей Петров", "t-11", "Кубок Независимости по стрельбе", "Стрельба", "2025-12-15", ViolationSeverity.WARNING, ViolationCategory.SAFETY, "Нарушение направления оружия при смене позиции", time = "11:15"),
        // t-13: Новогодний турнир
        Violation("v-15", "p-7", "Павел Новиков", "t-13", "Новогодний турнир по картингу", "Картинг", "2025-12-28", ViolationSeverity.WARNING, ViolationCategory.TRACK_LIMITS, "Выход за пределы трассы с получением преимущества", 2, "11:22"),
        // ref-h-1: Зимняя серия Этап 3
        Violation("v-9", "p-4", "Сергей Смирнов", "ref-h-1", "Зимняя серия - Этап 3", "Картинг", "2026-01-18", ViolationSeverity.PENALTY, ViolationCategory.FALSE_START, "Повторный фальстарт (второй за турнир)", 2, "11:00", "Старт с конца пелотона"),
        Violation("v-10", "p-6", "Михаил Васильев", "ref-h-1", "Зимняя серия - Этап 3", "Картинг", "2026-01-18", ViolationSeverity.WARNING, ViolationCategory.DELAY, "Опоздание на предстартовую процедуру на 5 минут", 1, "10:05"),
        // ref-h-2: Кубок Казахстана
        Violation("v-14", "p-2", "Дмитрий Сидоров", "ref-h-2", "Кубок Казахстана - Четвертьфинал", "Стрельба", "2026-01-10", ViolationSeverity.WARNING, ViolationCategory.SAFETY, "Несанкционированное извлечение оружия вне зоны подготовки", time = "09:50"),
        // ref-h-3: Финал сезона
        Violation("v-16", "p-3", "Иван Кузнецов", "ref-h-3", "Финал сезона по стрельбе 2025", "Стрельба", "2025-12-20", ViolationSeverity.PENALTY, ViolationCategory.RULES, "Превышение лимита времени на серию выстрелов", time = "15:10", penaltyApplied = "-2 очка"),
        // ref-h-4: Зимняя серия Этап 2
        Violation("v-17", "p-4", "Сергей Смирнов", "ref-h-4", "Зимняя серия - Этап 2", "Картинг", "2025-12-10", ViolationSeverity.WARNING, ViolationCategory.DANGEROUS_DRIVING, "Блокировка при обгоне: резкое изменение траектории", 3, "14:55"),
        Violation("v-18", "p-8", "Николай Морозов", "ref-h-4", "Зимняя серия - Этап 2", "Картинг", "2025-12-10", ViolationSeverity.WARNING, ViolationCategory.CONTACT, "Легкий контакт при попытке обгона в шикане", 4, "15:30")
    )

    // ── Incoming invites ──────────────────────────────────────────

    val incomingInvites = listOf(
        RefereeInvite("inv-1", "t-20", "Весенний Кубок по картингу", "Картинг", RefereeRole.HEAD_REFEREE, InviteStatus.PENDING, "Алматы", "2026-04-10", "2026-02-25"),
        RefereeInvite("inv-2", "t-21", "Открытый чемпионат по стрельбе", "Стрельба", RefereeRole.REFEREE, InviteStatus.PENDING, "Астана", "2026-04-15", "2026-02-24"),
        RefereeInvite("inv-3", "t-3", "Чемпионат Алматы по картингу", "Картинг", RefereeRole.HEAD_REFEREE, InviteStatus.ACCEPTED, "Алматы", "2026-03-15", "2026-02-10", "+7 701 123 4567", "С удовольствием приму участие"),
        RefereeInvite("inv-4", "t-22", "Региональные соревнования", "Теннис", RefereeRole.ASSISTANT, InviteStatus.DECLINED, "Шымкент", "2026-03-25", "2026-02-05", responseMessage = "К сожалению, занят на другом турнире")
    )

    // ── Outgoing applications ─────────────────────────────────────

    val outgoingApplications = listOf(
        RefereeInvite("app-1", "t-25", "Международный турнир по картингу", "Картинг", RefereeRole.HEAD_REFEREE, InviteStatus.PENDING, "Алматы", "2026-05-01", "2026-02-20"),
        RefereeInvite("app-2", "t-4", "Открытый турнир по стрельбе", "Стрельба", RefereeRole.REFEREE, InviteStatus.ACCEPTED, "Алматы", "2026-03-20", "2026-02-15", responseMessage = "Ваша заявка одобрена")
    )

    // ── Monthly stats ─────────────────────────────────────────────

    val monthlyStats = listOf(
        MonthlyStats("Февраль 2026", 2, 4.9f, 7),
        MonthlyStats("Январь 2026", 2, 4.85f, 3),
        MonthlyStats("Декабрь 2025", 4, 4.9f, 5),
        MonthlyStats("Октябрь 2025", 1, 5.0f, 3)
    )

    // ── Dashboard stats ───────────────────────────────────────────

    val stats = RefereeStats(
        totalTournaments = assignedTournaments.size + tournamentHistory.size,
        thisMonth = 2,
        pendingResults = 1,
        totalParticipants = tournamentHistory.sumOf { it.participants } + assignedTournaments.sumOf { it.participants },
        totalViolations = violations.size
    )

    // ── Helper functions ──────────────────────────────────────────

    fun getViolationsByTournamentId(tournamentId: String): List<Violation> =
        violations.filter { it.tournamentId == tournamentId }

    fun getViolationsByParticipantId(participantId: String): List<Violation> =
        violations.filter { it.participantId == participantId }

    fun getTournamentById(id: String): RefereeTournament? {
        if (currentTournament.id == id) return currentTournament
        return assignedTournaments.find { it.id == id }
            ?: tournamentHistory.find { it.id == id }
    }

    fun getViolationStats(violationList: List<Violation>): Triple<Int, Int, Int> {
        val warnings = violationList.count { it.severity == ViolationSeverity.WARNING }
        val penalties = violationList.count { it.severity == ViolationSeverity.PENALTY }
        val disqualifications = violationList.count { it.severity == ViolationSeverity.DISQUALIFICATION }
        return Triple(warnings, penalties, disqualifications)
    }
}
