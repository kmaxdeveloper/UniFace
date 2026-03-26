package com.uniface.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "attendance")
class Attendance() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    var student: Student? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    var subject: Subject? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    var group: StudentGroup? = null

    // String o'rniga Teacher entity-sini bog'laymiz (identifikatsiya uchun)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    var teacher: Teacher? = null

    var timestamp: LocalDateTime = LocalDateTime.now()

    // Statusni Enum qilishni maslahat beraman (PRESENT, ABSENT, LATE)
    var status: String = "PRESENT"

    constructor(
        student: Student,
        subject: Subject,
        group: StudentGroup,
        teacher: Teacher
    ) : this() {
        this.student = student
        this.subject = subject
        this.group = group
        this.teacher = teacher
    }
}