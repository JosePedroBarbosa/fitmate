package com.example.fitmate.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource

import com.example.fitmate.data.FirebaseRepository
import com.example.fitmate.data.RetrofitHelper
import com.example.fitmate.data.exercisesApi
import com.example.fitmate.model.ApiExercise
import com.example.fitmate.model.DailyWorkout
import com.example.fitmate.model.enums.WorkoutStatus
import com.example.fitmate.data.local.DatabaseProvider
import com.example.fitmate.data.local.entity.CachedWorkoutEntity
import com.example.fitmate.data.local.util.Converters
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import java.io.File

private val GoogleBlue = Color(0xFF1A73E8)
private val GoogleBlueDark = Color(0xFF1557B0)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WorkoutsScreen() {
    var dailyWorkout by remember { mutableStateOf<DailyWorkout?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var showMuscleDialog by remember { mutableStateOf(false) }
    var currentWorkoutId by remember { mutableStateOf<String?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val appContext = context.applicationContext

        if (uid != null) {
            try {
                val cached = withContext(Dispatchers.IO) {
                    DatabaseProvider.get(appContext).cachedWorkoutDao().getWorkout(uid)
                }
                if (cached != null) {
                    val exercises = Converters.toExercisesList(cached.exercisesJson) ?: emptyList()
                    dailyWorkout = DailyWorkout(
                        date = cached.date,
                        title = cached.title,
                        description = cached.description,
                        duration = cached.duration,
                        exercises = exercises,
                        status = cached.status,
                        photoPath = null
                    )
                    currentWorkoutId = cached.workoutId
                }
            } catch (_: Exception) { }
        }

        FirebaseRepository.fetchCurrentStartedWorkout { workout, id ->
            if (workout != null) {
                dailyWorkout = workout
                currentWorkoutId = id
                if (uid != null) {
                    scope.launch(Dispatchers.IO) {
                        val json = Converters.fromExercisesList(workout.exercises) ?: "[]"
                        DatabaseProvider.get(appContext).cachedWorkoutDao().upsert(
                            CachedWorkoutEntity(
                                uid = uid,
                                workoutId = id,
                                date = workout.date,
                                title = workout.title,
                                description = workout.description,
                                duration = workout.duration,
                                exercisesJson = json,
                                status = workout.status
                            )
                        )
                    }
                }
            }
        }
    }

    val muscleGroups = remember {
        listOf(
            "abdominals", "abductors", "adductors", "biceps", "calves", "chest",
            "forearms", "glutes", "hamstrings", "lats", "lower_back", "middle_back",
            "neck", "quadriceps", "traps", "triceps"
        )
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp, vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
        ) {

            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(id = com.example.fitmate.R.string.workouts_todays_workout),
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

            if (dailyWorkout == null && !isGenerating) {
                item {
                    GenerateWorkoutCard(onGenerateClick = {
                        showMuscleDialog = true
                    })
                }
            }

            if (isGenerating) {
                item {
                    GeneratingWorkoutCard()
                }
            }

            dailyWorkout?.let { workout ->
                item {
                    WorkoutOverviewCard(
                        workout = workout,
                        onDeleteClick = {
                            dailyWorkout = null
                        }
                    )
                }

                item {
                    if (workout.status != WorkoutStatus.STARTED) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                onClick = {
                                    val startedWorkout = workout.copy(status = WorkoutStatus.STARTED)
                                    FirebaseRepository.saveWorkoutForCurrentUser(startedWorkout) { id ->
                                        currentWorkoutId = id
                                        dailyWorkout = startedWorkout
                                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                                        val appContext = context.applicationContext
                                        if (uid != null) {
                                            scope.launch(Dispatchers.IO) {
                                                val json = Converters.fromExercisesList(startedWorkout.exercises) ?: "[]"
                                                DatabaseProvider.get(appContext).cachedWorkoutDao().upsert(
                                                    CachedWorkoutEntity(
                                                        uid = uid,
                                                        workoutId = id,
                                                        date = startedWorkout.date,
                                                        title = startedWorkout.title,
                                                        description = startedWorkout.description,
                                                        duration = startedWorkout.duration,
                                                        exercisesJson = json,
                                                        status = startedWorkout.status
                                                    )
                                                )
                                            }
                                        }
                                    }
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
                                            stringResource(id = com.example.fitmate.R.string.workouts_start_workout),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }

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
                                        stringResource(id = com.example.fitmate.R.string.workouts_generate_new),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                onClick = {
                                    showPhotoDialog = true
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
                                        stringResource(id = com.example.fitmate.R.string.workouts_complete),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }

                            Surface(
                                onClick = {
                                    currentWorkoutId?.let { id ->
                                        FirebaseRepository.updateWorkoutStatusForCurrentUser(id, WorkoutStatus.CANCELLED) { }
                                    }
                                    dailyWorkout = null
                                    currentWorkoutId = null
                                    val uid = FirebaseAuth.getInstance().currentUser?.uid
                                    val appContext = context.applicationContext
                                    if (uid != null) {
                                        scope.launch(Dispatchers.IO) {
                                            try {
                                                DatabaseProvider.get(appContext).cachedWorkoutDao().delete(uid)
                                            } catch (_: Exception) { }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFF44336)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Cancel,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        stringResource(id = com.example.fitmate.R.string.cancel),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        stringResource(id = com.example.fitmate.R.string.workouts_exercises_count, workout.exercises.size),
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

    val workoutIdForDialog = currentWorkoutId
    if (showPhotoDialog && workoutIdForDialog != null) {
        PhotoCaptureDialog(
            workoutId = workoutIdForDialog,
            onCompleted = {
                FirebaseRepository.updateWorkoutStatusForCurrentUser(workoutIdForDialog, WorkoutStatus.COMPLETED) { }
                dailyWorkout = null
                currentWorkoutId = null
                showPhotoDialog = false
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                val appContext = context.applicationContext
                if (uid != null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            DatabaseProvider.get(appContext).cachedWorkoutDao().delete(uid)
                        } catch (_: Exception) { }
                    }
                }
            },
            onDismiss = { showPhotoDialog = false }
        )
    }

    if (showMuscleDialog) {
        AlertDialog(
            onDismissRequest = { showMuscleDialog = false },
            title = {
                Text(
                    stringResource(id = com.example.fitmate.R.string.workouts_choose_muscle_group),
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
                    Text(stringResource(id = com.example.fitmate.R.string.cancel))
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
        val response = exercisesApi.getExercises(RetrofitHelper.API_KEY, muscle, difficulty)
        response
    } catch (e: Exception) {
        emptyList()
    }

    val exercises = apiResponse.take(6)

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
        status = WorkoutStatus.CANCELLED
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
                        stringResource(id = com.example.fitmate.R.string.workouts_generate_workout_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        stringResource(id = com.example.fitmate.R.string.workouts_generate_workout_subtitle),
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
                    stringResource(id = com.example.fitmate.R.string.workouts_generating_progress),
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
fun ExerciseCard(exercise: ApiExercise) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    exercise.name ?: stringResource(id = com.example.fitmate.R.string.workouts_unknown_exercise),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )

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
                        exercise.difficulty ?: stringResource(id = com.example.fitmate.R.string.na),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExerciseDetail(
                    icon = Icons.Outlined.FitnessCenter,
                    text = exercise.muscle?.replace("_", " ")?.replaceFirstChar { it.uppercase() } ?: stringResource(id = com.example.fitmate.R.string.na)
                )
                ExerciseDetail(
                    icon = Icons.Outlined.Construction,
                    text = exercise.equipment ?: stringResource(id = com.example.fitmate.R.string.na)
                )
            }

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
                            stringResource(id = com.example.fitmate.R.string.workouts_instructions_label),
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

@Composable
fun PhotoCaptureDialog(
    workoutId: String,
    onCompleted: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by rememberSaveable { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        launcher.launch(android.Manifest.permission.CAMERA)
    }

    Dialog(onDismissRequest = { if (!isUploading) onDismiss() }) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (hasPermission) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = androidx.camera.view.PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val capture = ImageCapture.Builder().setTargetRotation(previewView.display.rotation).build()
                            imageCapture = capture
                            val selector = CameraSelector.DEFAULT_BACK_CAMERA
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = { if (!isUploading) onDismiss() },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(id = com.example.fitmate.R.string.cancel))
                    }
                }

                Surface(
                    onClick = {
                        val capture = imageCapture ?: return@Surface
                        val file = File(context.filesDir, "workout_photo_${System.currentTimeMillis()}.jpg")
                        val output = ImageCapture.OutputFileOptions.Builder(file).build()
                        isUploading = true
                        capture.takePicture(output, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                FirebaseRepository.saveWorkoutPhotoPathForCurrentUser(workoutId, file.absolutePath) { _ ->
                                    onCompleted()
                                    isUploading = false
                                }
                            }
                            override fun onError(exception: ImageCaptureException) {
                                isUploading = false
                            }
                        })
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = GoogleBlue
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(id = com.example.fitmate.R.string.workouts_saving_ellipsis), color = Color.White)
                        } else {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(id = com.example.fitmate.R.string.workouts_capture_and_save), color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
