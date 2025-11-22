package com.example.fitmate.model

import com.example.fitmate.model.enums.WorkoutStatus
import java.time.LocalDate

data class DailyWorkout(
    val date: LocalDate,
    val title: String,
    val description: String,
    val duration: String,
    val exercises: List<ApiExercise>,
    val status: WorkoutStatus
)