package com.example.drivecoachapplication_01_26a10208.activity.activityInstructor

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.drivecoachapplication_01_26a10208.databinding.ActivityLessonSettingsBinding
import com.example.drivecoachapplication_01_26a10208.databinding.ItemLessonBinding
import com.example.drivecoach.model.LessonRule
import com.example.drivecoachapplication_01_26a10208.manager.LessonSettingsManager
import com.example.drivecoachapplication_01_26a10208.utils.SoundManager
import com.example.drivecoachapplication_01_26a10208.utils.Vibration

class LessonSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLessonSettingsBinding

    private val rulesList = mutableListOf<LessonRule>()
    private lateinit var rulesAdapter: RulesAdapter
    private val defaultRules = setOf("Parallel Parking", "Highway Driving", "Clutch Control", "Traffic Circles")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLessonSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initializing the list and visual interface
        rulesAdapter = RulesAdapter(rulesList)
        binding.rvLessonRules.layoutManager = LinearLayoutManager(this)
        binding.rvLessonRules.adapter = rulesAdapter

        // Call to the logic layer to load data from the cloud
        fetchRulesFromManager()

        binding.btnAddCustomRule.setOnClickListener {
            showAddCustomRuleDialog()
        }

        // Save button
        binding.btnSaveSettings.setOnClickListener {
            validateAndSaveThroughManager()
        }
    }

    private fun fetchRulesFromManager() {
        LessonSettingsManager.loadCurrentLessonRules(
            onSuccess = { minRequiredLessons, lessonSettings ->
                binding.etMinRequiredLessons.setText(minRequiredLessons.toString())
                rulesList.clear()

                if (lessonSettings.isNotEmpty()) {
                    rulesList.addAll(lessonSettings)
                } else {
                    rulesList.addAll(listOf(
                        LessonRule("Parallel Parking", 5),
                        LessonRule("Highway Driving", 3),
                        LessonRule("Clutch Control", 4),
                        LessonRule("Traffic Circles", 5)
                    ))
                }
                rulesAdapter.notifyDataSetChanged()
            },
            onFailure = { errorMessage ->
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun showAddCustomRuleDialog() {
        val builder = AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        builder.setTitle("Add Custom Lesson Task")

        val input = EditText(this)
        input.hint = "e.g., Reverse Parking, Hill Start"
        input.setTextColor(Color.WHITE)
        builder.setView(input)

        builder.setPositiveButton("Add") { dialog, _ ->
            val taskNameInput = input.text.toString().trim()
            if (taskNameInput.isEmpty()) {
                Toast.makeText(this, "Task name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val isDuplicate = rulesList.any { it.taskName.equals(taskNameInput, ignoreCase = true) }
            if (isDuplicate) {
                Toast.makeText(this, "This task already exists", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            rulesList.add(LessonRule(taskName = taskNameInput, requiredLessons = 1))
            rulesAdapter.notifyItemInserted(rulesList.size - 1)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun validateAndSaveThroughManager() {
        val minLessonsInput = binding.etMinRequiredLessons.text.toString().trim().toIntOrNull() ?: 0

        if (minLessonsInput <= 0) {
            triggerErrorAlert("Please enter a valid total minimum required lessons")
            return
        }

        val hasInvalidQuota = rulesList.any { it.requiredLessons <= 0 }
        if (hasInvalidQuota) {
            triggerErrorAlert("All task quotas must be greater than 0")
            return
        }

        binding.btnSaveSettings.isEnabled = false
        binding.btnSaveSettings.text = "Saving rules..."

        LessonSettingsManager.saveLessonRules(
            minRequiredLessons = minLessonsInput,
            rulesList = rulesList,
            onSuccess = {
                Vibration.vibrate(this, 100)
                SoundManager.playSound(this, R.raw.sound_success)
                Toast.makeText(this, "Rules updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            },
            onFailure = { errorMessage ->
                binding.btnSaveSettings.isEnabled = true
                binding.btnSaveSettings.text = "Save Rules & Quotas"
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun deleteRuleAtPosition(position: Int) {
        if (position in rulesList.indices) {
            rulesList.removeAt(position)
            rulesAdapter.notifyItemRemoved(position)
            rulesAdapter.notifyItemRangeChanged(position, rulesList.size)
        }
    }

    private fun triggerErrorAlert(message: String) {
        Vibration.vibrate(this, 200)
        SoundManager.playSound(this, R.raw.sound_error)
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }


    private inner class RulesAdapter(private val dataset: MutableList<LessonRule>) :
        RecyclerView.Adapter<RulesAdapter.ViewHolder>() {

        inner class ViewHolder(val itemBinding: ItemLessonBinding) :
            RecyclerView.ViewHolder(itemBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val itemBinding = ItemLessonBinding.inflate(layoutInflater, parent, false)
            return ViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val rule = dataset[holder.adapterPosition]

            holder.itemBinding.tvRuleName.text = rule.taskName

            // Removing the old listener from the EditText before updating the new text
            val oldWatcher = holder.itemBinding.etRuleQuota.tag as? TextWatcher
            if (oldWatcher != null) {
                holder.itemBinding.etRuleQuota.removeTextChangedListener(oldWatcher)
            }

            // Updating the current value into the input field
            holder.itemBinding.etRuleQuota.setText(rule.requiredLessons.toString())

            //Manage the visibility of the delete button for custom rules
            if (defaultRules.contains(rule.taskName)) {
                holder.itemBinding.btnDeleteRule.visibility = View.INVISIBLE
            } else {
                holder.itemBinding.btnDeleteRule.visibility = View.VISIBLE
                holder.itemBinding.btnDeleteRule.setOnClickListener {
                    val currentPos = holder.adapterPosition
                    if (currentPos != RecyclerView.NO_POSITION) {
                        deleteRuleAtPosition(currentPos)
                    }
                }
            }

            val newWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val currentPos = holder.adapterPosition
                    if (currentPos != RecyclerView.NO_POSITION) {
                        val inputVal = s.toString().toIntOrNull() ?: 0
                        dataset[currentPos].requiredLessons = inputVal
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            }

            //Saving the new listener in the Tag and attaching it to the box
            holder.itemBinding.etRuleQuota.addTextChangedListener(newWatcher)
            holder.itemBinding.etRuleQuota.tag = newWatcher
        }

        override fun getItemCount(): Int = dataset.size
    }
}