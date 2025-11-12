package com.example.fitmate.model

import com.example.fitmate.model.enums.ChallengeDifficulty

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val rewardPoints: Int,
    val difficulty: ChallengeDifficulty,
    val duration: String,
    val exerciseCount: Int,
    val currentProgress: Int = 0,
    val totalProgress: Int = 100,
    val isActive: Boolean = false
)