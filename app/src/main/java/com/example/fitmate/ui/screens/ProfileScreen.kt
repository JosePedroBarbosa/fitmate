package com.example.fitmate.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitmate.data.FirebaseRepository
import com.example.fitmate.model.FitnessLevelType
import com.example.fitmate.model.GenderType
import com.example.fitmate.model.UserProfile
import com.example.fitmate.ui.components.DateOfBirthPicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen() {
    val auth = FirebaseAuth.getInstance()
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var fitnessLevel by remember { mutableStateOf("") }

    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        FirebaseRepository.fetchUserProfile { profile ->
            userProfile = profile
            profile?.let {
                name = it.name
                height = it.height?.toString() ?: ""
                weight = it.weight?.toString() ?: ""
                dateOfBirth = it.dateOfBirth ?: ""
                gender = it.gender?.label ?: ""
                fitnessLevel = it.fitnessLevel?.label ?: ""
            } ?: run {
                name = auth.currentUser?.displayName ?: "Unknown"
                height = ""
                weight = ""
            }
            isLoading = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                        .shimmerEffect()
                )
                Spacer(Modifier.height(24.dp))
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .shimmerEffect()
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(10.dp))

                // Avatar
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(8.dp, CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .height(70.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "${userProfile?.points ?: 0} Points",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                ProfileTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name",
                    icon = Icons.Outlined.Person,
                    enabled = !isSaving
                )

                Spacer(Modifier.height(16.dp))

                ProfileTextField(
                    value = height,
                    onValueChange = { if (it.isEmpty() || it.all(Char::isDigit)) height = it },
                    label = "Height (cm)",
                    icon = Icons.Outlined.Height,
                    keyboardType = KeyboardType.Number,
                    enabled = !isSaving
                )

                Spacer(Modifier.height(16.dp))

                ProfileTextField(
                    value = weight,
                    onValueChange = { if (it.isEmpty() || it.all(Char::isDigit)) weight = it },
                    label = "Weight (kg)",
                    icon = Icons.Outlined.MonitorWeight,
                    keyboardType = KeyboardType.Number,
                    enabled = !isSaving
                )

                Spacer(Modifier.height(16.dp))

                DateOfBirthPicker(
                    selectedDate = dateOfBirth,
                    onDateSelected = { dateOfBirth = it },
                    enabled = !isSaving
                )

                Spacer(Modifier.height(16.dp))

                GenderDropdown(
                    selectedGender = gender,
                    onGenderSelected = { gender = it },
                    enabled = !isSaving
                )

                Spacer(Modifier.height(16.dp))

                FitnessLevelDropdown(
                    selectedLevel = fitnessLevel,
                    onLevelSelected = { fitnessLevel = it },
                    enabled = !isSaving
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        isSaving = true

                        val updatedProfile = UserProfile(
                            uid = userProfile?.uid ?: auth.currentUser?.uid ?: "",
                            name = name,
                            email = userProfile?.email ?: auth.currentUser?.email ?: "",
                            height = height.toIntOrNull(),
                            weight = weight.toDoubleOrNull(),
                            dateOfBirth = dateOfBirth,
                            gender = GenderType.fromLabel(gender),
                            fitnessLevel = FitnessLevelType.fromLabel(fitnessLevel),
                        )

                        FirebaseRepository.updateUserProfile(updatedProfile) { success ->
                            isSaving = false
                            if (success) {
                                val profileUpdate = userProfileChangeRequest {
                                    displayName = name
                                }
                                auth.currentUser?.updateProfile(profileUpdate)

                                showSuccessMessage = true
                                showErrorMessage = false
                                userProfile = updatedProfile
                            } else {
                                showErrorMessage = true
                                showSuccessMessage = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isSaving && name.isNotBlank()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save Changes", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
            }
        }

        AnimatedVisibility(
            visible = showSuccessMessage,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Snackbar(
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            ) { Text("Profile updated successfully!") }

            LaunchedEffect(Unit) {
                delay(2500)
                showSuccessMessage = false
            }
        }

        AnimatedVisibility(
            visible = showErrorMessage,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Snackbar(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = Color.White
            ) { Text("Failed to update profile.") }

            LaunchedEffect(Unit) {
                delay(2500)
                showErrorMessage = false
            }
        }
    }
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        enabled = enabled,
        singleLine = true
    )
}
@Composable
fun GenderDropdown(
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val options = GenderType.entries.map { it.label }

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedGender,
            onValueChange = {},
            label = { Text("Gender") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Wc,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            readOnly = true,
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { expanded = true },
            singleLine = true
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onGenderSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FitnessLevelDropdown(
    selectedLevel: String,
    onLevelSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val levels = FitnessLevelType.entries.map { it.label }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedLevel,
            onValueChange = {},
            label = { Text("Fitness Level") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded)
                            Icons.Outlined.KeyboardArrowUp
                        else
                            Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            readOnly = true,
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { expanded = true },
            singleLine = true
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            levels.forEach { level ->
                DropdownMenuItem(
                    text = { Text(level) },
                    onClick = {
                        onLevelSelected(level)
                        expanded = false
                    }
                )
            }
        }
    }
}