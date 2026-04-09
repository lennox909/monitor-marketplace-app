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

        val etFirstName  = findViewById<EditText>(R.id.etFirstName)
        val etLastName   = findViewById<EditText>(R.id.etLastName)
        val etAddress    = findViewById<EditText>(R.id.etShipAddress)
        val etApt        = findViewById<EditText>(R.id.etApt)
        val etCity       = findViewById<EditText>(R.id.etShipCity)
        val etState      = findViewById<EditText>(R.id.etState)
        val etZip        = findViewById<EditText>(R.id.etZip)
        val etCardNumber = findViewById<EditText>(R.id.etCardNumber)
        val etExpiry     = findViewById<EditText>(R.id.etExpiry)
        val etCVV        = findViewById<EditText>(R.id.etCVV)
        val etCardName   = findViewById<EditText>(R.id.etCardName)
        val btnPlace     = findViewById<Button>(R.id.btnPlaceOrder)

        btnPlace.setOnClickListener {
            val firstName  = etFirstName.text.toString().trim()
            val lastName   = etLastName.text.toString().trim()
            val address    = etAddress.text.toString().trim()
            val city       = etCity.text.toString().trim()
            val state      = etState.text.toString().trim()
            val zip        = etZip.text.toString().trim()
            val cardNumber = etCardNumber.text.toString().trim()
            val expiry     = etExpiry.text.toString().trim()
            val cvv        = etCVV.text.toString().trim()
            val cardName   = etCardName.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || address.isEmpty() ||
                city.isEmpty() || state.isEmpty() || zip.isEmpty()) {
                Toast.makeText(this, "Please fill in all shipping fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cardNumber.isEmpty() || expiry.isEmpty() || cvv.isEmpty() || cardName.isEmpty()) {
                Toast.makeText(this, "Please fill in all card fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cardNumber.length < 16) {
                Toast.makeText(this, "Enter a valid 16-digit card number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (cvv.length < 3) {
                Toast.makeText(this, "Enter a valid CVV", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnPlace.isEnabled = false
            btnPlace.text      = "Placing order..."

            val shippingInfo = "$firstName $lastName, $address, $city, $state $zip"

            // DB call off main thread
            Thread {
                val orderId = db.placeOrder(Session.userId, shippingInfo, "Card")

                runOnUiThread {
                    btnPlace.isEnabled = true
                    btnPlace.text      = "Place Order"

                    if (orderId <= 0) {
                        Toast.makeText(this, "Checkout failed — cart may be empty", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    val i = Intent(this, OrderConfirmationActivity::class.java)
                    i.putExtra("ORDER_ID", orderId)
                    startActivity(i)
                    finish()
                }
            }.start()
        }
    }
}