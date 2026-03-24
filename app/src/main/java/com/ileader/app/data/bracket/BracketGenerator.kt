package com.ileader.app.data.bracket

import java.util.UUID

// Input participant for bracket generation
data class BracketParticipant(
    val id: String,
    val name: String,
    val seed: Int? = null,
    val rating: Int? = null
)

// Generated match (before saving to DB)
data class GeneratedMatch(
    val id: String = UUID.randomUUID().toString(),
    val round: Int,
    val matchNumber: Int,
    val bracketType: String = "upper",
    val participant1Id: String? = null,
    val participant2Id: String? = null,
    val participant1Name: String? = null,
    val participant2Name: String? = null,
    val participant1Seed: Int? = null,
    val participant2Seed: Int? = null,
    val participant1Score: Int = 0,
    val participant2Score: Int = 0,
    val games: List<GeneratedGame> = emptyList(),
    val winnerId: String? = null,
    val loserId: String? = null,
    val status: String = "scheduled",
    var nextMatchId: String? = null,
    var loserNextMatchId: String? = null,
    val groupId: String? = null,
    val isBye: Boolean = false
)

data class GeneratedGame(
    val gameNumber: Int,
    val participant1Score: Int = 0,
    val participant2Score: Int = 0,
    val winnerId: String? = null,
    val status: String = "pending"
)

data class GeneratedGroup(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val participants: List<GroupStanding> = emptyList()
)

data class GroupStanding(
    val participantId: String,
    val athleteName: String,
    val team: String? = null,
    val seed: Int? = null,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val points: Int = 0,
    val gamesPlayed: Int = 0,
    val position: Int = 0,
    val qualified: Boolean = false
)

data class BracketGeneratorOptions(
    val tournamentId: String,
    val format: String, // single_elimination, double_elimination, round_robin, groups_single_elim, groups_double_elim
    val seedingType: String = "random", // random, rating, manual
    val matchFormat: String = "BO1",
    val stageMatchFormats: Map<String, String>? = null, // default, semiFinal, final
    val hasThirdPlaceMatch: Boolean = false,
    val groupCount: Int = 2,
    val advancePerGroup: Int = 2
)

data class BracketGeneratorResult(
    val matches: List<GeneratedMatch>,
    val groups: List<GeneratedGroup>
)

object BracketGenerator {

    fun generate(
        participants: List<BracketParticipant>,
        options: BracketGeneratorOptions
    ): BracketGeneratorResult {
        val seeded = seedParticipants(participants, options.seedingType)

        return when (options.format) {
            "single_elimination" -> generateSingleElim(seeded, options)
            "double_elimination" -> generateDoubleElim(seeded, options)
            "round_robin" -> generateRoundRobin(seeded, options)
            "groups_single_elim" -> generateGroupStage(seeded, options, "single_elimination")
            "groups_double_elim" -> generateGroupStage(seeded, options, "double_elimination")
            else -> generateSingleElim(seeded, options)
        }
    }

    fun seedParticipants(
        participants: List<BracketParticipant>,
        seedingType: String
    ): List<BracketParticipant> {
        val arr = participants.toMutableList()
        when (seedingType) {
            "rating" -> arr.sortByDescending { it.rating ?: 0 }
            "random" -> arr.shuffle()
            "manual" -> arr.sortBy { it.seed ?: Int.MAX_VALUE }
        }
        return arr.mapIndexed { index, p -> p.copy(seed = index + 1) }
    }

    private fun nextPowerOf2(n: Int): Int {
        var v = 1
        while (v < n) v *= 2
        return v
    }

    private fun generateSeedOrder(size: Int): List<Int> {
        if (size <= 1) return listOf(1)
        val result = mutableListOf(1, 2)
        var currentSize = 2
        while (currentSize < size) {
            val next = mutableListOf<Int>()
            val newSize = currentSize * 2
            for (seed in result) {
                next.add(seed)
                next.add(newSize + 1 - seed)
            }
            result.clear()
            result.addAll(next)
            currentSize = newSize
        }
        return result
    }

    private fun getMatchFormat(round: Int, totalRounds: Int, options: BracketGeneratorOptions): String {
        val smf = options.stageMatchFormats ?: return options.matchFormat
        val fromEnd = totalRounds - round
        return when {
            fromEnd == 0 && smf.containsKey("final") -> smf["final"]!!
            fromEnd == 1 && smf.containsKey("semiFinal") -> smf["semiFinal"]!!
            else -> smf["default"] ?: options.matchFormat
        }
    }

