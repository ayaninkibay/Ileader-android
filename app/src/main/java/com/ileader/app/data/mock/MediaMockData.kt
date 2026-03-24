package com.ileader.app.data.mock

/**
 * Моковые данные для роли Media (СМИ)
 * Референс: ileader/src/data/mock/media.ts
 */
object MediaMockData {

    // ── Enums ──────────────────────────────────────────────────────

    enum class ContentStatus(val label: String) {
        PUBLISHED("Опубликовано"),
        DRAFT("Черновик")
    }

    enum class ArticleCategory(val label: String) {
        NEWS("Новость"),
        REPORT("Репортаж"),
        INTERVIEW("Интервью"),
        HIGHLIGHT("Обзор"),
        ANALYTICS("Аналитика")
    }

    enum class TournamentStatus(val label: String) {
        REGISTRATION_OPEN("Регистрация"),
        IN_PROGRESS("Идёт"),
        COMPLETED("Завершён")
    }

    enum class InviteStatus(val label: String) {
        PENDING("Ожидает"),
        ACCEPTED("Принято"),
        DECLINED("Отклонено")
    }

    // ── Data classes ───────────────────────────────────────────────

    data class MediaProfile(
        val mediaName: String,
        val mediaType: String,
        val description: String,
        val website: String,
        val email: String,
        val phone: String,
        val address: String,
        val founded: String,
        val logoUrl: String? = null
    )

    data class Article(
        val id: String,
        val authorId: String,
        val title: String,
        val category: ArticleCategory,
        val status: ContentStatus,
        val views: Int,
        val authorName: String,
        val publishedAt: String?,
        val excerpt: String,
        val content: String = "",
        val tags: List<String>,
        val createdAt: String
    )

    data class MediaTournament(
        val id: String,
        val name: String,
        val sport: String,
        val location: String,
        val city: String,
        val date: String,
        val status: TournamentStatus,
        val participants: Int,
        val mediaCount: Int,
        val isRegistered: Boolean = false
    )

    data class ViewsDataPoint(
        val label: String,
        val views: Int
    )

    data class TopArticle(
        val title: String,
        val views: Int,
        val change: String,
        val trendUp: Boolean
    )

    data class CategoryStat(
        val category: ArticleCategory,
        val count: Int,
        val percentage: Int
    )

    data class AudienceStat(
        val age: String,
        val percentage: Int
    )

    data class MediaInvite(
        val id: String,
        val tournamentName: String,
        val sportName: String,
        val date: String,
        val location: String,
        val status: InviteStatus,
        val message: String?,
        val createdAt: String,
        val contactPhone: String? = null,
        val responseMessage: String? = null
    )

    // ── Профиль СМИ ───────────────────────────────────────────────

    val profile = MediaProfile(
        mediaName = "Спортивная Газета",
        mediaType = "Онлайн СМИ",
        description = "Ведущее спортивное издание, освещающее турниры по картингу, стрельбе и другим видам спорта",
        website = "https://sportgazeta.ru",
        email = "info@sportgazeta.ru",
        phone = "+7 (495) 123-45-67",
        address = "г. Алматы, ул. Спортивная, д. 10",
        founded = "2015"
    )

    val coverageAreas = listOf("Картинг", "Стрельба", "Теннис", "Футбол", "Бокс")

    val achievements = listOf(
        "Лучшее спортивное СМИ 2025 — Ассоциация спортивных журналистов",
        "Премия за лучший репортаж — Казахстанская федерация автоспорта, 2024",
        "Золотое перо спортивной журналистики — Союз журналистов Казахстана, 2024"
    )

    // ── Статьи / Контент ──────────────────────────────────────────

