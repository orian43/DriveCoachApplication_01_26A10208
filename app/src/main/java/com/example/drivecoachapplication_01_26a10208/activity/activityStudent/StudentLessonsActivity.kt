package com.example.drivecoachapplication_01_26a10208.activity.activityStudent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drivecoachapplication_01_26a10208.R
import com.example.drivecoachapplication_01_26a10208.databinding.ActivityStudentLessonsBinding
import com.example.drivecoachapplication_01_26a10208.manager.LessonManager

class StudentLessonsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentLessonsBinding
    private var studentId = ""
    private val lessonsList = mutableListOf<Map<String, Any>>()
    private lateinit var lessonsAdapter: StudentLessonsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentLessonsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        studentId = intent.getStringExtra("STUDENT_ID") ?: ""

        lessonsAdapter = StudentLessonsAdapter(lessonsList)
        binding.rvStudentLessonsList.layoutManager = LinearLayoutManager(this)
        binding.rvStudentLessonsList.adapter = lessonsAdapter

        fetchDataFromDataLayer()
    }

    private fun fetchDataFromDataLayer() {
        if (studentId.isEmpty()) return

        // To receive student lessons
        LessonManager.loadStudentLessons(studentId,
            onSuccess = { incomingLessons ->
                lessonsList.clear()
                lessonsList.addAll(incomingLessons)
                lessonsAdapter.notifyDataSetChanged()

                if (lessonsList.isEmpty()) {
                    Toast.makeText(this, "No scheduled lessons found.", Toast.LENGTH_SHORT).show()
                }
            },
            onFailure = { errorMessage ->
                Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_LONG).show()
            }
        )
    }

    private inner class StudentLessonsAdapter(private val dataset: List<Map<String, Any>>) :
        RecyclerView.Adapter<StudentLessonsAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvDate: TextView = view.findViewById(R.id.tvStudentLessonDate)
            val tvTime: TextView = view.findViewById(R.id.tvStudentLessonTime)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_student_lesson, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val lesson = dataset[position]


            holder.tvDate.text = "📅 Date: ${lesson["date"] ?: "--/--/----"}"
            holder.tvTime.text = "⏰ ${lesson["time"] ?: "00:00"}"
        }

        override fun getItemCount(): Int = dataset.size
    }
}