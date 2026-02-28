package com.example.myfirstapp.model

data class Listing(
    val id: Long = -1,
    val sellerId: Long,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val condition: String,
    val photoUri: String?,        // keep simple for A6
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis()
)
