package com.example.drivecoachapplication_01_26a10208.activity.activityInstructor

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.drivecoachapplication_01_26a10208.R
import com.example.drivecoachapplication_01_26a10208.databinding.ActivityScheduleLessonBinding
import com.example.drivecoach.model.Lesson
import com.example.drivecoachapplication_01_26a10208.manager.LessonManager
import com.example.drivecoachapplication_01_26a10208.utils.SoundManager
import com.example.drivecoachapplication_01_26a10208.utils.Vibration
import java.util.*

class ScheduleLessonActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleLessonBinding

    private var selectedDate = ""
    private var selectedTime = ""
    private var studentId = ""
    private var studentName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleLessonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Receiving the details transferred from the list via the Intent
        studentId = intent.getStringExtra("STUDENT_ID") ?: ""
        studentName = intent.getStringExtra("STUDENT_NAME") ?: "Unknown Student"

        binding.tvTargetStudentName.text = "For Student: $studentName"


        binding.btnSelectDate.setOnClickListener {
            openDatePicker()
        }

        binding.btnSelectTime.setOnClickListener {
            openTimePicker()
        }

        binding.btnSaveLesson.setOnClickListener {
            validateAndSaveLesson()
        }
    }

    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, android.R.style.Theme_Material_Dialog_Alert, { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
            binding.btnSelectDate.text = selectedDate
        }, year, month, day)

        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun openTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // Round Hour Constraint: Open the clock with the default minutes locked to 0
        val timePickerDialog = TimePickerDialog(this, R.style.CustomTimePickerTheme, { _, selectedHour, _ ->
            // Completely ignoring what the user chose in minutes and setting it to a hard 00 in the string
            selectedTime = String.format(Locale.getDefault(), "%02d:00", selectedHour)
            binding.btnSelectTime.text = selectedTime
        }, hour, 0, true)

        timePickerDialog.show()
    }

    private fun validateAndSaveLesson() {
        val locationInput = binding.etLessonLocation.text.toString().trim()

        if (selectedDate.isEmpty()) {
            triggerUIFault("Please select a valid date for the lesson")
            return
        }

        if (selectedTime.isEmpty()) {
            triggerUIFault("Please select a specific time for the lesson")
            return
        }

        if (locationInput.isEmpty()) {
            triggerUIFault("Please specify a pickup location details")
            return
        }

        binding.btnSaveLesson.isEnabled = false
        binding.btnSaveLesson.text = "Validating & Scheduling..."

        val newLesson = Lesson(
            studentId = studentId,
            studentName = studentName,
            date = selectedDate,
            time = selectedTime,
            location = locationInput
        )

        // Contacting a Manager who is currently performing a duplicate appointment check in the cloud
        LessonManager.createNewLesson(
            lesson = newLesson,
            onSuccess = {
                Vibration.vibrate(this, 100)
                SoundManager.playSound(this, R.raw.sound_success)
                Toast.makeText(this, "Lesson scheduled successfully!", Toast.LENGTH_LONG).show()
                finish()
            },
            onFailure = { errorText ->
                binding.btnSaveLesson.isEnabled = true
                binding.btnSaveLesson.text = "Confirm & Schedule Lesson"
                triggerUIFault(errorText)
            }
        )
    }

    private fun triggerUIFault(message: String) {
        Vibration.vibrate(this, 200)
        SoundManager.playSound(this, R.raw.sound_error)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}