package com.example.fitmate.model

import com.example.fitmate.model.enums.GoalType

sealed class Goal(
    open val type: GoalType,
    open val progress: Int = 0,
    open val createdAt: Long = System.currentTimeMillis(),
)