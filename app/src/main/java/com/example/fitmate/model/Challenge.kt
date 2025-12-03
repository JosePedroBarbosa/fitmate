package com.example.fitmate.model

import com.example.fitmate.model.enums.ChallengeDifficulty

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val rewardPoints: Int,
    val difficulty: ChallengeDifficulty,
    val exerciseCount: Int,
    val isActive: Boolean = false,
    val workout: DailyWorkout,
)