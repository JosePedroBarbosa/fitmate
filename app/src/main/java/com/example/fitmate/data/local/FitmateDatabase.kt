package com.example.fitmate.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fitmate.data.local.dao.CachedGoalDao
import com.example.fitmate.data.local.dao.CachedChallengeDao
import com.example.fitmate.data.local.dao.CachedLeaderboardDao
import com.example.fitmate.data.local.dao.CachedUserDao
import com.example.fitmate.data.local.dao.CachedNotificationDao
import com.example.fitmate.data.local.dao.CachedWorkoutDao
import com.example.fitmate.data.local.entity.CachedGoalEntity
import com.example.fitmate.data.local.entity.CachedLeaderboardEntryEntity
import com.example.fitmate.data.local.entity.CachedUserEntity
import com.example.fitmate.data.local.entity.CachedNotificationEntity
import com.example.fitmate.data.local.entity.CachedWorkoutEntity
import com.example.fitmate.data.local.entity.CachedChallengeEntity
import com.example.fitmate.data.local.entity.CachedWorkoutHistoryEntity
import com.example.fitmate.data.local.dao.CachedWorkoutHistoryDao
import com.example.fitmate.data.local.entity.CachedUserChallengeEntity
import com.example.fitmate.data.local.dao.CachedUserChallengeDao
import com.example.fitmate.data.local.util.Converters

@Database(
    entities = [CachedUserEntity::class, CachedGoalEntity::class, CachedWorkoutEntity::class, CachedLeaderboardEntryEntity::class, CachedNotificationEntity::class, CachedChallengeEntity::class, CachedWorkoutHistoryEntity::class, CachedUserChallengeEntity::class],
    version = 8,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FitmateDatabase : RoomDatabase() {
    abstract fun cachedUserDao(): CachedUserDao
    abstract fun cachedGoalDao(): CachedGoalDao
    abstract fun cachedWorkoutDao(): CachedWorkoutDao
    abstract fun cachedLeaderboardDao(): CachedLeaderboardDao
    abstract fun cachedNotificationDao(): CachedNotificationDao
    abstract fun cachedChallengeDao(): CachedChallengeDao
    abstract fun cachedWorkoutHistoryDao(): CachedWorkoutHistoryDao
    abstract fun cachedUserChallengeDao(): CachedUserChallengeDao
}
