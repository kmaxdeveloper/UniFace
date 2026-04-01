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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    var teacher: Teacher? = null

    // MANA SHU YERDA LESSON BO'LISHI SHART!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    var lesson: Lesson? = null

    var timestamp: LocalDateTime = LocalDateTime.now()
    var status: String = "PRESENT"

    constructor(
        student: Student?,
        subject: Subject?,
        group: StudentGroup?,
        teacher: Teacher?,
        lesson: Lesson? // Lesson qabul qilamiz
    ) : this() {
        this.student = student
        this.subject = subject
        this.group = group
        this.teacher = teacher
        this.lesson = lesson // Lesson-ni lesson maydoniga saqlaymiz
        this.timestamp = LocalDateTime.now()
    }
}