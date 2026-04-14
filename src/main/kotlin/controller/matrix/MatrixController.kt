package com.uniface.controller.matrix

import com.uniface.data.ApiResponse
import com.uniface.dto.matrix.JobStatus
import com.uniface.dto.matrix.MatrixLessonDto
import com.uniface.entity.Lesson
import com.uniface.matrix.service.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/matrix")
class MatrixController(
    private val matrixService: MatrixService
) {

    // ──────────────────────────────────────────────────────
    // SOLVE BOSHLASH
    // POST /api/matrix/solve?semester=1
    // ──────────────────────────────────────────────────────
    @PostMapping("/solve")
    fun solve(
        @RequestParam semester: Int
    ): ResponseEntity<ApiResponse<Map<String, String>>> {
        val jobId = matrixService.startSolving(semester)
        return ResponseEntity.ok(
            ApiResponse.success(
                data    = mapOf("jobId" to jobId),
                message = "Solver ishga tushdi. Status: GET /api/matrix/status/$jobId"
            )
        )
    }

    // ──────────────────────────────────────────────────────
    // STATUS TEKSHIRISH
    // GET /api/matrix/status/{jobId}
    // ──────────────────────────────────────────────────────
    @GetMapping("/status/{jobId}")
    fun getStatus(
        @PathVariable jobId: String
    ): ResponseEntity<ApiResponse<JobStatus>> {
        val status = matrixService.getStatus(jobId)
        return ResponseEntity.ok(ApiResponse.success(status, "OK"))
    }

    // ──────────────────────────────────────────────────────
    // TO'XTATISH
    // DELETE /api/matrix/stop/{jobId}
    // ──────────────────────────────────────────────────────
    @DeleteMapping("/stop/{jobId}")
    fun stop(
        @PathVariable jobId: String
    ): ResponseEntity<ApiResponse<String>> {
        matrixService.stopSolving(jobId)
        return ResponseEntity.ok(ApiResponse.success("OK", "Solver to'xtatildi"))
    }

    // ──────────────────────────────────────────────────────
    // GURUH JADVALI
    // GET /api/matrix/timetable/group/{groupId}
    // ──────────────────────────────────────────────────────
    @GetMapping("/timetable/group/{groupId}")
    fun groupTimetable(
        @PathVariable groupId: Long
    ): ResponseEntity<ApiResponse<List<MatrixLessonDto>>> {
        val lessons = matrixService.getGroupTimetable(groupId)
        return ResponseEntity.ok(ApiResponse.success(lessons.toDto(), "OK"))
    }

    // ──────────────────────────────────────────────────────
    // O'QITUVCHI JADVALI
    // GET /api/matrix/timetable/teacher/{teacherId}
    // ──────────────────────────────────────────────────────
    @GetMapping("/timetable/teacher/{teacherId}")
    fun teacherTimetable(
        @PathVariable teacherId: Long
    ): ResponseEntity<ApiResponse<List<MatrixLessonDto>>> {
        val lessons = matrixService.getTeacherTimetable(teacherId)
        return ResponseEntity.ok(ApiResponse.success(lessons.toDto(), "OK"))
    }

    // ──────────────────────────────────────────────────────
    // XONA JADVALI
    // GET /api/matrix/timetable/room/{roomId}
    // ──────────────────────────────────────────────────────
    @GetMapping("/timetable/room/{roomId}")
    fun roomTimetable(
        @PathVariable roomId: Long
    ): ResponseEntity<ApiResponse<List<MatrixLessonDto>>> {
        val lessons = matrixService.getRoomTimetable(roomId)
        return ResponseEntity.ok(ApiResponse.success(lessons.toDto(), "OK"))
    }

    // ──────────────────────────────────────────────────────
    // JADVAL TOZALASH
    // DELETE /api/matrix/timetable/clear
    // ──────────────────────────────────────────────────────
    @DeleteMapping("/timetable/clear")
    fun clear(): ResponseEntity<ApiResponse<String>> {
        val count = matrixService.clearTimetable()
        return ResponseEntity.ok(ApiResponse.success("OK", "$count ta dars tozalandi"))
    }
}

private fun List<Lesson>.toDto(): List<MatrixLessonDto> = map { l ->
    MatrixLessonDto(
        lessonId   = l.id ?: 0L,
        subject    = l.subject?.name ?: "",
        teacher    = l.teacher?.fullName ?: "",
        groups     = l.groups.map { it.name },
        type       = l.type.name,
        day        = l.timeslot?.dayOfWeek?.name ?: "—",
        pairNumber = l.timeslot?.pairNumber ?: 0,
        startTime  = l.timeslot?.startTime?.toString() ?: "—",
        endTime    = l.timeslot?.endTime?.toString() ?: "—",
        room       = l.room?.roomNumber ?: "—",
        building   = l.room?.building?.name ?: "—",
        isLab      = l.room?.isLaboratory ?: false
    )
}