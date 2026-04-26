package com.uniface.repository

import com.uniface.entity.SystemLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SystemLogRepository : JpaRepository<SystemLog, Long> {
    fun findAllByOrderByTimestampDesc(): List<SystemLog>
}
