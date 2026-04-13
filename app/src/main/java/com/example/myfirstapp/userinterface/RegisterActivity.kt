package com.example.myfirstapp.userinterface

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper
import com.example.myfirstapp.model.User

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = DatabaseHelper.getInstance(this)

        val etName            = findViewById<EditText>(R.id.etName)
        val etEmail           = findViewById<EditText>(R.id.etEmail)
        val etPassword        = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister       = findViewById<Button>(R.id.btnRegister)
        val btnBackToLogin    = findViewById<Button>(R.id.btnBackToLogin)

        btnRegister.setOnClickListener {
            val name            = etName.text.toString().trim()
            val email           = etEmail.text.toString().trim().lowercase()
            val password        = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!email.endsWith("@mavs.uta.edu")) {
                Toast.makeText(this, "Must use a @mavs.uta.edu email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (db.getUserByEmail(email) != null) {
                Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Everyone registers as USER — they can toggle buy/sell from dashboard
            val id = db.addUser(
                User(name = name, email = email, password = password, role = "USER")
            )

            if (id <= 0) {
                Toast.makeText(this, "Registration failed, try again", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Account created! Please log in.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        btnBackToLogin.setOnClickListener { finish() }
    }
}