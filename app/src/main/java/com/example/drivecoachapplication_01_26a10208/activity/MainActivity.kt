package com.example.drivecoachapplication_01_26a10208.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.drivecoachapplication_01_26a10208.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Clicking the Teacher button - takes you to the login screen with a tag for the teacher collection.
        binding.btnInstructorLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("USER_TYPE", "Instructors")
            startActivity(intent)
        }

        // Clicking the Student button - takes you to the login screen with the student collection labeled.
        binding.btnStudentLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("USER_TYPE", "Students")
            startActivity(intent)
        }
    }
}