package com.uniface.service

import com.uniface.dto.AttendanceRecordDto
import com.uniface.dto.AttendanceStatsDto
import com.uniface.entity.Attendance
import com.uniface.repository.*
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
    private val lessonRepository: LessonRepository
) {
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    fun markAttendance(studentId: String, lessonId: Long): AttendanceRecordDto {
        // 1. Talabani bazadan qidiramiz
        val student = studentRepository.findByStudentId(studentId)
            ?: throw Exception("Talaba topilmadi")

        // 2. Dars seansini topamiz (Lesson ichida hamma narsa bor)
        val lesson = lessonRepository.findById(lessonId)
            .orElseThrow { Exception("Dars seansi topilmadi") }

        // 3. Dublikatni tekshirish (Bir kunda bir marta darsga kirish uchun)
        val startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay()
        val endOfDay = LocalDateTime.now().toLocalDate().atTime(java.time.LocalTime.MAX)

        if (attendanceRepository.existsByStudentAndSubjectToday(student, lesson.subject, startOfDay, endOfDay)) {
            throw Exception("Siz bugun bu fandan davomatdan o'tgansiz!")
        }

        // 4. YANGI ATTENDANCE YARATISH (Sening konstruktoringga mos)
        val newAttendance = Attendance(
            student = student,
            subject = lesson.subject,
            group = lesson.group,
            teacher = lesson.teacher // String emas, Teacher obyektini o'zini beramiz!
        ).apply {
            this.timestamp = LocalDateTime.now()
            this.status = "PRESENT"
        }

        // 5. Saqlash va DTO ga o'girib qaytarish
        return attendanceRepository.save(newAttendance).toDto()
    }

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
        val group = groupRepository.findById(groupId).orElseThrow { Exception("Guruh topilmadi") }
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
}