package com.example.myfirstapp.userinterface

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper
import com.example.myfirstapp.model.Listing
import java.io.File
import java.io.FileOutputStream

class AddEditListingActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var editId: Long = -1
    private var savedImagePath: String? = null

    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val fileName     = "listing_${System.currentTimeMillis()}.jpg"
                val file         = File(filesDir, fileName)
                val inputStream  = contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                savedImagePath = file.absolutePath
                val ivImage = findViewById<ImageView>(R.id.ivListingImage)
                ivImage.setImageURI(Uri.fromFile(file))
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

        db     = DatabaseHelper.getInstance(this)
        editId = intent.getLongExtra("EDIT_ID", -1)

        val etTitle       = findViewById<EditText>(R.id.etTitle)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etBrand       = findViewById<EditText>(R.id.etBrand)
        val etScreenSize  = findViewById<EditText>(R.id.etScreenSize)
        val etResolution  = findViewById<EditText>(R.id.etResolution)
        val spCondition   = findViewById<Spinner>(R.id.spCondition)
        val spCategory    = findViewById<Spinner>(R.id.spCategory)
        val etPrice       = findViewById<EditText>(R.id.etPrice)
        val btnPickImage  = findViewById<Button>(R.id.btnPickImage)
        val ivImage       = findViewById<ImageView>(R.id.ivListingImage)
        val btnSave       = findViewById<Button>(R.id.btnSave)

        val conditions = listOf("New", "Like New", "Used")
        spCondition.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, conditions
        )

        val categories = listOf("Gaming", "Office", "4K", "Ultrawide", "General")
        spCategory.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, categories
        )

        if (editId > 0) {
            val listing = db.getListingById(editId)
            if (listing != null) {
                etTitle.setText(listing.title)
                etDescription.setText(listing.description)
                etBrand.setText(listing.brand)
                etScreenSize.setText(listing.screenSize)
                etResolution.setText(listing.resolution)
                etPrice.setText(listing.price.toString())
                val condIdx = conditions.indexOf(listing.condition)
                if (condIdx >= 0) spCondition.setSelection(condIdx)
                val catIdx = categories.indexOf(listing.category)
                if (catIdx >= 0) spCategory.setSelection(catIdx)
                if (!listing.photoUri.isNullOrEmpty()) {
                    savedImagePath = listing.photoUri
                    try {
                        ivImage.setImageURI(Uri.fromFile(File(listing.photoUri)))
                        ivImage.visibility = android.view.View.VISIBLE
                        findViewById<TextView>(R.id.tvImageHint).text = "Image selected"
                    } catch (e: Exception) {
                        ivImage.visibility = android.view.View.GONE
                    }
                }
            }
        }

        btnPickImage.setOnClickListener { pickImage.launch("image/*") }

        btnSave.setOnClickListener {
            val title      = etTitle.text.toString().trim()
            val desc       = etDescription.text.toString().trim()
            val brand      = etBrand.text.toString().trim()
            val screenSize = etScreenSize.text.toString().trim()
            val resolution = etResolution.text.toString().trim()
            val condition  = spCondition.selectedItem.toString()
            val category   = spCategory.selectedItem.toString()
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
                category    = category,
                price       = price,
                photoUri    = savedImagePath
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