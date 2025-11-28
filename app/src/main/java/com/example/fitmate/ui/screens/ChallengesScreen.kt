package com.example.fitmate.ui.screens

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
import com.example.fitmate.model.enums.ChallengeDifficulty

private val GoogleBlue = Color(0xFF1A73E8)
private val AccentGreen = Color(0xFF06D6A0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen() {
    val challenges = remember {
        listOf(
            Challenge(
                id = "1",
                title = "Full Body Blast",
                description = "Complete this intense full-body workout routine",
                rewardPoints = 150,
                difficulty = ChallengeDifficulty.MEDIUM,
                duration = "7 days",
                exerciseCount = 8,
                currentProgress = 42,
                isActive = true
            ),
            Challenge(
                id = "2",
                title = "Core Crusher",
                description = "Strengthen your core with this targeted workout",
                rewardPoints = 200,
                difficulty = ChallengeDifficulty.HARD,
                duration = "10 days",
                exerciseCount = 10,
                currentProgress = 65,
                isActive = true
            ),
            Challenge(
                id = "3",
                title = "Leg Day Legends",
                description = "Build powerful legs with this comprehensive routine",
                rewardPoints = 300,
                difficulty = ChallengeDifficulty.EXPERT,
                duration = "14 days",
                exerciseCount = 12,
                currentProgress = 0
            ),
            Challenge(
                id = "4",
                title = "Upper Body Power",
                description = "Develop strength in your chest, back, and arms",
                rewardPoints = 100,
                difficulty = ChallengeDifficulty.EASY,
                duration = "5 days",
                exerciseCount = 6,
                currentProgress = 0
            ),
            Challenge(
                id = "5",
                title = "HIIT Inferno",
                description = "High-intensity interval training for maximum results",
                rewardPoints = 120,
                difficulty = ChallengeDifficulty.EXPERT,
                duration = "7 days",
                exerciseCount = 8,
                currentProgress = 0
            ),
            Challenge(
                id = "6",
                title = "Beginner's Blueprint",
                description = "Perfect starting point for fitness newcomers",
                rewardPoints = 180,
                difficulty = ChallengeDifficulty.EASY,
                duration = "3 days",
                exerciseCount = 5,
                currentProgress = 0
            ),
            Challenge(
                id = "7",
                title = "Cardio Kickstart",
                description = "Get your heart pumping with this cardio-focused workout",
                rewardPoints = 250,
                difficulty = ChallengeDifficulty.MEDIUM,
                duration = "10 days",
                exerciseCount = 7,
                currentProgress = 0
            ),
            Challenge(
                id = "8",
                title = "Strength & Stamina",
                description = "Build both strength and endurance in one session",
                rewardPoints = 350,
                difficulty = ChallengeDifficulty.HARD,
                duration = "14 days",
                exerciseCount = 10,
                currentProgress = 0
            )
        )
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
            items(challenges) { challenge ->
                EnhancedChallengeCard(
                    challenge = challenge,
                    onAcceptClick = { /* TODO */ }
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun EnhancedChallengeCard(
    challenge: Challenge,
    onAcceptClick: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = challenge.currentProgress / challenge.totalProgress.toFloat(),
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
                            "${challenge.currentProgress}%",
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoChip(
                        icon = Icons.Outlined.Timer,
                        text = challenge.duration
                    )
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

                if (!challenge.isActive) {
                    Button(
                        onClick = onAcceptClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoogleBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Start")
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
