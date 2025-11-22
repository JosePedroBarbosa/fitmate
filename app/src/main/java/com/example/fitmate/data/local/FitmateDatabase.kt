package com.example.fitmate.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fitmate.data.local.dao.CachedGoalDao
import com.example.fitmate.data.local.dao.CachedUserDao
import com.example.fitmate.data.local.dao.CachedWorkoutDao
import com.example.fitmate.data.local.entity.CachedGoalEntity
import com.example.fitmate.data.local.entity.CachedUserEntity
import com.example.fitmate.data.local.entity.CachedWorkoutEntity
import com.example.fitmate.data.local.util.Converters

@Database(
    entities = [CachedUserEntity::class, CachedGoalEntity::class, CachedWorkoutEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FitmateDatabase : RoomDatabase() {
    abstract fun cachedUserDao(): CachedUserDao
    abstract fun cachedGoalDao(): CachedGoalDao
    abstract fun cachedWorkoutDao(): CachedWorkoutDao
}