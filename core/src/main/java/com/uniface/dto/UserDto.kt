package com.uniface.dto

data class UserDto(
    val fullName: String,
    val username: String,
    val password: String? = null // Update qilganda password bo'sh bo'lishi mumkin
)