    private fun createPendingGames(matchFormat: String): List<GeneratedGame> {
        val count = matchFormat.removePrefix("BO").toIntOrNull() ?: 1
        return (1..count).map { GeneratedGame(gameNumber = it) }
    }

    // ── SINGLE ELIMINATION ──

    private fun generateSingleElim(
        seeded: List<BracketParticipant>,
        options: BracketGeneratorOptions
    ): BracketGeneratorResult {
        val bracketSize = nextPowerOf2(seeded.size)
        val totalRounds = Integer.numberOfTrailingZeros(bracketSize)
        val seedOrder = generateSeedOrder(bracketSize)
        val allMatches = mutableListOf<GeneratedMatch>()
        var matchCounter = 1

        // Round 1
        val round1 = mutableListOf<GeneratedMatch>()
        for (i in seedOrder.indices step 2) {
            val seed1 = seedOrder[i]
            val seed2 = seedOrder[i + 1]
            val p1 = seeded.getOrNull(seed1 - 1)
            val p2 = seeded.getOrNull(seed2 - 1)
            val isBye = p1 == null || p2 == null
            val mf = getMatchFormat(1, totalRounds, options)

            val match = GeneratedMatch(
                round = 1,
                matchNumber = matchCounter++,
                bracketType = "upper",
                participant1Id = p1?.id,
                participant2Id = p2?.id,
                participant1Name = p1?.name,
                participant2Name = p2?.name,
                participant1Seed = p1?.seed,
                participant2Seed = p2?.seed,
                games = createPendingGames(mf),
                isBye = isBye,
                status = if (isBye) "completed" else "scheduled",
                winnerId = if (isBye) (p1?.id ?: p2?.id) else null,
                loserId = null
            )
            round1.add(match)
        }
        allMatches.addAll(round1)

        // Subsequent rounds
        var prevRound = round1
        for (r in 2..totalRounds) {
            val currentRound = mutableListOf<GeneratedMatch>()
            val mf = getMatchFormat(r, totalRounds, options)
            for (i in prevRound.indices step 2) {
                val match = GeneratedMatch(
                    round = r,
                    matchNumber = matchCounter++,
                    bracketType = "upper",
                    games = createPendingGames(mf)
                )
                // Link previous matches
                prevRound[i].nextMatchId = match.id
                prevRound[i + 1].nextMatchId = match.id

                // Auto-propagate bye winners
                val w1 = if (prevRound[i].isBye) prevRound[i].winnerId else null
                val w2 = if (prevRound[i + 1].isBye) prevRound[i + 1].winnerId else null
                val w1Name = if (prevRound[i].isBye) (prevRound[i].participant1Name ?: prevRound[i].participant2Name) else null
                val w2Name = if (prevRound[i + 1].isBye) (prevRound[i + 1].participant1Name ?: prevRound[i + 1].participant2Name) else null

                currentRound.add(match.copy(
                    participant1Id = w1,
                    participant1Name = w1Name,
                    participant2Id = w2,
                    participant2Name = w2Name
                ))
            }
            allMatches.addAll(currentRound)
            prevRound = currentRound
        }

        // Third place match
        if (options.hasThirdPlaceMatch && totalRounds >= 2) {
            val mf = getMatchFormat(totalRounds, totalRounds, options)
            val thirdPlace = GeneratedMatch(
                round = totalRounds,
                matchNumber = matchCounter,
                bracketType = "third_place",
                games = createPendingGames(mf)
            )
            allMatches.add(thirdPlace)
        }

        return BracketGeneratorResult(allMatches, emptyList())
    }

    // ── DOUBLE ELIMINATION ──

