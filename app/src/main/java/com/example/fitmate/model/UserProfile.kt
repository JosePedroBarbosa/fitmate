package com.example.fitmate.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val height: Int? = null,
    val weight: Int? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val fitnessLevel: String? = null
)