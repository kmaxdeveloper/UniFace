package com.uniface.repository.matrix

import com.uniface.entity.matrix.TimeSlot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.DayOfWeek

@Repository
interface TimeslotRepository : JpaRepository<TimeSlot, Long> {

    // Kun va soat raqami bo'yicha TimeSlot'ni topish
    fun findByDayOfWeekAndPairNumber(dayOfWeek: DayOfWeek, pairNumber: Int): TimeSlot?
}