    private fun generateDoubleElim(
        seeded: List<BracketParticipant>,
        options: BracketGeneratorOptions
    ): BracketGeneratorResult {
        val bracketSize = nextPowerOf2(seeded.size)
        val upperRounds = Integer.numberOfTrailingZeros(bracketSize)
        val allMatches = mutableListOf<GeneratedMatch>()
        var matchCounter = 1

        // ── Upper Bracket ──
        val seedOrder = generateSeedOrder(bracketSize)
        val upperByRound = mutableListOf<MutableList<GeneratedMatch>>()

        // Upper Round 1
        val upperR1 = mutableListOf<GeneratedMatch>()
        for (i in seedOrder.indices step 2) {
            val seed1 = seedOrder[i]
            val seed2 = seedOrder[i + 1]
            val p1 = seeded.getOrNull(seed1 - 1)
            val p2 = seeded.getOrNull(seed2 - 1)
            val isBye = p1 == null || p2 == null

            val match = GeneratedMatch(
                round = 1,
                matchNumber = matchCounter++,
                bracketType = "upper",
                participant1Id = p1?.id,
                participant2Id = p2?.id,
                participant1Name = p1?.name,
                participant2Name = p2?.name,
                participant1Seed = p1?.seed,
                participant2Seed = p2?.seed,
                games = createPendingGames(options.matchFormat),
                isBye = isBye,
                status = if (isBye) "completed" else "scheduled",
                winnerId = if (isBye) (p1?.id ?: p2?.id) else null
            )
            upperR1.add(match)
        }
        allMatches.addAll(upperR1)
        upperByRound.add(upperR1)

        // Upper rounds 2..N
        for (r in 2..upperRounds) {
            val prev = upperByRound.last()
            val current = mutableListOf<GeneratedMatch>()
            for (i in prev.indices step 2) {
                val match = GeneratedMatch(
                    round = r,
                    matchNumber = matchCounter++,
                    bracketType = "upper",
                    games = createPendingGames(options.matchFormat)
                )
                prev[i].nextMatchId = match.id
                prev[i + 1].nextMatchId = match.id
                current.add(match)
            }
            allMatches.addAll(current)
            upperByRound.add(current)
        }

        // ── Lower Bracket ──
        val lowerRoundCount = (upperRounds - 1) * 2
        val lowerByRound = mutableListOf<MutableList<GeneratedMatch>>()

        if (lowerRoundCount > 0) {
            // Lower Round 1: losers from upper R1
            val lr1 = mutableListOf<GeneratedMatch>()
            val upperR1Matches = upperByRound[0]
            for (i in upperR1Matches.indices step 2) {
                val match = GeneratedMatch(
                    round = 1,
                    matchNumber = matchCounter++,
                    bracketType = "lower",
                    games = createPendingGames(options.matchFormat)
                )
                upperR1Matches[i].loserNextMatchId = match.id
                upperR1Matches[i + 1].loserNextMatchId = match.id
                lr1.add(match)
            }
            allMatches.addAll(lr1)
            lowerByRound.add(lr1)

            // Lower rounds 2..N
            var upperRoundIdx = 1 // Starting from upper R2 losers
            for (lr in 2..lowerRoundCount) {
                val prevLower = lowerByRound.last()
                val isEvenRound = lr % 2 == 0
                val current = mutableListOf<GeneratedMatch>()

                if (isEvenRound && upperRoundIdx < upperByRound.size) {
                    // Mix: lower survivors + upper losers from next round
                    val upperLosers = upperByRound[upperRoundIdx]
                    for (i in prevLower.indices) {
                        val match = GeneratedMatch(
                            round = lr,
                            matchNumber = matchCounter++,
                            bracketType = "lower",
                            games = createPendingGames(options.matchFormat)
                        )
                        prevLower[i].nextMatchId = match.id
                        if (i < upperLosers.size) {
                            upperLosers[i].loserNextMatchId = match.id
                        }
                        current.add(match)
                    }
                    upperRoundIdx++
                } else {
                    // Internal: pair lower survivors
                    for (i in prevLower.indices step 2) {
                        if (i + 1 < prevLower.size) {
                            val match = GeneratedMatch(
                                round = lr,
                                matchNumber = matchCounter++,
                                bracketType = "lower",
                                games = createPendingGames(options.matchFormat)
                            )
                            prevLower[i].nextMatchId = match.id
                            prevLower[i + 1].nextMatchId = match.id
                            current.add(match)
                        } else {
                            // Odd number - auto advance
                            val match = GeneratedMatch(
                                round = lr,
                                matchNumber = matchCounter++,
                                bracketType = "lower",
                                games = createPendingGames(options.matchFormat)
                            )
                            prevLower[i].nextMatchId = match.id
                            current.add(match)
                        }
                    }
                }

                allMatches.addAll(current)
                lowerByRound.add(current)
            }
        }

        // ── Grand Final ──
        val grandFinal = GeneratedMatch(
            round = upperRounds + 1,
            matchNumber = matchCounter,
            bracketType = "grand_final",
            games = createPendingGames(options.matchFormat)
        )
        // Link upper final
        upperByRound.last().lastOrNull()?.nextMatchId = grandFinal.id
        // Link lower final
        lowerByRound.lastOrNull()?.lastOrNull()?.nextMatchId = grandFinal.id
        allMatches.add(grandFinal)

        return BracketGeneratorResult(allMatches, emptyList())
    }

    // ── ROUND ROBIN ──

