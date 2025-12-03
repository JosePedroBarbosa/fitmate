package com.example.fitmate.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class StepCounterManager(
    context: Context,
    private val onStepsChanged: (Int) -> Unit
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var initialSteps = -1

    fun start() {
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
    }

    override fun onSensorChanged(event: SensorEvent) {
        val totalSteps = event.values[0].toInt()

        if (initialSteps == -1) {
            initialSteps = totalSteps
        }

        val stepsToday = totalSteps - initialSteps
        onStepsChanged(stepsToday)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
