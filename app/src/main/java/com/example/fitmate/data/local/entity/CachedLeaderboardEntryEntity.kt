package com.example.fitmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_leaderboard")
data class CachedLeaderboardEntryEntity(
    @PrimaryKey val rank: Int,
    val uid: String,
    val name: String,
    val points: Int
)
