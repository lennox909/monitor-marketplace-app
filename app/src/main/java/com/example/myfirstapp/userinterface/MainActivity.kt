package com.example.myfirstapp.userinterface

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = DatabaseHelper(this)

        val tvWelcome     = findViewById<TextView>(R.id.tvWelcome)
        val btnAddListing = findViewById<Button>(R.id.btnAddListing)
        val btnCart       = findViewById<Button>(R.id.btnCart)
        val btnLogout     = findViewById<Button>(R.id.btnLogout)
        val recycler      = findViewById<RecyclerView>(R.id.recyclerView)
        val tvEmpty       = findViewById<TextView>(R.id.tvEmpty)

        // Search + filter views (buyer only)
        val searchBar     = findViewById<View>(R.id.searchBar)
        val etSearch      = findViewById<EditText>(R.id.etSearch)
        val spCategory    = findViewById<Spinner>(R.id.spCategory)
        val btnSearch     = findViewById<Button>(R.id.btnSearch)

        val isSeller = Session.role == "SELLER"

        // Title
        tvWelcome.text = if (isSeller) "My Listings" else "Monitor Marketplace"

        // Show/hide role-specific buttons
        btnAddListing.visibility = if (isSeller) View.VISIBLE else View.GONE
        btnCart.visibility       = if (isSeller) View.GONE else View.VISIBLE

        // Show/hide search and filter (buyers only)
        searchBar.visibility  = if (isSeller) View.GONE else View.VISIBLE
        spCategory.visibility = if (isSeller) View.GONE else View.VISIBLE
        btnSearch.visibility  = if (isSeller) View.GONE else View.VISIBLE

        // Recycler
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ListingAdapter(emptyList()) { listingId ->
            val i = Intent(this, ListingDetailActivity::class.java)
            i.putExtra("LISTING_ID", listingId)
            startActivity(i)
        }
        recycler.adapter = adapter

        // Condition filter options for buyers
        spCategory.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("All Conditions", "New", "Like New", "Used")
        )

        // Live search (buyer only)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                applyFilters(s.toString(), spCategory.selectedItem.toString(), tvEmpty, recycler)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                applyFilters(etSearch.text.toString(), spCategory.selectedItem.toString(), tvEmpty, recycler)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnSearch.setOnClickListener {
            applyFilters(etSearch.text.toString(), spCategory.selectedItem.toString(), tvEmpty, recycler)
        }

        btnAddListing.setOnClickListener {
            startActivity(Intent(this, AddEditListingActivity::class.java))
        }

        btnCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
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
        val etSearch   = findViewById<EditText>(R.id.etSearch)
        val spCategory = findViewById<Spinner>(R.id.spCategory)
        val tvEmpty    = findViewById<TextView>(R.id.tvEmpty)
        val recycler   = findViewById<RecyclerView>(R.id.recyclerView)
        refreshListings()
        applyFilters(etSearch.text.toString(), spCategory.selectedItem.toString(), tvEmpty, recycler)
    }

    private fun refreshListings() {
        allListings = if (Session.role == "SELLER")
            db.getListingsBySeller(Session.userId)
        else
            db.getAllListingsGlobal()
    }

    private fun applyFilters(
        query: String,
        condition: String,
        tvEmpty: TextView,
        recycler: RecyclerView
    ) {
        val filtered = allListings.filter { listing ->
            val matchesSearch    = query.isBlank() ||
                    listing.title.contains(query, ignoreCase = true) ||
                    listing.brand.contains(query, ignoreCase = true)
            val matchesCondition = condition == "All Conditions" ||
                    listing.condition.equals(condition, ignoreCase = true)
            matchesSearch && matchesCondition
        }

        adapter.updateData(filtered)

        tvEmpty.visibility  = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        recycler.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }
}