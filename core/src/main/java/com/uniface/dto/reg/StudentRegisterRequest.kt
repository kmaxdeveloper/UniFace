package com.uniface.dto.reg

import org.springframework.web.multipart.MultipartFile

data class StudentRegisterRequest(
    val fullName: String,
    val studentId: String,
    val groupId: Long,
    val username: String,
    val image: MultipartFile // Talabaning yuz rasmi
)