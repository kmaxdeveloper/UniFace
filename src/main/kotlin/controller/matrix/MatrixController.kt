package com.uniface.controller.matrix

import com.uniface.entity.Lesson
import com.uniface.matrix.service.MatrixService2 // Service nomiga e'tibor ber
import com.uniface.repository.LessonRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/matrix")
class MatrixController(
    private val matrixService: MatrixService2,
    private val lessonRepository: LessonRepository
) {

    @PostMapping("/generate")
    fun generate(): ResponseEntity<Map<String, Any>> {
        return try {
            // 1. Servisdagi generateAndSaveTimetable metodini chaqiramiz
            val solution = matrixService.generateAndSaveTimetable()

            // 2. Score'ni tekshiramiz (agar juda yomon bo'lsa, xabar berish uchun)
            val scoreStatus = solution.score?.toString() ?: "Noma'lum"

            ResponseEntity.ok(mapOf(
                "msg" to "Matrix AI jadvalni muvaffaqiyatli generatsiya qildi!",
                "count" to solution.lessons.size, // Jami joylashtirilgan darslar soni
                "score" to scoreStatus,           // Hard/Soft score natijasi
                "status" to "SUCCESS"
            ))
        } catch (e: Exception) {
            // Logga xatolikni yozamiz (optional)
            e.printStackTrace()

            ResponseEntity.internalServerError().body(mapOf(
                "err" to (e.message ?: "Algoritm ishga tushishida kutilmagan xatolik"),
                "status" to "FAILED",
                "hint" to "Bazada darslar (SubjectAllocation), xonalar yoki vaqtlar borligini tekshiring"
            ))
        }
    }

    @GetMapping("/view")
    fun viewSchedule(
        @RequestParam(required = false) groupName: String?,
        @RequestParam(required = false) teacherId: Long?
    ): ResponseEntity<List<Lesson>> {
        val lessons = when {
            groupName != null -> lessonRepository.findByGroups_Name(groupName)
            teacherId != null -> lessonRepository.findByTeacher_Id(teacherId)
            else -> lessonRepository.findAll()
        }
        return ResponseEntity.ok(lessons)
    }
}