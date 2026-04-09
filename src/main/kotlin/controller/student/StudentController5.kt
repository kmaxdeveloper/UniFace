package com.uniface.controller.student

import com.uniface.service.FaceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/uniface")
class StudentController5(private val faceService: FaceService) {

    @GetMapping("/setup")
    fun setup(): Any {
        return faceService.createCollection()
    }

    // 1. USTOZ UCHUN: Auditoriyani rasmga olib davomat qilish
    @PostMapping("/attendance/bulk")
    fun takeAttendance(
        @RequestParam("image") file: MultipartFile,
        @RequestParam("subjectId") subjectId: Long,
        @RequestParam("groupId") groupId: Long,
        @RequestParam("teacherId") teacherId: Long // String o'rniga Long ID
    ): ResponseEntity<Any> {
        return try {
            // Fayl borligini tekshiramiz
            if (file.isEmpty) {
                return ResponseEntity.badRequest().body(mapOf("error" to "Rasm yuklanmagan!"))
            }

            val result = faceService.processBulkAttendance(
                imageBytes = file.bytes,
                subjectId = subjectId,
                groupId = groupId,
                teacherId = teacherId // Service'ga ID ketyapti
            )
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            // Xatolikni chiroyli ko'rsatamiz
            ResponseEntity.status(500).body(mapOf("error" to (e.message ?: "Noma'lum xatolik")))
        }
    }

    // 2. ADMIN UCHUN: Yangi talabani ro'yxatdan o'tkazish (Eski metod)
    @PostMapping("/register")
    fun register(
        @RequestParam("id") id: String,
        @RequestParam("fullName") fullName: String,
        @RequestParam("groupId") groupId: Long, // Endi String emas, Long ID yuboramiz
        @RequestParam("image") file: MultipartFile
    ): ResponseEntity<Any> {
        val result = faceService.registerFace(id, fullName, groupId, file.bytes)
        return ResponseEntity.ok(result)
    }
}