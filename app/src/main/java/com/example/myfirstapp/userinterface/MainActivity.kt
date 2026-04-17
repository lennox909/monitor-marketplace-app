package com.example.myfirstapp.userinterface

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper
import com.example.myfirstapp.model.Listing

class MainActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: ListingAdapter
    private var allListings: List<Listing> = emptyList()

    private var selectedCategory  = "All"
    private var searchQuery       = ""
    private var needsRefresh      = true  // only reload when needed
    private var lastMode          = Session.isBuyMode

    private val categories = listOf("All", "Gaming", "Office", "4K", "Ultrawide", "General")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper.getInstance(this)

        val tvWelcome     = findViewById<TextView>(R.id.tvWelcome)
        val tvModeLabel   = findViewById<TextView>(R.id.tvModeLabel)
        val switchMode    = findViewById<Switch>(R.id.switchMode)
        val btnAddListing = findViewById<Button>(R.id.btnAddListing)
        val btnLogout     = findViewById<Button>(R.id.btnLogout)
        val recycler      = findViewById<RecyclerView>(R.id.recyclerView)
        val tvEmpty       = findViewById<TextView>(R.id.tvEmpty)
        val progressBar   = findViewById<ProgressBar>(R.id.progressBar)
        val etSearch      = findViewById<EditText>(R.id.etSearch)
        val buyerControls = findViewById<LinearLayout>(R.id.buyerControls)
        val categoryChips = findViewById<LinearLayout>(R.id.categoryChips)
        val bottomNav     = findViewById<BottomNavigationView>(R.id.bottomNav)

        recycler.layoutManager = GridLayoutManager(this, 2)
        adapter = ListingAdapter(emptyList()) { listingId ->
            val i = Intent(this, ListingDetailActivity::class.java)
            i.putExtra("LISTING_ID", listingId)
            startActivity(i)
        }
        recycler.adapter = adapter

        buildCategoryChips(categoryChips, recycler, tvEmpty)
        switchMode.isChecked = Session.isBuyMode
        updateModeUI(Session.isBuyMode, tvWelcome, tvModeLabel, btnAddListing, buyerControls, bottomNav)

        switchMode.setOnCheckedChangeListener { _, isChecked ->
            Session.isBuyMode = isChecked
            selectedCategory  = "All"
            searchQuery       = ""
            needsRefresh      = true
            etSearch.setText("")
            updateModeUI(isChecked, tvWelcome, tvModeLabel, btnAddListing, buyerControls, bottomNav)
            buildCategoryChips(categoryChips, recycler, tvEmpty)
            loadListings(progressBar, tvEmpty, recycler, etSearch)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s.toString()
                applyFilters(tvEmpty, recycler)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnAddListing.setOnClickListener {
            needsRefresh = true
            startActivity(Intent(this, AddEditListingActivity::class.java))
        }

        btnLogout.setOnClickListener {
            Session.userId    = -1L
            Session.role      = "USER"
            Session.isBuyMode = true
            needsRefresh      = true
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true

                R.id.nav_sell -> {
                    Session.isBuyMode    = false
                    needsRefresh         = true
                    switchMode.isChecked = false
                    updateModeUI(false, tvWelcome, tvModeLabel, btnAddListing, buyerControls, bottomNav)
                    buildCategoryChips(categoryChips, recycler, tvEmpty)
                    loadListings(progressBar, tvEmpty, recycler, etSearch)
                    startActivity(Intent(this, AddEditListingActivity::class.java))
                    true
                }

                R.id.nav_cart -> {
                    val i = Intent(this, CartActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(i)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    true
                }

                R.id.nav_profile -> {
                    val i = Intent(this, ProfileActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(i)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    true
                }

                else -> false
            }
        }

        // Initial load
        loadListings(progressBar, tvEmpty, recycler, etSearch)
        needsRefresh = false
    }

    override fun onResume() {
        super.onResume()
        val progressBar   = findViewById<ProgressBar>(R.id.progressBar)
        val tvEmpty       = findViewById<TextView>(R.id.tvEmpty)
        val recycler      = findViewById<RecyclerView>(R.id.recyclerView)
        val etSearch      = findViewById<EditText>(R.id.etSearch)
        val bottomNav     = findViewById<BottomNavigationView>(R.id.bottomNav)
        val tvWelcome     = findViewById<TextView>(R.id.tvWelcome)
        val tvModeLabel   = findViewById<TextView>(R.id.tvModeLabel)
        val btnAddListing = findViewById<Button>(R.id.btnAddListing)
        val buyerControls = findViewById<LinearLayout>(R.id.buyerControls)
        val categoryChips = findViewById<LinearLayout>(R.id.categoryChips)

        bottomNav.selectedItemId = R.id.nav_home

        // Only reload if mode changed or data was modified
        val modeChanged = lastMode != Session.isBuyMode
        if (modeChanged) {
            updateModeUI(Session.isBuyMode, tvWelcome, tvModeLabel, btnAddListing, buyerControls, bottomNav)
            buildCategoryChips(categoryChips, recycler, tvEmpty)
            lastMode = Session.isBuyMode
            needsRefresh = true
        }

        if (needsRefresh || allListings.isEmpty()) {
            loadListings(progressBar, tvEmpty, recycler, etSearch)
            needsRefresh = false
        } else {
            // Just reapply filters instantly — no DB call
            applyFilters(tvEmpty, recycler)
        }
    }

    private fun buildCategoryChips(
        container: LinearLayout,
        recycler: RecyclerView,
        tvEmpty: TextView
    ) {
        container.removeAllViews()
        categories.forEach { cat ->
            val chip = TextView(this).apply {
                text     = cat
                textSize = 13f
                setPadding(32, 16, 32, 16)
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 8, 0) }

                if (cat == selectedCategory) {
                    setBackgroundResource(R.drawable.chip_selected)
                    setTextColor(Color.WHITE)
                } else {
                    setBackgroundResource(R.drawable.chip_unselected)
                    setTextColor(Color.parseColor("#374151"))
                }

                setOnClickListener {
                    selectedCategory = cat
                    buildCategoryChips(container, recycler, tvEmpty)
                    applyFilters(tvEmpty, recycler)
                }
            }
            container.addView(chip)
        }
    }

    private fun updateModeUI(
        isBuyMode: Boolean,
        tvWelcome: TextView,
        tvModeLabel: TextView,
        btnAdd: Button,
        buyerControls: LinearLayout,
        bottomNav: BottomNavigationView
    ) {
        if (isBuyMode) {
            tvWelcome.text           = "Monitor Exchange"
            tvModeLabel.text         = "Buy Mode"
            btnAdd.visibility        = View.GONE
            buyerControls.visibility = View.VISIBLE
            bottomNav.menu.findItem(R.id.nav_cart)?.isVisible = true
        } else {
            tvWelcome.text           = "My Listings"
            tvModeLabel.text         = "Sell Mode"
            btnAdd.visibility        = View.VISIBLE
            buyerControls.visibility = View.GONE
            bottomNav.menu.findItem(R.id.nav_cart)?.isVisible = false
        }
    }

    private fun loadListings(
        progressBar: ProgressBar,
        tvEmpty: TextView,
        recycler: RecyclerView,
        etSearch: EditText
    ) {
        progressBar.visibility = View.VISIBLE

        Thread {
            val listings = if (Session.isBuyMode)
                db.getAllListingsGlobal(Session.userId)
            else
                db.getListingsBySeller(Session.userId)

            runOnUiThread {
                allListings            = listings
                progressBar.visibility = View.GONE
                searchQuery            = etSearch.text.toString()
                applyFilters(tvEmpty, recycler)
            }
        }.start()
    }

    private fun applyFilters(tvEmpty: TextView, recycler: RecyclerView) {
        val filtered = allListings.filter { listing ->
            val matchesSearch   = searchQuery.isBlank() ||
                    listing.title.contains(searchQuery, ignoreCase = true) ||
                    listing.brand.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" ||
                    listing.category.equals(selectedCategory, ignoreCase = true)
            matchesSearch && matchesCategory
        }

        adapter.updateData(filtered)
        tvEmpty.visibility  = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        recycler.visibility = if (filtered.isEmpty()) View.GONE    else View.VISIBLE
    }
}