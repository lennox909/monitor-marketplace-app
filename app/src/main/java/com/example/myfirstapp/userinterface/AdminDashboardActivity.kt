package com.example.myfirstapp.userinterface

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

        val tv = findViewById<TextView>(R.id.tvAdmin)
        val btnRefresh = findViewById<Button>(R.id.btnRefreshUsers)
        val list = findViewById<ListView>(R.id.lvUsers)

        fun refresh() {
            val users = db.getAllUsers()
            tv.text = "Admin Dashboard - Users: ${users.size}"

            val items = users.map { u ->
                "${u.id} | ${u.email} | ${u.role} | disabled=${u.isDisabled}"
            }

            list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)

            list.setOnItemClickListener { _, _, position, _ ->
                val u = users[position]
                if (u.role == "ADMIN") {
                    Toast.makeText(this, "Cannot disable admin", Toast.LENGTH_SHORT).show()
                    return@setOnItemClickListener
                }
                db.setUserDisabled(u.id, !u.isDisabled)
                refresh()
            }
        }

        btnRefresh.setOnClickListener { refresh() }
        refresh()
    }
}