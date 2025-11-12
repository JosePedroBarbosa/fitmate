package com.example.fitmate.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitmate.data.FirebaseRepository
import com.example.fitmate.model.*
import androidx.compose.foundation.text.KeyboardOptions
import com.example.fitmate.model.enums.GoalType

private val GoogleBlue = Color(0xFF1A73E8)
private val GoogleBlueDark = Color(0xFF1557B0)
private val LightBlue = Color(0xFFE8F0FE)

@Composable
fun GoalScreen() {
    var isLoading by remember { mutableStateOf(true) }
    var currentGoal by remember { mutableStateOf<Goal?>(null) }

    var selectedGoalType by remember { mutableStateOf<GoalType?>(null) }
    var initialValue by remember { mutableStateOf("") }
    var targetValue by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        FirebaseRepository.fetchUserGoal { goal ->
            currentGoal = goal
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                color = GoogleBlue,
                strokeWidth = 3.dp
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        if (currentGoal != null) {
            CurrentGoalCard(
                goal = currentGoal!!,
                onProgressUpdated = { updated ->
                    currentGoal = updated
                }
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GoalStatCard(
                    icon = Icons.Filled.TrendingUp,
                    value = when (currentGoal) {
                        is WeightLossGoal -> "${(currentGoal as WeightLossGoal).initialWeight}kg"
                        is MuscleGainGoal -> "${(currentGoal as MuscleGainGoal).initialMuscleMassPercent}%"
                        else -> "0"
                    },
                    label = "Initial",
                    gradient = Brush.linearGradient(
                        colors = listOf(GoogleBlue, GoogleBlueDark)
                    ),
                    modifier = Modifier.weight(1f)
                )

                GoalStatCard(
                    icon = Icons.Filled.Flag,
                    value = when (currentGoal) {
                        is WeightLossGoal -> "${(currentGoal as WeightLossGoal).targetWeight}kg"
                        is MuscleGainGoal -> "${(currentGoal as MuscleGainGoal).targetMuscleMassPercent}%"
                        else -> "0"
                    },
                    label = "Target",
                    gradient = Brush.linearGradient(
                        colors = listOf(GoogleBlueDark, GoogleBlue)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

        } else {
            CreateGoalSection(
                selectedGoalType = selectedGoalType,
                initialValue = initialValue,
                targetValue = targetValue,
                showSuccess = showSuccess,
                onGoalTypeSelected = { selectedGoalType = it },
                onInitialValueChange = { if (it.isEmpty() || it.all(Char::isDigit)) initialValue = it },
                onTargetValueChange = { if (it.isEmpty() || it.all(Char::isDigit)) targetValue = it },
                onSaveClick = {
                    val newGoal: Goal = when (selectedGoalType) {
                        GoalType.WEIGHT_LOSS -> WeightLossGoal(
                            initialWeight = initialValue.toDouble(),
                            currentWeight = initialValue.toDouble(),
                            targetWeight = targetValue.toDouble()
                        )
                        GoalType.MUSCLE_GAIN -> MuscleGainGoal(
                            initialMuscleMassPercent = initialValue.toDouble(),
                            currentMuscleMassPercent = initialValue.toDouble(),
                            targetMuscleMassPercent = targetValue.toDouble()
                        )
                        else -> return@CreateGoalSection
                    }

                    FirebaseRepository.updateUserGoal(newGoal) { success ->
                        if (success) {
                            showSuccess = true
                            currentGoal = newGoal
                        }
                    }
                }
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun CurrentGoalCard(
    goal: Goal,
    onProgressUpdated: (Goal?) -> Unit
) {
    var isUpdating by remember { mutableStateOf(false) }
    var newValue by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(colors = listOf(GoogleBlue, GoogleBlueDark))
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (goal.type) {
                                    GoalType.WEIGHT_LOSS -> Icons.Filled.TrendingDown
                                    GoalType.MUSCLE_GAIN -> Icons.Filled.FitnessCenter
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            goal.type.label,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            "Current Goal",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${goal.progress}%",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        "Completed",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.25f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(goal.progress / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(Color.White)
                    )
                }

                Spacer(Modifier.height(24.dp))

                if (isUpdating) {
                    OutlinedTextField(
                        value = newValue,
                        onValueChange = { if (it.isEmpty() || it.all(Char::isDigit)) newValue = it },
                        label = {
                            Text(
                                when (goal.type) {
                                    GoalType.WEIGHT_LOSS -> "Current Weight (kg)"
                                    GoalType.MUSCLE_GAIN -> "Current Muscle Mass (%)"
                                }
                            )
                        },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White.copy(alpha = 0.8f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (newValue.isNotBlank()) {
                                isLoading = true
                                val updatedGoal = when (goal) {
                                    is WeightLossGoal -> {
                                        val initial = goal.initialWeight
                                        val target = goal.targetWeight
                                        val current = newValue.toDouble()
                                        val progress = ((initial - current) / (initial - target)) * 100

                                        goal.copy(
                                            currentWeight = current,
                                            progress = progress.coerceIn(0.0, 100.0).toInt()
                                        )
                                    }

                                    is MuscleGainGoal -> {
                                        val initial = goal.initialMuscleMassPercent
                                        val target = goal.targetMuscleMassPercent
                                        val current = newValue.toDouble()
                                        val progress = ((current - initial) / (target - initial)) * 100

                                        goal.copy(
                                            currentMuscleMassPercent = current,
                                            progress = progress.coerceIn(0.0, 100.0).toInt()
                                        )
                                    }
                                }

                                FirebaseRepository.updateUserGoal(updatedGoal) { success ->
                                    isLoading = false
                                    if (success) {
                                        onProgressUpdated(updatedGoal)
                                        isUpdating = false
                                        newValue = ""
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Save Progress", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = { isUpdating = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue)
                    ) {
                        Text(
                            "Update Progress",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        isDeleting = true
                        FirebaseRepository.deleteUserGoal { success ->
                            isDeleting = false
                            if (success) onProgressUpdated(null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (goal.progress >= 100)
                            Color(0xFF4CAF50)
                        else
                            Color(0xFFFF5252)
                    )
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            if (goal.progress >= 100) "Finish Goal" else "Cancel Goal",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoalStatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(120.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        value,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        label,
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
fun CreateGoalSection(
    selectedGoalType: GoalType?,
    initialValue: String,
    targetValue: String,
    showSuccess: Boolean,
    onGoalTypeSelected: (GoalType) -> Unit,
    onInitialValueChange: (String) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = CircleShape, color = LightBlue, modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = null,
                            tint = GoogleBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Text("Create New Goal", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            }

            Spacer(Modifier.height(24.dp))

            GoalTypeDropdown(selectedType = selectedGoalType, onTypeSelected = onGoalTypeSelected)
            Spacer(Modifier.height(16.dp))

            when (selectedGoalType) {
                GoalType.WEIGHT_LOSS -> {
                    StyledGoalInputField("Initial Weight (kg)", initialValue, Icons.Outlined.MonitorWeight, onInitialValueChange)
                    Spacer(Modifier.height(12.dp))
                    StyledGoalInputField("Target Weight (kg)", targetValue, Icons.Outlined.Flag, onTargetValueChange)
                }
                GoalType.MUSCLE_GAIN -> {
                    StyledGoalInputField("Initial Muscle Mass (%)", initialValue, Icons.Outlined.FitnessCenter, onInitialValueChange)
                    Spacer(Modifier.height(12.dp))
                    StyledGoalInputField("Target Muscle Mass (%)", targetValue, Icons.Outlined.Flag, onTargetValueChange)
                }
                else -> {}
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onSaveClick,
                enabled = selectedGoalType != null && initialValue.isNotBlank() && targetValue.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoogleBlue)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("Save Goal", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = Color.White))
                }
            }

            if (showSuccess) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                        Text("Goal saved successfully!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun GoalTypeDropdown(selectedType: GoalType?, onTypeSelected: (GoalType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedType?.label ?: "",
        onValueChange = {},
        label = { Text("Goal Type") },
        leadingIcon = { Icon(Icons.Outlined.TrackChanges, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown, contentDescription = null)
            }
        },
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoogleBlue,
            unfocusedBorderColor = GoogleBlue.copy(alpha = 0.4f)
        )
    )

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth(0.9f)) {
        GoalType.entries.forEach { type ->
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(
                            imageVector = when (type) {
                                GoalType.WEIGHT_LOSS -> Icons.Outlined.TrendingDown
                                GoalType.MUSCLE_GAIN -> Icons.Outlined.FitnessCenter
                            },
                            contentDescription = null,
                            tint = GoogleBlue
                        )
                        Text(type.label)
                    }
                },
                onClick = {
                    onTypeSelected(type)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun StyledGoalInputField(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(imageVector = icon, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoogleBlue,
            unfocusedBorderColor = GoogleBlue.copy(alpha = 0.4f)
        )
    )
}