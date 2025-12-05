package com.example.fitmate.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource

import com.example.fitmate.data.FirebaseRepository
import com.example.fitmate.model.DailyWorkout
import com.example.fitmate.model.enums.WorkoutStatus
import com.example.fitmate.model.ApiExercise
import com.example.fitmate.ui.components.shimmerEffect
import java.time.format.DateTimeFormatter
import android.graphics.BitmapFactory
import androidx.compose.ui.platform.LocalContext
import java.io.File
import com.example.fitmate.R
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val GoogleBlue = Color(0xFF1A73E8)
private val GoogleBlueDark = Color(0xFF1557B0)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WorkoutHistoryScreen() {
    var isLoading by remember { mutableStateOf(true) }
    var workouts by remember { mutableStateOf<List<Pair<DailyWorkout, String>>>(emptyList()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        val appContext = context.applicationContext
        if (uid != null) {
            try {
                val cachedList = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.example.fitmate.data.local.DatabaseProvider.get(appContext).cachedWorkoutHistoryDao().getAll(uid)
                }
                if (cachedList.isNotEmpty()) {
                    val mapped = cachedList.map { e ->
                        val exercises = com.example.fitmate.data.local.util.Converters.toExercisesList(e.exercisesJson) ?: emptyList()
                        Pair(
                            com.example.fitmate.model.DailyWorkout(
                                date = e.date,
                                title = e.title,
                                description = e.description,
                                duration = e.duration,
                                exercises = exercises,
                                status = e.status,
                                photoPath = e.photoPath
                            ), e.id
                        )
                    }
                    workouts = mapped
                    isLoading = false
                }
            } catch (_: Exception) { }
        }

        FirebaseRepository.fetchAllWorkoutsForCurrentUser { list ->
            workouts = list
            isLoading = false
            if (uid != null) {
                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val entities = list.map { (w, id) ->
                            com.example.fitmate.data.local.entity.CachedWorkoutHistoryEntity(
                                id = id,
                                uid = uid,
                                date = w.date,
                                title = w.title,
                                description = w.description,
                                duration = w.duration,
                                exercisesJson = com.example.fitmate.data.local.util.Converters.fromExercisesList(w.exercises) ?: "[]",
                                status = w.status,
                                photoPath = w.photoPath
                            )
                        }
                        com.example.fitmate.data.local.DatabaseProvider.get(appContext).cachedWorkoutHistoryDao().upsertAll(entities)
                    } catch (_: Exception) { }
                }
            }
        }
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(id = R.string.drawer_workout_history),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        val completedCount = workouts.count { it.first.status == WorkoutStatus.COMPLETED }
                        Text(
                            text = stringResource(id = R.string.workouts_summary_format, completedCount, workouts.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isLoading) {
                items(4) { _ ->
                    HistoryCardShimmer()
                }
            } else if (workouts.isEmpty()) {
                item { EmptyHistoryState() }
            } else {
                items(workouts, key = { it.second }) { (workout, id) ->
                    WorkoutHistoryItem(workout = workout)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WorkoutHistoryItem(workout: DailyWorkout) {
    var expanded by remember { mutableStateOf(false) }
    val dateText = remember(workout.date) {
        workout.date.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = workout.status)
            }

            Text(
                text = workout.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (workout.description.isNotBlank()) {
                Text(
                    text = workout.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val photoPath = workout.photoPath
            if (!photoPath.isNullOrBlank()) {
                val file = File(photoPath)
                if (file.exists()) {
                    val bitmap = remember(photoPath) { BitmapFactory.decodeFile(photoPath) }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = workout.duration.ifBlank { stringResource(id = R.string.na) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(4.dp))

            if (workout.exercises.isNotEmpty()) {
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) stringResource(id = R.string.hide_details) else stringResource(id = R.string.view_details))
                    }
                }

            if (expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    workout.exercises.forEach { ex ->
                        ExerciseLine(ex)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: WorkoutStatus) {
    val (label, color) = when (status) {
        WorkoutStatus.STARTED -> stringResource(id = R.string.started_label) to GoogleBlue
        WorkoutStatus.COMPLETED -> stringResource(id = R.string.completed_label) to Color(0xFF4CAF50)
        WorkoutStatus.CANCELLED -> stringResource(id = R.string.cancelled_label) to Color(0xFFF44336)
    }

    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
private fun ExerciseLine(exercise: ApiExercise) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(GoogleBlue.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FitnessCenter,
                    contentDescription = null,
                    tint = GoogleBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name ?: stringResource(id = R.string.exercise_label),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                val meta = listOfNotNull(
                    exercise.muscle?.replace('_', ' '),
                    exercise.difficulty,
                    exercise.equipment
                ).joinToString(" • ")
                if (meta.isNotBlank()) {
                    Text(
                        text = meta,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCardShimmer() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp))
            .shimmerEffect(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
                Text(
                    text = stringResource(id = R.string.no_workouts_yet),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Start a workout to see your history here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
