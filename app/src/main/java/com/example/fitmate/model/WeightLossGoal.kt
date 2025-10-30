package com.example.fitmate.model

data class WeightLossGoal(
    override val createdAt: Long = System.currentTimeMillis(),
    override val progress: Int = 0,
    val initialWeight: Double,
    val currentWeight: Double,
    val targetWeight: Double
) : Goal(
    type = GoalType.WEIGHT_LOSS,
    createdAt = createdAt,
    progress = progress
)