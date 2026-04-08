package com.example.myfirstapp.userinterface

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper
import com.example.myfirstapp.model.Listing

class AddEditListingActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var editId: Long = -1
    private var selectedImageUri: Uri? = null

    // Image picker launcher
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                val ivImage = findViewById<ImageView>(R.id.ivListingImage)
                ivImage.setImageURI(uri)
                ivImage.visibility = android.view.View.VISIBLE
                findViewById<TextView>(R.id.tvImageHint).text = "Image selected"
            } catch (e: Exception) {
                Toast.makeText(this, "Could not load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_listing)

        db     = DatabaseHelper(this)
        editId = intent.getLongExtra("EDIT_ID", -1)

        val etTitle       = findViewById<EditText>(R.id.etTitle)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etBrand       = findViewById<EditText>(R.id.etBrand)
        val etScreenSize  = findViewById<EditText>(R.id.etScreenSize)
        val etResolution  = findViewById<EditText>(R.id.etResolution)
        val spCondition   = findViewById<Spinner>(R.id.spCondition)
        val etPrice       = findViewById<EditText>(R.id.etPrice)
        val btnPickImage  = findViewById<Button>(R.id.btnPickImage)
        val ivImage       = findViewById<ImageView>(R.id.ivListingImage)
        val btnSave       = findViewById<Button>(R.id.btnSave)

        val conditions = listOf("New", "Like New", "Used")
        spCondition.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, conditions
        )

        // Pre-fill if editing
        if (editId > 0) {
            val listing = db.getListingById(editId)
            if (listing != null) {
                etTitle.setText(listing.title)
                etDescription.setText(listing.description)
                etBrand.setText(listing.brand)
                etScreenSize.setText(listing.screenSize)
                etResolution.setText(listing.resolution)
                etPrice.setText(listing.price.toString())
                val idx = conditions.indexOf(listing.condition)
                if (idx >= 0) spCondition.setSelection(idx)

                // Load existing photo safely
                if (!listing.photoUri.isNullOrEmpty()) {
                    try {
                        selectedImageUri = Uri.parse(listing.photoUri)
                        ivImage.setImageURI(selectedImageUri)
                        ivImage.visibility = android.view.View.VISIBLE
                        findViewById<TextView>(R.id.tvImageHint).text = "Image selected"
                    } catch (e: Exception) {
                        ivImage.visibility = android.view.View.GONE
                    }
                }
            }
        }

        // Open gallery picker
        btnPickImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        btnSave.setOnClickListener {
            val title      = etTitle.text.toString().trim()
            val desc       = etDescription.text.toString().trim()
            val brand      = etBrand.text.toString().trim()
            val screenSize = etScreenSize.text.toString().trim()
            val resolution = etResolution.text.toString().trim()
            val condition  = spCondition.selectedItem.toString()
            val priceStr   = etPrice.text.toString().trim()

            if (title.isEmpty() || desc.isEmpty() || brand.isEmpty() ||
                screenSize.isEmpty() || resolution.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toDoubleOrNull()
            if (price == null || price < 0) {
                Toast.makeText(this, "Enter a valid price", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val listing = Listing(
                id          = if (editId > 0) editId else -1,
                sellerId    = Session.userId,
                title       = title,
                description = desc,
                brand       = brand,
                screenSize  = screenSize,
                resolution  = resolution,
                condition   = condition,
                price       = price,
                photoUri    = selectedImageUri?.toString()
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