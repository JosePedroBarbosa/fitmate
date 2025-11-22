package com.example.fitmate.data

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fitmate.model.ApiExercise
import com.example.fitmate.model.DailyWorkout
import com.example.fitmate.model.enums.FitnessLevelType
import com.example.fitmate.model.enums.GenderType
import com.example.fitmate.model.UserProfile
import com.example.fitmate.model.WeightLossGoal
import com.example.fitmate.model.enums.GoalType
import com.example.fitmate.model.MuscleGainGoal
import com.example.fitmate.model.Goal
import com.example.fitmate.model.enums.WorkoutStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate

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

                        foundWorkout = DailyWorkout(
                            date = LocalDate.parse(dateStr),
                            title = title,
                            description = description,
                            duration = duration,
                            exercises = exercises,
                            status = WorkoutStatus.STARTED
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
                            status = status
                        ), id
                    )
                }.sortedByDescending { it.first.date }

                onResult(list)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }
}