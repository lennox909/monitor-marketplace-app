package com.example.myfirstapp.userinterface

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper

class MainActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: ListingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)

        val switchMode = findViewById<Switch>(R.id.switchMode)
        val btnAddListing = findViewById<Button>(R.id.btnAddListing)
        val btnCart = findViewById<Button>(R.id.btnCart)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val recycler = findViewById<RecyclerView>(R.id.rvListings)

        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ListingAdapter(emptyList()) { listingId ->
            val i = Intent(this, ListingDetailActivity::class.java)
            i.putExtra("LISTING_ID", listingId)
            startActivity(i)
        }
        recycler.adapter = adapter

        switchMode.isChecked = Session.isBuyMode
        btnAddListing.isEnabled = !Session.isBuyMode
        btnCart.isEnabled = Session.isBuyMode

        switchMode.setOnCheckedChangeListener { _, isChecked ->
            Session.isBuyMode = isChecked
            btnAddListing.isEnabled = !Session.isBuyMode
            btnCart.isEnabled = Session.isBuyMode
            refreshListings()
        }

        btnAddListing.setOnClickListener {
            if (Session.isBuyMode) {
                Toast.makeText(this, "Switch to Sell Mode to add listings", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, AddEditListingActivity::class.java))
        }

        btnCart.setOnClickListener {
            if (!Session.isBuyMode) {
                Toast.makeText(this, "Switch to Buy Mode to view cart", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, CartActivity::class.java))
        }

        btnLogout.setOnClickListener {
            Session.userId = -1
            Session.role = "BUYER"
            Session.isBuyMode = true
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshListings()
    }

    private fun refreshListings() {
        val listings =
            if (Session.isBuyMode) db.getAllListingsGlobal()
            else db.getListingsBySeller(Session.userId)

        adapter.updateData(listings)
    }
}