package com.example.fitmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_user")
data class CachedUserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String? = null
)