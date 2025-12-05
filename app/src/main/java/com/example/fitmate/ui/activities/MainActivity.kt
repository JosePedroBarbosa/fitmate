package com.example.fitmate.ui.activities

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.fitmate.ui.screens.MainScreen
import com.example.fitmate.ui.theme.FitmateTheme
import com.example.fitmate.notifications.NotificationHelper
import com.example.fitmate.notifications.ReminderWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applySavedLocale()

        enableEdgeToEdge()

        NotificationHelper.createChannels(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }

        val work = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("daily_reminder", ExistingPeriodicWorkPolicy.KEEP, work)

        setContent {
            FitmateTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen()
                }
            }
        }
    }

    private fun applySavedLocale() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val code = prefs.getString("language_code", null)
        if (!code.isNullOrBlank()) {
            val locale = java.util.Locale(code)
            java.util.Locale.setDefault(locale)
            val res = resources
            val config = res.configuration
            config.setLocale(locale)
            res.updateConfiguration(config, res.displayMetrics)
        }
    }
}
