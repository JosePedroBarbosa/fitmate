package com.example.fitmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_notification")
data class CachedNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val timestamp: Long,
    val read: Boolean = false,
    val type: String? = null
)
