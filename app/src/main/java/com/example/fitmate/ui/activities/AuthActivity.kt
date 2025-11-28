package com.example.fitmate.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fitmate.data.FirebaseRepository
import com.example.fitmate.ui.screens.LoginScreen
import com.example.fitmate.ui.screens.RegisterScreen
import com.example.fitmate.ui.theme.FitmateTheme

private const val ROUTE_LOGIN = "login"
private const val ROUTE_REGISTER = "register"

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseRepository.isUserLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContent {
            FitmateTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_LOGIN
                    ) {
                        composable(ROUTE_LOGIN) { LoginScreen(navController) }
                        composable(ROUTE_REGISTER) { RegisterScreen(navController) }
                    }
                }
            }
        }
    }
}