    val articles = listOf(
        Article(
            id = "1",
            authorId = "media-1",
            title = "Захватывающий финал Кубка Казахстана по картингу",
            category = ArticleCategory.REPORT,
            status = ContentStatus.PUBLISHED,
            views = 2300,
            authorName = "Спортивная Газета",
            publishedAt = "12 фев 2026",
            excerpt = "Невероятная борьба до последних метров определила победителя главного национального турнира по картингу.",
            content = "Финал Кубка Казахстана по картингу 2026 года стал одним из самых захватывающих событий сезона. На трассе \"Алматы Картодром\" собрались лучшие пилоты страны, чтобы побороться за главный трофей.\n\nС первых кругов борьба развернулась между тремя лидерами — Алексеем Петровым, Дмитрием Волковым и Маратом Саидовым. Каждый из них демонстрировал великолепную технику прохождения поворотов и смелые обгоны.\n\nВ итоге победу одержал Алексей Петров, опередив ближайшего преследователя всего на 0.3 секунды. Это его третья победа в сезоне и первый Кубок Казахстана в карьере.",
            tags = listOf("картинг", "кубок", "финал"),
            createdAt = "11 фев 2026"
        ),
        Article(
            id = "2",
            authorId = "media-1",
            title = "Интервью с победителем чемпионата по стрельбе",
            category = ArticleCategory.INTERVIEW,
            status = ContentStatus.PUBLISHED,
            views = 1800,
            authorName = "Спортивная Газета",
            publishedAt = "10 фев 2026",
            excerpt = "Эксклюзивное интервью с чемпионом, который поделился секретами своего успеха.",
            content = "Мы встретились с чемпионом Казахстана по стрельбе Маратом Каировым после его триумфальной победы на национальном первенстве.\n\n— Марат, поздравляем! Расскажите о своих впечатлениях.\n— Спасибо! Это был долгий путь. Я готовился к этому турниру больше года, тренировался по 4-5 часов в день.\n\n— Что помогло вам победить?\n— Дисциплина и концентрация. В стрельбе нет места эмоциям. Нужно полностью контролировать своё тело и разум.",
            tags = listOf("стрельба", "интервью", "чемпионат"),
            createdAt = "09 фев 2026"
        ),
        Article(
            id = "3",
            authorId = "media-1",
            title = "Анализ результатов турнира \"Зимний кубок 2026\"",
            category = ArticleCategory.NEWS,
            status = ContentStatus.PUBLISHED,
            views = 1500,
            authorName = "Спортивная Газета",
            publishedAt = "08 фев 2026",
            excerpt = "Детальный разбор результатов и статистики одного из самых престижных турниров сезона.",
            tags = listOf("аналитика", "зимний кубок", "статистика"),
            createdAt = "07 фев 2026"
        ),
        Article(
            id = "4",
            authorId = "media-1",
            title = "Предстоящий чемпионат: превью и прогнозы",
            category = ArticleCategory.NEWS,
            status = ContentStatus.DRAFT,
            views = 0,
            authorName = "Спортивная Газета",
            publishedAt = null,
            excerpt = "Что ждёт зрителей на предстоящем чемпионате? Анализ команд и прогнозы экспертов.",
            tags = listOf("превью", "прогнозы"),
            createdAt = "15 фев 2026"
        ),
        Article(
            id = "5",
            authorId = "media-1",
            title = "История картинга в Казахстане: от истоков до наших дней",
            category = ArticleCategory.NEWS,
            status = ContentStatus.PUBLISHED,
            views = 980,
            authorName = "Спортивная Газета",
            publishedAt = "05 фев 2026",
            excerpt = "Увлекательный рассказ о развитии картинга в нашей стране за последние 30 лет.",
            tags = listOf("история", "картинг", "Казахстан"),
            createdAt = "04 фев 2026"
        ),
        Article(
            id = "6",
            authorId = "media-1",
            title = "Технические новинки в мире картинга 2026",
            category = ArticleCategory.REPORT,
            status = ContentStatus.PUBLISHED,
            views = 1200,
            authorName = "Спортивная Газета",
            publishedAt = "03 фев 2026",
            excerpt = "Обзор последних технологических достижений и инноваций в картинге.",
            tags = listOf("технологии", "картинг", "обзор"),
            createdAt = "02 фев 2026"
        ),
        Article(
            id = "7",
            authorId = "media-1",
            title = "Фоторепортаж: лучшие моменты сезона",
            category = ArticleCategory.HIGHLIGHT,
            status = ContentStatus.PUBLISHED,
            views = 3200,
            authorName = "Спортивная Газета",
            publishedAt = "01 фев 2026",
            excerpt = "Яркие снимки самых запоминающихся моментов прошедшего сезона.",
            tags = listOf("фото", "моменты", "сезон"),
            createdAt = "31 янв 2026"
        ),
        Article(
            id = "8",
            authorId = "media-1",
            title = "Новые правила ФИАК на 2026 год",
            category = ArticleCategory.NEWS,
            status = ContentStatus.DRAFT,
            views = 0,
            authorName = "Спортивная Газета",
            publishedAt = null,
            excerpt = "Разбор изменений в правилах международной федерации автоспорта.",
            tags = listOf("правила", "ФИАК", "регламент"),
            createdAt = "16 фев 2026"
        ),
        Article(
            id = "9",
            authorId = "media-1",
            title = "Топ-10 молодых талантов в картинге",
            category = ArticleCategory.HIGHLIGHT,
            status = ContentStatus.PUBLISHED,
            views = 2100,
            authorName = "Спортивная Газета",
            publishedAt = "28 янв 2026",
            excerpt = "Кто из молодых пилотов может стать будущей звездой мирового картинга?",
            tags = listOf("таланты", "молодежь", "картинг"),
            createdAt = "27 янв 2026"
        ),
        Article(
            id = "10",
            authorId = "media-1",
            title = "Советы начинающим: как стать картингистом",
            category = ArticleCategory.NEWS,
            status = ContentStatus.PUBLISHED,
            views = 1650,
            authorName = "Спортивная Газета",
            publishedAt = "25 янв 2026",
            excerpt = "Практическое руководство для тех, кто делает первые шаги в картинге.",
            tags = listOf("гайд", "новички", "картинг"),
            createdAt = "24 янв 2026"
        )
    )

