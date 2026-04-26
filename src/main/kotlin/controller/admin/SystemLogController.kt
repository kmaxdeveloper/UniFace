package com.uniface.controller.admin

import com.uniface.entity.SystemLog
import com.uniface.repository.SystemLogRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/logs")
class SystemLogController(private val systemLogRepository: SystemLogRepository) {

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getAllLogs(): List<SystemLog> {
        return systemLogRepository.findAllByOrderByTimestampDesc()
    }
}
