package com.example.myfirstapp.userinterface

object Session {
    var userId: Long = -1L
    var role: String = "BUYER"
    var isBuyMode: Boolean = true

    fun isAdmin(): Boolean = role == "ADMIN"
}