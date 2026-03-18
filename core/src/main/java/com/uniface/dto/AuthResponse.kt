package com.uniface.dto

// Bu DTO klassing
data class AuthResponse(
    val token: String,
    val role: String,      // Mana bu qatorni qo'sh!
    val username: String? = null
)