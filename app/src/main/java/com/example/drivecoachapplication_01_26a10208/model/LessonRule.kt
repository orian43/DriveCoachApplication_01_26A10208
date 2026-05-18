package com.example.drivecoach.model

// Official Class Method: Pure object mapping with default values for Firestore
data class LessonRule(
    var taskName: String = "",
    var requiredLessons: Int = 0
)