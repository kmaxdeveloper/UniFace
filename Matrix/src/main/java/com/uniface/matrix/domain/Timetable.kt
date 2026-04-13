package com.uniface.matrix.domain

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty
import ai.timefold.solver.core.api.domain.solution.PlanningScore
import ai.timefold.solver.core.api.domain.solution.PlanningSolution
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import com.uniface.entity.Lesson
import com.uniface.entity.StudentGroup
import com.uniface.entity.Teacher

// Matrix modulingdagi o'z entity'laringni import qilamiz ✅
import com.uniface.entity.matrix.TimeSlot
import com.uniface.entity.matrix.Room

@PlanningSolution
class Timetable(
    // AI tanlashi mumkin bo'lgan diapazonlar
    @ValueRangeProvider(id = "timeslotRange")
    @ProblemFactCollectionProperty
    var timeslots: List<TimeSlot> = mutableListOf(),

    @ValueRangeProvider(id = "roomRange")
    @ProblemFactCollectionProperty
    var rooms: List<Room> = mutableListOf(),

    // Problem Facts (AIning darslarni bir-biriga solishtirish uchun "lug'ati")
    @ProblemFactCollectionProperty
    var teachers: List<Teacher> = mutableListOf(),

    @ProblemFactCollectionProperty
    var studentGroups: List<StudentGroup> = mutableListOf(),

    // Planning Entities (Hali joylashtirilmagan darslar)
    @PlanningEntityCollectionProperty
    var lessons: MutableList<Lesson> = mutableListOf(),

    @PlanningScore
    var score: HardSoftScore? = null
)