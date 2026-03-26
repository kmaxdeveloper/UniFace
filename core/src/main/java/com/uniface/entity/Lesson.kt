package com.uniface.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "lessons")
class Lesson(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    var subject: Subject,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    var teacher: Teacher,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: StudentGroup,

    val startTime: LocalDateTime = LocalDateTime.now(),
    var endTime: LocalDateTime? = null,

    var isActive: Boolean = true // QR kod faqat isActive bo'lsa ishlaydi
)