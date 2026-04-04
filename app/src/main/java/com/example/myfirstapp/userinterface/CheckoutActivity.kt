package com.example.myfirstapp.userinterface

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper

class CheckoutActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        db = DatabaseHelper(this)

        val etName = findViewById<EditText>(R.id.etShipName)
        val etAddress = findViewById<EditText>(R.id.etShipAddress)
        val etCity = findViewById<EditText>(R.id.etShipCity)
        val spPayment = findViewById<Spinner>(R.id.spPayment)
        val btnPlace = findViewById<Button>(R.id.btnPlaceOrder)

        spPayment.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Mock Card", "Mock PayPal", "Mock Apple Pay")
        )

        btnPlace.setOnClickListener {
            val name = etName.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val city = etCity.text.toString().trim()
            val payment = spPayment.selectedItem.toString()

            if (name.isEmpty() || address.isEmpty() || city.isEmpty()) {
                Toast.makeText(this, "Fill shipping fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val shippingInfo = "Name=$name; Address=$address; City=$city"
            val orderId = db.placeOrder(Session.userId, shippingInfo, payment)

            if (orderId <= 0) {
                Toast.makeText(this, "Checkout failed (cart empty or items unavailable)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val i = Intent(this, OrderConfirmationActivity::class.java)
            i.putExtra("ORDER_ID", orderId)
            startActivity(i)
            finish()
        }
    }
}