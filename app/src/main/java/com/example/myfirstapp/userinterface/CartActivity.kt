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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        db = DatabaseHelper.getInstance(this)

        val rvCart      = findViewById<RecyclerView>(R.id.rvCart)
        val tvSubtotal  = findViewById<TextView>(R.id.tvSubtotal)
        val btnCheckout = findViewById<Button>(R.id.btnCheckout)
        val tvEmpty     = findViewById<TextView>(R.id.tvEmptyCart)
        val progress    = findViewById<ProgressBar>(R.id.progressCart)
        val bottomNav   = findViewById<BottomNavigationView>(R.id.bottomNav)

        rvCart.layoutManager = LinearLayoutManager(this)

        adapter = CartAdapter(
            items = mutableListOf(),
            onQtyChanged = { row, newQty ->
                Thread {
                    if (newQty <= 0) db.removeCartItem(row.cartItemId)
                    else db.updateCartItemQuantity(row.cartItemId, newQty)
                    runOnUiThread { loadCart(rvCart, tvSubtotal, tvEmpty, progress) }
                }.start()
            },
            onRemove = { row ->
                Thread {
                    db.removeCartItem(row.cartItemId)
                    runOnUiThread { loadCart(rvCart, tvSubtotal, tvEmpty, progress) }
                }.start()
            }
        )
        rvCart.adapter = adapter

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

        loadCart(rvCart, tvSubtotal, tvEmpty, progress)

        btnCheckout.setOnClickListener {
            startActivity(Intent(this, CheckoutActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        val rvCart     = findViewById<RecyclerView>(R.id.rvCart)
        val tvSubtotal = findViewById<TextView>(R.id.tvSubtotal)
        val tvEmpty    = findViewById<TextView>(R.id.tvEmptyCart)
        val progress   = findViewById<ProgressBar>(R.id.progressCart)
        loadCart(rvCart, tvSubtotal, tvEmpty, progress)
    }

    private fun loadCart(
        rvCart: RecyclerView,
        tvSubtotal: TextView,
        tvEmpty: TextView,
        progress: ProgressBar
    ) {
        progress.visibility = View.VISIBLE

        Thread {
            val cartItems = db.getCartItems(Session.userId)
            val rows      = mutableListOf<CartRow>()
            var subtotal  = 0.0

            for (ci in cartItems) {
                val listing = db.getListingById(ci.listingId) ?: continue
                rows.add(CartRow(
                    cartItemId = ci.id,
                    listingId  = ci.listingId,
                    title      = listing.title,
                    price      = listing.price,
                    qty        = ci.quantity
                ))
                subtotal += listing.price * ci.quantity
            }

            runOnUiThread {
                progress.visibility = View.GONE
                if (rows.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    rvCart.visibility  = View.GONE
                    tvSubtotal.text    = ""
                } else {
                    tvEmpty.visibility = View.GONE
                    rvCart.visibility  = View.VISIBLE
                    tvSubtotal.text    = "Subtotal: $${String.format("%.2f", subtotal)}"
                    adapter.setData(rows)
                }
            }
        }.start()
    }
}