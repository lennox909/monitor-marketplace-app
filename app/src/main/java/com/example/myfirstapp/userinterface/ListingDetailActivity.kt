package com.example.myfirstapp.userinterface

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
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

        val ivPhoto = findViewById<ImageView>(R.id.ivPhoto)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvPrice = findViewById<TextView>(R.id.tvPrice)
        val tvMeta = findViewById<TextView>(R.id.tvMeta)
        val tvDescription = findViewById<TextView>(R.id.tvDescription)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
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
        tvMeta.text = "${listing.category} • ${listing.condition}"
        tvDescription.text = listing.description

        if (!listing.photoUri.isNullOrBlank()) {
            ivPhoto.setImageURI(Uri.parse(listing.photoUri))
        } else {
            ivPhoto.setImageResource(R.drawable.ic_image_placeholder)
        }

        if (listing.status != "ACTIVE") {
            tvStatus.visibility = View.VISIBLE
            tvStatus.text = listing.status
        } else {
            tvStatus.visibility = View.GONE
        }

        val isOwner = listing.sellerId == Session.userId

        if (Session.isBuyMode) {
            btnPrimary.text = "Add to Cart"
            btnSecondary.text = "Back"

            if (listing.status != "ACTIVE") {
                btnPrimary.isEnabled = false
                btnPrimary.text = "Unavailable"
            }

            btnPrimary.setOnClickListener {
                db.addToCart(Session.userId, listingId, 1)
                Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
            }
            btnSecondary.setOnClickListener { finish() }

        } else {
            if (!isOwner) {
                btnPrimary.visibility = View.GONE
                btnSecondary.text = "Back"
                btnSecondary.setOnClickListener { finish() }
                return
            }

            btnPrimary.text = "Edit"
            btnSecondary.text = "Delete"

            btnPrimary.setOnClickListener {
                val i = Intent(this, AddEditListingActivity::class.java)
                i.putExtra("EDIT_ID", listingId)
                startActivity(i)
            }
            btnSecondary.setOnClickListener {
                db.deleteListing(listingId)
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
