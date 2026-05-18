package com.example.drivecoachapplication_01_26a10208.activity.activityInstructor

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.drivecoachapplication_01_26a10208.R
import com.example.drivecoachapplication_01_26a10208.databinding.ActivityViewStudentsBinding
import com.example.drivecoachapplication_01_26a10208.databinding.ItemStudentBinding
import com.example.drivecoach.model.Student
import com.example.drivecoachapplication_01_26a10208.manager.StudentManager
import com.example.drivecoachapplication_01_26a10208.utils.SoundManager
import com.example.drivecoachapplication_01_26a10208.utils.Vibration
import com.example.drivecoachapplication_01_26a10208.activity.activityStudent.StudentDashboardActivity

class ViewStudentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewStudentsBinding
    private val studentsList = mutableListOf<Student>()
    private lateinit var studentAdapter: StudentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewStudentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        studentAdapter = StudentAdapter(studentsList)
        binding.rvStudents.layoutManager = LinearLayoutManager(this)
        binding.rvStudents.adapter = studentAdapter

        fetchStudentsFromManager()
    }

    private fun fetchStudentsFromManager() {
        StudentManager.loadInstructorStudents(
            onSuccess = { incomingStudents ->
                studentsList.clear()
                studentsList.addAll(incomingStudents)
                studentAdapter.notifyDataSetChanged()

                if (studentsList.isEmpty()) {
                    binding.tvNoStudentsWarning.visibility = View.VISIBLE
                    binding.rvStudents.visibility = View.GONE
                } else {
                    binding.tvNoStudentsWarning.visibility = View.GONE
                    binding.rvStudents.visibility = View.VISIBLE
                }
            },
            onFailure = { errorText ->
                Toast.makeText(this, errorText, Toast.LENGTH_LONG).show()
            }
        )
    }

    //An automatic dialog pops up for the teacher asking whether to reset the password
    private fun showResetPasswordStep1Confirmation(studentId: String, fullName: String) {
        val builder = AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        builder.setTitle("Password reset")
        builder.setMessage("Should I reset the password for $fullName?")

        builder.setPositiveButton("yes") { dialog, _ ->
            dialog.dismiss()
            // If the teacher approved, the additional message to write the new password automatically pops up.
            showResetPasswordStep2Input(studentId, fullName)
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    //Additional message about writing a new password for the student
    private fun showResetPasswordStep2Input(studentId: String, fullName: String) {
        val builder = AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        builder.setTitle("Writing a new password")
        builder.setMessage("Enter a new password for $fullName:")

        val input = EditText(this)
        input.hint = "Minimum 6 characters recommended"
        input.setTextColor(Color.WHITE)
        builder.setView(input)

        builder.setPositiveButton("Update") { dialog, _ ->
            val newPasswordInput = input.text.toString().trim()
            if (newPasswordInput.isEmpty() || newPasswordInput.length < 4) {
                Vibration.vibrate(this, 200)
                SoundManager.playSound(this, R.raw.sound_error)
                Toast.makeText(this, "Password is too short or empty!", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            StudentManager.updateStudentPassword(studentId, newPasswordInput,
                onSuccess = {
                    Vibration.vibrate(this, 100)
                    SoundManager.playSound(this, R.raw.sound_success)
                    Toast.makeText(this, "Password for $fullName updated successfully!", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
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

    private fun showDeleteConfirmationDialog(studentId: String, fullName: String) {
        val builder = AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        builder.setTitle("Delete Student Account")
        builder.setMessage("Are you sure you want to permanently delete $fullName from the database?")

        builder.setPositiveButton("Delete") { dialog, _ ->
            StudentManager.deleteStudentFromSystem(
                studentId = studentId,
                onSuccess = {
                    Vibration.vibrate(this, 100)
                    SoundManager.playSound(this, R.raw.sound_success)
                    Toast.makeText(this, "Student account deleted successfully", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    fetchStudentsFromManager()
                },
                onFailure = { errorMessage ->
                    Vibration.vibrate(this, 200)
                    SoundManager.playSound(this, R.raw.sound_error)
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            )
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private inner class StudentAdapter(private val dataset: MutableList<Student>) :
        RecyclerView.Adapter<StudentAdapter.ViewHolder>() {

        inner class ViewHolder(val itemBinding: ItemStudentBinding) :
            RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val itemBinding = ItemStudentBinding.inflate(layoutInflater, parent, false)
            return ViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val student = dataset[holder.adapterPosition]

            holder.itemBinding.tvStudentName.text = "${student.firstName} ${student.lastName}"
            holder.itemBinding.tvStudentIdDetails.text = "ID: ${student.id}"
            holder.itemBinding.tvStudentPhoneDetails.text = "Phone: ${student.phoneNumber}"
            holder.itemBinding.tvStudentStartDate.text = "Started: ${student.startDate}"

            // Listening for the password reset button
            holder.itemBinding.btnCardResetPassword.setOnClickListener {
                val currentPos = holder.adapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    val targetStudent = dataset[currentPos]
                    showResetPasswordStep1Confirmation(targetStudent.id, "${targetStudent.firstName} ${targetStudent.lastName}")
                }
            }

            holder.itemBinding.btnActionAddLesson.setOnClickListener {
                val currentPos = holder.adapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    val targetStudent = dataset[currentPos]
                    val intent = Intent(this@ViewStudentsActivity, ScheduleLessonActivity::class.java).apply {
                        putExtra("STUDENT_ID", targetStudent.id)
                        putExtra("STUDENT_NAME", "${targetStudent.firstName} ${targetStudent.lastName}")
                    }
                    startActivity(intent)
                }
            }

            holder.itemBinding.btnActionFullAnalysis.setOnClickListener {
                val currentPos = holder.adapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    val targetStudent = dataset[currentPos]
                    val intent = Intent(this@ViewStudentsActivity, StudentDashboardActivity::class.java).apply {
                        putExtra("STUDENT_ID", targetStudent.id)
                        putExtra("IS_TEACHER_VIEW", true)
                    }
                    startActivity(intent)
                }
            }

            holder.itemBinding.btnActionViewHistory.setOnClickListener {
                val currentPos = holder.adapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    val targetStudent = dataset[currentPos]
                    val intent = Intent(this@ViewStudentsActivity, StudentLessonsHistoryActivity::class.java).apply {
                        putExtra("STUDENT_ID", targetStudent.id)
                        putExtra("STUDENT_NAME", "${targetStudent.firstName} ${targetStudent.lastName}")
                    }
                    startActivity(intent)
                }
            }

            holder.itemBinding.btnDeleteStudent.setOnClickListener {
                val currentPos = holder.adapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    val targetStudent = dataset[currentPos]
                    showDeleteConfirmationDialog(targetStudent.id, "${targetStudent.firstName} ${targetStudent.lastName}")
                }
            }
        }

        override fun getItemCount(): Int = dataset.size
    }
}