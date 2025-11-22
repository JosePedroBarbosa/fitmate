package com.example.fitmate.data.local.util

import androidx.room.TypeConverter
import com.example.fitmate.model.ApiExercise
import com.example.fitmate.model.enums.GoalType
import com.example.fitmate.model.enums.WorkoutStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

object Converters {
    private val gson = Gson()

    @TypeConverter
    @JvmStatic
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    @JvmStatic
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    @JvmStatic
    fun fromWorkoutStatus(status: WorkoutStatus?): String? = status?.name

    @TypeConverter
    @JvmStatic
    fun toWorkoutStatus(value: String?): WorkoutStatus? = value?.let { WorkoutStatus.valueOf(it) }

    @TypeConverter
    @JvmStatic
    fun fromGoalType(type: GoalType?): String? = type?.name

    @TypeConverter
    @JvmStatic
    fun toGoalType(value: String?): GoalType? = value?.let { GoalType.valueOf(it) }

    @TypeConverter
    @JvmStatic
    fun fromExercisesList(list: List<ApiExercise>?): String? = list?.let { gson.toJson(it) }

    @TypeConverter
    @JvmStatic
    fun toExercisesList(json: String?): List<ApiExercise>? {
        if (json.isNullOrBlank()) return null
        val type = object : TypeToken<List<ApiExercise>>() {}.type
        return gson.fromJson(json, type)
    }
}