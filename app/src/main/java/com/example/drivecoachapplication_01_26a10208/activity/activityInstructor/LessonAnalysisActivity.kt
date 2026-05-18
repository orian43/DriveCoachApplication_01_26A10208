package com.example.drivecoachapplication_01_26a10208.activity.activityInstructor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drivecoachapplication_01_26a10208.R
import com.example.drivecoachapplication_01_26a10208.databinding.ActivityLessonAnalysisBinding
import com.example.drivecoachapplication_01_26a10208.databinding.ItemProgressTaskBinding
import com.example.drivecoach.model.StudentProgressTask
import com.example.drivecoachapplication_01_26a10208.manager.LessonManager
import com.example.drivecoachapplication_01_26a10208.manager.StudentManager
import com.example.drivecoachapplication_01_26a10208.utils.SoundManager
import com.example.drivecoachapplication_01_26a10208.utils.Vibration

class LessonAnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLessonAnalysisBinding
    private var studentId = ""
    private var studentName = ""

    private val taskProgressList = mutableListOf<StudentProgressTask>()
    private lateinit var taskAdapter: AnalysisTaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLessonAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        studentId = intent.getStringExtra("STUDENT_ID") ?: ""
        studentName = intent.getStringExtra("STUDENT_NAME") ?: "Student"

        binding.tvAnalysisStudentName.text = "Student: $studentName"

        taskAdapter = AnalysisTaskAdapter(taskProgressList)
        binding.rvAnalysisTasks.layoutManager = LinearLayoutManager(this)
        binding.rvAnalysisTasks.adapter = taskAdapter

        // Combined data loading from the separated managers
        loadAnalysisData()

        // Saving changes to the database
        binding.btnSaveAnalysisChanges.setOnClickListener {
            saveUpdatedProgressToCloud()
        }
    }

    private fun loadAnalysisData() {
        if (studentId.isEmpty()) return

        // Obtaining the number of lessons actually completed to update the designed round bar
        LessonManager.countCompletedLessonsForStudent(studentId,
            onSuccess = { totalCompleted ->

                binding.tvCircleCount.text = totalCompleted.toString()
                binding.pbTotalProgressCircle.progress = if (totalCompleted > 28) 28 else totalCompleted
            },
            onFailure = { error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        )

        // Getting the set of tasks and quotas from StudentManager
        StudentManager.getStudentProfile(studentId,
            onSuccess = { studentProfile ->

                binding.tvStudentStartDateDisplay.text = "Started: ${studentProfile.startDate}"

                taskProgressList.clear()
                taskProgressList.addAll(studentProfile.progress)
                taskAdapter.notifyDataSetChanged()
            },
            onFailure = { error ->
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun saveUpdatedProgressToCloud() {
        binding.btnSaveAnalysisChanges.isEnabled = false
        binding.btnSaveAnalysisChanges.text = "Saving configuration..."

        StudentManager.updateStudentProgressTasks(studentId, taskProgressList,
            onSuccess = {
                Vibration.vibrate(this, 100)
                SoundManager.playSound(this, R.raw.sound_success)
                Toast.makeText(this, "Student progress updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            },
            onFailure = { error ->
                binding.btnSaveAnalysisChanges.isEnabled = true
                binding.btnSaveAnalysisChanges.text = "Save & Update Student Progress"
                Vibration.vibrate(this, 200)
                SoundManager.playSound(this, R.raw.sound_error)
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            }
        )
    }

    //Viewing and updating tasks
    private inner class AnalysisTaskAdapter(private val dataset: List<StudentProgressTask>) :
        RecyclerView.Adapter<AnalysisTaskAdapter.ViewHolder>() {

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

            // Displaying an estimate format of "Current Status / Desired Target"
            holder.itemBinding.tvProgressTaskCount.text = "Progress: ${task.completedLessons} / ${task.requiredLessons}"

            // Clicking the +1 button increases the value locally in the list and refreshes the row.
            holder.itemBinding.btnIncrementTask.setOnClickListener {
                val currentPos = holder.adapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    // Completely free upload without limits! Allows you to reach 5/4 for example
                    dataset[currentPos].completedLessons += 1
                    notifyItemChanged(currentPos)
                }
            }
        }

        override fun getItemCount(): Int = dataset.size
    }
}