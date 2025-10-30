package com.example.fitmate.model

enum class GoalType(val label: String) {
    WEIGHT_LOSS("Weight Loss"),
    MUSCLE_GAIN("Muscle Gain");

    companion object {
        fun fromLabel(label: String): GoalType? =
            entries.find { it.label.equals(label, ignoreCase = true) }
    }
}