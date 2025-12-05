package com.example.fitmate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitmate.data.local.entity.CachedChallengeEntity

@Dao
interface CachedChallengeDao {
    @Query("SELECT * FROM cached_challenge WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CachedChallengeEntity?

    @Query("SELECT * FROM cached_challenge ORDER BY workoutDate DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<CachedChallengeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CachedChallengeEntity)
}

