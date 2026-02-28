package com.example.myfirstapp.userinterface

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper
import com.example.myfirstapp.model.Listing

class AddEditListingActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var editId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_listing)

        db = DatabaseHelper(this)
        editId = intent.getLongExtra("EDIT_ID", -1)

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val etCategory = findViewById<EditText>(R.id.etCategory)
        val etPrice = findViewById<EditText>(R.id.etPrice)
        val etCondition = findViewById<EditText>(R.id.etCondition)
        val btnSave = findViewById<Button>(R.id.btnSave)

        if (editId > 0) {
            val listing = db.getListingById(editId)
            if (listing != null) {
                etTitle.setText(listing.title)
                etDescription.setText(listing.description)
                etCategory.setText(listing.category)
                etPrice.setText(listing.price.toString())
                etCondition.setText(listing.condition)
            }
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc = etDescription.text.toString().trim()
            val category = etCategory.text.toString().trim()
            val priceStr = etPrice.text.toString().trim()
            val condition = etCondition.text.toString().trim()

            if (title.isEmpty() || desc.isEmpty() || category.isEmpty() || condition.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toDoubleOrNull()
            if (price == null || price < 0) {
                Toast.makeText(this, "Enter valid price", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val listing = Listing(
                id = if (editId > 0) editId else -1,
                sellerId = Session.userId,
                title = title,
                description = desc,
                category = category,
                price = price,
                condition = condition,
                photoUri = null
            )

            if (editId > 0) {
                db.updateListing(listing)
                Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show()
            } else {
                db.addListing(listing)
                Toast.makeText(this, "Created", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}
