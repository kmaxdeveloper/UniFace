package com.uniface.dto.matrix

data class MatrixLessonDto(
    val lessonId    : Long,
    val subject     : String,
    val teacher     : String,
    val groups      : List<String>,
    val type        : String,   // LECTURE | LAB | PRACTICE
    val day         : String,   // MONDAY | TUESDAY ...
    val pairNumber  : Int,      // 1..6
    val startTime   : String,   // "08:30"
    val endTime     : String,   // "09:50"
    val room        : String,   // "212"
    val building    : String,   // "A-bino"
    val isLab       : Boolean
)