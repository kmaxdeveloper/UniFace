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

        // 2. TOZALASH: Matrix rejalashtirgan yoki ochiq qolgan barcha faol darslarni ro'yxat sifatida olamiz
        // Repository'da bu metod List<Lesson> qaytarishi shart!
        val allActiveLessons = lessonRepository.findByTeacherUserUsernameAndIsActiveTrue(teacher.user.username)

        // 3. Fanni va guruhlarni tekshirish
        val subject = subjectRepository.findById(request.subjectId)
            .orElseThrow { EntityNotFoundException("Fan topilmadi") }

        val groups = groupRepository.findAllById(request.groupIds)
        if (groups.size != request.groupIds.size) {
            throw EntityNotFoundException("Ba'zi guruhlar topilmadi!")
        }

        // 4. MATRIX LOGIKASI: Agar Matrix allaqachon shu fan uchun dars yaratgan bo'lsa, o'shani ishlatamiz
        val plannedLesson = allActiveLessons.find { it.subject?.id == request.subjectId }

        return if (plannedLesson != null) {
            // Bor darsni yangilaymiz (Matrix yaratgan darsni real darsga aylantiramiz)
            plannedLesson.startTime = LocalDateTime.now()
            plannedLesson.type = request.lessonType
            plannedLesson.groups = groups.toMutableSet()
            plannedLesson.isActive = true

            // Agar boshqa (masalan, Matrix yaratgan boshqa soatdagi) darslar bo'lsa, ularni yopamiz
            allActiveLessons.filter { it.id != plannedLesson.id }.forEach {
                it.isActive = false
                lessonRepository.save(it)
            }

            lessonRepository.save(plannedLesson).id!!
        } else {
            // Agar Matrix bu fanni rejalashtirmagan bo'lsa, yangi dars ochishdan oldin hammasini yopamiz
            allActiveLessons.forEach {
                it.isActive = false
                lessonRepository.save(it)
            }

            val newLesson = Lesson(
                subject = subject,
                teacher = teacher,
                groups = groups.toMutableSet(),
                type = request.lessonType,
                startTime = LocalDateTime.now(),
                isActive = true
            )
            lessonRepository.save(newLesson).id!!
        }
    }
}