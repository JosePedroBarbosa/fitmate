package com.example.fitmate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitmate.data.local.entity.CachedWorkoutEntity

@Dao
interface CachedWorkoutDao {
    @Query("SELECT * FROM cached_workout WHERE uid = :uid LIMIT 1")
    suspend fun getWorkout(uid: String): CachedWorkoutEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(workout: CachedWorkoutEntity)

    @Query("DELETE FROM cached_workout WHERE uid = :uid")
    suspend fun delete(uid: String)
}