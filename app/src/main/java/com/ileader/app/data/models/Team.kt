package com.ileader.app.data.models

data class Team(
    val id: String,
    val name: String,
    val logoUrl: String? = null,
    val sportId: String,
    val sportName: String,
    val trainerId: String,
    val trainerName: String,
    val sponsorName: String? = null,
    val foundedDate: String,
    val description: String,
    val members: List<TeamMember> = emptyList()
)

data class TeamMember(
    val id: String,
    val name: String,
    val role: String,
    val avatarUrl: String? = null,
    val tournaments: Int = 0,
    val wins: Int = 0,
    val podiums: Int = 0
)
