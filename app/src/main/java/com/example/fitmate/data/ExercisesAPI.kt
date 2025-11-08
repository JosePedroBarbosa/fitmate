package com.example.fitmate.data

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ExercisesApi {
    @GET("v1/exercises")
    suspend fun getExercises(
        @Header("X-Api-Key") apiKey: String,
        @Query("muscle") muscle: String,
        @Query("difficulty") difficulty: String
    ): List<ApiExercise>
}