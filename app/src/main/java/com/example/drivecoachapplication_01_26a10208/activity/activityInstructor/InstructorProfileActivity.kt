package com.example.drivecoachapplication_01_26a10208.activity.activityInstructor

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.drivecoach.model.Instructor
import com.example.drivecoachapplication_01_26a10208.R
import com.example.drivecoachapplication_01_26a10208.databinding.ActivityInstructorProfileBinding
import com.example.drivecoachapplication_01_26a10208.utils.SoundManager
import com.example.drivecoachapplication_01_26a10208.utils.Vibration
import com.example.validation_helper.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class InstructorProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInstructorProfileBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var currentInstructor: Instructor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInstructorProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadCurrentProfileData()

        binding.toggleGroupGender.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                updateGenderButtonsVisuals(checkedId)
            }
        }

        binding.btnSaveProfile.setOnClickListener {
            saveProfileChangesToFirestore()
        }
    }

   //A helper function that physically paints the selected button and clears the other one
    private fun updateGenderButtonsVisuals(checkedId: Int) {
        if (checkedId == R.id.btnGenderMale) {

            binding.btnGenderMale.setBackgroundColor(Color.parseColor("#829FFF"))
            binding.btnGenderMale.setTextColor(Color.BLACK)

            binding.btnGenderFemale.setBackgroundColor(Color.TRANSPARENT)
            binding.btnGenderFemale.setTextColor(Color.WHITE)
        } else if (checkedId == R.id.btnGenderFemale) {
            binding.btnGenderFemale.setBackgroundColor(Color.parseColor("#829FFF"))
            binding.btnGenderFemale.setTextColor(Color.BLACK)

            binding.btnGenderMale.setBackgroundColor(Color.TRANSPARENT)
            binding.btnGenderMale.setTextColor(Color.WHITE)
        }
    }

    private fun loadCurrentProfileData() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("Instructors").document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    currentInstructor = document.toObject(Instructor::class.java)
                    currentInstructor?.let { instructor ->

                        binding.etProfileId.setText(instructor.id)
                        binding.etProfileEmail.setText(instructor.email)
                        binding.etProfileFirstName.setText(instructor.firstName)
                        binding.etProfileLastName.setText(instructor.lastName)
                        binding.etProfilePhone.setText(instructor.phoneNumber)
                        binding.etProfileCity.setText(instructor.city)


                        var activeGenderId = R.id.btnGenderMale
                        instructor.gender?.let { gender ->
                            if (gender.uppercase(Locale.ENGLISH) == "FEMALE") {
                                binding.btnGenderFemale.isChecked = true
                                activeGenderId = R.id.btnGenderFemale
                            } else {
                                binding.btnGenderMale.isChecked = true
                            }
                        } ?: run {
                            binding.btnGenderMale.isChecked = true
                        }


                        updateGenderButtonsVisuals(activeGenderId)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfileChangesToFirestore() {
        val currentUserId = auth.currentUser?.uid ?: return

        val updatedFirstName = binding.etProfileFirstName.text.toString().trim()
        val updatedLastName = binding.etProfileLastName.text.toString().trim()
        val updatedPhone = binding.etProfilePhone.text.toString().trim()
        val updatedCity = binding.etProfileCity.text.toString().trim()

        val selectedGenderId = binding.toggleGroupGender.checkedButtonId
        val updatedGender = if (selectedGenderId == R.id.btnGenderMale) "MALE" else "FEMALE"

        if (updatedFirstName.isEmpty() || updatedLastName.isEmpty()) {
            triggerErrorAlert("First Name and Last Name cannot be empty")
            return
        }

        if (!ValidationUtils.isValidName(updatedFirstName) || !ValidationUtils.isValidName(updatedLastName)) {
            triggerErrorAlert("Names must contain letters only")
            return
        }

        if (updatedPhone.isNotEmpty() && !ValidationUtils.isValidPhoneNumber(updatedPhone)) {
            triggerErrorAlert("Invalid Israeli phone number (Must be 10 digits, starting with 05)")
            return
        }

        binding.btnSaveProfile.isEnabled = false
        binding.btnSaveProfile.text = "Saving changes..."

        val profileUpdates = hashMapOf<String, Any>(
            "firstName" to updatedFirstName,
            "lastName" to updatedLastName,
            "phoneNumber" to updatedPhone,
            "city" to updatedCity,
            "gender" to updatedGender
        )

        db.collection("Instructors").document(currentUserId)
            .update(profileUpdates)
            .addOnSuccessListener {
                Vibration.vibrate(this, 100)
                SoundManager.playSound(this, R.raw.sound_success)
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.btnSaveProfile.isEnabled = true
                binding.btnSaveProfile.text = "Save Changes"
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun triggerErrorAlert(message: String) {
        Vibration.vibrate(this, 200)
        SoundManager.playSound(this, R.raw.sound_error)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}