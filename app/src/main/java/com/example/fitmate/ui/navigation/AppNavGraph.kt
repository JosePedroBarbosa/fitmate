package com.example.fitmate.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.fitmate.ui.screens.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(NavRoutes.HOME) { HomeScreen(navController) }
        composable(NavRoutes.WORKOUTS) { WorkoutsScreen() }
        composable(NavRoutes.CHALLENGES) { ChallengesScreen() }
        composable(NavRoutes.LEADERBOARD) { LeaderboardScreen() }
        composable(NavRoutes.PROFILE) { ProfileScreen() }
        composable(NavRoutes.GOAL) { GoalScreen() }
        composable(NavRoutes.GYMS) { FindGymScreen() }
        composable(NavRoutes.WORKOUT_HISTORY) { WorkoutHistoryScreen() }
    }
}