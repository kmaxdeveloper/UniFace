package com.uniface.entity.matrix

import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalTime

@Entity
@Table(name = "time_slots")
data class TimeSlot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Enumerated(EnumType.STRING) // String emas, Enum ishlatish xavfsizroq ✅
    val dayOfWeek: DayOfWeek,

    @Column(nullable = false)
    val startTime: LocalTime,

    @Column(nullable = false)
    val endTime: LocalTime,

    @Column(name = "pair_number") // 1-para, 2-para...
    val pairNumber: Int = 1
)