package com.example.myfirstapp.userinterface

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val ivAvatar       = findViewById<ImageView>(R.id.ivAvatar)
        val tvProfileName  = findViewById<TextView>(R.id.tvProfileName)
        val tvProfileEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val tvProfileRole  = findViewById<TextView>(R.id.tvProfileRole)
        val btnEditProfile = findViewById<Button>(R.id.btnEditProfile)
        val btnMyOrders    = findViewById<Button>(R.id.btnMyOrders)
        val btnMyListings  = findViewById<Button>(R.id.btnMyListings)
        val btnSettings    = findViewById<Button>(R.id.btnSettings)
        val btnLogout      = findViewById<Button>(R.id.btnLogout)
        val progressBar    = findViewById<ProgressBar>(R.id.progressProfile)
        val bottomNav      = findViewById<BottomNavigationView>(R.id.bottomNav)

        progressBar.visibility = View.VISIBLE

        Thread {
            val db   = DatabaseHelper.getInstance(this)
            val user = db.getUserById(Session.userId)
            runOnUiThread {
                progressBar.visibility = View.GONE
                tvProfileName.text  = user?.name  ?: "N/A"
                tvProfileEmail.text = user?.email ?: "N/A"
                tvProfileRole.text  = Session.role
                val color    = user?.avatarColor ?: "#F97316"
                val initials = getInitials(user?.name ?: "U")
                ivAvatar.setImageBitmap(createAvatarBitmap(initials, color))
            }
        }.start()

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
            val i = Intent(this, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(i)
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnLogout.setOnClickListener {
            Session.userId    = -1L
            Session.role      = "USER"
            Session.isBuyMode = true
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Bottom nav
        bottomNav.selectedItemId = R.id.nav_profile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val i = Intent(this, MainActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(i)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                    true
                }
                R.id.nav_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val ivAvatar      = findViewById<ImageView>(R.id.ivAvatar)
        val tvProfileName = findViewById<TextView>(R.id.tvProfileName)
        Thread {
            val db   = DatabaseHelper.getInstance(this)
            val user = db.getUserById(Session.userId)
            runOnUiThread {
                tvProfileName.text = user?.name ?: "N/A"
                val color    = user?.avatarColor ?: "#F97316"
                val initials = getInitials(user?.name ?: "U")
                ivAvatar.setImageBitmap(createAvatarBitmap(initials, color))
            }
        }.start()
    }

    private fun getInitials(name: String): String {
        val parts = name.trim().split(" ")
        return if (parts.size >= 2)
            "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        else
            name.take(2).uppercase()
    }

    private fun createAvatarBitmap(initials: String, colorHex: String): Bitmap {
        val size   = 200
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(colorHex)
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color     = Color.WHITE
            textSize  = 72f
            textAlign = Paint.Align.CENTER
            typeface  = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val yPos = (size / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(initials, size / 2f, yPos, textPaint)

        return bitmap
    }
}