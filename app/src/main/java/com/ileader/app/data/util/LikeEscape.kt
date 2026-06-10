package com.ileader.app.data.util

/**
 * Эскейпит спецсимволы PostgreSQL LIKE-паттерна (`%`, `_`, `\`) в user-input
 * перед оборачиванием в `%...%` для `ilike()`. Без этого:
 *  - юзер вводит `%` → запрос `%%%` матчит вообще всё
 *  - юзер вводит `_` → запрос `%_%` матчит всё с ≥1 символом
 *  - юзер вводит `Smith\Jones` → backslash может сломать паттерн
 *
 * Не защищает от SQL-injection (этим занят сам PostgREST через HTTP-параметры) —
 * только корректит LIKE-поведение.
 */
fun String.escapeLikePattern(): String =
    this.replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")
