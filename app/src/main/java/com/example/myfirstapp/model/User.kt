package com.example.myfirstapp.model

data class User(
    val id: Long = -1,
    val name: String,
    val email: String,
    val password: String,
    val role: String,
    val disabled: Boolean = false,
    val avatarColor: String = "#F97316"  // default orange
)