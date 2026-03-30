package com.uniface.data

data class ErrorResponse(
    val message: String?,
    val status: Int,
    val timestamp: Long = System.currentTimeMillis()
)