package com.example.fitmate.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fitmate.model.ApiExercise
import com.example.fitmate.model.DailyWorkout
import com.example.fitmate.model.Challenge
import com.example.fitmate.model.enums.FitnessLevelType
import com.example.fitmate.model.enums.GenderType
import com.example.fitmate.model.UserProfile
import com.example.fitmate.model.WeightLossGoal
import com.example.fitmate.model.enums.GoalType
import com.example.fitmate.model.MuscleGainGoal
import com.example.fitmate.model.Goal
import com.example.fitmate.model.enums.WorkoutStatus
import com.example.fitmate.model.UserChallenge
import com.example.fitmate.model.enums.ChallengeDifficulty
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.time.LocalDate
import com.example.fitmate.data.exercisesApi
import com.example.fitmate.data.RetrofitHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase
        .getInstance("https://fitmate-8ad03-default-rtdb.europe-west1.firebasedatabase.app")
        .reference

    fun registerUser(
        name: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val user = auth.currentUser ?: return@addOnSuccessListener
                val uid = user.uid

                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                user.updateProfile(profileUpdates)

                val userProfile = UserProfile(
                    uid = uid,
                    name = name,
                    email = email
                )

                database.child("users").child(uid).setValue(userProfile)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Erro ao guardar utilizador")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Erro no registo")
            }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e.message ?: "Erro no login") }
    }

    fun logout() {
        auth.signOut()
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun fetchUserProfile(onResult: (UserProfile?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(null)

        database.child("users").child(uid).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val uidValue = snapshot.child("uid").getValue(String::class.java) ?: ""
                    val name = snapshot.child("name").getValue(String::class.java) ?: ""
                    val email = snapshot.child("email").getValue(String::class.java) ?: ""
                    val points = snapshot.child("points").getValue(Int::class.java) ?: 0
                    val height = snapshot.child("height").getValue(Int::class.java)
                    val weight = snapshot.child("weight").getValue(Double::class.java)
                    val dateOfBirth = snapshot.child("dateOfBirth").getValue(String::class.java)
                    val genderStr = snapshot.child("gender").getValue(String::class.java)
                    val fitnessStr = snapshot.child("fitnessLevel").getValue(String::class.java)

                    val userProfile = UserProfile(
                        uid = uidValue,
                        name = name,
                        email = email,
                        points = points,
                        height = height,
                        weight = weight,
                        dateOfBirth = dateOfBirth,
                        gender = genderStr?.let { GenderType.fromLabel(it) },
                        fitnessLevel = fitnessStr?.let { FitnessLevelType.fromLabel(it) }
                    )
                    onResult(userProfile)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun updateUserProfile(userProfile: UserProfile, onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onComplete(false)

        // Only update profile-related fields; do NOT overwrite points, goal, or workouts
        val updates = mutableMapOf<String, Any?>().apply {
            put("uid", userProfile.uid)
            put("name", userProfile.name)
            put("email", userProfile.email)
            put("height", userProfile.height)
            put("weight", userProfile.weight)
            put("dateOfBirth", userProfile.dateOfBirth)
            put("gender", userProfile.gender?.label)
            put("fitnessLevel", userProfile.fitnessLevel?.label)
        }

        // Remove nulls to avoid unintended deletions unless explicitly provided
        val nonNullUpdates = updates.filterValues { it != null }.mapValues { it.value!! }

        database.child("users").child(uid).updateChildren(nonNullUpdates)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun updateUserGoal(goal: Goal, onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onComplete(false)

        val goalMap = when (goal) {
            is WeightLossGoal -> mapOf(
                "type" to goal.type.name,
                "createdAt" to goal.createdAt,
                "progress" to goal.progress,
                "initialWeight" to goal.initialWeight,
                "currentWeight" to goal.currentWeight,
                "targetWeight" to goal.targetWeight
            )
            is MuscleGainGoal -> mapOf(
                "type" to goal.type.name,
                "createdAt" to goal.createdAt,
                "progress" to goal.progress,
                "initialMuscleMassPercent" to goal.initialMuscleMassPercent,
                "currentMuscleMassPercent" to goal.currentMuscleMassPercent,
                "targetMuscleMassPercent" to goal.targetMuscleMassPercent
            )
        }

        database.child("users").child(uid).child("goal").setValue(goalMap)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun deleteUserGoal(onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onComplete(false)

        database.child("users").child(uid).child("goal").removeValue()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun fetchUserGoal(onResult: (Goal?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(null)

        database.child("users").child(uid).child("goal").get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    onResult(null)
                    return@addOnSuccessListener
                }

                val typeStr = snapshot.child("type").getValue(String::class.java)
                val type = typeStr?.let { GoalType.valueOf(it) }

                val goal = when (type) {
                    GoalType.WEIGHT_LOSS -> WeightLossGoal(
                        createdAt = snapshot.child("createdAt").getValue(Long::class.java) ?: 0L,
                        progress = snapshot.child("progress").getValue(Int::class.java) ?: 0,
                        initialWeight = snapshot.child("initialWeight").getValue(Double::class.java) ?: 0.0,
                        currentWeight = snapshot.child("currentWeight").getValue(Double::class.java) ?: 0.0,
                        targetWeight = snapshot.child("targetWeight").getValue(Double::class.java) ?: 0.0
                    )

                    GoalType.MUSCLE_GAIN -> MuscleGainGoal(
                        createdAt = snapshot.child("createdAt").getValue(Long::class.java) ?: 0L,
                        progress = snapshot.child("progress").getValue(Int::class.java) ?: 0,
                        initialMuscleMassPercent = snapshot.child("initialMuscleMassPercent").getValue(Double::class.java) ?: 0.0,
                        currentMuscleMassPercent = snapshot.child("currentMuscleMassPercent").getValue(Double::class.java) ?: 0.0,
                        targetMuscleMassPercent = snapshot.child("targetMuscleMassPercent").getValue(Double::class.java) ?: 0.0
                    )

                    else -> null
                }

                onResult(goal)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun saveWorkoutForCurrentUser(workout: DailyWorkout, onComplete: (String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onComplete(null)
        val workoutsRef = database.child("users").child(uid).child("workouts").push()
        val key = workoutsRef.key ?: return onComplete(null)

        val workoutMap = mapOf(
            "id" to key,
            "date" to workout.date.toString(),
            "title" to workout.title,
            "description" to workout.description,
            "duration" to workout.duration,
            "status" to workout.status.name,
            "photoPath" to workout.photoPath,
            "exercises" to workout.exercises.map { ex ->
                mapOf(
                    "name" to ex.name,
                    "type" to ex.type,
                    "muscle" to ex.muscle,
                    "equipment" to ex.equipment,
                    "difficulty" to ex.difficulty,
                    "instructions" to ex.instructions
                )
            }
        )

        workoutsRef.setValue(workoutMap)
            .addOnSuccessListener { onComplete(key) }
            .addOnFailureListener { onComplete(null) }
    }

    fun updateWorkoutStatusForCurrentUser(workoutId: String, status: WorkoutStatus, onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onComplete(false)
        database.child("users").child(uid).child("workouts").child(workoutId).child("status")
            .setValue(status.name)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun saveWorkoutPhotoPathForCurrentUser(workoutId: String, path: String, onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onComplete(false)
        database.child("users").child(uid).child("workouts").child(workoutId).child("photoPath")
            .setValue(path)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchCurrentStartedWorkout(onResult: (DailyWorkout?, String?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(null, null)
        database.child("users").child(uid).child("workouts").get()
            .addOnSuccessListener { snapshot ->
                var foundWorkout: DailyWorkout? = null
                var foundId: String? = null
                for (child in snapshot.children) {
                    val statusStr = child.child("status").getValue(String::class.java) ?: ""
                    if (statusStr == WorkoutStatus.STARTED.name) {
                        foundId = child.key
                        val dateStr = child.child("date").getValue(String::class.java) ?: LocalDate.now().toString()
                        val title = child.child("title").getValue(String::class.java) ?: ""
                        val description = child.child("description").getValue(String::class.java) ?: ""
                        val duration = child.child("duration").getValue(String::class.java) ?: ""

                        val exercisesNode = child.child("exercises")
                        val exercises = exercisesNode.children.map { ex ->
                            ApiExercise(
                                name = ex.child("name").getValue(String::class.java),
                                type = ex.child("type").getValue(String::class.java),
                                muscle = ex.child("muscle").getValue(String::class.java),
                                equipment = ex.child("equipment").getValue(String::class.java),
                                difficulty = ex.child("difficulty").getValue(String::class.java),
                                instructions = ex.child("instructions").getValue(String::class.java)
                            )
                        }

                        val photoPath = child.child("photoPath").getValue(String::class.java)
                        foundWorkout = DailyWorkout(
                            date = LocalDate.parse(dateStr),
                            title = title,
                            description = description,
                            duration = duration,
                            exercises = exercises,
                            status = WorkoutStatus.STARTED,
                            photoPath = photoPath
                        )
                        break
                    }
                }
                onResult(foundWorkout, foundId)
            }
            .addOnFailureListener { onResult(null, null) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchAllWorkoutsForCurrentUser(onResult: (List<Pair<DailyWorkout, String>>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(emptyList())
        database.child("users").child(uid).child("workouts").get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.children.mapNotNull { child ->
                    val id = child.key ?: return@mapNotNull null
                    val statusStr = child.child("status").getValue(String::class.java) ?: WorkoutStatus.STARTED.name
                    val title = child.child("title").getValue(String::class.java) ?: ""
                    val description = child.child("description").getValue(String::class.java) ?: ""
                    val duration = child.child("duration").getValue(String::class.java) ?: ""
                    val dateStr = child.child("date").getValue(String::class.java) ?: java.time.LocalDate.now().toString()
                    val photoPath = child.child("photoPath").getValue(String::class.java)

                    val exercisesNode = child.child("exercises")
                    val exercises = exercisesNode.children.map { ex ->
                        ApiExercise(
                            name = ex.child("name").getValue(String::class.java),
                            type = ex.child("type").getValue(String::class.java),
                            muscle = ex.child("muscle").getValue(String::class.java),
                            equipment = ex.child("equipment").getValue(String::class.java),
                            difficulty = ex.child("difficulty").getValue(String::class.java),
                            instructions = ex.child("instructions").getValue(String::class.java)
                        )
                    }

                    val status = try {
                        WorkoutStatus.valueOf(statusStr)
                    } catch (_: Exception) {
                        WorkoutStatus.STARTED
                    }

                    val date = try {
                        java.time.LocalDate.parse(dateStr)
                    } catch (_: Exception) {
                        java.time.LocalDate.now()
                    }

                    Pair(
                        DailyWorkout(
                            date = date,
                            title = title,
                            description = description,
                            duration = duration,
                            exercises = exercises,
                            status = status,
                            photoPath = photoPath
                        ), id
                    )
                }.sortedByDescending { it.first.date }

                onResult(list)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun fetchTopUsersByPoints(limit: Int = 10, onResult: (List<UserProfile>) -> Unit) {
        database.child("users").get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.children.mapNotNull { child ->
                    val key = child.key ?: ""
                    val uidValue = child.child("uid").getValue(String::class.java) ?: key
                    val name = child.child("name").getValue(String::class.java) ?: ""
                    val email = child.child("email").getValue(String::class.java) ?: ""
                    val rawPoints = child.child("points").value
                    val points = when (rawPoints) {
                        is Long -> rawPoints.toInt()
                        is Double -> rawPoints.toInt()
                        is Int -> rawPoints
                        is String -> rawPoints.toIntOrNull() ?: 0
                        else -> 0
                    }
                    val height = child.child("height").getValue(Int::class.java)
                    val weight = child.child("weight").getValue(Double::class.java)
                    val dateOfBirth = child.child("dateOfBirth").getValue(String::class.java)
                    val genderStr = child.child("gender").getValue(String::class.java)
                    val fitnessStr = child.child("fitnessLevel").getValue(String::class.java)

                    UserProfile(
                        uid = uidValue,
                        name = name,
                        email = email,
                        points = points,
                        height = height,
                        weight = weight,
                        dateOfBirth = dateOfBirth,
                        gender = genderStr?.let { GenderType.fromLabel(it) },
                        fitnessLevel = fitnessStr?.let { FitnessLevelType.fromLabel(it) }
                    )
                }.sortedByDescending { it.points }.take(limit)

                onResult(list)
            }
            .addOnFailureListener { e ->
                onResult(emptyList())
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun ensureTodayCommunityChallenge(onComplete: (Boolean) -> Unit) {
        val today = LocalDate.now().toString()
        database.child("communityChallenges").child(today).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    maintainActiveChallengeState(today) {
                        deactivateOutdatedUserChallengesForCurrentUser(today) { onComplete(true) }
                    }
                } else {
                    val muscles = listOf(
                        "abdominals", "abductors", "adductors", "biceps", "calves", "chest",
                        "forearms", "glutes", "hamstrings", "lats", "lower_back", "middle_back",
                        "neck", "quadriceps", "traps", "triceps"
                    )
                    val muscle = muscles.random()
                    val difficulty = "intermediate"
                    ExercisesGenerator.generate(muscle, difficulty) { workout ->
                        if (workout == null) {
                            onComplete(false)
                        } else {
                            val challenge = Challenge(
                                id = today,
                                title = "Community Challenge",
                                description = "Complete the ${muscle.replace('_', ' ')} workout",
                                rewardPoints = (workout.exercises.size * 25).coerceAtLeast(50),
                                difficulty = ChallengeDifficulty.MEDIUM,
                                exerciseCount = workout.exercises.size,
                                isActive = true,
                                workout = workout
                            )

                            val map = mapOf(
                                "id" to challenge.id,
                                "title" to challenge.title,
                                "description" to challenge.description,
                                "rewardPoints" to challenge.rewardPoints,
                                "difficulty" to challenge.difficulty.name,
                                "exerciseCount" to challenge.exerciseCount,
                                "isActive" to challenge.isActive,
                                "workout" to mapOf(
                                    "date" to challenge.workout.date.toString(),
                                    "title" to challenge.workout.title,
                                    "description" to challenge.workout.description,
                                    "duration" to challenge.workout.duration,
                                    "status" to challenge.workout.status.name,
                                    "photoPath" to challenge.workout.photoPath,
                                    "exercises" to challenge.workout.exercises.map { ex ->
                                        mapOf(
                                            "name" to ex.name,
                                            "type" to ex.type,
                                            "muscle" to ex.muscle,
                                            "equipment" to ex.equipment,
                                            "difficulty" to ex.difficulty,
                                            "instructions" to ex.instructions
                                        )
                                    }
                                )
                            )

                            database.child("communityChallenges").child(today).setValue(map)
                                .addOnSuccessListener {
                                    maintainActiveChallengeState(today) {
                                        deactivateOutdatedUserChallengesForCurrentUser(today) { onComplete(true) }
                                    }
                                }
                                .addOnFailureListener { onComplete(false) }
                        }
                    }
                }
            }
            .addOnFailureListener { onComplete(false) }
    }

    private fun maintainActiveChallengeState(todayId: String, onDone: () -> Unit) {
        database.child("communityChallenges").get()
            .addOnSuccessListener { allSnap ->
                val updates = mutableMapOf<String, Any>()
                for (child in allSnap.children) {
                    val key = child.key ?: continue
                    val path = if (key == todayId) "$key/isActive" else "$key/isActive"
                    updates[path] = (key == todayId)
                }
                if (updates.isEmpty()) { onDone(); return@addOnSuccessListener }
                database.child("communityChallenges").updateChildren(updates)
                    .addOnSuccessListener { onDone() }
                    .addOnFailureListener { onDone() }
            }
            .addOnFailureListener { onDone() }
    }

    private fun deactivateOutdatedUserChallengesForCurrentUser(todayId: String, onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onComplete(false)
        val ref = database.child("users").child(uid).child("challenges")
        ref.get()
            .addOnSuccessListener { snapshot ->
                val updates = mutableMapOf<String, Any>()
                for (child in snapshot.children) {
                    val key = child.key ?: continue
                    val cid = child.child("challengeId").getValue(String::class.java) ?: key
                    val isActive = child.child("isActive").getValue(Boolean::class.java) ?: false
                    if (isActive && cid != todayId) {
                        updates["$key/isActive"] = false
                        updates["$key/isCompleted"] = false
                    }
                }
                if (updates.isEmpty()) { onComplete(true); return@addOnSuccessListener }
                ref.updateChildren(updates)
                    .addOnSuccessListener { onComplete(true) }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchTodayCommunityChallenge(onResult: (Challenge?) -> Unit) {
        val today = LocalDate.now().toString()
        database.child("communityChallenges").child(today).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    onResult(null)
                    return@addOnSuccessListener
                }
                val id = snapshot.child("id").getValue(String::class.java) ?: today
                val title = snapshot.child("title").getValue(String::class.java) ?: ""
                val description = snapshot.child("description").getValue(String::class.java) ?: ""
                val rewardPoints = snapshot.child("rewardPoints").getValue(Int::class.java) ?: 0
                val difficultyStr = snapshot.child("difficulty").getValue(String::class.java) ?: ChallengeDifficulty.MEDIUM.name
                val exerciseCount = snapshot.child("exerciseCount").getValue(Int::class.java) ?: 0
                val isActive = snapshot.child("isActive").getValue(Boolean::class.java) ?: true
                val workoutNode = snapshot.child("workout")
                val dateStr = workoutNode.child("date").getValue(String::class.java) ?: LocalDate.now().toString()
                val wTitle = workoutNode.child("title").getValue(String::class.java) ?: ""
                val wDesc = workoutNode.child("description").getValue(String::class.java) ?: ""
                val wDur = workoutNode.child("duration").getValue(String::class.java) ?: ""
                val wStatusStr = workoutNode.child("status").getValue(String::class.java) ?: WorkoutStatus.CANCELLED.name
                val wPhoto = workoutNode.child("photoPath").getValue(String::class.java)
                val exercisesNode = workoutNode.child("exercises")
                val exercises = exercisesNode.children.map { ex ->
                    ApiExercise(
                        name = ex.child("name").getValue(String::class.java),
                        type = ex.child("type").getValue(String::class.java),
                        muscle = ex.child("muscle").getValue(String::class.java),
                        equipment = ex.child("equipment").getValue(String::class.java),
                        difficulty = ex.child("difficulty").getValue(String::class.java),
                        instructions = ex.child("instructions").getValue(String::class.java)
                    )
                }
                val workout = DailyWorkout(
                    date = LocalDate.parse(dateStr),
                    title = wTitle,
                    description = wDesc,
                    duration = wDur,
                    exercises = exercises,
                    status = try { WorkoutStatus.valueOf(wStatusStr) } catch (_: Exception) { WorkoutStatus.CANCELLED },
                    photoPath = wPhoto
                )
                val challenge = Challenge(
                    id = id,
                    title = title,
                    description = description,
                    rewardPoints = rewardPoints,
                    difficulty = try { ChallengeDifficulty.valueOf(difficultyStr) } catch (_: Exception) { ChallengeDifficulty.MEDIUM },
                    exerciseCount = exerciseCount,
                    isActive = isActive,
                    workout = workout
                )
                onResult(challenge)
            }
            .addOnFailureListener { onResult(null) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchRecentCommunityChallenges(limit: Int = 2, onResult: (List<Challenge>) -> Unit) {
        database.child("communityChallenges").get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.children.mapNotNull { child ->
                    val id = child.child("id").getValue(String::class.java) ?: child.key ?: return@mapNotNull null
                    val title = child.child("title").getValue(String::class.java) ?: ""
                    val description = child.child("description").getValue(String::class.java) ?: ""
                    val rewardPoints = child.child("rewardPoints").getValue(Int::class.java) ?: 0
                    val difficultyStr = child.child("difficulty").getValue(String::class.java) ?: ChallengeDifficulty.MEDIUM.name
                    val exerciseCount = child.child("exerciseCount").getValue(Int::class.java) ?: 0
                    val isActive = child.child("isActive").getValue(Boolean::class.java) ?: true
                    val workoutNode = child.child("workout")
                    val dateStr = workoutNode.child("date").getValue(String::class.java) ?: java.time.LocalDate.now().toString()
                    val wTitle = workoutNode.child("title").getValue(String::class.java) ?: ""
                    val wDesc = workoutNode.child("description").getValue(String::class.java) ?: ""
                    val wDur = workoutNode.child("duration").getValue(String::class.java) ?: ""
                    val wStatusStr = workoutNode.child("status").getValue(String::class.java) ?: WorkoutStatus.CANCELLED.name
                    val wPhoto = workoutNode.child("photoPath").getValue(String::class.java)
                    val exercisesNode = workoutNode.child("exercises")
                    val exercises = exercisesNode.children.map { ex ->
                        ApiExercise(
                            name = ex.child("name").getValue(String::class.java),
                            type = ex.child("type").getValue(String::class.java),
                            muscle = ex.child("muscle").getValue(String::class.java),
                            equipment = ex.child("equipment").getValue(String::class.java),
                            difficulty = ex.child("difficulty").getValue(String::class.java),
                            instructions = ex.child("instructions").getValue(String::class.java)
                        )
                    }
                    val workout = DailyWorkout(
                        date = try { java.time.LocalDate.parse(dateStr) } catch (_: Exception) { java.time.LocalDate.now() },
                        title = wTitle,
                        description = wDesc,
                        duration = wDur,
                        exercises = exercises,
                        status = try { WorkoutStatus.valueOf(wStatusStr) } catch (_: Exception) { WorkoutStatus.CANCELLED },
                        photoPath = wPhoto
                    )
                    Challenge(
                        id = id,
                        title = title,
                        description = description,
                        rewardPoints = rewardPoints,
                        difficulty = try { ChallengeDifficulty.valueOf(difficultyStr) } catch (_: Exception) { ChallengeDifficulty.MEDIUM },
                        exerciseCount = exerciseCount,
                        isActive = isActive,
                        workout = workout
                    )
                }
                    .sortedByDescending { it.id }
                    .take(limit)

                onResult(list)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun fetchAllUserChallengesForCurrentUser(onResult: (List<Pair<UserChallenge, Challenge?>>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(emptyList())
        val userChallengesRef = database.child("users").child(uid).child("challenges")
        userChallengesRef.get()
            .addOnSuccessListener { snapshot ->
                val entries = snapshot.children.map { child ->
                    val challengeId = child.key ?: ""
                    val userId = child.child("userId").getValue(String::class.java) ?: uid
                    val progress = child.child("progress").getValue(Int::class.java) ?: 0
                    val isCompleted = child.child("isCompleted").getValue(Boolean::class.java) ?: false
                    val isActive = child.child("isActive").getValue(Boolean::class.java) ?: false
                    val startedAt = child.child("startedAt").getValue(Long::class.java)
                    UserChallenge(
                        userId = userId,
                        challengeId = challengeId,
                        progress = progress,
                        isCompleted = isCompleted,
                        isActive = isActive,
                        startedAt = startedAt
                    )
                }

                if (entries.isEmpty()) { onResult(emptyList()); return@addOnSuccessListener }

                val results = java.util.concurrent.CopyOnWriteArrayList<Pair<UserChallenge, Challenge?>>()
                var remaining = entries.size

                fun finishIfDone() {
                    if (remaining == 0) {
                        val sorted = results.sortedByDescending { pair ->
                            val uc = pair.first
                            val ch = pair.second
                            val date = try {
                                java.time.LocalDate.parse(ch?.id ?: "1970-01-01")
                            } catch (_: Exception) {
                                val ts = uc.startedAt ?: 0L
                                java.time.Instant.ofEpochMilli(ts).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                            }
                            date
                        }
                        onResult(sorted)
                    }
                }

                entries.forEach { uc ->
                    database.child("communityChallenges").child(uc.challengeId).get()
                        .addOnSuccessListener { chSnap ->
                            val challenge = if (chSnap.exists()) {
                                val id = chSnap.child("id").getValue(String::class.java) ?: uc.challengeId
                                val title = chSnap.child("title").getValue(String::class.java) ?: ""
                                val description = chSnap.child("description").getValue(String::class.java) ?: ""
                                val rewardPoints = chSnap.child("rewardPoints").getValue(Int::class.java) ?: 0
                                val difficultyStr = chSnap.child("difficulty").getValue(String::class.java) ?: ChallengeDifficulty.MEDIUM.name
                                val exerciseCount = chSnap.child("exerciseCount").getValue(Int::class.java) ?: 0
                                val isActive = chSnap.child("isActive").getValue(Boolean::class.java) ?: false
                                val workoutNode = chSnap.child("workout")
                                val dateStr = workoutNode.child("date").getValue(String::class.java) ?: java.time.LocalDate.now().toString()
                                val wTitle = workoutNode.child("title").getValue(String::class.java) ?: ""
                                val wDesc = workoutNode.child("description").getValue(String::class.java) ?: ""
                                val wDur = workoutNode.child("duration").getValue(String::class.java) ?: ""
                                val wStatusStr = workoutNode.child("status").getValue(String::class.java) ?: WorkoutStatus.CANCELLED.name
                                val wPhoto = workoutNode.child("photoPath").getValue(String::class.java)
                                val exercisesNode = workoutNode.child("exercises")
                                val exercises = exercisesNode.children.map { ex ->
                                    ApiExercise(
                                        name = ex.child("name").getValue(String::class.java),
                                        type = ex.child("type").getValue(String::class.java),
                                        muscle = ex.child("muscle").getValue(String::class.java),
                                        equipment = ex.child("equipment").getValue(String::class.java),
                                        difficulty = ex.child("difficulty").getValue(String::class.java),
                                        instructions = ex.child("instructions").getValue(String::class.java)
                                    )
                                }
                                val workout = DailyWorkout(
                                    date = try { java.time.LocalDate.parse(dateStr) } catch (_: Exception) { java.time.LocalDate.now() },
                                    title = wTitle,
                                    description = wDesc,
                                    duration = wDur,
                                    exercises = exercises,
                                    status = try { WorkoutStatus.valueOf(wStatusStr) } catch (_: Exception) { WorkoutStatus.CANCELLED },
                                    photoPath = wPhoto
                                )
                                Challenge(
                                    id = id,
                                    title = title,
                                    description = description,
                                    rewardPoints = rewardPoints,
                                    difficulty = try { ChallengeDifficulty.valueOf(difficultyStr) } catch (_: Exception) { ChallengeDifficulty.MEDIUM },
                                    exerciseCount = exerciseCount,
                                    isActive = isActive,
                                    workout = workout
                                )
                            } else null

                            results.add(uc to challenge)
                            remaining -= 1
                            finishIfDone()
                        }
                        .addOnFailureListener {
                            results.add(uc to null)
                            remaining -= 1
                            finishIfDone()
                        }
                }
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    fun startUserChallengeForCurrentUser(challengeId: String, onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onComplete(false)
        val entity = mapOf(
            "userId" to uid,
            "challengeId" to challengeId,
            "progress" to 0,
            "isCompleted" to false,
            "isActive" to true,
            "startedAt" to System.currentTimeMillis()
        )
        database.child("users").child(uid).child("challenges").child(challengeId).setValue(entity)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun fetchUserChallengeForCurrentUser(challengeId: String, onResult: (UserChallenge?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(null)
        database.child("users").child(uid).child("challenges").child(challengeId).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    onResult(null)
                    return@addOnSuccessListener
                }
                val userId = snapshot.child("userId").getValue(String::class.java) ?: uid
                val progress = snapshot.child("progress").getValue(Int::class.java) ?: 0
                val isCompleted = snapshot.child("isCompleted").getValue(Boolean::class.java) ?: false
                val isActive = snapshot.child("isActive").getValue(Boolean::class.java) ?: false
                val startedAt = snapshot.child("startedAt").getValue(Long::class.java)
                onResult(
                    UserChallenge(
                        userId = userId,
                        challengeId = challengeId,
                        progress = progress,
                        isCompleted = isCompleted,
                        isActive = isActive,
                        startedAt = startedAt
                    )
                )
            }
            .addOnFailureListener { onResult(null) }
    }

    fun updateUserChallengeProgressForCurrentUser(challengeId: String, progress: Int, onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onComplete(false)
        database.child("users").child(uid).child("challenges").child(challengeId).child("progress").setValue(progress)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun markUserChallengeCompletedForCurrentUser(challengeId: String, rewardPoints: Int, onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onComplete(false)
        val ref = database.child("users").child(uid)
        ref.child("challenges").child(challengeId).child("startedAt").get()
            .addOnSuccessListener { snapStarted ->
                val startedAt = snapStarted.getValue(Long::class.java)
                val now = System.currentTimeMillis()
                if (startedAt == null || now - startedAt < 60 * 60 * 1000L) {
                    onComplete(false)
                    return@addOnSuccessListener
                }

                ref.child("challenges").child(challengeId).updateChildren(mapOf(
                    "isCompleted" to true,
                    "isActive" to false,
                    "progress" to 100
                ))
                    .addOnSuccessListener {
                        ref.child("points").get()
                            .addOnSuccessListener { snap ->
                                val current = when (val v = snap.value) {
                                    is Long -> v.toInt()
                                    is Double -> v.toInt()
                                    is Int -> v
                                    is String -> v.toIntOrNull() ?: 0
                                    else -> 0
                                }
                                ref.child("points").setValue(current + rewardPoints)
                                    .addOnSuccessListener { onComplete(true) }
                                    .addOnFailureListener { onComplete(false) }
                            }
                            .addOnFailureListener { onComplete(false) }
                    }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    private object ExercisesGenerator {
        @RequiresApi(Build.VERSION_CODES.O)
        fun generate(muscle: String, difficulty: String, onResult: (DailyWorkout?) -> Unit) {
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val apiResponse: List<ApiExercise> = try {
                    exercisesApi.getExercises(RetrofitHelper.API_KEY, muscle, difficulty)
                } catch (e: Exception) {
                    emptyList()
                }
                val exercises = apiResponse.take(6)
                val formatted = muscle.replace('_', ' ').replaceFirstChar { it.uppercase() }
                val workout = DailyWorkout(
                    date = LocalDate.now(),
                    title = "$formatted Challenge Workout",
                    description = if (exercises.isEmpty()) "No exercises found." else "Community challenge generated",
                    duration = if (exercises.isEmpty()) "0 min" else "${exercises.size * 8} min",
                    exercises = exercises,
                    status = WorkoutStatus.CANCELLED
                )
                withContext(kotlinx.coroutines.Dispatchers.Main) { onResult(workout) }
            }
        }
    }
}
