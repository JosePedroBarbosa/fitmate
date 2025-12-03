package com.example.fitmate.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.fitmate.ui.navigation.NavRoutes
import androidx.compose.ui.res.stringResource

private val GoogleBlue = Color(0xFF1557B0)

@Composable
private fun navItemColors(): NavigationBarItemColors = NavigationBarItemDefaults.colors(
    selectedIconColor = GoogleBlue,
    selectedTextColor = GoogleBlue,
    indicatorColor = GoogleBlue.copy(alpha = 0.12f),
    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
)

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
            icon = { Icon(Icons.Filled.Home, contentDescription = stringResource(id = com.example.fitmate.R.string.cd_home)) },
            label = { Text(stringResource(id = com.example.fitmate.R.string.bottom_home)) },
            colors = navItemColors()
        )
        NavigationBarItem(
            selected = selectedItem == NavRoutes.WORKOUTS,
            onClick = { onItemSelected(NavRoutes.WORKOUTS) },
            icon = { Icon(Icons.Outlined.FitnessCenter, contentDescription = stringResource(id = com.example.fitmate.R.string.cd_workouts)) },
            label = { Text(stringResource(id = com.example.fitmate.R.string.bottom_workouts)) },
            colors = navItemColors()
        )
        NavigationBarItem(
            selected = selectedItem == NavRoutes.CHALLENGES,
            onClick = { onItemSelected(NavRoutes.CHALLENGES) },
            icon = { Icon(Icons.Outlined.EmojiEvents, contentDescription = stringResource(id = com.example.fitmate.R.string.cd_challenges)) },
            label = { Text(stringResource(id = com.example.fitmate.R.string.bottom_challenges)) },
            colors = navItemColors()
        )
        NavigationBarItem(
            selected = selectedItem == NavRoutes.LEADERBOARD,
            onClick = { onItemSelected(NavRoutes.LEADERBOARD) },
            icon = { Icon(Icons.Outlined.Leaderboard, contentDescription = stringResource(id = com.example.fitmate.R.string.cd_leaderboard)) },
            label = { Text(stringResource(id = com.example.fitmate.R.string.bottom_leaderboard)) },
            colors = navItemColors()
        )
    }
}
