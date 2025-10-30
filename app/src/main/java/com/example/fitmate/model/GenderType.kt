package com.example.fitmate.model

enum class GenderType(val label: String) {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    companion object {
        fun fromLabel(label: String): GenderType? =
            entries.find { it.label == label }
    }
}