package com.uniface.dto

data class FaceResponse(
    val success: Boolean,
    val message: String,
    val studentId: String? = null,
    val similarity: Float? = null
)