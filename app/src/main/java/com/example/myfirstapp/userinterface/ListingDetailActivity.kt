package com.example.myfirstapp.userinterface

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper
import java.io.File

class ListingDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listing_detail)

        val db        = DatabaseHelper.getInstance(this)
        val listingId = intent.getLongExtra("LISTING_ID", -1L)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        val ivPhoto       = findViewById<ImageView>(R.id.ivDetailPhoto)
        val tvTitle       = findViewById<TextView>(R.id.tvDetailTitle)
        val tvPrice       = findViewById<TextView>(R.id.tvDetailPrice)
        val tvBrand       = findViewById<TextView>(R.id.tvDetailBrand)
        val tvScreen      = findViewById<TextView>(R.id.tvDetailScreen)
        val tvResolution  = findViewById<TextView>(R.id.tvDetailResolution)
        val tvCondition   = findViewById<TextView>(R.id.tvDetailCondition)
        val tvCategory    = findViewById<TextView>(R.id.tvDetailCategory)
        val tvDesc        = findViewById<TextView>(R.id.tvDetailDesc)
        val btnAddToCart  = findViewById<Button>(R.id.btnAddToCart)
        val btnEdit       = findViewById<Button>(R.id.btnEditListing)
        val btnDelete     = findViewById<Button>(R.id.btnDeleteListing)

        // Bottom nav
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val i = Intent(this, MainActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(i)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                    true
                }
                R.id.nav_sell -> {
                    Session.isBuyMode = false
                    startActivity(Intent(this, AddEditListingActivity::class.java))
                    true
                }
                R.id.nav_cart -> {
                    val i = Intent(this, CartActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(i)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    val i = Intent(this, ProfileActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(i)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                    true
                }
                else -> false
            }
        }

        if (listingId == -1L) {
            Toast.makeText(this, "Listing not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Thread {
            val listing = db.getListingById(listingId)
            runOnUiThread {
                if (listing == null) {
                    Toast.makeText(this, "Listing not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@runOnUiThread
                }

                tvTitle.text      = listing.title
                tvPrice.text      = "$${String.format("%.2f", listing.price)}"
                tvBrand.text      = "Brand: ${listing.brand}"
                tvScreen.text     = "Screen Size: ${listing.screenSize}"
                tvResolution.text = "Resolution: ${listing.resolution}"
                tvCondition.text  = "Condition: ${listing.condition}"
                tvCategory.text   = "Category: ${listing.category}"
                tvDesc.text       = listing.description

                // Load photo
                if (!listing.photoUri.isNullOrEmpty()) {
                    try {
                        val file = File(listing.photoUri)
                        if (file.exists()) ivPhoto.setImageURI(Uri.fromFile(file))
                        else ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
                    } catch (e: Exception) {
                        ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                } else {
                    ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
                }

                // Show correct buttons based on mode
                if (Session.isBuyMode) {
                    btnAddToCart.visibility = View.VISIBLE
                    btnEdit.visibility      = View.GONE
                    btnDelete.visibility    = View.GONE

                    btnAddToCart.setOnClickListener {
                        Thread {
                            db.addToCart(Session.userId, listing.id)
                            runOnUiThread {
                                Toast.makeText(this, "${listing.title} added to cart", Toast.LENGTH_SHORT).show()
                            }
                        }.start()
                    }
                } else {
                    // Sell mode — only show edit/delete if it's the seller's own listing
                    if (listing.sellerId == Session.userId) {
                        btnAddToCart.visibility = View.GONE
                        btnEdit.visibility      = View.VISIBLE
                        btnDelete.visibility    = View.VISIBLE

                        btnEdit.setOnClickListener {
                            val i = Intent(this, AddEditListingActivity::class.java)
                            i.putExtra("LISTING_ID", listing.id)
                            startActivity(i)
                        }

                        btnDelete.setOnClickListener {
                            AlertDialog.Builder(this)
                                .setTitle("Delete Listing")
                                .setMessage("Are you sure you want to delete this listing?")
                                .setPositiveButton("Delete") { _, _ ->
                                    Thread {
                                        db.deleteListing(listing.id)
                                        runOnUiThread {
                                            Toast.makeText(this, "Listing deleted", Toast.LENGTH_SHORT).show()
                                            finish()
                                        }
                                    }.start()
                                }
                                .setNegativeButton("Cancel", null)
                                .show()
                        }
                    } else {
                        btnAddToCart.visibility = View.GONE
                        btnEdit.visibility      = View.GONE
                        btnDelete.visibility    = View.GONE
                    }
                }
            }
        }.start()
    }
}