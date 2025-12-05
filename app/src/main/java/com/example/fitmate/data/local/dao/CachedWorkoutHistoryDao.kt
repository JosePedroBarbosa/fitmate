package com.example.fitmate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitmate.data.local.entity.CachedWorkoutHistoryEntity

@Dao
interface CachedWorkoutHistoryDao {
    @Query("SELECT * FROM cached_workout_history WHERE uid = :uid ORDER BY date DESC")
    suspend fun getAll(uid: String): List<CachedWorkoutHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CachedWorkoutHistoryEntity>)

    @Query("DELETE FROM cached_workout_history WHERE uid = :uid")
    suspend fun clear(uid: String)
}
