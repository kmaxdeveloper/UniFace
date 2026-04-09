package com.uniface.controller.student

import com.uniface.repository.AttendanceRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/student")
class StudentController(private val attendanceRepo: AttendanceRepository) {

    // Talaba o'zining hamma davomatlarini ko'rishi uchun
    @GetMapping("/my-attendance/{studentId}")
    fun getMyAttendance(@PathVariable studentId: String): ResponseEntity<Any> {
        val list = attendanceRepo.findByStudentStudentId(studentId)
        return ResponseEntity.ok(list)
    }
}