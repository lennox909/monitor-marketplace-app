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
        val tvGreeting     = findViewById<TextView>(R.id.tvGreeting)
        val tvProfileEmail = findViewById<TextView>(R.id.tvProfileEmail)
        val btnEditProfile = findViewById<LinearLayout>(R.id.btnEditProfile)
        val btnMyOrders    = findViewById<LinearLayout>(R.id.btnMyOrders)
        val btnMyListings  = findViewById<LinearLayout>(R.id.btnMyListings)
        val dividerOrders  = findViewById<View>(R.id.dividerOrders)
        val btnSettings    = findViewById<LinearLayout>(R.id.btnSettings)
        val btnLogout      = findViewById<LinearLayout>(R.id.btnLogout)
        val bottomNav      = findViewById<BottomNavigationView>(R.id.bottomNav)

        Thread {
            val db   = DatabaseHelper.getInstance(this)
            val user = db.getUserById(Session.userId)
            runOnUiThread {
                tvGreeting.text     = "Hi, ${user?.name?.split(" ")?.firstOrNull() ?: "there"}"
                tvProfileEmail.text = user?.email ?: ""
                val color    = user?.avatarColor ?: "#F97316"
                val initials = getInitials(user?.name ?: "U")
                ivAvatar.setImageBitmap(createAvatarBitmap(initials, color))
            }
        }.start()

        if (Session.isBuyMode) {
            btnMyOrders.visibility   = View.VISIBLE
            dividerOrders.visibility = View.VISIBLE
            btnMyListings.visibility = View.GONE
        } else {
            btnMyOrders.visibility   = View.GONE
            dividerOrders.visibility = View.GONE
            btnMyListings.visibility = View.VISIBLE
        }

        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnMyOrders.setOnClickListener {
            startActivity(Intent(this, MyOrdersActivity::class.java))
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnMyListings.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(i)
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnLogout.setOnClickListener {
            Session.userId    = -1L
            Session.role      = "USER"
            Session.isBuyMode = true
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        bottomNav.selectedItemId = R.id.nav_profile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val i = Intent(this, MainActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(i)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    true
                }
                R.id.nav_sell -> {
                    Session.isBuyMode = false
                    startActivity(Intent(this, AddEditListingActivity::class.java))
                    true
                }
                R.id.nav_cart -> {
                    val i = Intent(this, CartActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(i)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val ivAvatar   = findViewById<ImageView>(R.id.ivAvatar)
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        Thread {
            val db   = DatabaseHelper.getInstance(this)
            val user = db.getUserById(Session.userId)
            runOnUiThread {
                tvGreeting.text = "Hi, ${user?.name?.split(" ")?.firstOrNull() ?: "there"}"
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
        else name.take(2).uppercase()
    }

    private fun createAvatarBitmap(initials: String, colorHex: String): Bitmap {
        val size   = 200
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor(colorHex) }
            .also { canvas.drawCircle(size / 2f, size / 2f, size / 2f, it) }
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color     = Color.WHITE
            textSize  = 72f
            textAlign = Paint.Align.CENTER
            typeface  = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }.also {
            val yPos = (size / 2f) - ((it.descent() + it.ascent()) / 2f)
            canvas.drawText(initials, size / 2f, yPos, it)
        }
        return bitmap
    }
}