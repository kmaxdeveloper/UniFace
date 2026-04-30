package com.uniface.controller.student

import com.uniface.data.ApiResponse
import com.uniface.dto.matrix.MatrixLessonDto
import com.uniface.entity.Lesson
import com.uniface.matrix.service.MatrixService
import com.uniface.dto.StudentAttendanceStatsDto
import com.uniface.repository.AttendanceRepository
import com.uniface.repository.StudentRepository
import com.uniface.repository.UserRepository
import com.uniface.service.AttendanceService
import com.uniface.iris.service.IrisService
import java.security.Principal
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/student")
class StudentController(
    private val attendanceRepo: AttendanceRepository,
    private val attendanceService: AttendanceService,
    private val matrixService: MatrixService,
    private val studentRepository: StudentRepository,
    private val userRepository: UserRepository,
    private val irisService: IrisService
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

    @GetMapping("/attendance/stats")
    fun getAttendanceStats(principal: Principal): ResponseEntity<StudentAttendanceStatsDto> {
        return ResponseEntity.ok(attendanceService.getStudentDetailedStats(principal.name))
    }

    @GetMapping("/profile")
    fun getProfile(principal: Principal): ResponseEntity<Map<String, Any>> {
        val student = studentRepository.findByUserUsername(principal.name)
            ?: return ResponseEntity.notFound().build()
            
        return ResponseEntity.ok(mapOf(
            "studentId" to student.studentId,
            "fullName" to student.fullName,
            "username" to (student.user?.username ?: ""),
            "groupName" to (student.group?.name ?: "Noma'lum"),
            "faceId" to student.faceId,
            "irisPoints" to student.irisPoints,
            "irisLevel" to student.irisLevel,
            "irisLevelName" to irisService.getStudentLevelName(student.irisLevel)
        ))
    }

    @GetMapping("/today-lessons")
    fun getTodayLessons(principal: Principal): ResponseEntity<List<Map<String, Any>>> {
        val lessons = matrixService.getStudentTimetableByUsername(principal.name)
        val now = LocalDateTime.now()
        val currentDay = now.dayOfWeek

        val todayLessons = lessons.filter { it.timeslot?.dayOfWeek == currentDay }
        
        val response = todayLessons.map { lesson ->
            mapOf(
                "id" to (lesson.id ?: 0L),
                "subject" to (lesson.subject?.name ?: ""),
                "time" to (lesson.timeslot?.startTime?.toString() ?: ""),
                "room" to (lesson.room?.roomNumber ?: ""),
                "teacher" to (lesson.teacher?.fullName ?: ""),
                "building" to (lesson.room?.building?.name ?: "")
            )
        }
        return ResponseEntity.ok(response)
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