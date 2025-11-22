package com.example.fitmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.fitmate.data.local.util.Converters
import com.example.fitmate.model.enums.WorkoutStatus
import java.time.LocalDate

@Entity(tableName = "cached_workout")
@TypeConverters(Converters::class)
data class CachedWorkoutEntity(
    @PrimaryKey val uid: String,
    val date: LocalDate,
    val title: String,
    val description: String,
    val duration: String,
    val exercisesJson: String, // armazenar lista como JSON para simplicidade
    val status: WorkoutStatus
)