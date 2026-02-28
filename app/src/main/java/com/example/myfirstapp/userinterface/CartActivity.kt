package com.example.myfirstapp.userinterface

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper

class CartActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        db = DatabaseHelper(this)

        val tvSummary = findViewById<TextView>(R.id.tvCartSummary)
        val btnCheckout = findViewById<Button>(R.id.btnCheckout)

        val items = db.getCartItems(Session.userId)
        if (items.isEmpty()) {
            tvSummary.text = "Cart is empty"
            btnCheckout.isEnabled = false
            return
        }

        var total = 0.0
        for (ci in items) {
            val listing = db.getListingById(ci.listingId)
            if (listing != null) {
                total += listing.price * ci.quantity
            }
        }

        tvSummary.text = "Items: ${items.size}\nSubtotal: $${"%.2f".format(total)}"

        btnCheckout.setOnClickListener {
            db.clearCart(Session.userId)
            Toast.makeText(this, "Order placed (mock)!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}