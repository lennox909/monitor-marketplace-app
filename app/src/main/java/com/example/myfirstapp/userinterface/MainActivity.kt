package com.example.myfirstapp.userinterface

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
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

        if (Session.role == "ADMIN") {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)

        val etSearch = findViewById<EditText>(R.id.etSearch)
        val spCategory = findViewById<Spinner>(R.id.spCategory)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)

        val categories = listOf("All", "Programming", "Design", "Office", "General")
        spCategory.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ListingAdapter(emptyList())
        recyclerView.adapter = adapter

        fun loadAllListings() {
            val listings =
                if (Session.role == "SELLER") {
                    db.getListingsBySeller(Session.userId)
                } else {
                    db.getAllListingsGlobal()
                }

            adapter.updateData(listings)
            tvEmpty.visibility = if (listings.isEmpty()) View.VISIBLE else View.GONE
        }

        btnSearch.setOnClickListener {
            val keyword = etSearch.text.toString().trim()
            val selectedCategory = spCategory.selectedItem.toString()

            val listings =
                if (Session.role == "SELLER") {
                    db.getListingsBySeller(Session.userId).filter { listing ->
                        val keywordMatch =
                            keyword.isBlank() ||
                                    listing.title.contains(keyword, ignoreCase = true) ||
                                    listing.description.contains(keyword, ignoreCase = true)

                        val categoryMatch =
                            selectedCategory == "All" ||
                                    listing.category.equals(selectedCategory, ignoreCase = true)

                        keywordMatch && categoryMatch
                    }
                } else {
                    db.searchListings(
                        keyword = if (keyword.isBlank()) null else keyword,
                        category = if (selectedCategory == "All") null else selectedCategory,
                        minPrice = null,
                        maxPrice = null
                    )
                }

            adapter.updateData(listings)
            tvEmpty.visibility = if (listings.isEmpty()) View.VISIBLE else View.GONE
        }

        loadAllListings()
    }

    override fun onResume() {
        super.onResume()
        if (::db.isInitialized && ::adapter.isInitialized) {
            val listings =
                if (Session.role == "SELLER") {
                    db.getListingsBySeller(Session.userId)
                } else {
                    db.getAllListingsGlobal()
                }
            adapter.updateData(listings)
        }
    }
}