package com.example.myfirstapp.userinterface

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper

class EditProfileActivity : AppCompatActivity() {

    private var selectedColor = "#F97316"

    private val avatarColors = listOf(
        "#F97316", "#3B82F6", "#10B981", "#8B5CF6",
        "#EF4444", "#F59E0B", "#EC4899", "#1F2A44"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val etName        = findViewById<EditText>(R.id.etEditName)
        val etEmail       = findViewById<EditText>(R.id.etEditEmail)
        val etPassword    = findViewById<EditText>(R.id.etEditPassword)
        val etConfirmPass = findViewById<EditText>(R.id.etEditConfirmPassword)
        val ivPreview     = findViewById<ImageView>(R.id.ivAvatarPreview)
        val colorRow      = findViewById<LinearLayout>(R.id.colorGrid)
        val btnSave       = findViewById<Button>(R.id.btnSaveProfile)
        val bottomNav     = findViewById<BottomNavigationView>(R.id.bottomNav)

        Thread {
            val db   = DatabaseHelper.getInstance(this)
            val user = db.getUserById(Session.userId)
            runOnUiThread {
                etName.setText(user?.name ?: "")
                etEmail.setText(user?.email ?: "")
                selectedColor = user?.avatarColor ?: "#F97316"
                updatePreview(ivPreview, etName.text.toString(), selectedColor)
            }
        }.start()

        colorRow.removeAllViews()
        avatarColors.forEach { color ->
            val iv = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(80, 80).apply { setMargins(8, 8, 8, 8) }
                setImageBitmap(createColorCircle(color))
                setOnClickListener {
                    selectedColor = color
                    updatePreview(ivPreview, etName.text.toString(), selectedColor)
                }
            }
            colorRow.addView(iv)
        }

        btnSave.setOnClickListener {
            val newName     = etName.text.toString().trim()
            val newEmail    = etEmail.text.toString().trim().lowercase()
            val newPassword = etPassword.text.toString()
            val confirmPass = etConfirmPass.text.toString()

            if (newName.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(this, "Name and email cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!newEmail.endsWith("@mavs.uta.edu")) {
                Toast.makeText(this, "Must use a @mavs.uta.edu email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPassword.isNotEmpty()) {
                if (newPassword.length < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (newPassword != confirmPass) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            btnSave.isEnabled = false
            btnSave.text      = "Saving..."

            Thread {
                val db   = DatabaseHelper.getInstance(this)
                val user = db.getUserById(Session.userId)
                val passwordToSave = if (newPassword.isNotEmpty()) newPassword else user?.password ?: ""
                db.updateUserFull(Session.userId, newName, newEmail, passwordToSave, selectedColor)
                runOnUiThread {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.start()
        }

        // Bottom nav - profile selected
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
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun updatePreview(ivPreview: ImageView, name: String, color: String) {
        ivPreview.setImageBitmap(createAvatarBitmap(getInitials(name.ifEmpty { "U" }), color))
    }

    private fun getInitials(name: String): String {
        val parts = name.trim().split(" ")
        return if (parts.size >= 2)
            "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        else name.take(2).uppercase()
    }

    private fun createColorCircle(colorHex: String): Bitmap {
        val size   = 120
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor(colorHex) }
            .also { canvas.drawCircle(size / 2f, size / 2f, size / 2f, it) }
        return bitmap
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