package com.uniface.matrix.controller

import com.uniface.matrix.service.MatrixService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/matrix")
class MatrixAdmin2Controller(
    private val matrixService: MatrixService
) {

    @PostMapping("/solve")
    fun solveTimetable(): ResponseEntity<String> {
        return try {
            // 1. Hisoblashni boshlaymiz
            val solution = matrixService.generateTimetable()

            // 2. Konsolga natijani chiqaramiz (tekshirish uchun)
            matrixService.printTimetable(solution)

            ResponseEntity.ok("Jadval muvaffaqiyatli generatsiya qilindi! Score: ${solution.score}")
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body("Xatolik yuz berdi: ${e.message}")
        }
    }
}