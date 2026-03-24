package com.ileader.app.data.models

enum class InviteStatus(val displayName: String) {
    PENDING("Ожидает"),
    ACCEPTED("Принято"),
    DECLINED("Отклонено");
}

data class TournamentInvite(
    val id: String,
    val tournamentId: String,
    val tournamentName: String,
    val status: InviteStatus = InviteStatus.PENDING,
    val message: String? = null,
    val createdAt: String
)

data class TeamRequest(
    val id: String,
    val teamId: String,
    val teamName: String,
    val status: InviteStatus = InviteStatus.PENDING,
    val message: String? = null,
    val responseMessage: String? = null,
    val createdAt: String
)
