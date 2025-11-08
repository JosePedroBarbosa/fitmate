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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val exercises: List<Exercise>
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
                .padding(padding)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {

            // Title
            item {
                Text(
                    "Daily Workout",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Date
            item {
                val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd"))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.CalendarToday, null)
                        Spacer(Modifier.width(8.dp))
                        Text(today)
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
                item { WorkoutOverviewCard(workout) }
                items(workout.exercises) { ExerciseCard(it) }
            }
        }
    }

    // Muscle Selection Dialog
    if (showMuscleDialog) {
        AlertDialog(
            onDismissRequest = { showMuscleDialog = false },
            title = { Text("Choose Muscle Group") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    muscleGroups.forEach { muscle ->
                        TextButton(onClick = {

                            // Prevent new generation if workout already exists
                            if (dailyWorkout != null) {
                                println("⛔ Workout already exists — skipping regeneration")
                                showMuscleDialog = false
                                return@TextButton
                            }

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
                        }) {
                            Text(muscle.replace("_", " ").replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun generateWorkoutFromApi(muscle: String, fitness: String): DailyWorkout {

    println("🔥 FITNESS LEVEL RECEIVED: $fitness")

    val difficulty = when (fitness.lowercase()) {
        "beginner" -> "beginner"
        "intermediate" -> "intermediate"
        "expert" -> "expert"
        else -> "beginner"
    }

    println("🎯 Calling API → muscle='$muscle', difficulty='$difficulty'")

    val apiResponse: List<ApiExercise> = try {
        exercisesApi.getExercises(RetrofitHelper.API_KEY, muscle, difficulty)
            .also { println("✅ API returned ${it.size} results") }
    } catch (e: Exception) {
        println("❌ API ERROR: ${e.localizedMessage}")
        emptyList()
    }

    val exercises = apiResponse.take(6).map {
        Exercise(it.name, it.type, it.muscle, it.equipment, it.difficulty, it.instructions)
    }

    println("💪 Final workout contains ${exercises.size} exercises")

    return DailyWorkout(
        date = LocalDate.now(),
        title = "${muscle.replace("_", " ").uppercase()} Workout",
        description = if (exercises.isEmpty()) "No exercises found." else "Generated for your fitness level: $fitness",
        duration = if (exercises.isEmpty()) "0 min" else "45 min",
        exercises = exercises
    )
}


@Composable
fun GenerateWorkoutCard(onGenerateClick: () -> Unit) {
    Button(onClick = onGenerateClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Icon(Icons.Filled.AutoAwesome, null)
        Spacer(Modifier.width(8.dp))
        Text("Generate Workout")
    }
}

@Composable
fun GeneratingWorkoutCard() {
    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun WorkoutOverviewCard(workout: DailyWorkout) {
    Box(
        Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(GoogleBlue, GoogleBlueDark))).padding(24.dp)
    ) {
        Column {
            Text(workout.title, color = Color.White, fontWeight = FontWeight.Bold)
            Text(workout.description, color = Color.White.copy(0.8f))
            Text("Duration: ${workout.duration}", color = Color.White.copy(0.9f))
        }
    }
}

@Composable
fun ExerciseCard(exercise: Exercise) {
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text(exercise.name ?: "Unknown Exercise", fontWeight = FontWeight.Bold)
            Text("Type: ${exercise.type ?: "--"}")
            Text("Muscle: ${exercise.muscle ?: "--"}")
            Text("Equipment: ${exercise.equipment ?: "--"}")
            Text("Difficulty: ${exercise.difficulty ?: "--"}")

            if (!exercise.instructions.isNullOrBlank()) {
                Divider()
                Text(exercise.instructions!!)
            }
        }
    }
}
