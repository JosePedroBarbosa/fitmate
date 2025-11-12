package com.example.fitmate.data

import com.example.fitmate.model.enums.FitnessLevelType
import com.example.fitmate.model.enums.GenderType
import com.example.fitmate.model.UserProfile
import com.example.fitmate.model.WeightLossGoal
import com.example.fitmate.model.enums.GoalType
import com.example.fitmate.model.MuscleGainGoal
import com.example.fitmate.model.Goal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

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

        val map = mapOf(
            "uid" to userProfile.uid,
            "name" to userProfile.name,
            "email" to userProfile.email,
            "points" to userProfile.points,
            "height" to userProfile.height,
            "weight" to userProfile.weight,
            "dateOfBirth" to userProfile.dateOfBirth,
            "gender" to userProfile.gender?.label,
            "fitnessLevel" to userProfile.fitnessLevel?.label
        )

        database.child("users").child(uid).setValue(map)
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
}