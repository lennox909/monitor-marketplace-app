package com.example.myfirstapp.userinterface

import android.graphics.*
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper

class EditProfileActivity : AppCompatActivity() {

    private var selectedColor = "#F97316"

    private val avatarColors = listOf(
        "#F97316", // Orange
        "#3B82F6", // Blue
        "#10B981", // Green
        "#8B5CF6", // Purple
        "#EF4444", // Red
        "#F59E0B", // Yellow
        "#EC4899", // Pink
        "#1F2A44"  // Dark
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val etName    = findViewById<EditText>(R.id.etEditName)
        val ivPreview = findViewById<ImageView>(R.id.ivAvatarPreview)
        val colorRow  = findViewById<LinearLayout>(R.id.colorGrid)
        val btnSave   = findViewById<Button>(R.id.btnSaveProfile)

        // Load current user
        Thread {
            val db   = DatabaseHelper.getInstance(this)
            val user = db.getUserById(Session.userId)
            runOnUiThread {
                etName.setText(user?.name ?: "")
                selectedColor = user?.avatarColor ?: "#F97316"
                updatePreview(ivPreview, etName.text.toString(), selectedColor)
            }
        }.start()

        // Build color circles in a single horizontal row
        colorRow.removeAllViews()
        avatarColors.forEach { color ->
            val size = 80
            val iv = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(8, 8, 8, 8)
                }
                setImageBitmap(createColorCircle(color))
                setOnClickListener {
                    selectedColor = color
                    updatePreview(ivPreview, etName.text.toString(), selectedColor)
                }
            }
            colorRow.addView(iv)
        }

        btnSave.setOnClickListener {
            val newName = etName.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            btnSave.text      = "Saving..."

            Thread {
                val db = DatabaseHelper.getInstance(this)
                db.updateUserProfile(Session.userId, newName, selectedColor)
                runOnUiThread {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.start()
        }
    }

    private fun updatePreview(ivPreview: ImageView, name: String, color: String) {
        val initials = getInitials(name.ifEmpty { "U" })
        ivPreview.setImageBitmap(createAvatarBitmap(initials, color))
    }

    private fun getInitials(name: String): String {
        val parts = name.trim().split(" ")
        return if (parts.size >= 2)
            "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
        else
            name.take(2).uppercase()
    }

    private fun createColorCircle(colorHex: String): Bitmap {
        val size   = 120
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint  = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor(colorHex)
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        return bitmap
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