    // ── Турниры ───────────────────────────────────────────────────

    val tournaments = listOf(
        MediaTournament(
            id = "t1",
            name = "Кубок Казахстана по картингу 2026",
            sport = "Картинг",
            location = "Алматы Картодром",
            city = "Алматы",
            date = "15 марта 2026",
            status = TournamentStatus.REGISTRATION_OPEN,
            participants = 48,
            mediaCount = 5,
            isRegistered = true
        ),
        MediaTournament(
            id = "t2",
            name = "Чемпионат РК по стрельбе",
            sport = "Стрельба",
            location = "Стрелковый клуб \"Мерген\"",
            city = "Астана",
            date = "22 марта 2026",
            status = TournamentStatus.REGISTRATION_OPEN,
            participants = 36,
            mediaCount = 3,
            isRegistered = false
        ),
        MediaTournament(
            id = "t3",
            name = "Гран-при Алматы по картингу",
            sport = "Картинг",
            location = "Алматы Картодром",
            city = "Алматы",
            date = "05 марта 2026",
            status = TournamentStatus.IN_PROGRESS,
            participants = 32,
            mediaCount = 7,
            isRegistered = true
        ),
        MediaTournament(
            id = "t4",
            name = "Открытый турнир по теннису",
            sport = "Теннис",
            location = "Теннисный центр",
            city = "Шымкент",
            date = "10 апреля 2026",
            status = TournamentStatus.REGISTRATION_OPEN,
            participants = 24,
            mediaCount = 2,
            isRegistered = false
        ),
        MediaTournament(
            id = "t5",
            name = "Зимний кубок 2026",
            sport = "Картинг",
            location = "Астана Картодром",
            city = "Астана",
            date = "20 фев 2026",
            status = TournamentStatus.COMPLETED,
            participants = 40,
            mediaCount = 8,
            isRegistered = true
        ),
        MediaTournament(
            id = "t6",
            name = "Кубок Астаны по боксу",
            sport = "Бокс",
            location = "Дворец спорта",
            city = "Астана",
            date = "18 фев 2026",
            status = TournamentStatus.COMPLETED,
            participants = 28,
            mediaCount = 4,
            isRegistered = false
        )
    )

    // ── Аналитика ─────────────────────────────────────────────────

