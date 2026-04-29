package com.uniface.dto

data class StudentAttendanceStatsDto(
    val subjects: List<SubjectAttendanceDetailDto>,
    val totalMissedHours: Int,
    val overallRiskStatus: String // SAFE, WARNING, DANGER
)

data class SubjectAttendanceDetailDto(
    val subjectId: Long,
    val subjectName: String,
    val totalLessons: Int,
    val presentCount: Int,
    val missedCount: Int,
    val missedPercentage: Double,
    val missedTopics: List<MissedTopicDto>,
    val riskStatus: String
)

data class MissedTopicDto(
    val topicTitle: String,
    val date: String
)
