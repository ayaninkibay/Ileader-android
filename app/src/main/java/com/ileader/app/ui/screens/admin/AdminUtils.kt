package com.ileader.app.ui.screens.admin

import androidx.compose.ui.graphics.Color
import com.ileader.app.data.models.UserRole
import com.ileader.app.ui.theme.ILeaderColors

object AdminUtils {

    fun roleColor(role: UserRole): Color = when (role) {
        UserRole.ATHLETE -> ILeaderColors.AthleteColor
        UserRole.TRAINER -> ILeaderColors.TrainerColor
        UserRole.ORGANIZER -> ILeaderColors.OrganizerColor
        UserRole.REFEREE -> ILeaderColors.RefereeColor
        UserRole.SPONSOR -> ILeaderColors.SponsorColor
        UserRole.MEDIA -> ILeaderColors.MediaColor
        UserRole.ADMIN -> ILeaderColors.AdminColor
        UserRole.USER -> ILeaderColors.ViewerColor
    }

    fun statusLabel(status: String): String = when (status) {
        "draft" -> "Черновик"; "registration_open" -> "Регистрация"
        "registration_closed" -> "Рег. закрыта"; "check_in" -> "Чек-ин"
        "in_progress" -> "Идёт"; "completed" -> "Завершён"
        "cancelled" -> "Отменён"; else -> status
    }

    fun statusColor(status: String): Color = when (status) {
        "draft" -> ILeaderColors.Warning; "registration_open" -> ILeaderColors.Info
        "registration_closed" -> ILeaderColors.Warning; "check_in" -> ILeaderColors.SponsorColor
        "in_progress" -> ILeaderColors.Success; "completed" -> ILeaderColors.ViewerColor
        "cancelled" -> ILeaderColors.Error; else -> ILeaderColors.ViewerColor
    }

    fun locationTypeLabel(type: String): String = when (type) {
        "karting" -> "Картодром"; "shooting" -> "Тир"; "stadium" -> "Стадион"
        "arena" -> "Арена"; "other" -> "Другое"; else -> type
    }

    fun locationTypeColor(type: String): Color = when (type) {
        "karting" -> ILeaderColors.AthleteColor; "shooting" -> ILeaderColors.TrainerColor
        "stadium" -> ILeaderColors.OrganizerColor; "arena" -> ILeaderColors.SponsorColor
        else -> ILeaderColors.ViewerColor
    }

    fun ageCategoryLabel(category: String?): String = when (category) {
        "children" -> "Детская"; "youth" -> "Юношеская"; "adult" -> "Взрослая"; else -> "—"
    }
}
