package com.example.fitmate.ui.screens

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.fitmate.data.FirebaseRepository
import com.example.fitmate.model.Goal
import com.example.fitmate.model.UserProfile
import com.example.fitmate.model.DailyWorkout
import com.example.fitmate.ui.navigation.NavRoutes
import com.example.fitmate.ui.components.shimmerEffect
import com.example.fitmate.ui.components.*
import com.example.fitmate.data.local.DatabaseProvider
import com.example.fitmate.data.local.entity.CachedGoalEntity
import com.example.fitmate.data.local.entity.CachedUserEntity
import com.example.fitmate.model.MuscleGainGoal
import com.example.fitmate.model.WeightLossGoal
import com.example.fitmate.model.Challenge
import com.example.fitmate.data.local.entity.CachedChallengeEntity
import com.example.fitmate.data.local.util.Converters
import com.example.fitmate.model.enums.ChallengeDifficulty
import com.example.fitmate.data.local.entity.CachedWorkoutEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import com.example.fitmate.sensors.StepsLiveData

private val GoogleBlue = Color(0xFF1A73E8)
private val GoogleBlueDark = Color(0xFF1557B0)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(navController: NavController) {
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoadingUser by remember { mutableStateOf(true) }
    
    val stepsToday by StepsLiveData.steps.observeAsState(initial = 0)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val intent = Intent(context, com.example.fitmate.sensors.FitnessTrackingService::class.java)
        intent.action = com.example.fitmate.sensors.FitnessTrackingService.ACTION_START
        context.startService(intent)

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val appContext = context.applicationContext

        // 1) Tenta ler do cache local primeiro
        if (uid != null) {
            try {
                val cached = withContext(Dispatchers.IO) {
                    DatabaseProvider.get(appContext).cachedUserDao().getUser(uid)
                }

                if (cached != null) {
                    userProfile = UserProfile(
                        uid = cached.uid,
                        name = cached.name,
                        email = cached.email ?: ""
                    )
                    isLoadingUser = false
                }

            } catch (_: Exception) { }
        }

        // 2) Depois faz fetch online e atualiza UI + cache
        FirebaseRepository.fetchUserProfile { profile ->
            userProfile = profile ?: userProfile
            isLoadingUser = false

            if (uid != null && profile != null) {
                scope.launch(Dispatchers.IO) {
                    try {
                        DatabaseProvider.get(appContext).cachedUserDao().upsert(
                            CachedUserEntity(
                                uid = profile.uid,
                                name = profile.name,
                                email = profile.email
                            )
                        )
                    } catch (_: Exception) { }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            if (isLoadingUser) {
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(32.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .shimmerEffect()
                )

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .width(250.dp)
                        .height(24.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(6.dp)
                        )
                        .shimmerEffect()
                )
            } else {
                val firstName = userProfile?.name?.split(" ")?.firstOrNull() ?: stringResource(id = com.example.fitmate.R.string.home_user_fallback)

                Text(
                    stringResource(id = com.example.fitmate.R.string.home_welcome_format, firstName),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val isTablet = maxWidth > 600.dp

            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        icon = Icons.AutoMirrored.Filled.DirectionsRun,
                        value = stepsToday.toString(),
                        unit = stringResource(id = com.example.fitmate.R.string.unit_steps),
                        label = stringResource(id = com.example.fitmate.R.string.steps_today),
                        gradient = Brush.linearGradient(
                            colors = listOf(GoogleBlue, GoogleBlueDark)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    GoalProgressCard(
                        modifier = Modifier.weight(1f),
                        onNavigateToGoal = {
                            navController.navigate(NavRoutes.GOAL) {
                                popUpTo(NavRoutes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )

                    QuickWorkoutCard(modifier = Modifier.weight(1f), onNavigateToWorkouts = {
                        navController.navigate(NavRoutes.WORKOUTS) {
                            popUpTo(NavRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    })

                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            icon = Icons.AutoMirrored.Filled.DirectionsRun,
                            value = stepsToday.toString(),
                            unit = "",
                            label = stringResource(id = com.example.fitmate.R.string.steps_label),
                            gradient = Brush.linearGradient(
                                colors = listOf(GoogleBlue, GoogleBlueDark)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        GoalProgressCard(
                            modifier = Modifier.weight(1f),
                            onNavigateToGoal = {
                                navController.navigate(NavRoutes.GOAL) {
                                    popUpTo(NavRoutes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    QuickWorkoutCard(modifier = Modifier.fillMaxWidth(), onNavigateToWorkouts = {
                        navController.navigate(NavRoutes.WORKOUTS) {
                            popUpTo(NavRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    })

                }
            }
        }

        Spacer(Modifier.height(32.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            var recentChallenges by remember { mutableStateOf<List<Challenge>>(emptyList()) }
            var loadingChallenges by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                val appContext = context.applicationContext
                try {
                    val cached = withContext(Dispatchers.IO) {
                        DatabaseProvider.get(appContext).cachedChallengeDao().getRecent(2)
                    }
                    if (cached.isNotEmpty()) {
                        recentChallenges = cached.map { e ->
                            val exercises = Converters.toExercisesList(e.workoutExercisesJson) ?: emptyList()
                            Challenge(
                                id = e.id,
                                title = e.title,
                                description = e.description,
                                rewardPoints = e.rewardPoints,
                                difficulty = try { ChallengeDifficulty.valueOf(e.difficulty) } catch (_: Exception) { ChallengeDifficulty.MEDIUM },
                                exerciseCount = e.exerciseCount,
                                isActive = e.isActive,
                                workout = DailyWorkout(
                                    date = e.workoutDate,
                                    title = e.workoutTitle,
                                    description = e.workoutDescription,
                                    duration = e.workoutDuration,
                                    exercises = exercises,
                                    status = e.workoutStatus,
                                    photoPath = e.workoutPhotoPath
                                )
                            )
                        }
                        loadingChallenges = false
                    }
                } catch (_: Exception) { }

                FirebaseRepository.fetchRecentCommunityChallenges(limit = 2) { list ->
                    recentChallenges = list
                    loadingChallenges = false
                    val appCtx = context.applicationContext
                    scope.launch(Dispatchers.IO) {
                        try {
                            val dao = DatabaseProvider.get(appCtx).cachedChallengeDao()
                            list.forEach { ch ->
                                val json = Converters.fromExercisesList(ch.workout.exercises) ?: "[]"
                                dao.upsert(
                                    CachedChallengeEntity(
                                        id = ch.id,
                                        title = ch.title,
                                        description = ch.description,
                                        rewardPoints = ch.rewardPoints,
                                        difficulty = ch.difficulty.name,
                                        exerciseCount = ch.exerciseCount,
                                        isActive = ch.isActive,
                                        workoutDate = ch.workout.date,
                                        workoutTitle = ch.workout.title,
                                        workoutDescription = ch.workout.description,
                                        workoutDuration = ch.workout.duration,
                                        workoutExercisesJson = json,
                                        workoutStatus = ch.workout.status,
                                        workoutPhotoPath = ch.workout.photoPath
                                    )
                                )
                            }
                        } catch (_: Exception) { }
                    }
                }
            }

            var userChallenges by remember { mutableStateOf<Map<String, com.example.fitmate.model.UserChallenge?>>(emptyMap()) }
            var userChallengesChecked by remember { mutableStateOf<Set<String>>(emptySet()) }
            LaunchedEffect(recentChallenges) {
                recentChallenges.forEach { ch ->
                    FirebaseRepository.fetchUserChallengeForCurrentUser(ch.id) { uc ->
                        userChallenges = userChallenges.toMutableMap().apply { put(ch.id, uc) }
                        userChallengesChecked = userChallengesChecked + ch.id
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(id = com.example.fitmate.R.string.home_challenges_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                TextButton(onClick = {
                    navController.navigate(NavRoutes.CHALLENGES) {
                        popUpTo(NavRoutes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }) {
                    Text(stringResource(id = com.example.fitmate.R.string.view_all))
                }
            }

            Spacer(Modifier.height(12.dp))

            if (loadingChallenges) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(12.dp)
                        )
                        .shimmerEffect()
                )
            } else {
                recentChallenges.take(2).forEachIndexed { idx, ch ->
                    if (idx > 0) Spacer(Modifier.height(12.dp))
                    val uc = userChallenges[ch.id]
                    val checked = userChallengesChecked.contains(ch.id)
                    val cta = if (!checked || !ch.isActive) null else when {
                        uc?.isCompleted == true -> null
                        uc?.isActive == true && uc.isCompleted == false -> stringResource(id = com.example.fitmate.R.string.continue_label)
                        else -> stringResource(id = com.example.fitmate.R.string.start_label)
                    }
                    ChallengeCard(
                        title = ch.title,
                        description = ch.description,
                        rewardPoints = ch.rewardPoints,
                        ctaText = cta,
                        onAcceptClick = if (cta != null && checked) {
                            {
                                navController.navigate(NavRoutes.CHALLENGES) {
                                    popUpTo(NavRoutes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        } else null
                    )
                }
                if (recentChallenges.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.EmojiEvents,
                                contentDescription = null,
                                tint = GoogleBlue,
                                modifier = Modifier.size(28.dp)
                            )
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    stringResource(id = com.example.fitmate.R.string.home_no_challenges),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    stringResource(id = com.example.fitmate.R.string.home_check_back_later),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    navController.navigate(NavRoutes.CHALLENGES) {
                                        popUpTo(NavRoutes.HOME) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(id = com.example.fitmate.R.string.view_all))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun GoalProgressCard(
    modifier: Modifier = Modifier,
    onNavigateToGoal: (() -> Unit)? = null
) {
    var goal by remember { mutableStateOf<Goal?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun toCached(uid: String, g: Goal): CachedGoalEntity = when (g) {
        is WeightLossGoal -> CachedGoalEntity(
            uid = uid,
            type = g.type,
            progress = g.progress,
            createdAt = g.createdAt,
            initialWeight = g.initialWeight,
            currentWeight = g.currentWeight,
            targetWeight = g.targetWeight,
            initialMuscleMassPercent = null,
            currentMuscleMassPercent = null,
            targetMuscleMassPercent = null
        )

        is MuscleGainGoal -> CachedGoalEntity(
            uid = uid,
            type = g.type,
            progress = g.progress,
            createdAt = g.createdAt,
            initialWeight = null,
            currentWeight = null,
            targetWeight = null,
            initialMuscleMassPercent = g.initialMuscleMassPercent,
            currentMuscleMassPercent = g.currentMuscleMassPercent,
            targetMuscleMassPercent = g.targetMuscleMassPercent
        )
    }

    fun fromCached(entity: CachedGoalEntity): Goal = when (entity.type) {
        com.example.fitmate.model.enums.GoalType.WEIGHT_LOSS -> WeightLossGoal(
            createdAt = entity.createdAt,
            progress = entity.progress,
            initialWeight = entity.initialWeight ?: 0.0,
            currentWeight = entity.currentWeight ?: (entity.initialWeight ?: 0.0),
            targetWeight = entity.targetWeight ?: (entity.initialWeight ?: 0.0)
        )

        com.example.fitmate.model.enums.GoalType.MUSCLE_GAIN -> MuscleGainGoal(
            createdAt = entity.createdAt,
            progress = entity.progress,
            initialMuscleMassPercent = entity.initialMuscleMassPercent ?: 0.0,
            currentMuscleMassPercent = entity.currentMuscleMassPercent ?: (entity.initialMuscleMassPercent ?: 0.0),
            targetMuscleMassPercent = entity.targetMuscleMassPercent ?: (entity.initialMuscleMassPercent ?: 0.0)
        )
    }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val appContext = context.applicationContext

        if (uid != null) {
            try {
                val cached = withContext(Dispatchers.IO) {
                    DatabaseProvider.get(appContext).cachedGoalDao().getGoal(uid)
                }
                if (cached != null) {
                    goal = fromCached(cached)
                    isLoading = false
                }
            } catch (_: Exception) { /* ignore */ }
        }

        FirebaseRepository.fetchUserGoal { fetchedGoal ->
            goal = fetchedGoal ?: goal
            isLoading = false

            if (uid != null && fetchedGoal != null) {
                scope.launch(Dispatchers.IO) {
                    try {
                        DatabaseProvider.get(appContext).cachedGoalDao().upsert(
                            toCached(uid, fetchedGoal)
                        )
                    } catch (_: Exception) { /* ignore */ }
                }
            }
        }
    }

    Surface(
        modifier = modifier
            .height(140.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable(enabled = !isLoading) { onNavigateToGoal?.invoke() },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(GoogleBlue, GoogleBlueDark)
                    )
                )
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .shimmerEffect()
                    )
                }

                goal == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(id = com.example.fitmate.R.string.goal_set_your_goal),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(id = com.example.fitmate.R.string.goal_tap_create_one),
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                else -> {
                    val progress = goal!!.progress.coerceIn(0, 100)
                    val typeLabel = when (goal!!.type) {
                        com.example.fitmate.model.enums.GoalType.WEIGHT_LOSS -> stringResource(id = com.example.fitmate.R.string.goal_type_weight_loss)
                        com.example.fitmate.model.enums.GoalType.MUSCLE_GAIN -> stringResource(id = com.example.fitmate.R.string.goal_type_muscle_gain)
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.TrackChanges,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(26.dp)
                            )
                            Text(
                                "$progress%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        Column {
                            Text(
                                stringResource(id = com.example.fitmate.R.string.goal_personal_goal),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.95f),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Spacer(Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(alpha = 0.25f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progress / 100f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.White)
                                )
                            }

                            Spacer(Modifier.height(6.dp))
                            Text(
                                typeLabel,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun QuickWorkoutCard(
    modifier: Modifier = Modifier,
    onNavigateToWorkouts: (() -> Unit)? = null
) {
    var currentWorkout by remember { mutableStateOf<DailyWorkout?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                    currentWorkout = DailyWorkout(
                        date = cached.date,
                        title = cached.title,
                        description = cached.description,
                        duration = cached.duration,
                        exercises = exercises,
                        status = cached.status,
                        photoPath = null
                    )
                    isLoading = false
                }
            } catch (_: Exception) { }
        }

        FirebaseRepository.fetchCurrentStartedWorkout { workout, id ->
            if (workout != null) {
                currentWorkout = workout
                isLoading = false
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
            } else {
                isLoading = false
            }
        }
    }

    Surface(
        onClick = { onNavigateToWorkouts?.invoke() },
        modifier = modifier
            .height(120.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(GoogleBlue, GoogleBlueDark)
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            stringResource(id = com.example.fitmate.R.string.workouts_todays_workout),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(Modifier.height(4.dp))
                        when {
                            isLoading -> {
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(18.dp)
                                        .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .shimmerEffect()
                                )
                            }
                            currentWorkout != null -> {
                                Text(
                                    currentWorkout!!.title.ifBlank { stringResource(id = com.example.fitmate.R.string.workouts_unnamed_workout) },
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                )
                            }
                            else -> {
                                Text(
                                    stringResource(id = com.example.fitmate.R.string.workouts_no_workout_started),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        val durationText = when {
                            isLoading -> "..."
                            currentWorkout != null -> currentWorkout!!.duration.ifBlank { "—" }
                            else -> "—"
                        }
                        Text(
                            durationText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }

                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(36.dp)
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
                                    .shimmerEffect()
                            )
                        }
                        currentWorkout == null -> {
                            Button(
                                onClick = { onNavigateToWorkouts?.invoke() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = GoogleBlue
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(stringResource(id = com.example.fitmate.R.string.workouts_create_workout))
                            }
                        }
                        else -> {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = stringResource(id = com.example.fitmate.R.string.cd_open),
                                        tint = GoogleBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
