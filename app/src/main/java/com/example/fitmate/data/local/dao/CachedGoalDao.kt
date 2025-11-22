package com.example.fitmate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitmate.data.local.entity.CachedGoalEntity

@Dao
interface CachedGoalDao {
    @Query("SELECT * FROM cached_goal WHERE uid = :uid LIMIT 1")
    suspend fun getGoal(uid: String): CachedGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: CachedGoalEntity)

    @Query("DELETE FROM cached_goal WHERE uid = :uid")
    suspend fun delete(uid: String)
}