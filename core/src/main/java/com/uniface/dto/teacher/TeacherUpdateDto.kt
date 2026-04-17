package com.uniface.dto.teacher

data class TeacherUpdateDto(
    val fullName: String,
    val username: String,
    val password: String? = null,
    val department: String,
    val faculty: String,
    val status: Boolean,
    val subjectIds: List<Long> = emptyList() // Yangilashda fanlarni ham o'zgartirish imkoniyati
)