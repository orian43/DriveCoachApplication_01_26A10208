package com.example.drivecoachapplication_01_26a10208.activity.activityInstructor

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drivecoachapplication_01_26a10208.R
import com.example.drivecoachapplication_01_26a10208.databinding.ActivityUpcomingLessonsBinding
import com.example.drivecoachapplication_01_26a10208.databinding.ItemUpcomingLessonBinding
import com.example.drivecoach.model.Lesson
import com.example.drivecoachapplication_01_26a10208.manager.LessonManager
import com.example.drivecoachapplication_01_26a10208.utils.SoundManager
import com.example.drivecoachapplication_01_26a10208.utils.Vibration

class UpcomingLessonsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpcomingLessonsBinding
    private val lessonsList = mutableListOf<Lesson>()
    private lateinit var lessonsAdapter: UpcomingLessonsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpcomingLessonsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        lessonsAdapter = UpcomingLessonsAdapter(lessonsList)
        binding.rvUpcomingLessons.layoutManager = LinearLayoutManager(this)
        binding.rvUpcomingLessons.adapter = lessonsAdapter

        // Loading the sorted lessons
        fetchUpcomingLessons()
    }

    private fun fetchUpcomingLessons() {
        LessonManager.loadUpcomingLessons(
            onSuccess = { incomingLessons ->
                lessonsList.clear()
                lessonsList.addAll(incomingLessons)
                lessonsAdapter.notifyDataSetChanged()

                if (lessonsList.isEmpty()) {
                    binding.tvNoLessonsWarning.visibility = View.VISIBLE
                    binding.rvUpcomingLessons.visibility = View.GONE
                } else {
                    binding.tvNoLessonsWarning.visibility = View.GONE
                    binding.rvUpcomingLessons.visibility = View.VISIBLE
                }
            },
            onFailure = { errorText ->
                Toast.makeText(this, errorText, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun showCompleteConfirmationDialog(lessonId: String, studentName: String) {
        val builder = AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        builder.setTitle("Complete Driving Lesson")
        builder.setMessage("Are you sure this driving session with $studentName has been completed?")

        builder.setPositiveButton("Yes, Completed") { dialog, _ ->
            LessonManager.markLessonAsCompleted(
                lessonId = lessonId,
                onSuccess = {
                    Vibration.vibrate(this, 100)
                    SoundManager.playSound(this, R.raw.sound_success)
                    Toast.makeText(this, "Lesson marked as completed!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    fetchUpcomingLessons()
                },
                onFailure = { error ->
                    Vibration.vibrate(this, 200)
                    SoundManager.playSound(this, R.raw.sound_error)
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                }
            )
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

//Confirmation dialog to delete/cancel the lesson completely from the database
    private fun showDeleteConfirmationDialog(lessonId: String, studentName: String) {
        val builder = AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        builder.setTitle("Cancel & Delete Lesson")
        builder.setMessage("Are you sure you want to completely remove this driving lesson with $studentName? This action will wipe it from the cloud database.")

        builder.setPositiveButton("Delete Permanently") { dialog, _ ->

            LessonManager.deleteLessonFromSystem(
                lessonId = lessonId,
                onSuccess = {
                    Vibration.vibrate(this, 100)
                    SoundManager.playSound(this, R.raw.sound_success)
                    Toast.makeText(this, "Lesson canceled and wiped successfully", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    fetchUpcomingLessons() // Refresh the list live
                },
                onFailure = { error ->
                    Vibration.vibrate(this, 200)
                    SoundManager.playSound(this, R.raw.sound_error)
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                }
            )
        }

        builder.setNegativeButton("Keep Lesson") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

  //adapter of the list of upcoming lessons
    private inner class UpcomingLessonsAdapter(private val dataset: MutableList<Lesson>) :
        RecyclerView.Adapter<UpcomingLessonsAdapter.ViewHolder>() {

        inner class ViewHolder(val itemBinding: ItemUpcomingLessonBinding) :
            RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val itemBinding = ItemUpcomingLessonBinding.inflate(layoutInflater, parent, false)
            return ViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val lesson = dataset[holder.adapterPosition]

            holder.itemBinding.tvLessonStudentName.text = lesson.studentName
            holder.itemBinding.tvLessonDateTime.text = "📅 ${lesson.date}  |  🕒 ${lesson.time}"
            holder.itemBinding.tvLessonLocation.text = "📍 ${lesson.location}"

            //  Clicking on the green V to mark the end of a lesson
            holder.itemBinding.btnCompleteLesson.setOnClickListener {
                val currentPos = holder.adapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    val targetLesson = dataset[currentPos]
                    showCompleteConfirmationDialog(targetLesson.id, targetLesson.studentName)
                }
            }

            // Clicking the red X opens the delete from database dialog.
            holder.itemBinding.btnDeleteLesson.setOnClickListener {
                val currentPos = holder.adapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    val targetLesson = dataset[currentPos]
                    showDeleteConfirmationDialog(targetLesson.id, targetLesson.studentName)
                }
            }

            holder.itemBinding.btnAnalyzeLesson.setOnClickListener {
                val currentPos = holder.adapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    val targetLesson = dataset[currentPos]
                    val intent = Intent(this@UpcomingLessonsActivity, LessonAnalysisActivity::class.java).apply {
                        putExtra("STUDENT_ID", targetLesson.studentId)
                        putExtra("STUDENT_NAME", targetLesson.studentName)
                    }
                    startActivity(intent)
                }
            }
        }

        override fun getItemCount(): Int = dataset.size
    }
}