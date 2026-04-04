package com.example.myfirstapp.userinterface

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
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

        val rvCart = findViewById<RecyclerView>(R.id.rvCart)
        val tvSubtotal = findViewById<TextView>(R.id.tvSubtotal)
        val tvEmpty = findViewById<TextView>(R.id.tvEmptyCart)
        val btnCheckout = findViewById<Button>(R.id.btnCheckout)

        rvCart.layoutManager = LinearLayoutManager(this)

        adapter = CartAdapter(
            items = mutableListOf(),
            onQtyChanged = { row, newQty ->
                db.updateCartItemQuantity(row.cartItemId, newQty)
                refresh(tvSubtotal, tvEmpty, btnCheckout)
            },
            onRemove = { row ->
                db.removeCartItem(row.cartItemId)
                refresh(tvSubtotal, tvEmpty, btnCheckout)
            }
        )
        rvCart.adapter = adapter

        btnCheckout.setOnClickListener {
            startActivity(Intent(this, CheckoutActivity::class.java))
        }

        refresh(tvSubtotal, tvEmpty, btnCheckout)
    }

    private fun refresh(tvSubtotal: TextView, tvEmpty: TextView, btnCheckout: Button) {
        val cartItems = db.getCartItems(Session.userId)

        val rows = cartItems.mapNotNull { ci ->
            val l = db.getListingById(ci.listingId) ?: return@mapNotNull null
            CartRow(ci.id, l.id, l.title, l.price, ci.quantity)
        }

        adapter.setData(rows)

        val subtotal = rows.sumOf { it.price * it.qty }
        tvSubtotal.text = "Subtotal: $${"%.2f".format(subtotal)}"
        btnCheckout.isEnabled = rows.isNotEmpty()
        tvEmpty.visibility = if (rows.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }
}