package com.ileader.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SportDao {
    @Query("SELECT * FROM cached_sports WHERE isActive = 1 ORDER BY name")
    suspend fun getAll(): List<CachedSport>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sports: List<CachedSport>)

    @Query("DELETE FROM cached_sports")
    suspend fun deleteAll()
}

@Dao
interface TournamentDao {
    @Query("SELECT * FROM cached_tournaments ORDER BY startDate DESC")
    suspend fun getAll(): List<CachedTournament>

    @Query("SELECT * FROM cached_tournaments WHERE status IN ('registration_open', 'in_progress', 'check_in') ORDER BY startDate ASC LIMIT :limit")
    suspend fun getUpcoming(limit: Int = 20): List<CachedTournament>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tournaments: List<CachedTournament>)

    @Query("DELETE FROM cached_tournaments")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM cached_tournaments")
    suspend fun count(): Int
}
