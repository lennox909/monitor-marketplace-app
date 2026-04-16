package com.example.myfirstapp.userinterface

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper

class CheckoutActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private var isFormattingCard   = false
    private var isFormattingExpiry = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        db = DatabaseHelper.getInstance(this)

        val etFirstName  = findViewById<EditText>(R.id.etFirstName)
        val etLastName   = findViewById<EditText>(R.id.etLastName)
        val etAddress    = findViewById<EditText>(R.id.etShipAddress)
        val etCity       = findViewById<EditText>(R.id.etShipCity)
        val etState      = findViewById<EditText>(R.id.etState)
        val etZip        = findViewById<EditText>(R.id.etZip)
        val etCardNumber = findViewById<EditText>(R.id.etCardNumber)
        val etExpiry     = findViewById<EditText>(R.id.etExpiry)
        val etCVV        = findViewById<EditText>(R.id.etCVV)
        val etCardName   = findViewById<EditText>(R.id.etCardName)
        val tvCardType   = findViewById<TextView>(R.id.tvCardType)
        val btnPlace     = findViewById<Button>(R.id.btnPlaceOrder)
        val bottomNav    = findViewById<BottomNavigationView>(R.id.bottomNav)

        // Guard — if cart is empty send back
        Thread {
            val cartItems = db.getCartItems(Session.userId)
            runOnUiThread {
                if (cartItems.isEmpty()) {
                    Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }.start()

        // Card number max 19 chars
        etCardNumber.filters = arrayOf(InputFilter.LengthFilter(19))
        etExpiry.filters     = arrayOf(InputFilter.LengthFilter(5))

        // Auto-format card number
        etCardNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isFormattingCard) return
                isFormattingCard = true
                val digits    = s.toString().replace(" ", "")
                val limited   = digits.take(16)
                val formatted = limited.chunked(4).joinToString(" ")
                etCardNumber.setText(formatted)
                etCardNumber.setSelection(formatted.length)
                tvCardType.text = detectCardType(limited)
                val isAmex = limited.startsWith("34") || limited.startsWith("37")
                etCVV.filters = arrayOf(InputFilter.LengthFilter(if (isAmex) 4 else 3))
                isFormattingCard = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Auto-format expiry MM/YY
        etExpiry.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isFormattingExpiry) return
                isFormattingExpiry = true
                val digits    = s.toString().replace("/", "")
                val limited   = digits.take(4)
                val formatted = when {
                    limited.length >= 3 -> "${limited.take(2)}/${limited.drop(2)}"
                    limited.length == 2 -> "$limited/"
                    else -> limited
                }
                etExpiry.setText(formatted)
                etExpiry.setSelection(formatted.length)
                isFormattingExpiry = false
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

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
                    finish()
                    true
                }
                R.id.nav_sell -> {
                    Session.isBuyMode = false
                    startActivity(Intent(this, AddEditListingActivity::class.java))
                    true
                }
                R.id.nav_cart -> {
                    val i = Intent(this, CartActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(i)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    val i = Intent(this, ProfileActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(i)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                    true
                }
                else -> false
            }
        }

        btnPlace.setOnClickListener {
            val firstName  = etFirstName.text.toString().trim()
            val lastName   = etLastName.text.toString().trim()
            val address    = etAddress.text.toString().trim()
            val city       = etCity.text.toString().trim()
            val state      = etState.text.toString().trim()
            val zip        = etZip.text.toString().trim()
            val cardNumber = etCardNumber.text.toString().replace(" ", "")
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

            if (cardNumber.length < 15) {
                Toast.makeText(this, "Enter a valid card number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!expiry.matches(Regex("\\d{2}/\\d{2}"))) {
                Toast.makeText(this, "Enter expiry as MM/YY", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isAmex = cardNumber.startsWith("34") || cardNumber.startsWith("37")
            if (cvv.length < (if (isAmex) 4 else 3)) {
                Toast.makeText(this, "Enter a valid CVV", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (zip.length < 5) {
                Toast.makeText(this, "Enter a valid ZIP code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnPlace.isEnabled = false
            btnPlace.text      = "Processing..."

            val cardType      = detectCardType(cardNumber)
            val shippingInfo  = "$firstName $lastName, $address, $city, $state $zip"
            val paymentMethod = "$cardType ending in ${cardNumber.takeLast(4)}"

            Thread {
                val orderId = db.placeOrder(Session.userId, shippingInfo, paymentMethod)
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

    private fun detectCardType(number: String): String {
        return when {
            number.startsWith("4")                                                   -> "Visa"
            number.startsWith("34") || number.startsWith("37")                       -> "Amex"
            number.take(2).toIntOrNull()?.let { it in 51..55 } == true              -> "Mastercard"
            number.take(4).toIntOrNull()?.let { it in 2221..2720 } == true          -> "Mastercard"
            number.startsWith("6011") || number.startsWith("65")                    -> "Discover"
            else -> "Card"
        }
    }
}