package com.example.fitmate.data

import com.example.fitmate.model.UserProfile
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
                val userProfile = snapshot.getValue(UserProfile::class.java)
                onResult(userProfile)
            }
            .addOnFailureListener { _ ->
                onResult(null)
            }
    }

    fun updateUserProfile(userProfile: UserProfile, onComplete: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onComplete(false)
            return
        }

        database.child("users").child(uid).setValue(userProfile)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}