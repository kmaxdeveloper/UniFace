package com.uniface.dto

class StudentDto(
    fullName: String, username: String, password: String,
    val groupId: Long,
    val faceId: String? = null
) : BaseUserDto(fullName, username, password)