package com.uniface.matrix.controller

import com.uniface.matrix.service.MatrixService
import com.uniface.matrix.service.MatrixService2
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity

@RestController
@RequestMapping("/api/admin/matrix")
class MatrixAdmin2Controller(
    private val matrixService: MatrixService
) {

//    @PostMapping("/generate")
//    fun solveTimetable(): ResponseEntity<String> {
//        return try {
//            // 1. Hisoblashni boshlaymiz
//            val solution = matrixService.startSolver()
//
//            // 2. Konsolga natijani chiqaramiz (tekshirish uchun)
//            //matrixService.printTimetable(solution)
//
//            //ResponseEntity.ok("Jadval muvaffaqiyatli generatsiya qilindi! Score: ${solution.score}")
//        } catch (e: Exception) {
//            ResponseEntity.internalServerError().body("Xatolik yuz berdi: ${e.message}")
//        }
//    }
}