package com.example.drivecoachapplication_01_26a10208.activity.activityInstructor

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.drivecoach.model.Instructor
import com.example.drivecoachapplication_01_26a10208.activity.MainActivity
import com.example.drivecoachapplication_01_26a10208.databinding.ActivityInstructorDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InstructorDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInstructorDashboardBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInstructorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Automatically calculate and display the current day and date
        displayCurrentDateTime()

        binding.cardProfileImage.setOnClickListener {
            val intent = Intent(this, InstructorProfileActivity::class.java)
            startActivity(intent)
        }

        binding.cardAddNewStudent.setOnClickListener {
            val intent = Intent(this, AddStudentActivity::class.java)
            startActivity(intent)

        }

        binding.cardViewStudents.setOnClickListener {
            val intent = Intent(this, ViewStudentsActivity::class.java)
            startActivity(intent)

        }

        binding.cardUpcomingLessons.setOnClickListener {
            val intent = Intent(this, UpcomingLessonsActivity::class.java)
            startActivity(intent)

        }

        binding.cardLessonSettings.setOnClickListener {
            val intent = Intent(this, LessonSettingsActivity::class.java)
            startActivity(intent)
        }


        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    /**
     * Every time the screen returns to the foreground (including returning from the delete student screen)
     * We pull fresh data from the cloud and the number is updated in an instant without the need for a reboot!
     */
    override fun onResume() {
        super.onResume()
        loadInstructorDataFromFirestore()
    }

    private fun displayCurrentDateTime() {
        try {
            val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US)
            val currentDateString = dateFormat.format(Date())
            binding.tvCurrentDate.text = currentDateString
        } catch (e: Exception) {
            binding.tvCurrentDate.text = "Welcome Back"
        }
    }

    private fun loadInstructorDataFromFirestore() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("Instructors").document(currentUserId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val instructor = document.toObject(Instructor::class.java)
                    if (instructor != null) {
                        binding.tvWelcomeTitle.text = "Welcome, ${instructor.firstName} ${instructor.lastName}"
                        // Synchronize the exact number of students live from the cloud array
                        binding.tvActiveStudentsCount.text = instructor.studentsList.size.toString()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to sync dashboard: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}