package com.example.myfirstapp.userinterface

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        db = DatabaseHelper(this)

        val tvAdmin        = findViewById<TextView>(R.id.tvAdmin)
        val btnRefreshUsers= findViewById<Button>(R.id.btnRefreshUsers)
        val lvUsers        = findViewById<ListView>(R.id.lvUsers)
        val btnAdminLogout = findViewById<Button>(R.id.btnAdminLogout)

        fun refreshUsers() {
            val users = db.getAllUsers()
            tvAdmin.text = "Admin Dashboard — ${users.size} users"

            val displayList = users.map { user ->
                val status = if (user.disabled) "DISABLED" else "ACTIVE"
                "${user.name}  |  ${user.role}  |  $status\n${user.email}"
            }

            lvUsers.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                displayList
            )

            lvUsers.setOnItemClickListener { _, _, position, _ ->
                val user = users[position]

                if (user.role == "ADMIN") {
                    Toast.makeText(this, "Cannot disable admin account", Toast.LENGTH_SHORT).show()
                    return@setOnItemClickListener
                }

                val newState = !user.disabled
                db.setUserDisabled(user.id, newState)
                Toast.makeText(
                    this,
                    if (newState) "${user.name} disabled" else "${user.name} enabled",
                    Toast.LENGTH_SHORT
                ).show()
                refreshUsers()
            }
        }

        btnRefreshUsers.setOnClickListener { refreshUsers() }

        btnAdminLogout.setOnClickListener {
            Session.userId    = -1L
            Session.role      = "BUYER"
            Session.isBuyMode = true
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        refreshUsers()
    }
}