package com.example.myfirstapp.model

data class User(
    val id: Long = -1,
    val name: String,
    val email: String,
    val password: String,
    val role: String,           // BUYER, SELLER, ADMIN
    val disabled: Boolean = false
)