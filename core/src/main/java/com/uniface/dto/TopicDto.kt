package com.uniface.dto

data class TopicDto(
    val id: Long? = null,
    val title: String,
    val description: String? = null,
    val subjectId: Long
)
