package com.uniface.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "attendance")
class Attendance(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "student_id")
    val student: Student,

    val timestamp: LocalDateTime = LocalDateTime.now(),

    val status: String = "PRESENT"
)