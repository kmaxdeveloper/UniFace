package com.uniface.controller

import com.uniface.service.FaceService
import com.uniface.service.TeacherService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/teacher")
class TeacherController(
    private val faceService: FaceService,
    private val teacherService: TeacherService
) {

    // Auditoriyani ommaviy rasmga olish (100 kishigacha)
    @PostMapping("/attendance/bulk")
    fun takeBulkAttendance(
        @RequestParam("image") file: MultipartFile,
        @RequestParam("subjectId") subjectId: Long,
        @RequestParam("groupId") groupId: Long,
        @RequestParam("teacherId") teacherId: Long // String o'rniga ID
    ) = ResponseEntity.ok(faceService.processBulkAttendance(file.bytes, subjectId, groupId, teacherId))

    @PostMapping("/attendance/single")
    fun takeSingleAttendance(
        @RequestParam("image") file: MultipartFile,
        @RequestParam("subjectId") subjectId: Long,
        @RequestParam("groupId") groupId: Long,
        @RequestParam("teacherId") teacherId: Long // String o'rniga ID
    ) = ResponseEntity.ok(faceService.identifyStudent(file.bytes, subjectId, groupId, teacherId))

    @GetMapping("/{teacherId}/lessons")
    fun getLessons(@PathVariable teacherId: Long): ResponseEntity<List<Map<String, Any>>> {
        val allocations = teacherService.getMyAllocations(teacherId)

        // Android-ga qulay formatda javob qaytaramiz
        val response = allocations.map {
            mapOf(
                "allocationId" to it.id!!,
                "subjectName" to it.subject!!.name,
                "groupName" to (it.group?.name ?: it.patokName ?: "Noma'lum"),
                "isPatok" to it.isPatok
            )
        }

        return ResponseEntity.ok(response)
    }
}