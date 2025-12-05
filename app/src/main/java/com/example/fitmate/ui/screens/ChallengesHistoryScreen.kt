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
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.fitmate.R
import com.example.fitmate.data.FirebaseRepository
import com.example.fitmate.model.ApiExercise
import com.example.fitmate.model.Challenge
import com.example.fitmate.model.UserChallenge
import com.example.fitmate.ui.components.shimmerEffect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val GoogleBlue = Color(0xFF1A73E8)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChallengesHistoryScreen() {
    var isLoading by remember { mutableStateOf(true) }
    var itemsState by remember { mutableStateOf<List<Pair<UserChallenge, Challenge?>>>(emptyList()) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val appCtx = context.applicationContext
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                val cached = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.example.fitmate.data.local.DatabaseProvider.get(appCtx).cachedUserChallengeDao().getAll(uid)
                }
                if (cached.isNotEmpty()) {
                    val mapped = cached.map { e ->
                        val exercises = com.example.fitmate.data.local.util.Converters.toExercisesList(e.workoutExercisesJson) ?: emptyList()
                        val ch = com.example.fitmate.model.Challenge(
                            id = e.challengeId,
                            title = e.title,
                            description = e.description,
                            rewardPoints = e.rewardPoints,
                            difficulty = try { com.example.fitmate.model.enums.ChallengeDifficulty.valueOf(e.difficulty) } catch (_: Exception) { com.example.fitmate.model.enums.ChallengeDifficulty.MEDIUM },
                            exerciseCount = e.exerciseCount,
                            isActive = e.isActive,
                            workout = com.example.fitmate.model.DailyWorkout(
                                date = e.workoutDate,
                                title = e.workoutTitle,
                                description = e.workoutDescription,
                                duration = e.workoutDuration,
                                exercises = exercises,
                                status = e.workoutStatus,
                                photoPath = e.workoutPhotoPath
                            )
                        )
                        val uc = com.example.fitmate.model.UserChallenge(
                            userId = e.userId,
                            challengeId = e.challengeId,
                            progress = e.progress,
                            isCompleted = e.isCompleted,
                            isActive = e.isActive,
                            startedAt = e.startedAt
                        )
                        uc to ch
                    }
                    itemsState = mapped
                    isLoading = false
                }
            }
        } catch (_: Exception) { }

        FirebaseRepository.fetchAllUserChallengesForCurrentUser { list ->
            itemsState = list
            isLoading = false
            val appCtx = context.applicationContext
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val entities = list.mapNotNull { (uc, ch) ->
                            ch?.let {
                                com.example.fitmate.data.local.entity.CachedUserChallengeEntity(
                                    userId = uc.userId,
                                    challengeId = uc.challengeId,
                                    progress = uc.progress,
                                    isCompleted = uc.isCompleted,
                                    isActive = uc.isActive,
                                    startedAt = uc.startedAt,
                                    title = it.title,
                                    description = it.description,
                                    rewardPoints = it.rewardPoints,
                                    difficulty = it.difficulty.name,
                                    exerciseCount = it.exerciseCount,
                                    workoutDate = it.workout.date,
                                    workoutTitle = it.workout.title,
                                    workoutDescription = it.workout.description,
                                    workoutDuration = it.workout.duration,
                                    workoutExercisesJson = com.example.fitmate.data.local.util.Converters.fromExercisesList(it.workout.exercises) ?: "[]",
                                    workoutStatus = it.workout.status,
                                    workoutPhotoPath = it.workout.photoPath
                                )
                            }
                        }
                        com.example.fitmate.data.local.DatabaseProvider.get(appCtx).cachedUserChallengeDao().upsertAll(entities)
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
                        text = stringResource(id = R.string.drawer_challenges_history),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        val completedCount = itemsState.count { it.first.isCompleted }
                        Text(
                            text = stringResource(id = R.string.workouts_summary_format, completedCount, itemsState.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isLoading) {
                items(4) { _ ->
                    ChallengeHistoryCardShimmer()
                }
            } else if (itemsState.isEmpty()) {
                item { EmptyChallengesState() }
            } else {
                items(itemsState, key = { it.first.challengeId }) { (uc, ch) ->
                    ChallengeHistoryItem(userChallenge = uc, challenge = ch)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ChallengeHistoryItem(userChallenge: UserChallenge, challenge: Challenge?) {
    var expanded by remember { mutableStateOf(false) }
    val dateText = remember(challenge?.workout?.date, challenge?.id) {
        val d = challenge?.workout?.date ?: run {
            try {
                LocalDate.parse(challenge?.id ?: LocalDate.now().toString())
            } catch (_: Exception) {
                LocalDate.now()
            }
        }
        d.format(DateTimeFormatter.ofPattern("EEE, MMM d"))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                ChallengeStatusBadge(isActive = userChallenge.isActive, isCompleted = userChallenge.isCompleted)
            }

            Text(
                text = challenge?.title ?: stringResource(id = R.string.challenge_label),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            val desc = challenge?.description.orEmpty()
            if (desc.isNotBlank()) {
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                val exCount = challenge?.exerciseCount ?: 0
                Text(
                    text = if (exCount > 0) stringResource(id = R.string.challenges_exercises_count, exCount) else stringResource(id = R.string.na),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Outlined.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                val points = challenge?.rewardPoints ?: 0
                Text(
                    text = if (points > 0) "+" + stringResource(id = R.string.leaderboard_points_short_format, points) else stringResource(id = R.string.na),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LinearProgressIndicator(
                    progress = { (userChallenge.progress.coerceIn(0, 100)) / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = GoogleBlue,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text(
                    text = "${userChallenge.progress.coerceIn(0, 100)}%",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = GoogleBlue
                )
            }

            if ((challenge?.workout?.exercises?.isNotEmpty() == true)) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) stringResource(id = R.string.hide_details) else stringResource(id = R.string.view_details))
                }
            }

            if (expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    challenge?.workout?.exercises?.forEach { ex ->
                        ChallengeExerciseLine(ex)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeStatusBadge(isActive: Boolean, isCompleted: Boolean) {
    val (label, color) = when {
        isCompleted -> stringResource(id = R.string.completed_label) to Color(0xFF4CAF50)
        isActive -> stringResource(id = R.string.started_label) to GoogleBlue
        else -> stringResource(id = R.string.inactive_label) to Color(0xFFF44336)
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
private fun ChallengeExerciseLine(exercise: ApiExercise) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
            val instr = exercise.instructions?.trim().orEmpty()
            if (instr.isNotBlank()) {
                Text(
                    text = instr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChallengeHistoryCardShimmer() {
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
private fun EmptyChallengesState() {
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
                    imageVector = Icons.Outlined.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(id = R.string.no_challenges_yet),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Start a challenge to see your history here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

