package com.example.drivecoach.model

data class Student(
    var id: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var city: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var password: String = "",
    var startDate: String = "",
    var endDate: String = "",
    var testDate: String = "",
    var instructorId: String = "",
    var gender: String = "",
    var active: Boolean = true,

    var lessonsCount: Int = 0,
    var targetLessonsCount: Int = 28,

    var progress: List<StudentProgressTask> = listOf(
        StudentProgressTask("Parallel Parking", 0),
        StudentProgressTask("Highway Driving", 0),
        StudentProgressTask("Clutch Control", 0),
        StudentProgressTask("Traffic Circles", 0)
    )
)