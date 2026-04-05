package com.example.myfirstapp.userinterface

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper
import com.example.myfirstapp.model.User

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    private fun isValidPassword(password: String): Boolean {
        val regex = Regex("^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*()_+\\-={}:;\"'<>,.?/]).{8,}$")
        return regex.matches(password)
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = DatabaseHelper(this)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val tvPasswordMatch = findViewById<TextView>(R.id.tvPasswordMatch)
        val spRole = findViewById<Spinner>(R.id.spRole)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBackToLogin = findViewById<Button>(R.id.btnBackToLogin)

        val roles = listOf("BUYER", "SELLER")
        spRole.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            roles
        )

        val passwordWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val password = etPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()

                if (confirmPassword.isEmpty()) {
                    tvPasswordMatch.text = ""
                    return
                }

                if (password == confirmPassword) {
                    tvPasswordMatch.text = "Passwords match"
                    tvPasswordMatch.setTextColor(Color.parseColor("#15803D"))
                } else {
                    tvPasswordMatch.text = "Passwords do not match"
                    tvPasswordMatch.setTextColor(Color.parseColor("#DC2626"))
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        etPassword.addTextChangedListener(passwordWatcher)
        etConfirmPassword.addTextChangedListener(passwordWatcher)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim().lowercase()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()
            val role = spRole.selectedItem.toString()

            when {
                name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    showMessage("Fill all fields")
                }

                !email.endsWith("@mavs.uta.edu") -> {
                    showMessage("Use a UTA email ending in @mavs.uta.edu")
                }

                !isValidPassword(password) -> {
                    showMessage("Password must be 8+ chars with uppercase, number, and special character")
                }

                password != confirmPassword -> {
                    showMessage("Passwords do not match")
                }

                db.getUserByEmail(email) != null -> {
                    showMessage("Email already registered")
                }

                else -> {
                    val user = User(
                        name = name,
                        email = email,
                        password = password,
                        role = role
                    )

                    val result = db.addUser(user)

                    if (result > 0) {
                        showMessage("Account created successfully")
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        showMessage("Registration failed")
                    }
                }
            }
        }

        btnBackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}