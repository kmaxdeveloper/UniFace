package com.uniface.dto

import com.uniface.entity.SubjectAllocation
import com.uniface.entity.matrix.Room
import com.uniface.entity.matrix.TimeSlot

data class LessonResult(
    val allocation: SubjectAllocation,
    val timeSlot: TimeSlot,
    val room: Room
)