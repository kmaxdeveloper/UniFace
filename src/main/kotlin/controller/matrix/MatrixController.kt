package com.uniface.controller.matrix

import com.uniface.matrix.service.MatrixService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/matrix")
class MatrixController(private val matrixService: MatrixService) {

    @PostMapping("/generate")
    fun generate(): ResponseEntity<Map<String, String>> {
        return try {
            // Service'dagi metod nomiga mosladik: startSolver()
            val count = matrixService.startSolver()

            ResponseEntity.ok(mapOf(
                "msg" to "Jadval muvaffaqiyatli tuzildi!",
                "count" to "$count ta dars joylashtirildi",
                "status" to "SUCCESS"
            ))
        } catch (e: Exception) {
            // Xatolik bo'lsa, aniq sababini ko'rsatamiz
            ResponseEntity.badRequest().body(mapOf(
                "err" to (e.message ?: "Algoritm ishga tushishida xatolik"),
                "status" to "FAILED"
            ))
        }
    }
}