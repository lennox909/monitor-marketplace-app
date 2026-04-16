package com.example.myfirstapp.userinterface

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myfirstapp.R
import com.example.myfirstapp.data.DatabaseHelper
import com.example.myfirstapp.model.Listing
import com.example.myfirstapp.model.User

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        db = DatabaseHelper.getInstance(this)

        val tvUserCount    = findViewById<TextView>(R.id.tvUserCount)
        val tvListingCount = findViewById<TextView>(R.id.tvListingCount)
        val btnTabUsers    = findViewById<Button>(R.id.btnTabUsers)
        val btnTabListings = findViewById<Button>(R.id.btnTabListings)
        val lvContent      = findViewById<ListView>(R.id.lvContent)
        val tvEmpty        = findViewById<TextView>(R.id.tvAdminEmpty)
        val tvLabel        = findViewById<TextView>(R.id.tvContentLabel)
        val progressAdmin  = findViewById<ProgressBar>(R.id.progressAdmin)
        val btnAdminLogout = findViewById<Button>(R.id.btnAdminLogout)

        val orange = Color.parseColor("#F97316")
        val white  = Color.WHITE
        val dark   = Color.parseColor("#1F2A44")

        fun setActiveTab(usersActive: Boolean) {
            btnTabUsers.backgroundTintList    = ColorStateList.valueOf(if (usersActive) orange else Color.TRANSPARENT)
            btnTabListings.backgroundTintList = ColorStateList.valueOf(if (!usersActive) orange else Color.TRANSPARENT)
            btnTabUsers.setTextColor(if (usersActive) white else dark)
            btnTabListings.setTextColor(if (!usersActive) white else dark)
        }

        fun loadStats() {
            Thread {
                val users    = db.getAllUsers().size
                val listings = db.getAllListingsForAdmin().size
                runOnUiThread {
                    tvUserCount.text    = users.toString()
                    tvListingCount.text = listings.toString()
                }
            }.start()
        }

        fun loadUsers() {
            setActiveTab(true)
            tvLabel.text             = "Users — tap to enable / disable"
            progressAdmin.visibility = View.VISIBLE
            lvContent.visibility     = View.GONE
            tvEmpty.visibility       = View.GONE

            Thread {
                val users = db.getAllUsers()
                runOnUiThread {
                    progressAdmin.visibility = View.GONE

                    if (users.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        tvEmpty.text       = "No users found"
                        return@runOnUiThread
                    }

                    val adapter = object : ArrayAdapter<User>(this, R.layout.item_admin_row, users) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = convertView ?: LayoutInflater.from(context)
                                .inflate(R.layout.item_admin_row, parent, false)
                            val user = users[position]

                            view.findViewById<TextView>(R.id.tvRowTitle).text    = user.name
                            view.findViewById<TextView>(R.id.tvRowSubtitle).text = "${user.email}  •  ${user.role}"

                            val badge = view.findViewById<TextView>(R.id.tvRowStatus)
                            if (user.disabled) {
                                badge.text = "DISABLED"
                                badge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EF4444"))
                            } else {
                                badge.text = "ACTIVE"
                                badge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10B981"))
                            }
                            return view
                        }
                    }

                    lvContent.adapter    = adapter
                    lvContent.visibility = View.VISIBLE

                    lvContent.setOnItemClickListener { _, _, position, _ ->
                        val user = users[position]
                        if (user.role == "ADMIN") {
                            Toast.makeText(this, "Cannot disable admin account", Toast.LENGTH_SHORT).show()
                            return@setOnItemClickListener
                        }
                        val newState = !user.disabled
                        AlertDialog.Builder(this)
                            .setTitle(if (newState) "Disable User" else "Enable User")
                            .setMessage("Name: ${user.name}\nEmail: ${user.email}\nRole: ${user.role}\n\n${if (newState) "Disable this account?" else "Enable this account?"}")
                            .setPositiveButton(if (newState) "Disable" else "Enable") { _, _ ->
                                Thread {
                                    db.setUserDisabled(user.id, newState)
                                    runOnUiThread {
                                        Toast.makeText(this, if (newState) "${user.name} disabled" else "${user.name} enabled", Toast.LENGTH_SHORT).show()
                                        loadUsers()
                                        loadStats()
                                    }
                                }.start()
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
            }.start()
        }

        fun loadListings() {
            setActiveTab(false)
            tvLabel.text             = "Listings — tap to remove"
            progressAdmin.visibility = View.VISIBLE
            lvContent.visibility     = View.GONE
            tvEmpty.visibility       = View.GONE

            Thread {
                val listings = db.getAllListingsForAdmin()
                runOnUiThread {
                    progressAdmin.visibility = View.GONE

                    if (listings.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                        tvEmpty.text       = "No listings available"
                        return@runOnUiThread
                    }

                    val adapter = object : ArrayAdapter<Listing>(this, R.layout.item_admin_row, listings) {
                        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = convertView ?: LayoutInflater.from(context)
                                .inflate(R.layout.item_admin_row, parent, false)
                            val listing = listings[position]

                            view.findViewById<TextView>(R.id.tvRowTitle).text    = listing.title
                            view.findViewById<TextView>(R.id.tvRowSubtitle).text =
                                "$${String.format("%.2f", listing.price)}  •  ${listing.condition}  •  ${listing.category}"

                            val badge = view.findViewById<TextView>(R.id.tvRowStatus)
                            when (listing.status) {
                                "ACTIVE"  -> {
                                    badge.text = "ACTIVE"
                                    badge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#10B981"))
                                }
                                "REMOVED" -> {
                                    badge.text = "REMOVED"
                                    badge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#EF4444"))
                                }
                                else -> {
                                    badge.text = listing.status
                                    badge.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#F97316"))
                                }
                            }
                            return view
                        }
                    }

                    lvContent.adapter    = adapter
                    lvContent.visibility = View.VISIBLE

                    lvContent.setOnItemClickListener { _, _, position, _ ->
                        val listing = listings[position]
                        if (listing.status == "REMOVED") {
                            Toast.makeText(this, "Already removed", Toast.LENGTH_SHORT).show()
                            return@setOnItemClickListener
                        }
                        AlertDialog.Builder(this)
                            .setTitle("Remove Listing")
                            .setMessage("Title: ${listing.title}\nPrice: $${String.format("%.2f", listing.price)}\nCondition: ${listing.condition}\n\nRemove this listing? Buyers will no longer see it.")
                            .setPositiveButton("Remove") { _, _ ->
                                Thread {
                                    db.markListingRemoved(listing.id)
                                    runOnUiThread {
                                        Toast.makeText(this, "Listing removed", Toast.LENGTH_SHORT).show()
                                        loadListings()
                                        loadStats()
                                    }
                                }.start()
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
            }.start()
        }

        btnTabUsers.setOnClickListener    { loadUsers() }
        btnTabListings.setOnClickListener { loadListings() }

        btnAdminLogout.setOnClickListener {
            Session.userId    = -1L
            Session.role      = "BUYER"
            Session.isBuyMode = true
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        loadStats()
        loadUsers()
    }
}