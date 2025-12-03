package com.example.fitmate.model

data class UserChallenge (
    val userId: String,
    val challengeId: String,
    val progress: Int = 0,
    val isCompleted: Boolean = false,
    val isActive: Boolean = false,
    val startedAt: Long? = null,
)
