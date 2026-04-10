package com.example.myfirstapp.userinterface

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val etName  = findViewById<EditText>(R.id.etEditName)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)

        // Load current name
        Thread {
            val db   = DatabaseHelper(this)
            val user = db.getUserById(Session.userId)
            runOnUiThread { etName.setText(user?.name ?: "") }
        }.start()

        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            btnSave.text      = "Saving..."

            Thread {
                val db = DatabaseHelper(this)
                db.updateUserProfile(Session.userId, newName)
                runOnUiThread {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.start()
        }
    }
}