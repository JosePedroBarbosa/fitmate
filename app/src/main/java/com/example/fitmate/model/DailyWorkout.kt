package com.example.fitmate.model

import java.time.LocalDate

data class DailyWorkout(
    val date: LocalDate,
    val title: String,
    val description: String,
    val duration: String,
    val exercises: List<Exercise>,
    val isStarted: Boolean = false
)