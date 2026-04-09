package com.example.myfirstapp.userinterface

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvProfileInfo = findViewById<TextView>(R.id.tvProfileInfo)

        // Load user info off main thread
        Thread {
            val db   = DatabaseHelper(this)
            val user = db.getAllUsers().firstOrNull { it.id == Session.userId }

            runOnUiThread {
                tvProfileInfo.text =
                    "Name:   ${user?.name  ?: "N/A"}\n\n" +
                            "Email:  ${user?.email ?: "N/A"}\n\n" +
                            "Role:   ${Session.role}"
            }
        }.start()

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            Session.userId    = -1L
            Session.role      = "BUYER"
            Session.isBuyMode = true
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}