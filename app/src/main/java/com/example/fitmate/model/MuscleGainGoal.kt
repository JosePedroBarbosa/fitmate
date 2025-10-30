package com.example.fitmate.model

data class MuscleGainGoal(
    override val createdAt: Long = System.currentTimeMillis(),
    override val progress: Int = 0,
    val initialMuscleMassPercent: Double,
    val currentMuscleMassPercent: Double,
    val targetMuscleMassPercent: Double
) : Goal(
    type = GoalType.MUSCLE_GAIN,
    createdAt = createdAt,
    progress = progress
)