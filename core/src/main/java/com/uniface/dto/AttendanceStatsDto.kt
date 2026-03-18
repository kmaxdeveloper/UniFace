package com.uniface.dto

data class AttendanceStatsDto(
    val totalStudents: Int,
    val presentCount: Int,
    val attendancePercent: Double,
    val records: List<com.uniface.dto.AttendanceRecordDto>
)