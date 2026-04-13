package com.example.myfirstapp.userinterface

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper

class ListingDetailActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var listingId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listing_detail)

        db        = DatabaseHelper.getInstance(this)
        listingId = intent.getLongExtra("LISTING_ID", -1)

        val ivImage       = findViewById<ImageView>(R.id.ivListingImage)
        val tvTitle       = findViewById<TextView>(R.id.tvTitle)
        val tvPrice       = findViewById<TextView>(R.id.tvPrice)
        val tvBrand       = findViewById<TextView>(R.id.tvBrand)
        val tvScreenSize  = findViewById<TextView>(R.id.tvScreenSize)
        val tvResolution  = findViewById<TextView>(R.id.tvResolution)
        val tvCondition   = findViewById<TextView>(R.id.tvCondition)
        val tvDescription = findViewById<TextView>(R.id.tvDescription)
        val btnPrimary    = findViewById<Button>(R.id.btnPrimary)
        val btnSecondary  = findViewById<Button>(R.id.btnSecondary)

        val listing = db.getListingById(listingId)
        if (listing == null) {
            Toast.makeText(this, "Listing not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load image safely — hide if missing or unreadable
        if (!listing.photoUri.isNullOrEmpty()) {
            try {
                ivImage.setImageURI(Uri.parse(listing.photoUri))
                ivImage.visibility = View.VISIBLE
            } catch (e: Exception) {
                ivImage.visibility = View.GONE
            }
        } else {
            ivImage.visibility = View.GONE
        }

        tvTitle.text       = listing.title
        tvPrice.text       = "$${String.format("%.2f", listing.price)}"
        tvBrand.text       = "Brand: ${listing.brand}"
        tvScreenSize.text  = "Screen Size: ${listing.screenSize}"
        tvResolution.text  = "Resolution: ${listing.resolution}"
        tvCondition.text   = "Condition: ${listing.condition}"
        tvDescription.text = listing.description

        val isOwner = listing.sellerId == Session.userId

        if (Session.isBuyMode) {
            btnPrimary.text   = "Add to Cart"
            btnSecondary.text = "Back"

            btnPrimary.setOnClickListener {
                db.addToCart(Session.userId, listingId, 1)
                Toast.makeText(this, "Added to cart!", Toast.LENGTH_SHORT).show()
            }
            btnSecondary.setOnClickListener { finish() }

        } else {
            btnPrimary.text   = "Edit"
            btnSecondary.text = "Delete"

            btnPrimary.isEnabled   = isOwner
            btnSecondary.isEnabled = isOwner
            btnPrimary.alpha       = if (isOwner) 1f else 0.4f
            btnSecondary.alpha     = if (isOwner) 1f else 0.4f

            btnPrimary.setOnClickListener {
                val i = Intent(this, AddEditListingActivity::class.java)
                i.putExtra("EDIT_ID", listingId)
                startActivity(i)
            }
            btnSecondary.setOnClickListener {
                db.deleteListing(listingId)
                Toast.makeText(this, "Listing deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}