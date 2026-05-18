package com.example.drivecoachapplication_01_26a10208.activity.activityInstructor

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drivecoachapplication_01_26a10208.R
import com.example.drivecoachapplication_01_26a10208.databinding.ActivityStudentLessonsHistoryBinding
import com.example.drivecoachapplication_01_26a10208.databinding.ItemHistoryLessonBinding
import com.example.drivecoach.model.Lesson
import com.example.drivecoachapplication_01_26a10208.manager.LessonManager

class StudentLessonsHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentLessonsHistoryBinding
    private val historyList = mutableListOf<Lesson>()
    private lateinit var historyAdapter: HistoryAdapter

    private var studentId = ""
    private var studentName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentLessonsHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Receiving the data passed from the Intent
        studentId = intent.getStringExtra("STUDENT_ID") ?: ""
        studentName = intent.getStringExtra("STUDENT_NAME") ?: "Student"

        binding.tvHistoryStudentSub.text = "Student: $studentName"

        // Initializing the visual list
        historyAdapter = HistoryAdapter(historyList)
        binding.rvLessonHistory.layoutManager = LinearLayoutManager(this)
        binding.rvLessonHistory.adapter = historyAdapter

        loadHistoryFromManager()
    }

    private fun loadHistoryFromManager() {
        LessonManager.loadAllLessonsForStudent(
            studentId = studentId,
            onSuccess = { incomingHistory ->
                historyList.clear()
                historyList.addAll(incomingHistory)
                historyAdapter.notifyDataSetChanged()

                if (historyList.isEmpty()) {
                    binding.tvNoHistoryWarning.visibility = View.VISIBLE
                    binding.rvLessonHistory.visibility = View.GONE
                } else {
                    binding.tvNoHistoryWarning.visibility = View.GONE
                    binding.rvLessonHistory.visibility = View.VISIBLE
                }
            },
            onFailure = { error ->
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        )
    }

 //History List Adapter
    private inner class HistoryAdapter(private val dataset: MutableList<Lesson>) :
        RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        inner class ViewHolder(val itemBinding: ItemHistoryLessonBinding) :
            RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val itemBinding = ItemHistoryLessonBinding.inflate(layoutInflater, parent, false)
            return ViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val lesson = dataset[holder.adapterPosition]

            holder.itemBinding.tvHistoryDateTime.text = "${lesson.date}  |  🕒 ${lesson.time}"
            holder.itemBinding.tvHistoryLocation.text = "📍 ${lesson.location}"

            // Status: Setting the text and color according to the lesson's performance in the cloud
            if (lesson.completed) {
                holder.itemBinding.tvLessonStatus.text = "COMPLETED"
                holder.itemBinding.tvLessonStatus.setBackgroundColor(Color.parseColor("#2E7D32"))
            } else {
                holder.itemBinding.tvLessonStatus.text = "PENDING"
                holder.itemBinding.tvLessonStatus.setBackgroundColor(Color.parseColor("#EF6C00"))
            }
        }

        override fun getItemCount(): Int = dataset.size
    }
}