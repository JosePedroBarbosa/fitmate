package com.example.fitmate.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun AppBottomBar(
    selectedItem: String = "Home",
    onItemSelected: (String) -> Unit = {}
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedItem == "Home",
            onClick = { onItemSelected("Home") },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") }
        )
        NavigationBarItem(
            selected = selectedItem == "Workouts",
            onClick = { onItemSelected("Workouts") },
            icon = { Icon(Icons.Outlined.FitnessCenter, contentDescription = "Workouts") },
            label = { Text("Workouts") }
        )
        NavigationBarItem(
            selected = selectedItem == "Challenges",
            onClick = { onItemSelected("Challenges") },
            icon = { Icon(Icons.Outlined.EmojiEvents, contentDescription = "Challenges") },
            label = { Text("Challenges") }
        )
        NavigationBarItem(
            selected = selectedItem == "Leaderboard",
            onClick = { onItemSelected("Leaderboard") },
            icon = { Icon(Icons.Outlined.Leaderboard, contentDescription = "Leaderboard") },
            label = { Text("Leaderboard") }
        )
    }
}