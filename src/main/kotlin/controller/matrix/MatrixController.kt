package com.uniface.controller.matrix

import com.uniface.matrix.service.MatrixService2 // Service nomiga e'tibor ber
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/matrix")
class MatrixController(private val matrixService: MatrixService2) {

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
}