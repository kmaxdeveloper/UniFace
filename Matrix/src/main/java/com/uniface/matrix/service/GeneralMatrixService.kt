package com.uniface.matrix.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class GeneralMatrixService(
    private val allocationService: SubjectAllocationService2,
    private val matrixService: MatrixService5
) {
    @Transactional
    fun startTheMatrix(): Map<String, Any> {
        // 1. Reja bo'yicha o'qituvchilarni avtomatik biriktiramiz
        allocationService.autoAllocateTeachers()

        // 2. Taqsimot (Allocation) asosida AI jadvalni generatsiya qiladi
        val solution = matrixService.runFullAutomation()

        return mapOf(
            "status" to "SUCCESS",
            "message" to "Tizim to'liq muvaffaqiyatli generatsiya qilindi!",
            "score" to solution.score.toString()
        )
    }
}