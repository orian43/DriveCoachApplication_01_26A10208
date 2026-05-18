package com.example.drivecoach.model

// Official Class Method: Pure object mapping for student progress card
data class StudentProgressTask(
    var taskName: String = "",
    var completedLessons: Int = 0,
    var requiredLessons: Int = 0
)