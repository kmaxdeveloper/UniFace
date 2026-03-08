package com.uniface.service

import com.uniface.dto.AttendanceRecordDto
import com.uniface.dto.AttendanceStatsDto
import com.uniface.repository.AttendanceRepository
import com.uniface.repository.StudentRepository
import com.uniface.repository.StudentGroupRepository
import com.uniface.repository.SubjectRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Service
class AttendanceService(
    private val attendanceRepository: AttendanceRepository,
    private val studentRepository: StudentRepository,
    private val groupRepository: StudentGroupRepository,
    private val subjectRepository: SubjectRepository
) {
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    // Guruh bo'yicha statistika
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

    // Fan bo'yicha statistika
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

    // Talaba bo'yicha o'z davomati
    fun getStudentAttendance(studentId: String): AttendanceStatsDto {
        val records = attendanceRepository.findByStudentStudentId(studentId)

        return AttendanceStatsDto(
            totalStudents = 1,
            presentCount = records.size,
            attendancePercent = 100.0,
            records = records.map { it.toDto() }
        )
    }

    // Bugungi davomat
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

    // Extension function
    private fun com.uniface.entity.Attendance.toDto() = AttendanceRecordDto(
        studentId = student?.studentId ?: "",
        studentName = student?.fullName ?: "",
        subjectName = subject?.name ?: "",
        groupName = group?.name ?: "",
        timestamp = timestamp.format(formatter),
        status = status
    )
}