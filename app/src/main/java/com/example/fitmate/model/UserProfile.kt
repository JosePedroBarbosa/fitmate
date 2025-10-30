package com.example.fitmate.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val points: Int = 0,
    val height: Int? = null,
    val weight: Double? = null,
    val dateOfBirth: String? = null,
    val gender: GenderType? = null,
    val fitnessLevel: FitnessLevelType? = null,
    val goal: Goal? = null
)