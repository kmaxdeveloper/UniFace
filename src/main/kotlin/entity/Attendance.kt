package com.uniface.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "attendance")
class Attendance(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    val student: Student? = null, // null bo'lishi mumkin deb belgilaymiz, JPA uchun osonroq

    val timestamp: LocalDateTime = LocalDateTime.now(),

    val status: String = "PRESENT"
)