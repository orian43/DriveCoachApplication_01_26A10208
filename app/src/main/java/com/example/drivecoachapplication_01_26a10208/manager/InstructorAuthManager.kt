package com.example.drivecoachapplication_01_26a10208.manager

import com.example.drivecoach.model.Instructor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object InstructorAuthManager {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun registerInstructor(
        instructor: Instructor,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Creating a user in Firebase Authentication using email and password
        auth.createUserWithEmailAndPassword(instructor.email, instructor.password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId != null) {

                    db.collection("Instructors")
                        .document(userId)
                        .set(instructor)
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            onFailure("Database error: ${e.message}")
                        }
                } else {
                    onFailure("Failed to retrieve system User ID.")
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Registration failed")
            }
    }
}