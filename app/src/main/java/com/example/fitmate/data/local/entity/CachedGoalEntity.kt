package com.example.fitmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fitmate.model.enums.GoalType

@Entity(tableName = "cached_goal")
data class CachedGoalEntity(
    @PrimaryKey val uid: String,
    val type: GoalType,
    val progress: Int,
    val createdAt: Long,
    // Weight loss
    val initialWeight: Double? = null,
    val currentWeight: Double? = null,
    val targetWeight: Double? = null,
    // Muscle gain
    val initialMuscleMassPercent: Double? = null,
    val currentMuscleMassPercent: Double? = null,
    val targetMuscleMassPercent: Double? = null
)