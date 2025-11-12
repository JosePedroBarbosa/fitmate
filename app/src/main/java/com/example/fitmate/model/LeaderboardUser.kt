package com.example.fitmate.model

data class LeaderboardUser(
    val name: String,
    val points: Int,
    val rank: Int,
    val avatar: String = "",
    val isCurrentUser: Boolean = false
)