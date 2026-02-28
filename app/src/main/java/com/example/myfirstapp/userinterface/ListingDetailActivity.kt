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
        val tvMeta = findViewById<TextView>(R.id.tvMeta)
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
        tvMeta.text = "${listing.category} • ${listing.condition}"
        tvDescription.text = listing.description

        val isOwner = listing.sellerId == Session.userId

        if (Session.isBuyMode) {
            btnPrimary.text = "Add to Cart"
            btnSecondary.text = "Back"

            btnPrimary.setOnClickListener {
                db.addToCart(Session.userId, listingId, 1)
                Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
            }
            btnSecondary.setOnClickListener { finish() }
        } else {
            btnPrimary.text = "Edit"
            btnSecondary.text = "Delete"

            btnPrimary.isEnabled = isOwner
            btnSecondary.isEnabled = isOwner

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
