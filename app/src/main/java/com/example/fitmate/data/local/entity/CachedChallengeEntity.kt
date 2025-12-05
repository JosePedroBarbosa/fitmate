package com.example.fitmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.fitmate.data.local.util.Converters
import com.example.fitmate.model.enums.WorkoutStatus
import java.time.LocalDate

@Entity(tableName = "cached_challenge")
@TypeConverters(Converters::class)
data class CachedChallengeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val rewardPoints: Int,
    val difficulty: String,
    val exerciseCount: Int,
    val isActive: Boolean,
    val workoutDate: LocalDate,
    val workoutTitle: String,
    val workoutDescription: String,
    val workoutDuration: String,
    val workoutExercisesJson: String,
    val workoutStatus: WorkoutStatus,
    val workoutPhotoPath: String? = null
)