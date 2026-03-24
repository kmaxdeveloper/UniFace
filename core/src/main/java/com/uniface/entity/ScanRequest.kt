package com.uniface.entity

data class ScanRequest(
    val qrToken: String,
    val lessonId: Long,
    val studentId: String,
    val lat: Double,
    val lng: Double
)