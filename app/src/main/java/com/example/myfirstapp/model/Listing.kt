package com.example.myfirstapp.model

data class Listing(
    val id: Long = -1,
    val sellerId: Long,
    val title: String,
    val description: String,
    val brand: String,
    val screenSize: String,
    val resolution: String,
    val condition: String,
    val category: String,
    val price: Double,
    val photoUri: String? = null,
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis()
)