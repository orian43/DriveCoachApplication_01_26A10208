package com.example.drivecoachapplication_01_26a10208.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.drivecoachapplication_01_26a10208.databinding.ActivityLoginBinding
import com.example.drivecoachapplication_01_26a10208.utils.SoundManager
import com.example.drivecoachapplication_01_26a10208.utils.Vibration
import com.example.drivecoachapplication_01_26a10208.R
import com.example.drivecoachapplication_01_26a10208.activity.activityInstructor.InstructorDashboardActivity
import com.example.drivecoachapplication_01_26a10208.activity.activityInstructor.RegisterActivity
import com.example.drivecoachapplication_01_26a10208.activity.activityStudent.StudentDashboardActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var userType: String = "Instructors"

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userType = intent.getStringExtra("USER_TYPE") ?: "Instructors"

        // Hiding the registration button for a student (registration is only open to teachers)
        if (userType == "Students") {
            binding.btnGoToRegister.visibility = View.GONE
        } else {
            binding.btnGoToRegister.visibility = View.VISIBLE
        }

        binding.btnGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            handleForgotPassword()
        }

        binding.btnLogin.setOnClickListener {
            performFirebaseLogin()
        }
    }

    private fun performFirebaseLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Vibration.vibrate(this, 200)
            SoundManager.playSound(this, R.raw.sound_error)
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Checking..."


        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId != null) {
                    verifyUserRoleInFirestore(userId, email)
                } else {
                    resetLoginButton()
                }
            }
            .addOnFailureListener { exception ->

                if (userType == "Students") {
                    checkDirectStudentLogin(email, password)
                } else {
                    Vibration.vibrate(this, 200)
                    SoundManager.playSound(this, R.raw.sound_error)
                    Toast.makeText(this, "Login Failed: ${exception.message}", Toast.LENGTH_LONG).show()
                    resetLoginButton()
                }
            }
    }

    private fun verifyUserRoleInFirestore(userId: String, email: String) {
        if (userType == "Instructors") {
            db.collection("Instructors").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        SoundManager.playSound(this, R.raw.sound_success)
                        val intent = Intent(this, InstructorDashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        auth.signOut()
                        triggerAccessDenied()
                    }
                }
                .addOnFailureListener { e ->
                    auth.signOut()
                    Toast.makeText(this, "Database error: ${e.message}", Toast.LENGTH_SHORT).show()
                    resetLoginButton()
                }
        } else {
            db.collection("Students").whereEqualTo("email", email).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        SoundManager.playSound(this, R.raw.sound_success)
                        val studentDoc = querySnapshot.documents[0]
                        val studentId = studentDoc.id

                        val intent = Intent(this, StudentDashboardActivity::class.java).apply {
                            putExtra("STUDENT_ID", studentId)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        auth.signOut()
                        triggerAccessDenied()
                    }
                }
                .addOnFailureListener { e ->
                    auth.signOut()
                    Toast.makeText(this, "Database error: ${e.message}", Toast.LENGTH_SHORT).show()
                    resetLoginButton()
                }
        }
    }

    private fun checkDirectStudentLogin(email: String, password: String) {
        db.collection("Students")
            .whereEqualTo("email", email)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    SoundManager.playSound(this, R.raw.sound_success)
                    val studentDoc = querySnapshot.documents[0]
                    val studentId = studentDoc.id

                    val intent = Intent(this, StudentDashboardActivity::class.java).apply {
                        putExtra("STUDENT_ID", studentId)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                } else {
                    Vibration.vibrate(this, 200)
                    SoundManager.playSound(this, R.raw.sound_error)
                    Toast.makeText(this, "Invalid email or password credentials", Toast.LENGTH_LONG).show()
                    resetLoginButton()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Mapping connection error: ${e.message}", Toast.LENGTH_LONG).show()
                resetLoginButton()
            }
    }

    private fun handleForgotPassword() {
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            Vibration.vibrate(this, 100)
            Toast.makeText(this, "Please enter your email to reset password", Toast.LENGTH_LONG).show()
            return
        }

        //Full blocking message for the student
        if (userType == "Students") {
            Vibration.vibrate(this, 100)
            SoundManager.playSound(this, R.raw.sound_error)
            Toast.makeText(this, "Students cannot reset passwords via email. Please contact your driving instructor.", Toast.LENGTH_LONG).show()
        } else {
            // Teacher - An email will be sent to update your password.
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(this, "Reset link sent to your email!", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun triggerAccessDenied() {
        Vibration.vibrate(this, 300)
        SoundManager.playSound(this, R.raw.sound_error)
        Toast.makeText(this, "Access Denied: Incorrect user type selected.", Toast.LENGTH_LONG).show()
        resetLoginButton()
    }

    private fun resetLoginButton() {
        binding.btnLogin.isEnabled = true
        binding.btnLogin.text = "Login"
    }
}