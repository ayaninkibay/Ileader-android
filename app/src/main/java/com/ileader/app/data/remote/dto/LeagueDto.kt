package com.ileader.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class LeagueDto(
    val id: String,
    val name: String,
    @SerialName("sport_id") val sportId: String? = null,
    @SerialName("organizer_id") val organizerId: String? = null,
    val description: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val season: String? = null,
    val status: String? = null,
    @SerialName("total_stages") val totalStages: Int = 4,
    @SerialName("best_of") val bestOf: Int? = null,
    @SerialName("scoring_table") val scoringTable: JsonElement? = null,
    @SerialName("allow_late_join") val allowLateJoin: Boolean? = true,
    @SerialName("entry_fee") val entryFee: Double? = null,
    @SerialName("entry_fee_per_stage") val entryFeePerStage: Boolean? = false,
    @SerialName("created_at") val createdAt: String? = null,
    // JOIN fields
    val sports: SportDto? = null,
    val profiles: ProfileMinimalDto? = null
)

@Serializable
data class LeagueStageDto(
    val id: String,
    @SerialName("league_id") val leagueId: String,
    @SerialName("tournament_id") val tournamentId: String? = null,
    @SerialName("stage_number") val stageNumber: Int,
    val title: String? = null,
    val status: String = "upcoming",
    @SerialName("created_at") val createdAt: String? = null,
    // JOIN
    val tournaments: TournamentMinimalDto? = null
)

@Serializable
data class LeagueStandingDto(
    val id: String,
    @SerialName("league_id") val leagueId: String,
    @SerialName("athlete_id") val athleteId: String,
    @SerialName("total_points") val totalPoints: Int = 0,
    @SerialName("stages_participated") val stagesParticipated: Int = 0,
    @SerialName("best_finish") val bestFinish: Int? = null,
    @SerialName("worst_finish") val worstFinish: Int? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    // JOIN
    val profiles: ProfileMinimalDto? = null
)
