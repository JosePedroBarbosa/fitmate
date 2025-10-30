package com.example.fitmate.model

sealed class Goal(
    open val type: GoalType,
    open val progress: Int = 0,
    open val createdAt: Long = System.currentTimeMillis(),
)