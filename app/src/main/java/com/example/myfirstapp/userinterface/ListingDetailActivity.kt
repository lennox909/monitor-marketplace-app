package com.example.myfirstapp.userinterface

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper

class ListingDetailActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var listingId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listing_detail)

        db = DatabaseHelper(this)
        listingId = intent.getLongExtra("LISTING_ID", -1)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvPrice = findViewById<TextView>(R.id.tvPrice)
        val tvBrand = findViewById<TextView>(R.id.tvBrand)
        val tvSize = findViewById<TextView>(R.id.tvSize)
        val tvResolution = findViewById<TextView>(R.id.tvResolution)
        val tvRefreshRate = findViewById<TextView>(R.id.tvRefreshRate)
        val tvCourseTag = findViewById<TextView>(R.id.tvCourseTag)
        val tvCategory = findViewById<TextView>(R.id.tvCategory)
        val tvCondition = findViewById<TextView>(R.id.tvCondition)
        val tvDescription = findViewById<TextView>(R.id.tvDescription)
        val btnPrimary = findViewById<Button>(R.id.btnPrimary)
        val btnSecondary = findViewById<Button>(R.id.btnSecondary)

        val listing = db.getListingById(listingId)

        if (listing == null) {
            Toast.makeText(this, "Listing not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvTitle.text = listing.title
        tvPrice.text = "$${"%.2f".format(listing.price)}"
        tvBrand.text = "Brand: ${listing.brand}"
        tvSize.text = "Size: ${listing.size}"
        tvResolution.text = "Resolution: ${listing.resolution}"
        tvRefreshRate.text = "Refresh Rate: ${listing.refreshRate}"
        tvCourseTag.text = "Course Tag: ${listing.courseTag}"
        tvCategory.text = "Category: ${listing.category}"
        tvCondition.text = "Condition: ${listing.condition}"
        tvDescription.text = listing.description

        val isOwner = listing.sellerId == Session.userId

        if (Session.role == "BUYER") {
            btnPrimary.text = "Add to Cart"
            btnSecondary.text = "Back"

            btnPrimary.setOnClickListener {
                db.addToCart(Session.userId, listing.id, 1)
                Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
            }

            btnSecondary.setOnClickListener {
                finish()
            }
        } else {
            btnPrimary.text = "Edit Listing"
            btnSecondary.text = "Delete Listing"

            btnPrimary.setOnClickListener {
                if (!isOwner) {
                    Toast.makeText(this, "You can only edit your own listing", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val intent = Intent(this, AddEditListingActivity::class.java)
                intent.putExtra("EDIT_ID", listing.id)
                startActivity(intent)
            }

            btnSecondary.setOnClickListener {
                if (!isOwner) {
                    Toast.makeText(this, "You can only delete your own listing", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                db.deleteListing(listing.id)
                Toast.makeText(this, "Listing deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}