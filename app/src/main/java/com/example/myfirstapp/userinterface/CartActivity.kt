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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        db = DatabaseHelper.getInstance(this)

        val rvCart    = findViewById<RecyclerView>(R.id.rvCart)
        tvSubtotal    = findViewById(R.id.tvSubtotal)
        btnCheckout   = findViewById(R.id.btnCheckout)
        val tvEmpty   = findViewById<TextView>(R.id.tvEmptyCart)
        val progress  = findViewById<ProgressBar>(R.id.progressCart)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        rvCart.layoutManager = LinearLayoutManager(this)

        adapter = CartAdapter(
            items = mutableListOf(),
            onQtyChanged = { row, newQty ->
                // Update subtotal immediately without reloading
                updateSubtotal()
                // Save to DB in background
                Thread {
                    db.updateCartItemQuantity(row.cartItemId, newQty)
                }.start()
            },
            onRemove = { row ->
                Thread {
                    db.removeCartItem(row.cartItemId)
                    runOnUiThread { loadCart(rvCart, tvEmpty, progress) }
                }.start()
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

        // Checkout button — check cart is not empty first
        btnCheckout.setOnClickListener {
            if (adapter.itemCount == 0) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, CheckoutActivity::class.java))
        }

        loadCart(rvCart, tvEmpty, progress)
    }

    override fun onResume() {
        super.onResume()
        val rvCart  = findViewById<RecyclerView>(R.id.rvCart)
        val tvEmpty = findViewById<TextView>(R.id.tvEmptyCart)
        val progress = findViewById<ProgressBar>(R.id.progressCart)
        loadCart(rvCart, tvEmpty, progress)
    }

    private fun loadCart(
        rvCart: RecyclerView,
        tvEmpty: TextView,
        progress: ProgressBar
    ) {
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

                if (rows.isEmpty()) {
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
                    adapter.setData(rows)
                    updateSubtotal()
                }
            }
        }.start()
    }

    private fun updateSubtotal() {
        tvSubtotal.text = "Subtotal: $${String.format("%.2f", adapter.getTotalPrice())}"
    }
}