package com.example.fitmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitmate.data.FirebaseRepository
import com.example.fitmate.model.Goal
import com.example.fitmate.model.UserProfile
import com.example.fitmate.model.DailyWorkout
import com.example.fitmate.ui.navigation.NavRoutes
import com.example.fitmate.sensors.StepCounterManager
import com.example.fitmate.ui.components.shimmerEffect
import com.example.fitmate.ui.components.*

private val GoogleBlue = Color(0xFF1A73E8)
private val GoogleBlueDark = Color(0xFF1557B0)

@Composable
fun HomeScreen(navController: NavController) {
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoadingUser by remember { mutableStateOf(true) }

    val context = LocalContext.current
    var stepsToday by remember { mutableStateOf(0) }

    val stepCounterManager = remember {
        StepCounterManager(context) { steps ->
            stepsToday = steps
        }
    }

    DisposableEffect(Unit) {
        stepCounterManager.start()

        onDispose {
            stepCounterManager.stop()
        }
    }

    LaunchedEffect(Unit) {
        FirebaseRepository.fetchUserProfile { profile ->
            userProfile = profile
            isLoadingUser = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            if (isLoadingUser) {
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(32.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .shimmerEffect()
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(250.dp)
                        .height(24.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(6.dp)
                        )
                        .shimmerEffect()
                )
            } else {
                val firstName = userProfile?.name?.split(" ")?.firstOrNull() ?: "User"
                Text(
                    "Welcome, $firstName! 👋",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val isTablet = maxWidth > 600.dp

            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.Default.DirectionsRun,
                        value = stepsToday.toString(),
                        unit = "steps",
                        label = "Steps Today",
                        gradient = Brush.linearGradient(
                            colors = listOf(GoogleBlue, GoogleBlueDark)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    GoalProgressCard(
                        modifier = Modifier.weight(1f),
                        onNavigateToGoal = {
                            navController.navigate("goal") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    QuickWorkoutCard(modifier = Modifier.weight(1f), onNavigateToWorkouts = {
                        navController.navigate(NavRoutes.WORKOUTS) {
                            popUpTo(NavRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    })
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            icon = Icons.Default.DirectionsRun,
                            value = stepsToday.toString(),
                            unit = "",
                            label = "Steps",
                            gradient = Brush.linearGradient(
                                colors = listOf(GoogleBlue, GoogleBlueDark)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        GoalProgressCard(
                            modifier = Modifier.weight(1f),
                            onNavigateToGoal = {
                                navController.navigate("goal") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    QuickWorkoutCard(modifier = Modifier.fillMaxWidth(), onNavigateToWorkouts = {
                        navController.navigate(NavRoutes.WORKOUTS) {
                            popUpTo(NavRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    })
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Challenges",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                TextButton(onClick = { }) {
                    Text("View all")
                }
            }

            Spacer(Modifier.height(12.dp))

            ChallengeCard(
                title = "7-Day Active Challenge",
                description = "Stay active for 7 consecutive days",
                rewardPoints = 150,
                onAcceptClick = { }
            )

            Spacer(Modifier.height(12.dp))

            ChallengeCard(
                title = "Burn 3000 Calories",
                description = "Burn 3000 calories this week",
                rewardPoints = 150,
                onAcceptClick = { }
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun GoalProgressCard(
    modifier: Modifier = Modifier,
    onNavigateToGoal: (() -> Unit)? = null
) {
    var goal by remember { mutableStateOf<Goal?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseRepository.fetchUserGoal { fetchedGoal ->
            goal = fetchedGoal
            isLoading = false
        }
    }

    Surface(
        modifier = modifier
            .height(140.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable(enabled = !isLoading) { onNavigateToGoal?.invoke() },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(GoogleBlue, GoogleBlueDark)
                    )
                )
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .shimmerEffect()
                    )
                }

                goal == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Set your goal",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Tap to create one",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                else -> {
                    val progress = goal!!.progress.coerceIn(0, 100)
                    val typeLabel = goal!!.type.label

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.TrackChanges,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(26.dp)
                            )
                            Text(
                                "$progress%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        Column {
                            Text(
                                "Personal Goal",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.95f),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(alpha = 0.25f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress / 100f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.White)
                                )
                            }

                            Spacer(Modifier.height(6.dp))
                            Text(
                                typeLabel,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickWorkoutCard(
    modifier: Modifier = Modifier,
    onNavigateToWorkouts: (() -> Unit)? = null
) {
    var currentWorkout by remember { mutableStateOf<DailyWorkout?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseRepository.fetchCurrentStartedWorkout { workout, _ ->
            currentWorkout = workout
            isLoading = false
        }
    }

    Surface(
        onClick = { onNavigateToWorkouts?.invoke() },
        modifier = modifier
            .height(120.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(GoogleBlue, GoogleBlueDark)
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            "Today's Workout",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        when {
                            isLoading -> {
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(18.dp)
                                        .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .shimmerEffect()
                                )
                            }
                            currentWorkout != null -> {
                                Text(
                                    currentWorkout!!.title.ifBlank { "Unnamed Workout" },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                )
                            }
                            else -> {
                                Text(
                                    "No workout started",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        val durationText = when {
                            isLoading -> "..."
                            currentWorkout != null -> currentWorkout!!.duration.ifBlank { "—" }
                            else -> "—"
                        }
                        Text(
                            durationText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }

                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(36.dp)
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                                    .shimmerEffect()
                            )
                        }
                        currentWorkout == null -> {
                            Button(
                                onClick = { onNavigateToWorkouts?.invoke() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = GoogleBlue
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Create workout")
                            }
                        }
                        else -> {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = "Open",
                                        tint = GoogleBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
