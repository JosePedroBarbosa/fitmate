package com.example.fitmate.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.fitmate.ui.navigation.NavRoutes

@Composable
fun AppBottomBar(
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedItem == NavRoutes.HOME,
            onClick = { onItemSelected(NavRoutes.HOME) },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = selectedItem == NavRoutes.WORKOUTS,
            onClick = { onItemSelected(NavRoutes.WORKOUTS) },
            icon = { Icon(Icons.Outlined.FitnessCenter, contentDescription = "Workouts") },
            label = { Text("Workouts") }
        )
        NavigationBarItem(
            selected = selectedItem == NavRoutes.CHALLENGES,
            onClick = { onItemSelected(NavRoutes.CHALLENGES) },
            icon = { Icon(Icons.Outlined.EmojiEvents, contentDescription = "Challenges") },
            label = { Text("Challenges") }
        )
        NavigationBarItem(
            selected = selectedItem == NavRoutes.LEADERBOARD,
            onClick = { onItemSelected(NavRoutes.LEADERBOARD) },
            icon = { Icon(Icons.Outlined.Leaderboard, contentDescription = "Leaderboard") },
            label = { Text("Leaderboard") }
        )
    }
}