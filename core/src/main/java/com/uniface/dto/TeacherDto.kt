package com.uniface.dto

data class TeacherDto(
    val fullName: String,
    val username: String,
    val password: String,
    val department: String,
    val faculty: String,
    val points: Int = 0,
    val status: Boolean = true,
    // Mana shu joyi qolib ketgan edi:
    val subjectIds: List<Long> = emptyList(),
    val groupIds: List<Long> = emptyList()
)