package com.example.fitmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.fitmate.data.local.util.Converters
import com.example.fitmate.model.enums.WorkoutStatus
import java.time.LocalDate

@Entity(tableName = "cached_workout_history")
@TypeConverters(Converters::class)
data class CachedWorkoutHistoryEntity(
    @PrimaryKey val id: String,
    val uid: String,
    val date: LocalDate,
    val title: String,
    val description: String,
    val duration: String,
    val exercisesJson: String,
    val status: WorkoutStatus,
    val photoPath: String? = null
)
