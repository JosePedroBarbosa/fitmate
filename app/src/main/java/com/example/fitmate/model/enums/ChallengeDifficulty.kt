package com.example.fitmate.model.enums

import androidx.compose.ui.graphics.Color

private val AccentOrange = Color(0xFFFF6B35)
private val AccentPurple = Color(0xFF7B2CBF)
private val AccentGreen = Color(0xFF06D6A0)
private val AccentRed = Color(0xFFEF476F)

enum class ChallengeDifficulty(val label: String, val color: Color) {
    EASY("Easy", AccentGreen),
    MEDIUM("Medium", AccentOrange),
    HARD("Hard", AccentRed),
    EXPERT("Expert", AccentPurple)
}