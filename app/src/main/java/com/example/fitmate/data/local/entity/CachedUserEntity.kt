package com.example.fitmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_user")
data class CachedUserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String? = null,
    val points: Int = 0,
    val height: Int? = null,
    val weight: Double? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val fitnessLevel: String? = null
)
