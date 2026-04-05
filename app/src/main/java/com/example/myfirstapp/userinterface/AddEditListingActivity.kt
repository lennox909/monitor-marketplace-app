package com.example.myfirstapp.userinterface

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper
import com.example.myfirstapp.model.Listing

class AddEditListingActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var editId: Long = -1
    private var selectedPhotoUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_listing)

        db = DatabaseHelper(this)
        editId = intent.getLongExtra("EDIT_ID", -1)

        val ivPreview = findViewById<ImageView>(R.id.ivPreview)
        val btnTakePhoto = findViewById<Button>(R.id.btnTakePhoto)
        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etPrice = findViewById<EditText>(R.id.etPrice)
        val etCategory = findViewById<EditText>(R.id.etCategory)
        val etExpirationDate = findViewById<EditText>(R.id.etExpirationDate)
        val btnSave = findViewById<Button>(R.id.btnSave)

        if (editId > 0) {
            val listing = db.getListingById(editId)
            if (listing != null) {
                etTitle.setText(listing.title)
                etDescription.setText(listing.description)
                etPrice.setText(listing.price.toString())
                etCategory.setText(listing.category)
                etExpirationDate.setText(listing.expirationDate)
                selectedPhotoUri = listing.photoUri
            }
        }

        btnTakePhoto.setOnClickListener {
            Toast.makeText(
                this,
                "Photo capture wiring is next. For now the field is ready.",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val priceStr = etPrice.text.toString().trim()
            val category = etCategory.text.toString().trim()
            val expirationDate = etExpirationDate.text.toString().trim()

            if (
                title.isEmpty() ||
                description.isEmpty() ||
                priceStr.isEmpty() ||
                category.isEmpty() ||
                expirationDate.isEmpty()
            ) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toDoubleOrNull()
            if (price == null || price <= 0.0) {
                Toast.makeText(this, "Enter a valid price", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val listing = Listing(
                id = if (editId > 0) editId else -1,
                sellerId = Session.userId,
                title = title,
                description = description,
                price = price,
                category = category,
                expirationDate = expirationDate,
                photoUri = selectedPhotoUri
            )

            if (editId > 0) {
                db.updateListing(listing)
                Toast.makeText(this, "Listing updated", Toast.LENGTH_SHORT).show()
            } else {
                db.addListing(listing)
                Toast.makeText(this, "Listing created", Toast.LENGTH_SHORT).show()
            }

            finish()
        }
    }
}