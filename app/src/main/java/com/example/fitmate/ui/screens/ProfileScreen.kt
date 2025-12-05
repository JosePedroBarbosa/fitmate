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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitmate.data.FirebaseRepository
import com.example.fitmate.data.local.DatabaseProvider
import com.example.fitmate.data.local.entity.CachedUserEntity
import com.example.fitmate.model.enums.FitnessLevelType
import com.example.fitmate.model.enums.GenderType
import com.example.fitmate.model.UserProfile
import com.example.fitmate.ui.components.DateOfBirthPicker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.delay
import com.example.fitmate.ui.components.shimmerEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

private val GoogleBlue = Color(0xFF1A73E8)
private val GoogleBlueDark = Color(0xFF1557B0)

@Composable
fun ProfileScreen() {
    val auth = FirebaseAuth.getInstance()
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appContext = context.applicationContext

    var name by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var fitnessLevel by remember { mutableStateOf("") }

    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            try {
                val cached = withContext(Dispatchers.IO) {
                    DatabaseProvider.get(appContext).cachedUserDao().getUser(uid)
                }
                if (cached != null) {
                    userProfile = UserProfile(
                        uid = cached.uid,
                        name = cached.name,
                        email = cached.email ?: "",
                        points = cached.points,
                        height = cached.height,
                        weight = cached.weight,
                        dateOfBirth = cached.dateOfBirth,
                        gender = cached.gender?.let { GenderType.fromLabel(it) },
                        fitnessLevel = cached.fitnessLevel?.let { FitnessLevelType.fromLabel(it) }
                    )
                    name = cached.name
                    height = cached.height?.toString() ?: ""
                    weight = cached.weight?.toString() ?: ""
                    dateOfBirth = cached.dateOfBirth ?: ""
                    gender = cached.gender ?: ""
                    fitnessLevel = cached.fitnessLevel ?: ""
                    isLoading = false
                }
            } catch (_: Exception) { }
        }

        FirebaseRepository.fetchUserProfile { profile ->
            userProfile = profile ?: userProfile
            profile?.let {
                name = it.name
                height = it.height?.toString() ?: height
                weight = it.weight?.toString() ?: weight
                dateOfBirth = it.dateOfBirth ?: dateOfBirth
                gender = it.gender?.label ?: gender
                fitnessLevel = it.fitnessLevel?.label ?: fitnessLevel
            } ?: run {
                if (userProfile == null) {
                    name = auth.currentUser?.displayName ?: "Unknown"
                }
            }
            isLoading = false

            val fetchedUid = profile?.uid
            if (profile != null) {
                val roomUid = fetchedUid?.takeIf { it.isNotBlank() } ?: FirebaseAuth.getInstance().currentUser?.uid
                if (roomUid == null || roomUid.isBlank()) return@fetchUserProfile
                scope.launch(Dispatchers.IO) {
                    try {
                        DatabaseProvider.get(appContext).cachedUserDao().upsert(
                            CachedUserEntity(
                                uid = roomUid,
                                name = profile.name,
                                email = profile.email,
                                points = profile.points,
                                height = profile.height,
                                weight = profile.weight,
                                dateOfBirth = profile.dateOfBirth,
                                gender = profile.gender?.label,
                                fitnessLevel = profile.fitnessLevel?.label
                            )
                        )
                    } catch (_: Exception) { }
                }
            }
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
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            CircleShape
                        )
                        .shimmerEffect()
                )
                Spacer(Modifier.height(24.dp))
                repeat(5) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(16.dp)
                            )
                            .shimmerEffect()
                    )
                    Spacer(Modifier.height(12.dp))
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
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFFA726), Color(0xFFFF7043))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.size(50.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        "${userProfile?.points ?: 0}",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        stringResource(id = com.example.fitmate.R.string.profile_total_points),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.size(60.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

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
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AccountCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Text(
                                stringResource(id = com.example.fitmate.R.string.profile_personal_information),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        ProfileTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = stringResource(id = com.example.fitmate.R.string.profile_full_name),
                            icon = Icons.Outlined.Person,
                            enabled = !isSaving
                        )

                        Spacer(Modifier.height(16.dp))

                        ProfileTextField(
                            value = height,
                            onValueChange = { if (it.isEmpty() || it.all(Char::isDigit)) height = it },
                            label = stringResource(id = com.example.fitmate.R.string.profile_height_cm),
                            icon = Icons.Outlined.Height,
                            keyboardType = KeyboardType.Number,
                            enabled = !isSaving
                        )

                        Spacer(Modifier.height(16.dp))

                        ProfileTextField(
                            value = weight,
                            onValueChange = { if (it.isEmpty() || it.all(Char::isDigit)) weight = it },
                            label = stringResource(id = com.example.fitmate.R.string.profile_weight_kg),
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
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        isSaving = true

                        val updatedProfile = UserProfile(
                            uid = userProfile?.uid ?: auth.currentUser?.uid ?: "",
                            name = name,
                            email = userProfile?.email ?: auth.currentUser?.email ?: "",
                            points = userProfile?.points ?: 0,
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
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        DatabaseProvider.get(appContext).cachedUserDao().upsert(
                                            CachedUserEntity(
                                                uid = updatedProfile.uid,
                                                name = updatedProfile.name,
                                                email = updatedProfile.email,
                                                points = updatedProfile.points,
                                                height = updatedProfile.height,
                                                weight = updatedProfile.weight,
                                                dateOfBirth = updatedProfile.dateOfBirth,
                                                gender = updatedProfile.gender?.label,
                                                fitnessLevel = updatedProfile.fitnessLevel?.label
                                            )
                                        )
                                    } catch (_: Exception) { }
                                }
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
                    enabled = !isSaving && name.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(GoogleBlue, GoogleBlueDark)
                                ),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Save,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    stringResource(id = com.example.fitmate.R.string.profile_save_changes),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

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
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Language,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Text(
                                stringResource(id = com.example.fitmate.R.string.language),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        LanguageSelector()
                    }
                }
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
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF4CAF50),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                        Text(
                            stringResource(id = com.example.fitmate.R.string.profile_success),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                }
            }

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
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.error,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                        Text(
                            stringResource(id = com.example.fitmate.R.string.profile_error),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                }
            }

            LaunchedEffect(Unit) {
                delay(2500)
                showErrorMessage = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelector() {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val languages = listOf("English" to "en", "Português" to "pt")
    val currentLocale = context.resources.configuration.locales[0]
    var selectedIndex by remember { mutableStateOf(if (currentLocale.language == "pt") 1 else 0) }

    TabRow(selectedTabIndex = selectedIndex) {
        languages.forEachIndexed { index, (label, code) ->
            Tab(
                selected = selectedIndex == index,
                onClick = {
                    selectedIndex = index
                    if (code != currentLocale.language) {
                        activity?.let { setLocale(it, code) }
                    }
                },
                text = { Text(label) }
            )
        }
    }
}

private fun setLocale(activity: android.app.Activity, languageCode: String) {
    val locale = java.util.Locale(languageCode)
    java.util.Locale.setDefault(locale)
    val prefs = activity.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
    prefs.edit().putString("language_code", languageCode).apply()
    val resources = activity.resources
    val config = resources.configuration
    config.setLocale(locale)
    resources.updateConfiguration(config, resources.displayMetrics)
    activity.recreate()
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        modifier = modifier.fillMaxWidth(),
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
    val options = GenderType.entries
    val localizedSelected = when (selectedGender) {
        GenderType.MALE.label -> stringResource(id = com.example.fitmate.R.string.gender_male)
        GenderType.FEMALE.label -> stringResource(id = com.example.fitmate.R.string.gender_female)
        GenderType.OTHER.label -> stringResource(id = com.example.fitmate.R.string.gender_other)
        else -> selectedGender
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = localizedSelected,
            onValueChange = {},
            label = { Text(stringResource(id = com.example.fitmate.R.string.profile_gender_label)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Wc,
                    contentDescription = null
                )
            },
            trailingIcon = {
                IconButton(onClick = { if (enabled) expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded)
                            Icons.Outlined.KeyboardArrowUp
                        else
                            Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null
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
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = when (option) {
                                    GenderType.MALE -> Icons.Outlined.Male
                                    GenderType.FEMALE -> Icons.Outlined.Female
                                    else -> Icons.Outlined.Wc
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                when (option) {
                                    GenderType.MALE -> stringResource(id = com.example.fitmate.R.string.gender_male)
                                    GenderType.FEMALE -> stringResource(id = com.example.fitmate.R.string.gender_female)
                                    GenderType.OTHER -> stringResource(id = com.example.fitmate.R.string.gender_other)
                                }
                            )
                        }
                    },
                    onClick = {
                        onGenderSelected(option.label)
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
    val options = FitnessLevelType.entries
    val localizedSelected = when (selectedLevel) {
        FitnessLevelType.BEGINNER.label -> stringResource(id = com.example.fitmate.R.string.fitness_beginner)
        FitnessLevelType.INTERMEDIATE.label -> stringResource(id = com.example.fitmate.R.string.fitness_intermediate)
        FitnessLevelType.EXPERT.label -> stringResource(id = com.example.fitmate.R.string.fitness_expert)
        else -> selectedLevel
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = localizedSelected,
            onValueChange = {},
            label = { Text(stringResource(id = com.example.fitmate.R.string.profile_fitness_level_label)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.FitnessCenter,
                    contentDescription = null
                )
            },
            trailingIcon = {
                IconButton(onClick = { if (enabled) expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded)
                            Icons.Outlined.KeyboardArrowUp
                        else
                            Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null
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
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = when (option) {
                                    FitnessLevelType.BEGINNER -> Icons.Outlined.SelfImprovement
                                    FitnessLevelType.INTERMEDIATE -> Icons.Outlined.DirectionsRun
                                    FitnessLevelType.EXPERT -> Icons.Outlined.FitnessCenter
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                when (option) {
                                    FitnessLevelType.BEGINNER -> stringResource(id = com.example.fitmate.R.string.fitness_beginner)
                                    FitnessLevelType.INTERMEDIATE -> stringResource(id = com.example.fitmate.R.string.fitness_intermediate)
                                    FitnessLevelType.EXPERT -> stringResource(id = com.example.fitmate.R.string.fitness_expert)
                                }
                            )
                        }
                    },
                    onClick = {
                        onLevelSelected(option.label)
                        expanded = false
                    }
                )
            }
        }
    }
}
