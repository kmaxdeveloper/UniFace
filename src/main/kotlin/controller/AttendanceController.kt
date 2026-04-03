package com.uniface.controller

import com.uniface.dto.AttendanceRecordDto
import com.uniface.dto.StartLessonRequest
import com.uniface.entity.ScanRequest
import com.uniface.repository.LessonRepository
import com.uniface.service.AttendanceService
import com.uniface.service.QrService // Yangi servis
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessagingTemplate // WebSocket uchun
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class AttendanceController(
    private val attendanceService: AttendanceService,
    private val qrService: QrService,
    private val messagingTemplate: SimpMessagingTemplate,
    private val lessonRepository: LessonRepository
) {

    @GetMapping("/teacher/refresh-qr/{lessonId}")
    fun refreshQr(@PathVariable lessonId: Long): ResponseEntity<Any> {
        // 1. Dars hali faolmi? (isActive == true)
        val lesson = lessonRepository.findById(lessonId).orElse(null)
        if (lesson == null || !lesson.isActive) {
            return ResponseEntity.status(403).body("Dars yakunlangan yoki topilmadi!")
        }

        val token = qrService.generateQrToken(lessonId)
        return ResponseEntity.ok(mapOf("qrToken" to token))
    }

    @PostMapping("/student/scan", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun scan(@RequestBody request: ScanRequest): ResponseEntity<Any> {
        return try {
            // 1. Servis orqali davomatni belgilash
            val result = attendanceService.markAttendance(
                request.studentId,
                request.qrToken
            )

            // 2. WebSocket orqali xabar yuborish
            val lessonId = qrService.getLessonIdFromToken(request.qrToken)
            messagingTemplate.convertAndSend("/topic/lesson/$lessonId", result)

            // Muvaffaqiyatli javob (String)
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            // Xatolik xabarini toza matn ko'rinishida qaytaramiz
            // Bu Android'dagi response.errorBody()?.string() ga tushadi
            ResponseEntity
                .status(400)
                .contentType(MediaType.TEXT_PLAIN)
                .body(e.message ?: "Serverda kutilmagan xatolik yuz berdi")
        }
    }

    @PostMapping("/teacher/start-lesson")
    fun startLesson(@RequestBody request: StartLessonRequest): ResponseEntity<Any> {
        return try {
            // Darsni boshlash va ID ni olish
            val lessonId = attendanceService.startNewLesson(request)

            println("INFO: Yangi dars boshlandi. ID: $lessonId, Ustoz: ${request.teacherUsername}")
            ResponseEntity.ok(lessonId)
        } catch (e: EntityNotFoundException) {
            // Masalan: Fan yoki Guruh topilmasa
            ResponseEntity.status(404).body(mapOf("error" to e.message))
        } catch (e: IllegalStateException) {
            // Masalan: O'qituvchida allaqachon faol dars bo'lsa
            ResponseEntity.status(409).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            // Kutilmagan boshqa xatolar
            println("ERROR: Dars boshlashda xatolik: ${e.message}")
            ResponseEntity.status(500).body(mapOf("error" to "Serverda ichki xatolik yuz berdi"))
        }
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