    val viewsDataWeek = listOf(
        ViewsDataPoint("Пн", 1200),
        ViewsDataPoint("Вт", 1800),
        ViewsDataPoint("Ср", 1500),
        ViewsDataPoint("Чт", 2200),
        ViewsDataPoint("Пт", 2800),
        ViewsDataPoint("Сб", 3200),
        ViewsDataPoint("Вс", 2600)
    )

    val viewsDataMonth = listOf(
        ViewsDataPoint("Нед 1", 8500),
        ViewsDataPoint("Нед 2", 11200),
        ViewsDataPoint("Нед 3", 9800),
        ViewsDataPoint("Нед 4", 13500)
    )

    val topArticles = listOf(
        TopArticle("Захватывающий финал Кубка Казахстана", 4500, "+15%", true),
        TopArticle("Фоторепортаж: лучшие моменты сезона", 3800, "+22%", true),
        TopArticle("Интервью с победителем чемпионата", 3200, "+8%", true),
        TopArticle("Топ-10 молодых талантов в картинге", 2900, "-3%", false),
        TopArticle("Анализ результатов турнира", 2600, "+12%", true)
    )

    val categoryStats = listOf(
        CategoryStat(ArticleCategory.REPORT, 45, 30),
        CategoryStat(ArticleCategory.INTERVIEW, 32, 21),
        CategoryStat(ArticleCategory.NEWS, 28, 19),
        CategoryStat(ArticleCategory.HIGHLIGHT, 25, 17),
        CategoryStat(ArticleCategory.ANALYTICS, 20, 13)
    )

    val audienceStats = listOf(
        AudienceStat("18-24", 25),
        AudienceStat("25-34", 35),
        AudienceStat("35-44", 20),
        AudienceStat("45-54", 15),
        AudienceStat("55+", 5)
    )

    // ── Приглашения / Уведомления ─────────────────────────────────

    val invites = listOf(
        MediaInvite(
            id = "inv1",
            tournamentName = "Кубок Казахстана по картингу 2026",
            sportName = "Картинг",
            date = "15 марта 2026",
            location = "Алматы",
            status = InviteStatus.PENDING,
            message = "Приглашаем ваше издание для освещения Кубка Казахстана",
            createdAt = "20 фев 2026"
        ),
        MediaInvite(
            id = "inv2",
            tournamentName = "Открытый турнир по теннису",
            sportName = "Теннис",
            date = "10 апреля 2026",
            location = "Шымкент",
            status = InviteStatus.PENDING,
            message = "Приглашаем на освещение турнира по теннису",
            createdAt = "18 фев 2026"
        ),
        MediaInvite(
            id = "inv3",
            tournamentName = "Гран-при Алматы по картингу",
            sportName = "Картинг",
            date = "05 марта 2026",
            location = "Алматы",
            status = InviteStatus.ACCEPTED,
            message = "Приглашение на Гран-при",
            createdAt = "10 фев 2026",
            contactPhone = "+7 (777) 123-45-67",
            responseMessage = "Спасибо, мы будем!"
        ),
        MediaInvite(
            id = "inv4",
            tournamentName = "Зимний кубок 2026",
            sportName = "Картинг",
            date = "20 фев 2026",
            location = "Астана",
            status = InviteStatus.DECLINED,
            message = "Приглашение на Зимний кубок",
            createdAt = "05 фев 2026",
            responseMessage = "К сожалению, у нас нет возможности освещать этот турнир"
        )
    )

    // ── Dashboard stats ───────────────────────────────────────────

    val totalAccreditations = 12
    val approvedAccreditations = 8
    val pendingAccreditations = 3
    val availableTournaments = tournaments.count { it.status != TournamentStatus.COMPLETED }

    val totalArticles = articles.size
    val publishedArticles = articles.count { it.status == ContentStatus.PUBLISHED }
    val draftArticles = articles.count { it.status == ContentStatus.DRAFT }
    val totalViews = articles.sumOf { it.views }

    // ── Хелперы ───────────────────────────────────────────────────

    fun getArticleById(id: String): Article? = articles.find { it.id == id }
    fun getPublishedArticles(): List<Article> = articles.filter { it.status == ContentStatus.PUBLISHED }
    fun getDraftArticles(): List<Article> = articles.filter { it.status == ContentStatus.DRAFT }
}