    private fun generateRoundRobin(
        seeded: List<BracketParticipant>,
        options: BracketGeneratorOptions
    ): BracketGeneratorResult {
        val groupId = UUID.randomUUID().toString()
        val matches = mutableListOf<GeneratedMatch>()
        var matchCounter = 1

        for (i in seeded.indices) {
            for (j in i + 1 until seeded.size) {
                val match = GeneratedMatch(
                    round = 1,
                    matchNumber = matchCounter++,
                    bracketType = "upper",
                    participant1Id = seeded[i].id,
                    participant2Id = seeded[j].id,
                    participant1Name = seeded[i].name,
                    participant2Name = seeded[j].name,
                    participant1Seed = seeded[i].seed,
                    participant2Seed = seeded[j].seed,
                    games = createPendingGames(options.matchFormat),
                    groupId = groupId
                )
                matches.add(match)
            }
        }

        val group = GeneratedGroup(
            id = groupId,
            name = "Общая таблица",
            participants = seeded.mapIndexed { index, p ->
                GroupStanding(
                    participantId = p.id,
                    athleteName = p.name,
                    seed = p.seed,
                    position = index + 1
                )
            }
        )

        return BracketGeneratorResult(matches, listOf(group))
    }

    // ── GROUP STAGE ──

    private fun generateGroupStage(
        seeded: List<BracketParticipant>,
        options: BracketGeneratorOptions,
        playoffFormat: String
    ): BracketGeneratorResult {
        val groupCount = options.groupCount.coerceIn(2, seeded.size / 2)
        val allMatches = mutableListOf<GeneratedMatch>()
        val allGroups = mutableListOf<GeneratedGroup>()
        var matchCounter = 1

        // Snake draft into groups
        val groupArrays = Array(groupCount) { mutableListOf<BracketParticipant>() }
        seeded.forEachIndexed { index, participant ->
            val round = index / groupCount
            val pos = if (round % 2 == 0) index % groupCount else groupCount - 1 - (index % groupCount)
            groupArrays[pos].add(participant)
        }

        // Round robin within each group
        val groupNames = listOf("A", "B", "C", "D", "E", "F", "G", "H")
        groupArrays.forEachIndexed { gIdx, groupParticipants ->
            val groupId = UUID.randomUUID().toString()
            val groupName = "Группа ${groupNames.getOrElse(gIdx) { "${gIdx + 1}" }}"

            for (i in groupParticipants.indices) {
                for (j in i + 1 until groupParticipants.size) {
                    val p1 = groupParticipants[i]
                    val p2 = groupParticipants[j]
                    val match = GeneratedMatch(
                        round = 1,
                        matchNumber = matchCounter++,
                        bracketType = "upper",
                        participant1Id = p1.id,
                        participant2Id = p2.id,
                        participant1Name = p1.name,
                        participant2Name = p2.name,
                        participant1Seed = p1.seed,
                        participant2Seed = p2.seed,
                        games = createPendingGames(options.matchFormat),
                        groupId = groupId
                    )
                    allMatches.add(match)
                }
            }

            val group = GeneratedGroup(
                id = groupId,
                name = groupName,
                participants = groupParticipants.mapIndexed { index, p ->
                    GroupStanding(
                        participantId = p.id,
                        athleteName = p.name,
                        seed = p.seed,
                        position = index + 1
                    )
                }
            )
            allGroups.add(group)
        }

        // Playoff bracket with TBD participants
        val advancePerGroup = options.advancePerGroup.coerceIn(1, 4)
        val playoffParticipants = mutableListOf<BracketParticipant>()
        for (pos in 1..advancePerGroup) {
            for (gIdx in 0 until groupCount) {
                val gName = groupNames.getOrElse(gIdx) { "${gIdx + 1}" }
                playoffParticipants.add(
                    BracketParticipant(
                        id = "tbd-${gName}${pos}",
                        name = "${pos}-е место $gName",
                        seed = playoffParticipants.size + 1
                    )
                )
            }
        }

        val playoffOptions = options.copy(format = playoffFormat, hasThirdPlaceMatch = options.hasThirdPlaceMatch)
        val playoffResult = when (playoffFormat) {
            "double_elimination" -> generateDoubleElim(playoffParticipants, playoffOptions)
            else -> generateSingleElim(playoffParticipants, playoffOptions)
        }

        // Adjust match numbers for playoff
        val playoffMatches = playoffResult.matches.map {
            it.copy(matchNumber = matchCounter++, round = it.round + 1) // offset round to distinguish from group
        }
        allMatches.addAll(playoffMatches)

        return BracketGeneratorResult(allMatches, allGroups)
    }
}
