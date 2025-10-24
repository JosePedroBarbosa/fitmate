package com.example.fitmate.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.example.fitmate.ui.components.AppBottomBar
import com.example.fitmate.ui.components.AppTopBar
import com.example.fitmate.ui.navigation.AppNavGraph
import com.example.fitmate.ui.navigation.NavRoutes

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(NavRoutes.HOME) }

    Scaffold(
        topBar = { AppTopBar() },
        bottomBar = {
            AppBottomBar(
                selectedItem = selectedItem,
                onItemSelected = { route ->
                    selectedItem = route
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