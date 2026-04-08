package com.example.myfirstapp.userinterface

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper

class LoginActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = DatabaseHelper(this)

        val etEmail       = findViewById<EditText>(R.id.etEmail)
        val etPassword    = findViewById<EditText>(R.id.etPassword)
        val btnLogin      = findViewById<Button>(R.id.btnLogin)
        val btnGoRegister = findViewById<Button>(R.id.btnGoRegister)

        btnLogin.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = db.login(email, password)

            if (user == null) {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (user.disabled) {
                Toast.makeText(this, "Your account has been disabled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Session.userId = user.id
            Session.role   = user.role

            when (user.role) {
                "ADMIN" -> {
                    Session.isBuyMode = false
                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                }
                "SELLER" -> {
                    Session.isBuyMode = false  // sellers land in sell mode
                    startActivity(Intent(this, MainActivity::class.java))
                }
                else -> {
                    Session.isBuyMode = true   // buyers land in buy mode
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
            finish()
        }

        btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}