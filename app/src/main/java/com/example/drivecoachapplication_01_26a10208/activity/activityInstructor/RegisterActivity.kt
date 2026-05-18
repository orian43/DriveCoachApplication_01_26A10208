package com.example.drivecoachapplication_01_26a10208.activity.activityInstructor

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.drivecoach.model.Instructor
import com.example.drivecoachapplication_01_26a10208.R
import com.example.drivecoachapplication_01_26a10208.databinding.ActivityRegisterBinding
import com.example.drivecoachapplication_01_26a10208.manager.InstructorAuthManager
import com.example.drivecoachapplication_01_26a10208.utils.SoundManager
import com.example.drivecoachapplication_01_26a10208.utils.Vibration
import com.example.validation_helper.ValidationUtils

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegisterInstructor.setOnClickListener {
            performRegistration()
        }
    }

    private fun performRegistration() {

        val idNumber = binding.etIdNumber.text.toString().trim()
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etRegisterEmail.text.toString().trim()
        val password = binding.etRegisterPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()


        if (idNumber.isEmpty() || fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            triggerErrorAlert("Please fill in all fields")
            return
        }

        //9-digit validity check via the new external library
        if (!ValidationUtils.isValidIsraeliId(idNumber)) {
            triggerErrorAlert("Invalid ID number (Must be exactly 9 digits)")
            return
        }


        if (!ValidationUtils.isValidName(fullName)) {
            triggerErrorAlert("Please enter a valid full name (letters only)")
            return
        }

        if (!ValidationUtils.isValidEmail(email)) {
            triggerErrorAlert("Invalid email address format")
            return
        }

        if (!ValidationUtils.isValidPassword(password)) {
            triggerErrorAlert("Password must be at least 6 characters long")
            return
        }

        if (password != confirmPassword) {
            triggerErrorAlert("Passwords do not match")
            return
        }

        binding.btnRegisterInstructor.isEnabled = false
        binding.btnRegisterInstructor.text = "Creating account..."

        val nameParts = fullName.split(" ", limit = 2)
        val firstName = nameParts.getOrNull(0) ?: fullName
        val lastName = nameParts.getOrNull(1) ?: ""

        // Creating the teacher object and entering the original ID (id) directly into it
        val newInstructor = Instructor(
            id = idNumber,
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password
        )


        InstructorAuthManager.registerInstructor(
            instructor = newInstructor,
            onSuccess = {
                SoundManager.playSound(this, R.raw.sound_success)
                Toast.makeText(this, "Registration Successful!", Toast.LENGTH_LONG).show()

                val intent = Intent(this, InstructorDashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            },
            onFailure = { errorMessage ->
                triggerErrorAlert(errorMessage)
                binding.btnRegisterInstructor.isEnabled = true
                binding.btnRegisterInstructor.text = "Create Account"
            }
        )
    }

    private fun triggerErrorAlert(message: String) {
        Vibration.vibrate(this, 200)
        SoundManager.playSound(this, R.raw.sound_error)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}