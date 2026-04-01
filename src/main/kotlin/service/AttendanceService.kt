package com.uniface.service

import com.uniface.dto.AttendanceRecordDto
import com.uniface.dto.AttendanceStatsDto
import com.uniface.dto.StartLessonRequest
import com.uniface.entity.Attendance
import com.uniface.entity.Lesson
import com.uniface.exception.AlreadyMarkedException
import com.uniface.exception.InvalidAttendanceException
import com.uniface.exception.StudentNotFoundException
import com.uniface.repository.*
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Service
class AttendanceService(
    private val attendanceRepository: AttendanceRepository,
    private val studentRepository: StudentRepository,
    private val groupRepository: StudentGroupRepository,
    private val subjectRepository: SubjectRepository,
    private val lessonRepository: LessonRepository,
    private val teacherRepository: TeacherRepository,
    private val qrService: QrService
) {
    //InvalidAttendanceException
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    private fun Attendance.toDto() = AttendanceRecordDto(
        studentId = student?.studentId ?: "",
        studentName = student?.fullName ?: "",
        subjectName = subject?.name ?: "",
        groupName = group?.name ?: "",
        timestamp = timestamp.format(formatter),
        status = status
    )

    // Joylashuvni tekshirish (Aytganingdek, vaqtincha bypass/true)
    fun checkLocation(lat: Double, lng: Double): Boolean = true

    // --- MAVJUD STATISTIKA METODLARI ---

    fun getGroupStats(groupId: Long): AttendanceStatsDto {
        val group = groupRepository.findById(groupId).orElseThrow { NoSuchElementException("Guruh topilmadi") }
        val totalStudents = studentRepository.countByGroupId(groupId).toInt()
        val records = attendanceRepository.findByGroupId(groupId)

        val presentCount = records.map { it.student?.studentId }.distinct().count()
        val percent = if (totalStudents > 0) (presentCount * 100.0 / totalStudents) else 0.0

        return AttendanceStatsDto(
            totalStudents = totalStudents,
            presentCount = presentCount,
            attendancePercent = percent,
            records = records.map { it.toDto() }
        )
    }

    fun getSubjectStats(subjectId: Long): AttendanceStatsDto {
        val records = attendanceRepository.findBySubjectId(subjectId)
        val presentCount = records.map { it.student?.studentId }.distinct().count()

        return AttendanceStatsDto(
            totalStudents = presentCount,
            presentCount = presentCount,
            attendancePercent = 100.0,
            records = records.map { it.toDto() }
        )
    }

    fun getStudentAttendance(studentId: String): AttendanceStatsDto {
        val records = attendanceRepository.findByStudentStudentId(studentId)
        return AttendanceStatsDto(
            totalStudents = 1,
            presentCount = records.size,
            attendancePercent = 100.0,
            records = records.map { it.toDto() }
        )
    }

    fun getTodayStats(): AttendanceStatsDto {
        val start = LocalDate.now().atStartOfDay()
        val end = LocalDate.now().atTime(LocalTime.MAX)
        val records = attendanceRepository.findByDateRange(start, end)
        val presentCount = records.map { it.student?.studentId }.distinct().count()

        return AttendanceStatsDto(
            totalStudents = presentCount,
            presentCount = presentCount,
            attendancePercent = 100.0,
            records = records.map { it.toDto() }
        )
    }


    // ==========================================================================
    @Transactional
    fun markAttendance(studentId: String, qrToken: String): AttendanceRecordDto {
        // 1. JWT Token tekshiruvi
        val lessonId = qrService.getLessonIdFromToken(qrToken)
            ?: throw InvalidAttendanceException("QR-kod muddati o'tgan yoki xato!")

        val student = studentRepository.findByStudentId(studentId)
            ?: throw StudentNotFoundException("Talaba topilmadi")

        val lesson = lessonRepository.findById(lessonId)
            .orElseThrow { NoSuchElementException("Dars seansi topilmadi") }

        // 2. Dars va Guruh tekshiruvi
        if (!lesson.isActive) {
            throw InvalidAttendanceException("Dars yakunlangan!")
        }

        val isStudentInGroup = lesson.groups.any { it.id == student.group?.id }
        if (!isStudentInGroup) {
            throw InvalidAttendanceException("Sizning guruhingiz bu darsga biriktirilmagan!")
        }

        // 3. Dublikat tekshiruvi
        if (attendanceRepository.existsByStudentAndLesson(student, lesson)) {
            throw AlreadyMarkedException("Siz allaqachon davomatdan o'tgansiz!")
        }

        // 4. Attendance yaratish (Secondary Constructor tartibida)
        // DIQQAT: Ismlarni yozmaymiz, faqat tartib bilan o'zgaruvchilarni yuboramiz
        val newAttendance = Attendance(
            student,          // student
            lesson.subject,   // subject
            student.group,    // group
            lesson.teacher,   // teacher
            lesson            // lesson
        )

        val savedAttendance = attendanceRepository.save(newAttendance)

        return savedAttendance.toDto()
    }

    @Transactional
    fun startNewLesson(request: StartLessonRequest): Long {
        // 1. O'qituvchini tekshirish
        val teacher = teacherRepository.findByUserUsername(request.teacherUsername ?: throw IllegalArgumentException("Username bo'sh!"))
            ?: throw EntityNotFoundException("O'qituvchi topilmadi")

        // 2. XAVFSIZLIK: O'qituvchida allaqachon faol dars bormi?
        // Agar bo'lsa, uni avtomatik yopamiz yoki xato beramiz.
        val activeLesson = lessonRepository.findByTeacherUserUsernameAndIsActiveTrue(teacher.user.username)
        if (activeLesson != null) {
            // Variant A: Eskisini yopish
            activeLesson.isActive = false
            activeLesson.endTime = LocalDateTime.now()
            lessonRepository.save(activeLesson)
            // Variant B: throw IllegalStateException("Sizda hali yakunlanmagan dars bor!")
        }

        // 3. Fanni tekshirish
        val subject = subjectRepository.findById(request.subjectId)
            .orElseThrow { EntityNotFoundException("Fan topilmadi") }

        // 4. Guruhlarni tekshirish (To'liqligini tekshiramiz)
        val groups = groupRepository.findAllById(request.groupIds)
        if (groups.size != request.groupIds.size) {
            throw EntityNotFoundException("Ba'zi guruhlar topilmadi! (Kiritilgan: ${request.groupIds.size}, Topilgan: ${groups.size})")
        }

        val newLesson = Lesson(
            subject = subject,
            teacher = teacher,
            groups = groups.toMutableSet(),
            type = request.lessonType,
            startTime = LocalDateTime.now(),
            isActive = true
        )

        val savedLesson = lessonRepository.save(newLesson)
        return savedLesson.id ?: throw IllegalStateException("Bazaga saqlashda ID generatsiya bo'lmadi")
    }
}