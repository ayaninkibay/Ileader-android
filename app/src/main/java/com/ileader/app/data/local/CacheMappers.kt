package com.ileader.app.data.local

import com.ileader.app.data.remote.dto.SportDto
import com.ileader.app.data.remote.dto.TournamentWithCountsDto

fun SportDto.toCached() = CachedSport(
    id = id,
    name = name,
    slug = slug,
    athleteLabel = athleteLabel,
    iconUrl = iconUrl,
    isActive = isActive
)

fun CachedSport.toDto() = SportDto(
    id = id,
    name = name,
    slug = slug,
    athleteLabel = athleteLabel,
    iconUrl = iconUrl,
    isActive = isActive
)

fun TournamentWithCountsDto.toCached() = CachedTournament(
    id = id,
    name = name,
    sportId = sportId,
    sportName = sportName,
    locationName = locationName,
    organizerName = organizerName,
    status = status,
    startDate = startDate,
    endDate = endDate,
    imageUrl = imageUrl,
    maxParticipants = maxParticipants,
    participantCount = participantCount,
    format = format,
    region = region,
    ageCategory = ageCategory
)

fun CachedTournament.toDto() = TournamentWithCountsDto(
    id = id,
    name = name,
    sportId = sportId,
    sportName = sportName,
    locationName = locationName,
    organizerName = organizerName,
    status = status,
    startDate = startDate,
    endDate = endDate,
    imageUrl = imageUrl,
    maxParticipants = maxParticipants,
    participantCount = participantCount,
    format = format,
    region = region,
    ageCategory = ageCategory
)
