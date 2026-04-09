package com.uniface.controller.admin

import com.uniface.service.SubjectAllocationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/allocations")
class AllocationController(private val service: SubjectAllocationService) {

    @PostMapping("/assign")
    fun assign(
        @RequestParam subjectId: Long,
        @RequestParam teacherId: Long,
        @RequestParam(required = false) groupId: Long?,
        @RequestParam(required = false) patokName: String?,
        @RequestParam isPatok: Boolean
    ): ResponseEntity<String> {
        return try {
            val msg = service.createAllocation(subjectId, teacherId, groupId, patokName, isPatok)
            ResponseEntity.ok(msg)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(e.message)
        }
    }
}