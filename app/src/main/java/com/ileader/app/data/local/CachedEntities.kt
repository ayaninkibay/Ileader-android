package com.ileader.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_sports")
data class CachedSport(
    @PrimaryKey val id: String,
    val name: String,
    val slug: String?,
    val athleteLabel: String?,
    val iconUrl: String?,
    val isActive: Boolean = true,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cached_tournaments")
data class CachedTournament(
    @PrimaryKey val id: String,
    val name: String,
    val sportId: String?,
    val sportName: String?,
    val locationName: String?,
    val organizerName: String?,
    val status: String?,
    val startDate: String?,
    val endDate: String?,
    val imageUrl: String?,
    val maxParticipants: Int?,
    val participantCount: Int = 0,
    val format: String?,
    val region: String?,
    val ageCategory: String?,
    val cachedAt: Long = System.currentTimeMillis()
)
