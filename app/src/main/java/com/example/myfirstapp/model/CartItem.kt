package com.example.myfirstapp.model

data class CartItem(
    val id: Long = -1,
    val userId: Long,
    val listingId: Long,
    val quantity: Int
)