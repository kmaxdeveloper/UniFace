package com.uniface.entity.matrix

import com.uniface.entity.StudentGroup
import com.uniface.entity.Subject
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity(name = "MatrixLesson") // Hibernate uchun nomini o'zgartirdik
@Table(name = "matrix_lessons")
data class Lesson(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne val subject: Subject,
    val teacherName: String, // Keyinchalik Teacher entityga o'tkazamiz
    @ManyToOne val studentGroup: StudentGroup,

    // Algoritm tomonidan to'ldiriladigan maydonlar:
    @ManyToOne var timeslot: TimeSlot? = null,
    @ManyToOne var room: Room? = null
)