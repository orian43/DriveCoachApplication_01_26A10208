package com.example.drivecoachapplication_01_26a10208.activity.activityInstructor

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.drivecoachapplication_01_26a10208.R
import com.example.drivecoachapplication_01_26a10208.databinding.ActivityAddStudentBinding
import com.example.drivecoach.model.Student
import com.example.drivecoachapplication_01_26a10208.manager.StudentManager
import com.example.drivecoachapplication_01_26a10208.utils.SoundManager
import com.example.drivecoachapplication_01_26a10208.utils.Vibration
import com.example.validation_helper.ValidationUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddStudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStudentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Listening to the gender square color swap
        binding.toggleStudentGender.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                updateGenderToggleVisuals(checkedId)
            }
        }

        // Initial coloring of the default button (male)
        updateGenderToggleVisuals(R.id.btnStudentMale)

        binding.btnSaveStudent.setOnClickListener {
            performStudentOnboarding()
        }
    }

    private fun updateGenderToggleVisuals(checkedId: Int) {
        if (checkedId == R.id.btnStudentMale) {
            binding.btnStudentMale.setBackgroundColor(Color.parseColor("#829FFF"))
            binding.btnStudentMale.setTextColor(Color.BLACK)
            binding.btnStudentFemale.setBackgroundColor(Color.TRANSPARENT)
            binding.btnStudentFemale.setTextColor(Color.WHITE)
        } else if (checkedId == R.id.btnStudentFemale) {
            binding.btnStudentFemale.setBackgroundColor(Color.parseColor("#829FFF"))
            binding.btnStudentFemale.setTextColor(Color.BLACK)
            binding.btnStudentMale.setBackgroundColor(Color.TRANSPARENT)
            binding.btnStudentMale.setTextColor(Color.WHITE)
        }
    }

    private fun performStudentOnboarding() {
        val idNumber = binding.etStudentId.text.toString().trim()
        val firstName = binding.etStudentFirstName.text.toString().trim()
        val lastName = binding.etStudentLastName.text.toString().trim()
        val email = binding.etStudentEmail.text.toString().trim()
        val phone = binding.etStudentPhone.text.toString().trim()
        val city = binding.etStudentCity.text.toString().trim()
        val password = binding.etStudentPassword.text.toString().trim()

        val selectedGenderId = binding.toggleStudentGender.checkedButtonId
        val gender = if (selectedGenderId == R.id.btnStudentFemale) "FEMALE" else "MALE"


        if (idNumber.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || city.isEmpty() || password.isEmpty()) {
            triggerUIFault("Please fill in all student records")
            return
        }


        if (!ValidationUtils.isValidIsraeliId(idNumber)) {
            triggerUIFault("Invalid Student ID structure (Must be exactly 9 digits)")
            return
        }


        if (!ValidationUtils.isValidName(firstName) || !ValidationUtils.isValidName(lastName)) {
            triggerUIFault("Names must contain alphabetic letters only")
            return
        }


        if (!ValidationUtils.isValidEmail(email)) {
            triggerUIFault("Invalid email format structures")
            return
        }


        if (!ValidationUtils.isValidPhoneNumber(phone)) {
            triggerUIFault("Invalid phone layout (Must be 10 digits, starting with 05)")
            return
        }

        if (password.length < 4) {
            triggerUIFault("Password must be at least 4 characters long")
            return
        }

        binding.btnSaveStudent.isEnabled = false
        binding.btnSaveStudent.text = "Syncing with cloud databases..."

        // Automatic current date generation in a clean day/month/year format
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentRegistrationDate = dateFormatter.format(Date())


        val newStudent = Student(
            id = idNumber,
            firstName = firstName,
            lastName = lastName,
            email = email,
            phoneNumber = phone,
            city = city,
            gender = gender,
            password = password,
            startDate = currentRegistrationDate
        )


        StudentManager.createNewStudent(
            student = newStudent,
            onSuccess = {
                Vibration.vibrate(this, 100)
                SoundManager.playSound(this, R.raw.sound_success)
                Toast.makeText(this, "Student registered and linked successfully!", Toast.LENGTH_LONG).show()
                finish()
            },
            onFailure = { errorText ->
                binding.btnSaveStudent.isEnabled = true
                binding.btnSaveStudent.text = "Add Student to System"
                Toast.makeText(this, errorText, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun triggerUIFault(message: String) {
        Vibration.vibrate(this, 200)
        SoundManager.playSound(this, R.raw.sound_error)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}