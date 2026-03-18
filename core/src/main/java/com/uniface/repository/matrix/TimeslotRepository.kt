package com.uniface.repository.matrix

import com.uniface.entity.matrix.TimeSlot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TimeslotRepository : JpaRepository<TimeSlot, Long>