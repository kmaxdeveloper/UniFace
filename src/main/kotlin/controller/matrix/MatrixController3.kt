package com.uniface.controller.matrix

import com.uniface.entity.Lesson
import com.uniface.matrix.service.MatrixService5
import com.uniface.matrix.service.SubjectAllocationService2
import com.uniface.repository.LessonRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/matrix")
class MatrixController3(
    private val matrixService: MatrixService5,
    private val lessonRepository: LessonRepository,
    private val subjectRepository: SubjectAllocationService2
) {

    // 2. ASOSIY GENERATSIYA (Zanjir: Allocation -> Solve -> Save)
    @PostMapping("/generate")
    fun generate(): ResponseEntity<Map<String, Any>> {
        return try {
            // Birinchi o'qituvchilarni avtomatik taqsimlaymiz
            subjectRepository.autoAllocateTeachers()

            // Keyin AI jadvalni generatsiya qiladi
            val solution = matrixService.runFullAutomation()

            val score = solution.score
            ResponseEntity.ok(mapOf(
                "msg" to "Matrix AI jadvalni muvaffaqiyatli generatsiya qildi!",
                "count" to solution.lessons.size,
                "score" to score.toString(),
                "is_feasible" to (score?.isFeasible ?: false),
                "status" to "SUCCESS"
            ))
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.internalServerError().body(mapOf(
                "err" to (e.message ?: "Algoritmda kutilmagan xatolik"),
                "status" to "FAILED"
            ))
        }
    }

    // 3. JADVALNI KO'RISH
    @GetMapping("/view")
    fun viewSchedule(
        @RequestParam(required = false) groupName: String?,
        @RequestParam(required = false) teacherId: Long?
    ): ResponseEntity<List<Lesson>> {
        val lessons = when {
            !groupName.isNullOrBlank() -> lessonRepository.findByGroups_Name(groupName)
            teacherId != null -> lessonRepository.findByTeacher_Id(teacherId)
            else -> lessonRepository.findAll()
        }
        return ResponseEntity.ok(lessons)
    }

    // 4. TOZALASH
    @DeleteMapping("/reset")
    fun resetTimetable(): ResponseEntity<String> {
        lessonRepository.deleteAll()
        return ResponseEntity.ok("Dars jadvali tozalandi.")
    }
}