package com.uniface.entity

import ai.timefold.solver.core.api.domain.entity.PlanningEntity
import ai.timefold.solver.core.api.domain.lookup.PlanningId
import ai.timefold.solver.core.api.domain.variable.PlanningVariable
import com.uniface.data.LessonType
import com.uniface.entity.matrix.Room
import com.uniface.entity.matrix.TimeSlot
import jakarta.persistence.*
import java.time.LocalDateTime

@PlanningEntity
@Entity
@Table(name = "lessons")
class Lesson(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @PlanningId
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    var subject: Subject? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    var teacher: Teacher? = null,

    // 1. O'ZGARISH: Bir nechta guruhni qo'llab-quvvatlash (Potok uchun)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "lesson_groups",
        joinColumns = [JoinColumn(name = "lesson_id")],
        inverseJoinColumns = [JoinColumn(name = "group_id")]
    )
    var groups: MutableSet<StudentGroup> = mutableSetOf(),

    // 2. O'ZGARISH: Dars turi (Ma'ruza yoki Amaliyot)
    @Enumerated(EnumType.STRING)
    var type: LessonType = LessonType.PRACTICE,

    @PlanningVariable(valueRangeProviderRefs = ["timeslotRange"]) // AI vaqtni tanlaydi
    @ManyToOne(fetch = FetchType.LAZY)
    var timeslot: TimeSlot? = null,

    @PlanningVariable(valueRangeProviderRefs = ["roomRange"]) // AI xonani tanlaydi
    @ManyToOne(fetch = FetchType.LAZY)
    var room: Room? = null,

    var startTime: LocalDateTime = LocalDateTime.now(),
    var endTime: LocalDateTime? = null,

    var isActive: Boolean = true
)