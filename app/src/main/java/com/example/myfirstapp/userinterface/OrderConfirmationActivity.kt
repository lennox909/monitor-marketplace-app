package com.example.myfirstapp.userinterface

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper

class OrderConfirmationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirmation)

        val orderId     = intent.getLongExtra("ORDER_ID", -1)
        val tvOrderId   = findViewById<TextView>(R.id.tvOrderId)
        val tvSummary   = findViewById<TextView>(R.id.tvOrderSummary)
        val tvTotal     = findViewById<TextView>(R.id.tvOrderTotal)
        val btnContinue = findViewById<Button>(R.id.btnContinue)
        val progressBar = findViewById<ProgressBar>(R.id.progressConfirmation)

        progressBar.visibility = View.VISIBLE

        Thread {
            val db     = DatabaseHelper.getInstance(this)
            val items  = db.getOrderItems(orderId)
            val orders = db.getOrdersByUser(Session.userId)
            val order  = orders.firstOrNull { (it["id"] as Long) == orderId }
            val total  = order?.get("total") as? Double ?: 0.0

            runOnUiThread {
                progressBar.visibility = View.GONE
                tvOrderId.text = "✅ Order Confirmed!\nOrder #$orderId"

                if (items.isEmpty()) {
                    tvSummary.text = "No items found"
                } else {
                    val summary = items.joinToString("\n\n") { item ->
                        val title = item["title"] as String
                        val price = item["price"] as Double
                        val qty   = item["quantity"] as Int
                        "• $title\n  Qty: $qty  •  $${String.format("%.2f", price * qty)}"
                    }
                    tvSummary.text = summary
                }

                tvTotal.text = "Total Paid: $${String.format("%.2f", total)}"
            }
        }.start()

        btnContinue.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(i)
            finish()
        }
    }
}