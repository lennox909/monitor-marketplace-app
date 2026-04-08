package com.example.myfirstapp.userinterface

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R

class OrderConfirmationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirmation)

        val orderId = intent.getLongExtra("ORDER_ID", -1)

        findViewById<TextView>(R.id.tvOrderId).text =
            "Order Confirmed!\nOrder #$orderId\n\nThank you for your purchase."

        findViewById<Button>(R.id.btnContinue).setOnClickListener {
            // Go back to main and clear the back stack
            val i = Intent(this, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(i)
            finish()
        }
    }
}