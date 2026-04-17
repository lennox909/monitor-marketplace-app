package com.example.myfirstapp.userinterface

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper

class CartActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: CartAdapter
    private lateinit var tvSubtotal: TextView
    private lateinit var btnCheckout: Button
    private lateinit var tvEmpty: TextView
    private lateinit var rvCart: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        db          = DatabaseHelper.getInstance(this)
        rvCart      = findViewById(R.id.rvCart)
        tvSubtotal  = findViewById(R.id.tvSubtotal)
        btnCheckout = findViewById(R.id.btnCheckout)
        tvEmpty     = findViewById(R.id.tvEmptyCart)
        val progress  = findViewById<ProgressBar>(R.id.progressCart)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        rvCart.layoutManager = LinearLayoutManager(this)

        adapter = CartAdapter(
            items = mutableListOf(),
            onQtyChanged = { row, newQty ->
                // Update subtotal instantly — no reload
                updateSubtotal()
                Thread { db.updateCartItemQuantity(row.cartItemId, newQty) }.start()
            },
            onRemove = { row ->
                // Remove from adapter instantly — no flicker, no reload
                adapter.removeItemById(row.cartItemId)
                updateSubtotal()
                checkEmptyState()
                // Save to DB in background
                Thread { db.removeCartItem(row.cartItemId) }.start()
            }
        )
        rvCart.adapter = adapter

        // Bottom nav
        bottomNav.selectedItemId = R.id.nav_cart
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val i = Intent(this, MainActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(i)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    true
                }
                R.id.nav_sell -> {
                    Session.isBuyMode = false
                    startActivity(Intent(this, AddEditListingActivity::class.java))
                    true
                }
                R.id.nav_cart -> true
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

        btnCheckout.setOnClickListener {
            if (adapter.isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, CheckoutActivity::class.java))
        }

        loadCart(progress)
    }

    override fun onResume() {
        super.onResume()
        val progress = findViewById<ProgressBar>(R.id.progressCart)
        loadCart(progress)
    }

    private fun loadCart(progress: ProgressBar) {
        progress.visibility = View.VISIBLE

        Thread {
            val cartItems = db.getCartItems(Session.userId)
            val rows      = mutableListOf<CartRow>()

            for (ci in cartItems) {
                val listing = db.getListingById(ci.listingId) ?: continue
                rows.add(CartRow(
                    cartItemId = ci.id,
                    listingId  = ci.listingId,
                    title      = listing.title,
                    price      = listing.price,
                    qty        = ci.quantity
                ))
            }

            runOnUiThread {
                progress.visibility = View.GONE
                adapter.setData(rows)
                updateSubtotal()
                checkEmptyState()
            }
        }.start()
    }

    private fun updateSubtotal() {
        tvSubtotal.text = "Subtotal: $${String.format("%.2f", adapter.getTotalPrice())}"
    }

    private fun checkEmptyState() {
        if (adapter.isEmpty()) {
            tvEmpty.visibility    = View.VISIBLE
            rvCart.visibility     = View.GONE
            tvSubtotal.text       = ""
            btnCheckout.isEnabled = false
            btnCheckout.alpha     = 0.5f
        } else {
            tvEmpty.visibility    = View.GONE
            rvCart.visibility     = View.VISIBLE
            btnCheckout.isEnabled = true
            btnCheckout.alpha     = 1.0f
        }
    }
}