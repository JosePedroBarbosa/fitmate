package com.example.fitmate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitmate.data.local.entity.CachedNotificationEntity

@Dao
interface CachedNotificationDao {
    @Query("SELECT * FROM cached_notification ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatest(limit: Int = 20): List<CachedNotificationEntity>

    @Query("SELECT COUNT(*) FROM cached_notification WHERE read = 0")
    suspend fun countUnread(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CachedNotificationEntity)

    @Query("UPDATE cached_notification SET read = 1")
    suspend fun markAllRead()
}
