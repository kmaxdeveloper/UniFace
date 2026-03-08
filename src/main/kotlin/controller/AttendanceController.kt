package com.uniface.controller

import com.uniface.service.AttendanceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class AttendanceController(private val attendanceService: AttendanceService) {

    // Admin — guruh bo'yicha
    @GetMapping("/admin/attendance/group/{groupId}")
    fun getGroupStats(@PathVariable groupId: Long) =
        ResponseEntity.ok(attendanceService.getGroupStats(groupId))

    // Admin — fan bo'yicha
    @GetMapping("/admin/attendance/subject/{subjectId}")
    fun getSubjectStats(@PathVariable subjectId: Long) =
        ResponseEntity.ok(attendanceService.getSubjectStats(subjectId))

    // Admin — bugungi
    @GetMapping("/admin/attendance/today")
    fun getTodayStats() =
        ResponseEntity.ok(attendanceService.getTodayStats())

    // Teacher — o'z guruhining davomati
    @GetMapping("/teacher/attendance/group/{groupId}")
    fun getTeacherGroupStats(@PathVariable groupId: Long) =
        ResponseEntity.ok(attendanceService.getGroupStats(groupId))

    // Talaba — o'z davomati
    @GetMapping("/student/attendance/{studentId}")
    fun getStudentAttendance(@PathVariable studentId: String) =
        ResponseEntity.ok(attendanceService.getStudentAttendance(studentId))
}