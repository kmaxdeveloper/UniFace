package com.uniface.matrix.domain

import com.uniface.entity.*
import com.uniface.entity.matrix.*
import com.uniface.data.LessonType

data class ScheduledLesson(
    val subject: Subject?,
    val teacher: Teacher?,
    val room: Room,
    val timeslot: TimeSlot,
    val groups: MutableSet<StudentGroup>,
    val type: LessonType
)