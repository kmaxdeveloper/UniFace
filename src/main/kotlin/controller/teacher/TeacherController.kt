package com.uniface.controller.teacher

import com.uniface.data.ApiResponse
import com.uniface.dto.StartLessonRequest
import com.uniface.dto.matrix.MatrixLessonDto
import com.uniface.entity.Lesson
import com.uniface.matrix.service.MatrixService
import com.uniface.repository.SubjectRepository
import com.uniface.repository.TeacherRepository
import com.uniface.repository.UserRepository
import com.uniface.service.AttendanceService
import com.uniface.service.FaceService
import com.uniface.service.TeacherService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.security.Principal

@RestController
@RequestMapping("/api/v1/teacher")
class TeacherController(
    private val faceService: FaceService,
    private val teacherService: TeacherService,
    private val subjectRepository: SubjectRepository,
    private val attendanceService: AttendanceService,
    private val userRepository: UserRepository,
    private val teacherRepository: TeacherRepository,
    private val matrixService: MatrixService
) {

    // Auditoriyani ommaviy rasmga olish (100 kishigacha)
    @PostMapping("/attendance/bulk")
    fun takeBulkAttendance(
        @RequestParam("image") file: MultipartFile,
        @RequestParam("subjectId") subjectId: Long,
        @RequestParam("groupId") groupId: Long,
        @RequestParam("teacherId") teacherId: Long // String o'rniga ID
    ) = ResponseEntity.ok(faceService.processBulkAttendance(file.bytes, subjectId, groupId, teacherId))

    @PostMapping("/attendance/single")
    fun takeSingleAttendance(
        @RequestParam("image") file: MultipartFile,
        @RequestParam("subjectId") subjectId: Long,
        @RequestParam("groupId") groupId: Long,
        @RequestParam("teacherId") teacherId: Long // String o'rniga ID
    ) = ResponseEntity.ok(faceService.identifyStudent(file.bytes, subjectId, groupId, teacherId))

    @GetMapping("/{teacherId}/lessons")
    fun getLessons(@PathVariable teacherId: Long): ResponseEntity<List<Map<String, Any>>> {
        val allocations = teacherService.getMyAllocations(teacherId)

        // Android-ga qulay formatda javob qaytaramiz
        val response = allocations.map {
            mapOf(
                "allocationId" to it.id!!,
                "subjectName" to it.subject!!.name,
                "groupName" to (it.group?.name ?: it.patokName ?: "Noma'lum"),
                "isPatok" to it.isPatok
            )
        }

        return ResponseEntity.ok(response)
    }

    @GetMapping("/subjects")
    fun getAllSubjects() = ResponseEntity.ok(subjectRepository.findAll())

    @PostMapping("/lessons/start")
    fun startLesson(
        @RequestBody request: StartLessonRequest,
        principal: Principal
    ): ResponseEntity<Long> {
        // Requestni yangi username bilan nusxalaymiz (copy)
        val updatedRequest = request.copy(teacherUsername = principal.name)

        // Endi service-ga bitta argument yuboryapmiz, xato yo'qoladi!
        val lessonId = attendanceService.startNewLesson(updatedRequest)

        return ResponseEntity.ok(lessonId)
    }

    @GetMapping("/get-subjects")
    fun getSubjects(): ResponseEntity<Any> {
        // 1. Login qilgan odamning ma'lumotlarini context'dan olamiz (Parametrda null bermasligi uchun)
        val auth = SecurityContextHolder.getContext().authentication

        // 2. Tizimga kirganini tekshiramiz
        if (auth == null || !auth.isAuthenticated || auth.name == "anonymousUser") {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Tizimga kirmagansiz!")
        }

        // 3. Username orqali o'qituvchini va uning fanlarini bazadan olamiz
        val teacher = teacherRepository.findByUserUsername(auth.name)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ustoz profili topilmadi!")

        // 4. Faqat fanlar ro'yxatini qaytaramiz
        // 'Set'ni 'List'ga o'girib yuborsang, frontendda array bo'lib boradi
        return ResponseEntity.ok(teacher.subjects.toList())
    }

    @GetMapping("/timetable/{username}")
    fun teacherTimetable(@PathVariable username: String): ResponseEntity<ApiResponse<List<MatrixLessonDto>>> {
        val lessons = matrixService.getTeacherTimetableByUsername(username)
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