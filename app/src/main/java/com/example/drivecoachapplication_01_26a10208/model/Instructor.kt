package com.example.drivecoach.model

data class Instructor(
    var id: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var city: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var password: String = "",
    var gender: String = "",
    var studentsList: List<String> = listOf(),

    var minRequiredLessons: Int = 28,

    // List of teacher-specific task objects
    var lessonSettings: List<LessonRule> = listOf(
        LessonRule("Parallel Parking", 5),
        LessonRule("Highway Driving", 3),
        LessonRule("Clutch Control", 4),
        LessonRule("Traffic Circles", 5)
    )
)