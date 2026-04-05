package com.example.myfirstapp.model

data class Listing(
    val id: Long = -1,
    val sellerId: Long,
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val brand: String = "",
    val size: String = "",
    val resolution: String = "",
    val refreshRate: String = "",
    val courseTag: String = "",
    val condition: String = "",
    val expirationDate: String,
    val photoUri: String?,
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis()
)