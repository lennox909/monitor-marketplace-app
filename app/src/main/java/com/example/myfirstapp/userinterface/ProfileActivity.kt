package com.example.myfirstapp.userinterface

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvName         = findViewById<TextView>(R.id.tvProfileName)
        val tvEmail        = findViewById<TextView>(R.id.tvProfileEmail)
        val tvRole         = findViewById<TextView>(R.id.tvProfileRole)
        val btnEditProfile = findViewById<Button>(R.id.btnEditProfile)
        val btnMyOrders    = findViewById<Button>(R.id.btnMyOrders)
        val btnMyListings  = findViewById<Button>(R.id.btnMyListings)
        val btnSettings    = findViewById<Button>(R.id.btnSettings)
        val btnLogout      = findViewById<Button>(R.id.btnLogout)
        val progressBar    = findViewById<ProgressBar>(R.id.progressProfile)

        progressBar.visibility = View.VISIBLE

        // Load user info off main thread
        Thread {
            val db   = DatabaseHelper(this)
            val user = db.getUserById(Session.userId)

            runOnUiThread {
                progressBar.visibility = View.GONE
                tvName.text  = user?.name  ?: "N/A"
                tvEmail.text = user?.email ?: "N/A"
                tvRole.text  = Session.role
            }
        }.start()

        // Show My Orders for buyers, My Listings for sellers
        if (Session.isBuyMode) {
            btnMyOrders.visibility   = View.VISIBLE
            btnMyListings.visibility = View.GONE
        } else {
            btnMyOrders.visibility   = View.GONE
            btnMyListings.visibility = View.VISIBLE
        }

        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        btnMyOrders.setOnClickListener {
            startActivity(Intent(this, MyOrdersActivity::class.java))
        }

        btnMyListings.setOnClickListener {
            // Go back to main in sell mode
            finish()
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnLogout.setOnClickListener {
            Session.userId    = -1L
            Session.role      = "BUYER"
            Session.isBuyMode = true
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh name in case it was edited
        val tvName = findViewById<TextView>(R.id.tvProfileName)
        Thread {
            val db   = DatabaseHelper(this)
            val user = db.getUserById(Session.userId)
            runOnUiThread { tvName.text = user?.name ?: "N/A" }
        }.start()
    }
}