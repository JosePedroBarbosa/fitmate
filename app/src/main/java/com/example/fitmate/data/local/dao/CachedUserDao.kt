package com.example.fitmate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fitmate.data.local.entity.CachedUserEntity

@Dao
interface CachedUserDao {
    @Query("SELECT * FROM cached_user WHERE uid = :uid LIMIT 1")
    suspend fun getUser(uid: String): CachedUserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: CachedUserEntity)

    @Query("DELETE FROM cached_user WHERE uid = :uid")
    suspend fun delete(uid: String)
}