package com.example.myfirstapp.userinterface

import android.content.Intent
import android.os.Bundle
import android.widget.*
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

            btnLogin.isEnabled = false
            btnLogin.text      = "Logging in..."

            Thread {
                val user = db.login(email, password)

                runOnUiThread {
                    btnLogin.isEnabled = true
                    btnLogin.text      = "Login"

                    if (user == null) {
                        Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    if (user.disabled) {
                        Toast.makeText(this, "Your account has been disabled", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    Session.userId    = user.id
                    Session.role      = user.role
                    Session.isBuyMode = true // everyone starts in buy mode

                    when (user.role) {
                        "ADMIN" -> {
                            Session.isBuyMode = false
                            startActivity(Intent(this, AdminDashboardActivity::class.java))
                        }
                        else -> startActivity(Intent(this, MainActivity::class.java))
                    }
                    finish()
                }
            }.start()
        }

        btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}