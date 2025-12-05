package com.example.fitmate.data.local.entity

import androidx.room.Entity
import androidx.room.TypeConverters
import com.example.fitmate.data.local.util.Converters
import java.time.LocalDate
import com.example.fitmate.model.enums.WorkoutStatus

@Entity(tableName = "cached_user_challenge", primaryKeys = ["userId", "challengeId"])
@TypeConverters(Converters::class)
data class CachedUserChallengeEntity(
    val userId: String,
    val challengeId: String,
    val progress: Int,
    val isCompleted: Boolean,
    val isActive: Boolean,
    val startedAt: Long?,
    val title: String,
    val description: String,
    val rewardPoints: Int,
    val difficulty: String,
    val exerciseCount: Int,
    val workoutDate: LocalDate,
    val workoutTitle: String,
    val workoutDescription: String,
    val workoutDuration: String,
    val workoutExercisesJson: String,
    val workoutStatus: WorkoutStatus,
    val workoutPhotoPath: String?
)
