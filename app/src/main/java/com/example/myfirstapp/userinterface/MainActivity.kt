package com.example.myfirstapp.userinterface

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper
import com.example.myfirstapp.model.Listing

class MainActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: ListingAdapter
    private var allListings: List<Listing> = emptyList()

    private var filterCategory  = "All Categories"
    private var filterCondition = "All Conditions"
    private var filterMinPrice  : Double? = null
    private var filterMaxPrice  : Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper.getInstance(this)

        val tvWelcome     = findViewById<TextView>(R.id.tvWelcome)
        val tvModeLabel   = findViewById<TextView>(R.id.tvModeLabel)
        val switchMode    = findViewById<Switch>(R.id.switchMode)
        val btnAddListing = findViewById<Button>(R.id.btnAddListing)
        val btnCart       = findViewById<Button>(R.id.btnCart)
        val btnLogout     = findViewById<Button>(R.id.btnLogout)
        val btnProfile    = findViewById<Button>(R.id.btnProfile)
        val recycler      = findViewById<RecyclerView>(R.id.recyclerView)
        val tvEmpty       = findViewById<TextView>(R.id.tvEmpty)
        val progressBar   = findViewById<ProgressBar>(R.id.progressBar)
        val etSearch      = findViewById<EditText>(R.id.etSearch)
        val btnFilter     = findViewById<Button>(R.id.btnFilter)
        val buyerControls = findViewById<LinearLayout>(R.id.buyerControls)

        // Recycler
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ListingAdapter(emptyList()) { listingId ->
            val i = Intent(this, ListingDetailActivity::class.java)
            i.putExtra("LISTING_ID", listingId)
            startActivity(i)
        }
        recycler.adapter = adapter

        // Initial UI
        switchMode.isChecked = Session.isBuyMode
        updateModeUI(Session.isBuyMode, tvWelcome, tvModeLabel, btnAddListing, btnCart, buyerControls)

        switchMode.setOnCheckedChangeListener { _, isChecked ->
            Session.isBuyMode = isChecked
            updateModeUI(isChecked, tvWelcome, tvModeLabel, btnAddListing, btnCart, buyerControls)
            loadListings(progressBar, tvEmpty, recycler, etSearch)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                applyFilters(s.toString(), tvEmpty, recycler)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnFilter.setOnClickListener {
            showFilterDialog(tvEmpty, recycler)
        }

        btnAddListing.setOnClickListener {
            startActivity(Intent(this, AddEditListingActivity::class.java))
        }

        btnCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnLogout.setOnClickListener {
            Session.userId    = -1L
            Session.role      = "BUYER"
            Session.isBuyMode = true
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val tvEmpty     = findViewById<TextView>(R.id.tvEmpty)
        val recycler    = findViewById<RecyclerView>(R.id.recyclerView)
        val etSearch    = findViewById<EditText>(R.id.etSearch)
        loadListings(progressBar, tvEmpty, recycler, etSearch)
    }

    private fun showFilterDialog(tvEmpty: TextView, recycler: RecyclerView) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_filter, null)

        val spCategory  = view.findViewById<Spinner>(R.id.spFilterCategory)
        val spCondition = view.findViewById<Spinner>(R.id.spFilterCondition)
        val etMinPrice  = view.findViewById<EditText>(R.id.etFilterMinPrice)
        val etMaxPrice  = view.findViewById<EditText>(R.id.etFilterMaxPrice)

        val categories = listOf("All Categories", "Gaming", "Office", "4K", "Ultrawide", "General")
        val conditions = listOf("All Conditions", "New", "Like New", "Used")

        spCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spCondition.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, conditions)

        spCategory.setSelection(categories.indexOf(filterCategory).coerceAtLeast(0))
        spCondition.setSelection(conditions.indexOf(filterCondition).coerceAtLeast(0))
        etMinPrice.setText(filterMinPrice?.toString() ?: "")
        etMaxPrice.setText(filterMaxPrice?.toString() ?: "")

        AlertDialog.Builder(this)
            .setTitle("Filter Listings")
            .setView(view)
            .setPositiveButton("Apply") { _, _ ->
                filterCategory  = spCategory.selectedItem.toString()
                filterCondition = spCondition.selectedItem.toString()
                filterMinPrice  = etMinPrice.text.toString().toDoubleOrNull()
                filterMaxPrice  = etMaxPrice.text.toString().toDoubleOrNull()

                val hasFilter = filterCategory != "All Categories" ||
                        filterCondition != "All Conditions" ||
                        filterMinPrice != null || filterMaxPrice != null
                findViewById<Button>(R.id.btnFilter).text = if (hasFilter) "Filter ●" else "Filter"
                applyFilters(findViewById<EditText>(R.id.etSearch).text.toString(), tvEmpty, recycler)
            }
            .setNegativeButton("Clear") { _, _ ->
                filterCategory  = "All Categories"
                filterCondition = "All Conditions"
                filterMinPrice  = null
                filterMaxPrice  = null
                findViewById<Button>(R.id.btnFilter).text = "Filter"
                applyFilters(findViewById<EditText>(R.id.etSearch).text.toString(), tvEmpty, recycler)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun updateModeUI(
        isBuyMode: Boolean,
        tvWelcome: TextView,
        tvModeLabel: TextView,
        btnAdd: Button,
        btnCart: Button,
        buyerControls: LinearLayout
    ) {
        if (isBuyMode) {
            tvWelcome.text           = "Monitor Marketplace"
            tvModeLabel.text         = "Buy Mode"
            btnAdd.visibility        = View.GONE
            btnCart.visibility       = View.VISIBLE
            buyerControls.visibility = View.VISIBLE
        } else {
            tvWelcome.text           = "My Listings"
            tvModeLabel.text         = "Sell Mode"
            btnAdd.visibility        = View.VISIBLE
            btnCart.visibility       = View.GONE
            buyerControls.visibility = View.GONE
        }
    }

    private fun loadListings(
        progressBar: ProgressBar,
        tvEmpty: TextView,
        recycler: RecyclerView,
        etSearch: EditText
    ) {
        progressBar.visibility = View.VISIBLE
        recycler.visibility    = View.GONE
        tvEmpty.visibility     = View.GONE

        Thread {
            val listings = if (Session.isBuyMode)
                db.getAllListingsGlobal()
            else
                db.getListingsBySeller(Session.userId)

            runOnUiThread {
                allListings            = listings
                progressBar.visibility = View.GONE
                applyFilters(etSearch.text.toString(), tvEmpty, recycler)
            }
        }.start()
    }

    private fun applyFilters(query: String, tvEmpty: TextView, recycler: RecyclerView) {
        val filtered = allListings.filter { listing ->
            val matchesSearch    = query.isBlank() ||
                    listing.title.contains(query, ignoreCase = true) ||
                    listing.brand.contains(query, ignoreCase = true)
            val matchesCategory  = filterCategory == "All Categories" ||
                    listing.category.equals(filterCategory, ignoreCase = true)
            val matchesCondition = filterCondition == "All Conditions" ||
                    listing.condition.equals(filterCondition, ignoreCase = true)
            val matchesMin       = filterMinPrice == null || listing.price >= filterMinPrice!!
            val matchesMax       = filterMaxPrice == null || listing.price <= filterMaxPrice!!
            matchesSearch && matchesCategory && matchesCondition && matchesMin && matchesMax
        }

        adapter.updateData(filtered)
        tvEmpty.visibility  = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        recycler.visibility = if (filtered.isEmpty()) View.GONE    else View.VISIBLE
    }
}