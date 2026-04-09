package com.example.myfirstapp.userinterface

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper

class CartActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var adapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        db = DatabaseHelper(this)

        val rvCart      = findViewById<RecyclerView>(R.id.rvCart)
        val tvSubtotal  = findViewById<TextView>(R.id.tvSubtotal)
        val tvEmpty     = findViewById<TextView>(R.id.tvEmptyCart)
        val btnCheckout = findViewById<Button>(R.id.btnCheckout)

        rvCart.layoutManager = LinearLayoutManager(this)

        adapter = CartAdapter(
            items = mutableListOf(),
            onQtyChanged = { row, newQty ->
                Thread {
                    if (newQty <= 0) db.removeCartItem(row.cartItemId)
                    else db.updateCartItemQuantity(row.cartItemId, newQty)
                    runOnUiThread { refresh(tvSubtotal, tvEmpty, btnCheckout) }
                }.start()
            },
            onRemove = { row ->
                Thread {
                    db.removeCartItem(row.cartItemId)
                    runOnUiThread { refresh(tvSubtotal, tvEmpty, btnCheckout) }
                }.start()
            }
        )
        rvCart.adapter = adapter

        btnCheckout.setOnClickListener {
            startActivity(Intent(this, CheckoutActivity::class.java))
        }

        refresh(tvSubtotal, tvEmpty, btnCheckout)
    }

    override fun onResume() {
        super.onResume()
        val tvSubtotal  = findViewById<TextView>(R.id.tvSubtotal)
        val tvEmpty     = findViewById<TextView>(R.id.tvEmptyCart)
        val btnCheckout = findViewById<Button>(R.id.btnCheckout)
        refresh(tvSubtotal, tvEmpty, btnCheckout)
    }

    private fun refresh(tvSubtotal: TextView, tvEmpty: TextView, btnCheckout: Button) {
        Thread {
            val cartItems = db.getCartItems(Session.userId)
            val rows = cartItems.mapNotNull { ci ->
                val l = db.getListingById(ci.listingId) ?: return@mapNotNull null
                CartRow(ci.id, l.id, l.title, l.price, ci.quantity)
            }
            val subtotal = rows.sumOf { it.price * it.qty }

            runOnUiThread {
                adapter.setData(rows)
                tvSubtotal.text     = "Subtotal: $${String.format("%.2f", subtotal)}"
                btnCheckout.isEnabled = rows.isNotEmpty()
                tvEmpty.visibility  = if (rows.isEmpty()) View.VISIBLE else View.GONE
            }
        }.start()
    }
}