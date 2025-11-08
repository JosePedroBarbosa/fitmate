package com.example.fitmate.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {

    const val API_KEY = "DGn3W3ZOT35zjaKWz+1cmg==hvNGJA9l5cjXwyjx"

    fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.api-ninjas.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

val exercisesApi = RetrofitHelper.getInstance().create(ExercisesApi::class.java)