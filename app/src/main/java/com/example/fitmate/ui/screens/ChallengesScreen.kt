package com.example.fitmate.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitmate.model.Challenge
import com.example.fitmate.model.UserChallenge
import com.example.fitmate.data.FirebaseRepository
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.fitmate.model.enums.ChallengeDifficulty
import com.example.fitmate.model.ApiExercise
import android.content.Intent

private val GoogleBlue = Color(0xFF1A73E8)
private val AccentGreen = Color(0xFF06D6A0)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen() {
    var challenge by remember { mutableStateOf<Challenge?>(null) }
    var userChallenge by remember { mutableStateOf<UserChallenge?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        FirebaseRepository.ensureTodayCommunityChallenge { _ ->
            FirebaseRepository.fetchTodayCommunityChallenge { ch ->
                challenge = ch
                isLoading = false
                if (ch != null) {
                    FirebaseRepository.fetchUserChallengeForCurrentUser(ch.id) { uc ->
                        userChallenge = uc
                    }
                }
            }
        }
    }

    Scaffold(
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Today's Challenge",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                    val todayText = remember { java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d")) }
                    Text(
                        text = "$todayText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                } else {
                    challenge?.let { ch ->
                        EnhancedChallengeCard(
                            challenge = ch,
                            userChallenge = userChallenge,
                            onStart = {
                                FirebaseRepository.startUserChallengeForCurrentUser(ch.id) { ok ->
                                    if (ok) {
                                        FirebaseRepository.fetchUserChallengeForCurrentUser(ch.id) { uc -> userChallenge = uc }
                                    }
                                }
                            },
                            onComplete = {
                                FirebaseRepository.markUserChallengeCompletedForCurrentUser(ch.id, ch.rewardPoints) { ok ->
                                    if (ok) {
                                        FirebaseRepository.fetchUserChallengeForCurrentUser(ch.id) { uc -> userChallenge = uc }
                                    }
                                }
                            }
                        )
                    } ?: run {
                        Text("No community challenge today", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedChallengeCard(
    challenge: Challenge,
    userChallenge: UserChallenge?,
    onStart: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(userChallenge?.isActive, userChallenge?.isCompleted) {
        expanded = userChallenge?.isActive == true && userChallenge.isCompleted == false
    }
    val progressValue = (userChallenge?.progress ?: 0).coerceIn(0, 100)
    val progress by animateFloatAsState(
        targetValue = progressValue / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GoogleBlue.copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FitnessCenter,
                                    contentDescription = null,
                                    tint = GoogleBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = challenge.difficulty.color.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = challenge.difficulty.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = challenge.difficulty.color,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = challenge.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = challenge.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (challenge.isActive) {
                    Surface(
                        shape = CircleShape,
                        color = AccentGreen.copy(alpha = 0.1f)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Active",
                            tint = AccentGreen,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(16.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = challenge.isActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Progress",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$progressValue%",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = GoogleBlue
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = GoogleBlue,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isActiveForUser = userChallenge?.isActive == true && userChallenge.isCompleted == false

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InfoChip(
                            icon = Icons.Outlined.FitnessCenter,
                            text = "${challenge.exerciseCount} exercises"
                        )
                        InfoChip(
                            icon = Icons.Filled.EmojiEvents,
                            text = "${challenge.rewardPoints}",
                            tint = Color(0xFFFFB800)
                        )
                    }

                    if (isActiveForUser) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, "Install FitMate and join today's challenge!")
                                }
                                context.startActivity(Intent.createChooser(intent, "Invite a friend"))
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("Invite Friend") }
                    }
                }

                if (userChallenge == null) {
                    Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Start") }
                } else if (isActiveForUser) {
                    val canComplete = userChallenge?.startedAt?.let { System.currentTimeMillis() - it >= 60 * 60 * 1000L } ?: false
                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        shape = RoundedCornerShape(12.dp),
                        enabled = canComplete
                    ) { Text("Complete") }
                } else {
                    Text(
                        "Completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentGreen
                    )
                }
            }

            if (userChallenge != null) {
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Hide details" else "View details") }
            }

            if (expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    challenge.workout.exercises.forEach { ex ->
                        ChallengeExerciseLine(ex)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: ImageVector,
    text: String,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = tint
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
                text = exercise.name ?: "Exercise",
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
