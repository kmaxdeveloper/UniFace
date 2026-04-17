package com.uniface.matrix.solver

import ai.timefold.solver.core.api.domain.solution.*
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import com.uniface.entity.Lesson
import com.uniface.entity.Teacher
import com.uniface.entity.matrix.Room
import com.uniface.entity.matrix.TimeSlot

/**
 * Timefold uchun asosiy "savat" klassi.
 *
 * Timefold shu klassni oladi:
 *  - timeSlots va rooms → bulardan tanlaydi
 *  - lessons → bularning timeslot va room fieldini belgilaydi
 *  - score → qanchalik yaxshi ekanini shu yerga yozadi
 */
@PlanningSolution
class TimetableSolution(

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "timeslotRange")
    var timeSlots: List<TimeSlot> = emptyList(),

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "roomRange")
    var rooms: List<Room> = emptyList(),

    @PlanningEntityCollectionProperty
    var lessons: List<Lesson> = emptyList(),

    // 🔥 YANGI: AI o'qituvchini tanlashi uchun range qo'shamiz
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "teacherRange")
    var teachers: List<Teacher> = emptyList(),

    @PlanningScore
    var score: HardSoftScore = HardSoftScore.ZERO
)