package com.example.myfirstapp.userinterface

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.*

class MyOrdersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_orders)

        val lvOrders = findViewById<ListView>(R.id.lvOrders)
        val tvEmpty  = findViewById<TextView>(R.id.tvEmptyOrders)
        val progress = findViewById<ProgressBar>(R.id.progressOrders)

        progress.visibility = View.VISIBLE

        Thread {
            val db     = DatabaseHelper(this)
            val orders = db.getOrdersByUser(Session.userId)

            runOnUiThread {
                progress.visibility = View.GONE

                if (orders.isEmpty()) {
                    tvEmpty.visibility  = View.VISIBLE
                    lvOrders.visibility = View.GONE
                    return@runOnUiThread
                }

                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val displayList = orders.map { order ->
                    val id      = order["id"] as Long
                    val total   = order["total"] as Double
                    val created = order["created"] as Long
                    "Order #$id\nTotal: $${String.format("%.2f", total)}\n${sdf.format(Date(created))}"
                }

                lvOrders.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    displayList
                )
                tvEmpty.visibility  = View.GONE
                lvOrders.visibility = View.VISIBLE
            }
        }.start()
    }
}