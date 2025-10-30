package com.example.fitmate.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitmate.ui.components.AppBottomBar
import com.example.fitmate.ui.components.AppTopBar
import com.example.fitmate.ui.navigation.AppNavGraph
import com.example.fitmate.ui.navigation.NavRoutes

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.HOME

    Scaffold(
        topBar = { AppTopBar(navController = navController) },
        bottomBar = {
            AppBottomBar(
                selectedItem = currentRoute,
                onItemSelected = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            innerPadding = innerPadding
        )
    }
}