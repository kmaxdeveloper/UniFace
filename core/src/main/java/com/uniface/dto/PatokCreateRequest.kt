package com.uniface.dto

// DTO - Data Transfer Object
data class PatokCreateRequest(
    val name: String,
    val groupIds: List<Long>
)