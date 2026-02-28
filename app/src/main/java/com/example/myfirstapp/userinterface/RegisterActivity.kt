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

        db = DatabaseHelper(this)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val spRole = findViewById<Spinner>(R.id.spRole)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        spRole.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("BUYER", "SELLER")
        )

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val role = spRole.selectedItem.toString()

            if (name.isEmpty() || email.isEmpty() || password.length < 4) {
                Toast.makeText(this, "Enter valid name/email and 4+ char password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (db.getUserByEmail(email) != null) {
                Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val id = db.addUser(User(name = name, email = email, password = password, role = role))
            if (id <= 0) {
                Toast.makeText(this, "Register failed", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Registered! Please login.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}
