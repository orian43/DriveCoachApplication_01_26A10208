package com.example.drivecoachapplication_01_26a10208.manager

import com.example.drivecoach.model.Student
import com.example.drivecoach.model.Instructor
import com.example.drivecoach.model.StudentProgressTask
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object StudentManager {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()


    //Creating a new student in the system and injecting the current teacher's mandatory quotas directly into their profile
    fun createNewStudent(
        student: Student,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentInstructorId = auth.currentUser?.uid
        if (currentInstructorId == null) {
            onFailure("Session expired. Please reconnect.")
            return
        }

        // Retrieving the connected teacher's profile to copy their required lesson settings
        db.collection("Instructors").document(currentInstructorId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    onFailure("Instructor account records not found.")
                    return@addOnSuccessListener
                }

                val instructor = document.toObject(Instructor::class.java)
                if (instructor == null) {
                    onFailure("Failed to decode instructor setup data.")
                    return@addOnSuccessListener
                }

                // Copying teacher rules to the Progress array
                val dynamicProgressList = instructor.lessonSettings.map { rule ->
                    StudentProgressTask(
                        taskName = rule.taskName,
                        completedLessons = 0,
                        requiredLessons = rule.requiredLessons
                    )
                }

                student.progress = dynamicProgressList
                student.instructorId = currentInstructorId

                //Saving the student document in the Students collection
                db.collection("Students").document(student.id).set(student)
                    .addOnSuccessListener {
                        // Adding the student ID to the current teacher's student array to update the counter
                        db.collection("Instructors").document(currentInstructorId)
                            .update("studentsList", FieldValue.arrayUnion(student.id))
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { e -> onFailure("Failed to bind to instructor list: ${e.message}") }
                    }
                    .addOnFailureListener { e -> onFailure("Failed to save account database: ${e.message}") }
            }
            .addOnFailureListener { e -> onFailure("Cloud connectivity fault: ${e.message}") }
    }

 //Retrieving all students associated with the current teacher
    fun loadInstructorStudents(
        onSuccess: (List<Student>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentInstructorId = auth.currentUser?.uid
        if (currentInstructorId == null) {
            onFailure("Instructor session expired.")
            return
        }

        db.collection("Students")
            .whereEqualTo("instructorId", currentInstructorId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val studentsList = querySnapshot.toObjects(Student::class.java)
                onSuccess(studentsList)
            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Failed to pull roster.") }
    }

  //Retrieving an individual student profile by ID
    fun getStudentProfile(
        studentId: String,
        onSuccess: (Student) -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection("Students").document(studentId).get()
            .addOnSuccessListener { document ->
                val student = document.toObject(Student::class.java)
                if (student != null) onSuccess(student) else onFailure("Student profile missing.")
            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Cloud sync issue.") }
    }

//Saving the student's updated progress task set in the cloud
    fun updateStudentProgressTasks(
        studentId: String,
        updatedProgress: List<StudentProgressTask>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection("Students").document(studentId)
            .update("progress", updatedProgress)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure("Failed to update goals layout: ${e.message}") }
    }

 //Complete and duplicate deletion of a student from the system
    fun deleteStudentFromSystem(
        studentId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val currentInstructorId = auth.currentUser?.uid ?: return

        db.collection("Students").document(studentId).delete()
            .addOnSuccessListener {
                db.collection("Instructors").document(currentInstructorId)
                    .update("studentsList", FieldValue.arrayRemove(studentId))
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure("Wipe detached cleanly but failed to remove link: ${e.message}") }
            }
            .addOnFailureListener { e -> onFailure("Failed to wipe account document: ${e.message}") }
    }

 //Updating and resetting a student password by the teacher
    fun updateStudentPassword(
        studentId: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection("Students").document(studentId)
            .update("password", newPassword)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure("Failed to update password: ${e.message}") }
    }
}