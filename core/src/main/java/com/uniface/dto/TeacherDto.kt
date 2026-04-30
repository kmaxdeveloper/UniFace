package com.uniface.dto

data class TeacherDto(
    val fullName: String,
    val username: String,
    val password: String,
    val department: String,
    val faculty: String,
    val points: Double = 0.0,
    val status: Boolean = true,
    val subjectIds: List<Long> = emptyList() // Faqat fanlar qoldi ✅
)