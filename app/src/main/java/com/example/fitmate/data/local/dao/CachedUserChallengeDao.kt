package com.example.fitmate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitmate.data.local.entity.CachedUserChallengeEntity

@Dao
interface CachedUserChallengeDao {
    @Query("SELECT * FROM cached_user_challenge WHERE userId = :uid ORDER BY startedAt DESC")
    suspend fun getAll(uid: String): List<CachedUserChallengeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CachedUserChallengeEntity>)

    @Query("DELETE FROM cached_user_challenge WHERE userId = :uid")
    suspend fun clear(uid: String)
}
