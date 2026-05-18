package com.example.drivecoachapplication_01_26a10208.activity.activityStudent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drivecoachapplication_01_26a10208.activity.MainActivity
import com.example.drivecoachapplication_01_26a10208.databinding.ActivityStudentDashboardBinding
import com.example.drivecoachapplication_01_26a10208.databinding.ItemProgressTaskBinding
import com.example.drivecoach.model.StudentProgressTask
import com.example.drivecoachapplication_01_26a10208.manager.LessonManager
import com.example.drivecoachapplication_01_26a10208.manager.StudentManager
import com.google.firebase.auth.FirebaseAuth
import com.example.drivecoachapplication_01_26a10208.R

class StudentDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentDashboardBinding
    private val auth = FirebaseAuth.getInstance()
    private var studentId = ""
    private var isTeacherView = false

    private val taskList = mutableListOf<StudentProgressTask>()
    private lateinit var taskAdapter: StudentTasksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        studentId = intent.getStringExtra("STUDENT_ID") ?: ""
        // Receiving the new flag that comes from the teacher (if nothing is sent, the default is false)
        isTeacherView = intent.getBooleanExtra("IS_TEACHER_VIEW", false)

        taskAdapter = StudentTasksAdapter(taskList)
        binding.rvStudentDashboardTasks.layoutManager = LinearLayoutManager(this)
        binding.rvStudentDashboardTasks.adapter = taskAdapter

        // Go to the student profile screen
        binding.cardStudentProfileImage.setOnClickListener {
            val intent = Intent(this, StudentProfileActivity::class.java).apply {
                putExtra("STUDENT_ID", studentId)
            }
            startActivity(intent)
        }

        //Go to the lesson list screen
        binding.btnViewMyLessons.setOnClickListener {
            val intent = Intent(this, StudentLessonsActivity::class.java).apply {
                putExtra("STUDENT_ID", studentId)
            }
            startActivity(intent)
        }

        //If a teacher is watching, disable the logout button so they don't accidentally log out of their teacher account.
        if (isTeacherView) {
            binding.btnStudentLogout.visibility = View.GONE
        } else {
            binding.btnStudentLogout.visibility = View.VISIBLE
            binding.btnStudentLogout.setOnClickListener {
                auth.signOut()

                // Clearing the student's local session from the device
                val prefs = getSharedPreferences("DriveCoachPrefs", Context.MODE_PRIVATE)
                prefs.edit().clear().apply()

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadStudentDashboardData()
    }

    private fun loadStudentDashboardData() {
        if (studentId.isEmpty()) return

        LessonManager.countCompletedLessonsForStudent(studentId,
            onSuccess = { totalCompleted ->
                binding.tvStudentCircleCount.text = totalCompleted.toString()
                binding.pbStudentProgressCircle.progress = if (totalCompleted > 28) 28 else totalCompleted
            },
            onFailure = { error ->
                Toast.makeText(this, "Error loading statistics: $error", Toast.LENGTH_SHORT).show()
            }
        )

        StudentManager.getStudentProfile(studentId,
            onSuccess = { studentProfile ->
                if (isTeacherView) {
                    binding.tvStudentWelcomeTitle.text = "${studentProfile.firstName} ${studentProfile.lastName}"
                } else {
                    binding.tvStudentWelcomeTitle.text = "Welcome, ${studentProfile.firstName}!"
                }
                binding.tvStudentStartDateDisplay.text = "Started: ${studentProfile.startDate}"

                taskList.clear()
                taskList.addAll(studentProfile.progress)
                taskAdapter.notifyDataSetChanged()
            },
            onFailure = { error ->
                Toast.makeText(this, "Profile sync error: $error", Toast.LENGTH_LONG).show()
            }
        )
    }

    private inner class StudentTasksAdapter(private val dataset: List<StudentProgressTask>) :
        RecyclerView.Adapter<StudentTasksAdapter.ViewHolder>() {

        inner class ViewHolder(val itemBinding: ItemProgressTaskBinding) :
            RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val itemBinding = ItemProgressTaskBinding.inflate(layoutInflater, parent, false)
            return ViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val task = dataset[holder.adapterPosition]
            holder.itemBinding.tvProgressTaskName.text = task.taskName
            holder.itemBinding.tvProgressTaskCount.text = "Progress: ${task.completedLessons} / ${task.requiredLessons}"
            holder.itemBinding.btnIncrementTask.visibility = View.GONE
        }

        override fun getItemCount(): Int = dataset.size
    }
}