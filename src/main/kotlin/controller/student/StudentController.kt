package com.uniface.controller.student

import com.uniface.data.ApiResponse
import com.uniface.dto.matrix.MatrixLessonDto
import com.uniface.entity.Lesson
import com.uniface.matrix.service.MatrixService
import com.uniface.repository.AttendanceRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/student")
class StudentController(
    private val attendanceRepo: AttendanceRepository,
    private val matrixService: MatrixService
) {

    // Talaba o'zining hamma davomatlarini ko'rishi uchun
    @GetMapping("/my-attendance/{studentId}")
    fun getMyAttendance(@PathVariable studentId: String): ResponseEntity<Any> {
        val list = attendanceRepo.findByStudentStudentId(studentId)
        return ResponseEntity.ok(list)
    }

    @GetMapping("/timetable/{username}")
    fun studentTimetable(
        @PathVariable username: String
    ): ResponseEntity<ApiResponse<List<MatrixLessonDto>>> {
        // Bu yerda matrixService orqali servisdagi metodni chaqiramiz
        val lessons = matrixService.getStudentTimetableByUsername(username)

        return ResponseEntity.ok(ApiResponse.success(lessons.toDto(), "OK"))
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