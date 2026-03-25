package com.uniface.controller

import com.uniface.entity.ScanRequest
import com.uniface.service.AttendanceService
import com.uniface.service.QrService // Yangi servis
import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessagingTemplate // WebSocket uchun
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class AttendanceController(
    private val attendanceService: AttendanceService,
    private val qrService: QrService,
    private val messagingTemplate: SimpMessagingTemplate
) {

    @GetMapping("teacher/generate-qr/{lessonId}")
    fun getQr(@PathVariable lessonId: Long) =
        ResponseEntity.ok(mapOf("qrToken" to qrService.generateQrToken(lessonId)))

    // 2. TALABA UCHUN: Skaner qilganda yuboriladigan API
    @PostMapping("student/scan")
    fun scan(@RequestBody request: ScanRequest): ResponseEntity<Any> {
        // Tokendan lessonId ni olamiz (vaqtini ham tekshiradi)
        val lessonId = qrService.getLessonIdFromToken(request.qrToken)
            ?: return ResponseEntity.status(401).body("QR kod muddati o'tgan yoki xato!")

        // Davomatni yozish
        val result = attendanceService.markAttendance(request.studentId, lessonId)

        // O'qituvchi ekraniga "Pistonchi keldi" deb xabar borishi uchun:
        messagingTemplate.convertAndSend("/topic/lesson/$lessonId", result)

        return ResponseEntity.ok(result)
    }

    // --- ESKI ADMIN/TEACHER STATISTIKALARI ---
    @GetMapping("/admin/attendance/group/{groupId}")
    fun getGroupStats(@PathVariable groupId: Long) =
        ResponseEntity.ok(attendanceService.getGroupStats(groupId))

    @GetMapping("/admin/attendance/subject/{subjectId}")
    fun getSubjectStats(@PathVariable subjectId: Long) =
        ResponseEntity.ok(attendanceService.getSubjectStats(subjectId))

    @GetMapping("/admin/attendance/today")
    fun getTodayStats() =
        ResponseEntity.ok(attendanceService.getTodayStats())

    @GetMapping("/teacher/attendance/group/{groupId}")
    fun getTeacherGroupStats(@PathVariable groupId: Long) =
        ResponseEntity.ok(attendanceService.getGroupStats(groupId))

    @GetMapping("/student/attendance/{studentId}")
    fun getStudentAttendance(@PathVariable studentId: String) =
        ResponseEntity.ok(attendanceService.getStudentAttendance(studentId))
}