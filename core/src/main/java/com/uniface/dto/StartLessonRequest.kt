package com.uniface.dto

data class StartLessonRequest(
    val subjectId: Long,
    val groupId: Long,
    val teacherUsername: String? = null
)