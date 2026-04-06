package com.uniface.dto.teacher

data class TeacherUpdateDto(
    val fullName: String,
    val username: String,
    val password: String? = null, // Agar parol yangilanmoqchi bo'lsa
    val department: String,
    val faculty: String,
    val status: Boolean
)