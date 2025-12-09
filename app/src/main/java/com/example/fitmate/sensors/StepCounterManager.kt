package com.example.fitmate.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.LocalDate

class StepCounterManager(
    context: Context,
    private val onStepsChanged: (Int) -> Unit
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val sharedPref = context.getSharedPreferences("steps_tracker", Context.MODE_PRIVATE)

    private var initialSteps = -1
    private var lastResetDate = ""
    private var lastTotalSteps = 0

    @RequiresApi(Build.VERSION_CODES.O)
    fun start() {
        loadLastResetDate()
        checkAndResetDaily()
        
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        } ?: run {
            onStepsChanged(0)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    fun isAvailable(): Boolean = stepSensor != null

    fun reset() {
        initialSteps = -1
        lastTotalSteps = 0
        sharedPref.edit().apply {
            remove("initial_steps")
            remove("last_reset_date")
            commit()
        }
    }

    private fun loadLastResetDate() {
        lastResetDate = sharedPref.getString("last_reset_date", "") ?: ""
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkAndResetDaily() {
        val today = LocalDate.now().toString()
        
        if (lastResetDate != today) {
            Log.d("StepCounter", "Novo dia detectado, resetando passos")
            initialSteps = -1
            lastTotalSteps = 0
            lastResetDate = today
            sharedPref.edit().apply {
                putString("last_reset_date", today)
                remove("initial_steps")
                commit()
            }
            onStepsChanged(0)
        } else {
            initialSteps = sharedPref.getInt("initial_steps", -1)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent) {
        val totalSteps = event.values[0].toInt()
        lastTotalSteps = totalSteps

        val today = LocalDate.now().toString()
        if (lastResetDate != today) {
            checkAndResetDaily()
            return
        }

        if (initialSteps == -1) {
            initialSteps = totalSteps
            sharedPref.edit().putInt("initial_steps", initialSteps).commit()
            Log.d("StepCounter", "Initial steps definido para: $initialSteps")
        }

        val stepsToday = totalSteps - initialSteps
        Log.d("StepCounter", "Total: $totalSteps, Initial: $initialSteps, Hoje: $stepsToday")
        onStepsChanged(stepsToday)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}