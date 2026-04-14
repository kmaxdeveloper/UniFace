package com.uniface.repository.matrix

import com.uniface.entity.matrix.TimeSlot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.DayOfWeek

@Repository
interface TimeslotRepository : JpaRepository<TimeSlot, Long> {

    // Kun va soat raqami bo'yicha TimeSlot'ni topish
    fun findByDayOfWeekAndPairNumber(dayOfWeek: DayOfWeek, pairNumber: Int): TimeSlot?

    @Query("SELECT t FROM TimeSlot t ORDER BY t.dayOfWeek, t.pairNumber")
    fun findAllOrdered(): List<TimeSlot>
}