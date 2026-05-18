package com.example.drivecoachapplication_01_26a10208.activity.activityStudent

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.drivecoachapplication_01_26a10208.R
import com.example.drivecoachapplication_01_26a10208.databinding.ActivityStudentProfileBinding
import com.example.drivecoach.model.Student
import com.example.drivecoachapplication_01_26a10208.manager.StudentManager
import com.example.drivecoachapplication_01_26a10208.utils.SoundManager
import com.example.drivecoachapplication_01_26a10208.utils.Vibration
import com.example.validation_helper.ValidationUtils
import com.google.firebase.firestore.FirebaseFirestore

class StudentProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentProfileBinding
    private val db = FirebaseFirestore.getInstance()
    private var studentId = ""
    private var cachedStudentProfile: Student? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        studentId = intent.getStringExtra("STUDENT_ID") ?: ""

        loadStudentProfileFields()

        binding.btnSaveStudentProfile.setOnClickListener {
            validateAndSyncProfileChanges()
        }
    }

    private fun loadStudentProfileFields() {
        if (studentId.isEmpty()) return

        StudentManager.getStudentProfile(studentId,
            onSuccess = { student ->
                cachedStudentProfile = student

                binding.etProfileStudentId.setText(student.id)
                binding.etProfileStudentEmail.setText(student.email)
                binding.etProfileStudentFirstName.setText(student.firstName)
                binding.etProfileStudentLastName.setText(student.lastName)
                binding.etProfileStudentPhone.setText(student.phoneNumber)
                binding.etProfileStudentCity.setText(student.city)
            },
            onFailure = { error ->
                Toast.makeText(this, "Failed to load profile data: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun validateAndSyncProfileChanges() {
        val studentProfile = cachedStudentProfile ?: return

        val inputFirstName = binding.etProfileStudentFirstName.text.toString().trim()
        val inputLastName = binding.etProfileStudentLastName.text.toString().trim()
        val inputPhone = binding.etProfileStudentPhone.text.toString().trim()
        val inputCity = binding.etProfileStudentCity.text.toString().trim()

        if (inputFirstName.isEmpty() || inputLastName.isEmpty() || inputPhone.isEmpty() || inputCity.isEmpty()) {
            triggerUIFault("Fields cannot be left empty")
            return
        }

        if (!ValidationUtils.isValidName(inputFirstName) || !ValidationUtils.isValidName(inputLastName)) {
            triggerUIFault("Names must contain alphabet characters only")
            return
        }

        if (!ValidationUtils.isValidPhoneNumber(inputPhone)) {
            triggerUIFault("Invalid phone number format setup")
            return
        }

        binding.btnSaveStudentProfile.isEnabled = false
        binding.btnSaveStudentProfile.text = "Syncing profile updates..."

        studentProfile.firstName = inputFirstName
        studentProfile.lastName = inputLastName
        studentProfile.phoneNumber = inputPhone
        studentProfile.city = inputCity

//Direct and secure saving to the cloud according to the correct studentId
        db.collection("Students").document(studentId).set(studentProfile)
            .addOnSuccessListener {
                Vibration.vibrate(this, 100)
                SoundManager.playSound(this, R.raw.sound_success)
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.btnSaveStudentProfile.isEnabled = true
                binding.btnSaveStudentProfile.text = "Save Profile Changes"
                triggerUIFault("Sync error: ${e.message}")
            }
    }

    private fun triggerUIFault(message: String) {
        Vibration.vibrate(this, 200)
        SoundManager.playSound(this, R.raw.sound_error)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}