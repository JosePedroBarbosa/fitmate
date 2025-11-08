package com.example.fitmate.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitmate.data.FirebaseRepository
import com.example.fitmate.data.RetrofitHelper
import com.example.fitmate.data.ApiExercise
import com.example.fitmate.data.exercisesApi
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val GoogleBlue = Color(0xFF1A73E8)
private val GoogleBlueDark = Color(0xFF1557B0)
private val LightBlue = Color(0xFFE8F0FE)

// UI MODEL
data class Exercise(
    val name: String?,
    val type: String?,
    val muscle: String?,
    val equipment: String?,
    val difficulty: String?,
    val instructions: String?
)

data class DailyWorkout(
    val date: LocalDate,
    val title: String,
    val description: String,
    val duration: String,
    val exercises: List<Exercise>,
    val isStarted: Boolean = false
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WorkoutsScreen(navController: NavController) {
    var dailyWorkout by remember { mutableStateOf<DailyWorkout?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var showMuscleDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val muscleGroups = listOf(
        "abdominals", "abductors", "adductors", "biceps", "calves", "chest",
        "forearms", "glutes", "hamstrings", "lats", "lower_back", "middle_back",
        "neck", "quadriceps", "traps", "triceps"
    )

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp, vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
        ) {

            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Today's Workout",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd"))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            today,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Show Generate button (if no workout yet)
            if (dailyWorkout == null && !isGenerating) {
                item {
                    GenerateWorkoutCard(onGenerateClick = {
                        println("▶️ User clicked Generate Workout")
                        showMuscleDialog = true
                    })
                }
            }

            // Loading State
            if (isGenerating) {
                item {
                    GeneratingWorkoutCard()
                }
            }

            // Show Workout
            dailyWorkout?.let { workout ->
                item {
                    WorkoutOverviewCard(
                        workout = workout,
                        onDeleteClick = {
                            dailyWorkout = null
                        }
                    )
                }

                // Action Buttons - Different based on workout state
                item {
                    if (!workout.isStarted) {
                        // Before Starting: Show "Start" and "Generate New"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Start Workout Button
                            Surface(
                                onClick = {
                                    // TODO: Register workout start in Firebase
                                    println("🚀 Workout started!")
                                    dailyWorkout = workout.copy(isStarted = true)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .shadow(3.dp, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(GoogleBlue, GoogleBlueDark)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            "Start Workout",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            // Generate New Button
                            Surface(
                                onClick = {
                                    showMuscleDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Refresh,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Generate New",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        // After Starting: Show "Complete" and "Generate New"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Complete Workout Button
                            Surface(
                                onClick = {
                                    // TODO: Mark workout as completed and save to Firebase
                                    println("✅ Workout completed!")
                                    dailyWorkout = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .shadow(3.dp, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF4CAF50)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Complete",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }

                            // Generate New Button
                            Surface(
                                onClick = {
                                    showMuscleDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Refresh,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Generate New",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Exercises (${workout.exercises.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(workout.exercises) { exercise ->
                    ExerciseCard(exercise)
                }
            }
        }
    }

    // Muscle Selection Dialog
    if (showMuscleDialog) {
        AlertDialog(
            onDismissRequest = { showMuscleDialog = false },
            title = {
                Text(
                    "Choose Muscle Group",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(muscleGroups) { muscle ->
                        Surface(
                            onClick = {
                                println("✅ Generating workout for muscle: $muscle")
                                showMuscleDialog = false
                                isGenerating = true

                                FirebaseRepository.fetchUserProfile { user ->
                                    val fitnessLevel = user?.fitnessLevel?.label?.lowercase() ?: "beginner"
                                    scope.launch {
                                        dailyWorkout = generateWorkoutFromApi(muscle, fitnessLevel)
                                        isGenerating = false
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    muscle.replace("_", " ").replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMuscleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun generateWorkoutFromApi(muscle: String, fitness: String): DailyWorkout {

    val difficulty = when (fitness.lowercase()) {
        "beginner" -> "beginner"
        "intermediate" -> "intermediate"
        "expert" -> "expert"
        else -> "beginner"
    }

    val apiResponse: List<ApiExercise> = try {
        exercisesApi.getExercises(RetrofitHelper.API_KEY, muscle, difficulty)
    } catch (e: Exception) {
        emptyList()
    }

    val exercises = apiResponse.take(6).map {
        Exercise(
            name = it.name?.replace("_", " ")?.replaceFirstChar { c -> c.uppercase() },
            type = it.type?.replace("_", " ")?.replaceFirstChar { c -> c.uppercase() },
            muscle = it.muscle?.replace("_", " ")?.replaceFirstChar { c -> c.uppercase() },
            equipment = it.equipment?.replace("_", " ")?.replaceFirstChar { c -> c.uppercase() },
            difficulty = it.difficulty?.replaceFirstChar { c -> c.uppercase() },
            instructions = it.instructions
        )
    }

    val formattedFitness = fitness.replaceFirstChar { it.uppercase() }

    return DailyWorkout(
        date = LocalDate.now(),
        title = "${muscle.replace("_", " ").replaceFirstChar { it.uppercase() }} Workout",
        description = if (exercises.isEmpty())
            "No exercises found."
        else
            "Generated for your fitness level: $formattedFitness",
        duration = if (exercises.isEmpty()) "0 min" else "${exercises.size * 8} min",
        exercises = exercises,
        isStarted = false
    )
}

@Composable
fun GenerateWorkoutCard(onGenerateClick: () -> Unit) {
    Surface(
        onClick = onGenerateClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(GoogleBlue, GoogleBlueDark)
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Generate Workout",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        "Tap to create your personalized workout",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun GeneratingWorkoutCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = GoogleBlue,
                    strokeWidth = 3.dp
                )
                Text(
                    "Generating your workout...",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
fun WorkoutOverviewCard(
    workout: DailyWorkout,
    onDeleteClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(GoogleBlue, GoogleBlueDark)
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Filled.FitnessCenter,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column {
                    Text(
                        workout.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        workout.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseCard(exercise: Exercise) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Exercise Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    exercise.name ?: "Unknown Exercise",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Difficulty Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (exercise.difficulty?.lowercase()) {
                        "beginner" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                        "intermediate" -> Color(0xFFFFA726).copy(alpha = 0.15f)
                        "expert" -> Color(0xFFEF5350).copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        exercise.difficulty ?: "N/A",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = when (exercise.difficulty?.lowercase()) {
                                "beginner" -> Color(0xFF2E7D32)
                                "intermediate" -> Color(0xFFF57C00)
                                "expert" -> Color(0xFFC62828)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExerciseDetail(
                    icon = Icons.Outlined.FitnessCenter,
                    text = exercise.muscle?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: "N/A"
                )
                ExerciseDetail(
                    icon = Icons.Outlined.Construction,
                    text = exercise.equipment ?: "N/A"
                )
            }

            // Instructions
            if (!exercise.instructions.isNullOrBlank()) {
                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = GoogleBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Instructions",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = GoogleBlue
                            )
                        )
                    }
                    Text(
                        exercise.instructions!!,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseDetail(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}