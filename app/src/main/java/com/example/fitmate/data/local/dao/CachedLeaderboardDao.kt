package com.example.fitmate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitmate.data.local.entity.CachedLeaderboardEntryEntity

@Dao
interface CachedLeaderboardDao {
    @Query("SELECT * FROM cached_leaderboard ORDER BY rank ASC")
    suspend fun getAll(): List<CachedLeaderboardEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<CachedLeaderboardEntryEntity>)

    @Query("DELETE FROM cached_leaderboard")
    suspend fun clear()
}
