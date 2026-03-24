package com.ileader.app.data.repository

import com.ileader.app.data.models.*
import com.ileader.app.data.remote.dto.*

// ── DTO → Domain Mappers (shared across repositories) ──

internal fun TournamentDto.toDomain(): Tournament {
    val tournamentStatus = when (status) {
        "draft" -> TournamentStatus.DRAFT
        "registration_open" -> TournamentStatus.REGISTRATION_OPEN
        "registration_closed" -> TournamentStatus.REGISTRATION_CLOSED
        "check_in" -> TournamentStatus.CHECK_IN
        "in_progress" -> TournamentStatus.IN_PROGRESS
        "completed" -> TournamentStatus.COMPLETED
        "cancelled" -> TournamentStatus.CANCELLED
        else -> TournamentStatus.DRAFT
    }
    val locationStr = locations?.let {
        buildString {
            append(it.name)
            if (!it.city.isNullOrEmpty()) append(", ${it.city}")
        }
    } ?: ""

    return Tournament(
        id = id,
        name = name,
        sportId = sportId ?: "",
        sportName = sports?.name ?: "",
        status = tournamentStatus,
        startDate = startDate ?: "",
        endDate = endDate,
        location = locationStr,
        description = description ?: "",
        format = format ?: "",
        maxParticipants = maxParticipants ?: 0,
        currentParticipants = 0,
        prize = prize ?: "",
        imageUrl = imageUrl,
        organizerName = profiles?.name ?: "",
        organizerId = organizerId ?: "",
        locationId = locationId ?: "",
        visibility = visibility ?: "public",
        ageCategory = ageCategory
    )
}

internal fun TournamentWithCountsDto.toDomain(): Tournament {
    val tournamentStatus = when (status) {
        "draft" -> TournamentStatus.DRAFT
        "registration_open" -> TournamentStatus.REGISTRATION_OPEN
        "registration_closed" -> TournamentStatus.REGISTRATION_CLOSED
        "check_in" -> TournamentStatus.CHECK_IN
        "in_progress" -> TournamentStatus.IN_PROGRESS
        "completed" -> TournamentStatus.COMPLETED
        "cancelled" -> TournamentStatus.CANCELLED
        else -> TournamentStatus.DRAFT
    }
    val locationStr = buildString {
        if (!locationName.isNullOrEmpty()) append(locationName)
    }

    return Tournament(
        id = id,
        name = name,
        sportId = sportId ?: "",
        sportName = sportName ?: "",
        status = tournamentStatus,
        startDate = startDate ?: "",
        endDate = endDate,
        location = locationStr,
        description = description ?: "",
        format = format ?: "",
        maxParticipants = maxParticipants ?: 0,
        currentParticipants = participantCount,
        prize = prize ?: "",
        imageUrl = imageUrl,
        organizerName = organizerName ?: "",
        visibility = visibility ?: "public",
        ageCategory = ageCategory
    )
}

internal fun ResultDto.toDomain(): TournamentResult {
    return TournamentResult(
        id = id ?: "",
        tournamentId = tournamentId,
        tournamentName = tournaments?.name ?: "",
        date = tournaments?.startDate ?: "",
        position = position,
        points = points ?: 0,
        participants = 0,
        sportId = tournaments?.sportId ?: "",
        sportName = tournaments?.sports?.name ?: ""
    )
}

internal fun GoalDto.toDomain(): AthleteGoal {
    val goalType = when (type) {
        "tournament" -> GoalType.TOURNAMENT
        "points" -> GoalType.POINTS
        else -> GoalType.RATING
    }
    val goalStatus = when (status) {
        "completed" -> GoalStatus.COMPLETED
        "failed" -> GoalStatus.FAILED
        else -> GoalStatus.ACTIVE
    }
    val target = when (goalType) {
        GoalType.RATING -> targetRating ?: 0
        GoalType.TOURNAMENT -> targetWins ?: 0
        GoalType.POINTS -> targetPoints ?: 0
    }
    val current = when (goalType) {
        GoalType.RATING -> 0 // filled from stats separately
        GoalType.TOURNAMENT -> currentWins ?: 0
        GoalType.POINTS -> currentPoints ?: 0
    }

    return AthleteGoal(
        id = id,
        type = goalType,
        title = title,
        description = description ?: "",
        deadline = deadline,
        status = goalStatus,
        targetValue = target,
        currentValue = current,
        createdAt = createdAt ?: ""
    )
}
