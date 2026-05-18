package com.example.drivecoachapplication_01_26a10208.manager

import com.example.drivecoach.model.Lesson
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LessonManager {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()


    //  Logic for creating a new driving lesson that includes checking for time conflicts in the cloud

    fun createNewLesson(
        lesson: Lesson,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentInstructorId = auth.currentUser?.uid
        if (currentInstructorId == null) {
            onFailure("Instructor session is inactive. Request denied.")
            return
        }

        lesson.instructorId = currentInstructorId
        lesson.completed = false

        db.collection("Lessons")
            .whereEqualTo("instructorId", currentInstructorId)
            .whereEqualTo("date", lesson.date)
            .whereEqualTo("time", lesson.time)
            .whereEqualTo("completed", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    onFailure("Scheduling Conflict: You already have another driving lesson scheduled at this specific hour.")
                    return@addOnSuccessListener
                }

                val newLessonRef = db.collection("Lessons").document()
                lesson.id = newLessonRef.id

                newLessonRef.set(lesson)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure("Failed to schedule lesson: ${e.message}") }
            }
            .addOnFailureListener { e ->
                onFailure("Time validation error: ${e.message}")
            }
    }

  //Retrieving only active lessons and sorting chronologically from closest to furthest
    fun loadUpcomingLessons(
        onSuccess: (List<Lesson>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentInstructorId = auth.currentUser?.uid
        if (currentInstructorId == null) {
            onFailure("Instructor session expired.")
            return
        }

        db.collection("Lessons")
            .whereEqualTo("instructorId", currentInstructorId)
            .whereEqualTo("completed", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val lessonsList = querySnapshot.toObjects(Lesson::class.java)

                val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val sortedLessons = lessonsList.sortedBy { lesson ->
                    try {
                        dateTimeFormatter.parse("${lesson.date} ${lesson.time}")
                    } catch (e: Exception) {
                        Date(Long.MAX_VALUE)
                    }
                }

                onSuccess(sortedLessons)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to load upcoming lessons.")
            }
    }

 //Updating a lesson status to "Completed" in the database
    fun markLessonAsCompleted(
        lessonId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection("Lessons").document(lessonId)
            .update("completed", true)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure("Failed to update status: ${e.message}") }
    }

//Retrieving all lessons (both past and future) for a specific student by their ID
    fun loadAllLessonsForStudent(
        studentId: String,
        onSuccess: (List<Lesson>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection("Lessons")
            .whereEqualTo("studentId", studentId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val lessonsList = querySnapshot.toObjects(Lesson::class.java)

                val sortedLessons = lessonsList.sortedWith(
                    compareByDescending<Lesson> { it.date }.thenByDescending { it.time }
                )
                onSuccess(sortedLessons)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to retrieve student lesson history.")
            }
    }

    //Physical and permanent deletion of a lesson document from the database
    fun deleteLessonFromSystem(
        lessonId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection("Lessons").document(lessonId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure("Database deletion error: ${e.message}") }
    }


    //Counting the number of lessons the student has actually completed so far
    fun countCompletedLessonsForStudent(
        studentId: String,
        onSuccess: (Int) -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection("Lessons")
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("completed", true)
            .get()
            .addOnSuccessListener { querySnapshot ->
                onSuccess(querySnapshot.size())
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to count completed sessions.")
            }
    }

    fun loadStudentLessons(
        studentId: String,
        onSuccess: (List<Map<String, Any>>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        FirebaseFirestore.getInstance().collection("Lessons")
            .whereEqualTo("studentId", studentId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val lessonsList = mutableListOf<Map<String, Any>>()
                for (doc in querySnapshot.documents) {
                    doc.data?.let { lessonsList.add(it) }
                }
                onSuccess(lessonsList)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Unknown database error")
            }
    }

}