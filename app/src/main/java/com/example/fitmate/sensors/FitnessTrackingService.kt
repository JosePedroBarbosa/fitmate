package com.example.fitmate.sensors

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.content.Context
import android.hardware.SensorManager
import android.util.Log

class FitnessTrackingService : Service() {

    private lateinit var sensorManager: SensorManager
    private var stepCounterManager: StepCounterManager? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("FitnessService", "Service criado")
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_STICKY

        when (action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTracking()
        }

        return START_STICKY
    }

    private fun startTracking() {
        Log.d("FitnessService", "Iniciando rastreamento de passos")

        stepCounterManager = StepCounterManager(this) { steps ->
            Log.d("FitnessService", "Passos atualizados: $steps")
            StepsLiveData.updateSteps(steps)
        }
        stepCounterManager?.start()
    }

    private fun stopTracking() {
        Log.d("FitnessService", "Parando rastreamento")
        stepCounterManager?.stop()
        StepsLiveData.reset()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d("FitnessService", "Service destruído")
        stepCounterManager?.stop()
    }

    companion object {
        const val ACTION_START = "com.example.fitmate.START_TRACKING"
        const val ACTION_STOP = "com.example.fitmate.STOP_TRACKING"
    }
}