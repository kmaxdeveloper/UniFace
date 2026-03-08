package com.uniface.dto

data class AttendanceRecordDto(
    val studentId: String,
    val studentName: String,
    val subjectName: String,
    val groupName: String,
    val timestamp: String,
    val status: String
)