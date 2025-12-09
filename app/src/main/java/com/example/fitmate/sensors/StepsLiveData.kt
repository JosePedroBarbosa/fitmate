package com.example.fitmate.sensors

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object StepsLiveData {
    private val _steps = MutableLiveData<Int>(0)
    val steps: LiveData<Int> = _steps

    fun updateSteps(newSteps: Int) {
        _steps.value = newSteps
    }

    fun reset() {
        _steps.value = 0
    }
}