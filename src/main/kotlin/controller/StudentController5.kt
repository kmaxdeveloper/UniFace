package com.uniface.controller

import com.uniface.service.FaceService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/uniface")
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
        @RequestParam("teacherName") teacherName: String
    ): ResponseEntity<Any> {
        return try {
            val result = faceService.processBulkAttendance(
                imageBytes = file.bytes,
                subjectId = subjectId,
                groupId = groupId,
                teacherName = teacherName
            )
            ResponseEntity.ok(result)
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
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