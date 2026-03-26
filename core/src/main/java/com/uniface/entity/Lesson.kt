package com.uniface.entity

import com.uniface.entity.matrix.Room
import com.uniface.entity.matrix.TimeSlot
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "lessons")
class Lesson(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    var subject: Subject? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    var teacher: Teacher? = null, // Endi teacherName o'rniga haqiqiy Teacher entity ishlatamiz

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: StudentGroup? = null,

    // Algoritm (Matrix) uchun kerakli maydonlar
    @ManyToOne(fetch = FetchType.LAZY)
    var timeslot: TimeSlot? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    var room: Room? = null,

    // Davomat va QR-kod uchun maydonlar
    var startTime: LocalDateTime = LocalDateTime.now(),
    var endTime: LocalDateTime? = null,

    var isActive: Boolean = true // QR-kod faolligini tekshirish uchun
)