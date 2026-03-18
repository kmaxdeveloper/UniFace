package com.uniface.matrix.domain

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty
import ai.timefold.solver.core.api.domain.solution.PlanningScore
import ai.timefold.solver.core.api.domain.solution.PlanningSolution
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore

// Matrix modulingdagi o'z entity'laringni import qilamiz ✅
import com.uniface.entity.matrix.Lesson
import com.uniface.entity.matrix.TimeSlot
import com.uniface.entity.matrix.Room

@PlanningSolution
class Timetable(
    // Darslar qaysi vaqt oralig'ida bo'lishi mumkinligi (Manba)
    @ValueRangeProvider(id = "timeslotRange")
    @ProblemFactCollectionProperty
    var timeslots: List<TimeSlot> = emptyList(),

    // Darslar qaysi xonalarda bo'lishi mumkinligi (Manba)
    @ValueRangeProvider(id = "roomRange")
    @ProblemFactCollectionProperty
    var rooms: List<Room> = emptyList(),

    // Rejalashtirilishi kerak bo'lga darslar ro'yxati (Planning Entities)
    @PlanningEntityCollectionProperty
    var lessons: List<Lesson> = emptyList(),

    // Algoritm natijasini saqlash uchun ball (Score)
    @PlanningScore
    var score: HardSoftScore? = null
) {
    // Timefold solver ishlashi uchun bo'sh konstruktor shart
    constructor() : this(emptyList(), emptyList(), emptyList())
}