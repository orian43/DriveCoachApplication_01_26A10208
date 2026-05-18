package com.example.drivecoachapplication_01_26a10208.manager

import com.example.drivecoach.model.Instructor
import com.example.drivecoach.model.LessonRule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object LessonSettingsManager {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

//Logic for retrieving current lesson rules from the cloud
    fun loadCurrentLessonRules(
        onSuccess: (minRequiredLessons: Int, lessonSettings: List<LessonRule>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            onFailure("User session expired. Please log in again.")
            return
        }

        db.collection("Instructors").document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val instructor = document.toObject(Instructor::class.java)
                    if (instructor != null) {
                        //Returning data back to the interface layer
                        onSuccess(instructor.minRequiredLessons, instructor.lessonSettings)
                    } else {
                        onFailure("Failed to parse instructor database model.")
                    }
                } else {
                    onFailure("Instructor account records not found.")
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Unknown database reading error.")
            }
    }

 //To synchronize and update the entire new set of rules in the cloud
    fun saveLessonRules(
        minRequiredLessons: Int,
        rulesList: List<LessonRule>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            onFailure("User session expired. Connection closed.")
            return
        }

        // Create the cloud update map
        val updates = hashMapOf<String, Any>(
            "minRequiredLessons" to minRequiredLessons,
            "lessonSettings" to rulesList
        )

        db.collection("Instructors").document(currentUserId)
            .update(updates)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Cloud sync failed.")
            }
    }
}