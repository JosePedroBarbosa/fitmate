package com.example.fitmate.model.enums

enum class FitnessLevelType(val label: String) {
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    EXPERT("Expert");

    companion object {
        fun fromLabel(label: String): FitnessLevelType? =
            entries.find { it.label == label }
    }
}