package com.uniface.service

import com.uniface.dto.*
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
    private val topicRepository: TopicRepository,
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
        // 1. O'qituvchini topish (Username orqali, chunki qo'limizda ID yo'q)
        val username = request.teacherUsername ?: throw IllegalArgumentException("Username bo'sh!")
        val teacher = teacherRepository.findByUserUsername(username)
            ?: throw EntityNotFoundException("O'qituvchi [$username] topilmadi")

        // 2. TOZALASH: Matrix rejalashtirgan yoki ochiq qolgan barcha darslarni yig'amiz
        // Bu metod LessonRepository-da List<Lesson> qaytarishi shart!
        val allActiveLessons = lessonRepository.findByTeacherUserUsernameAndIsActiveTrue(username)

        // 3. Resurslarni (Fan va Guruhlar) tekshirish
        val subject = subjectRepository.findById(request.subjectId)
            .orElseThrow { EntityNotFoundException("Fan topilmadi") }

        val groups = groupRepository.findAllById(request.groupIds)
        if (groups.size != request.groupIds.size) {
            throw EntityNotFoundException("Ba'zi guruhlar topilmadi yoki guruhlar ro'yxati noto'g'ri!")
        }

        // 4. MATRIX LOGIKASI: Matrix bu fanni allaqachon rejalashtirganmi?
        val plannedLesson = allActiveLessons.find { it.subject?.id == request.subjectId }

        return if (plannedLesson != null) {
            // A) MATRIX DARSINI ISHLATISH:
            // Matrix yaratgan "shablon" darsni real vaqt va guruhlar bilan to'ldiramiz
            plannedLesson.apply {
                startTime = LocalDateTime.now()
                type = request.lessonType
                this.groups = groups.toMutableSet() // Guruhlarni yangilaymiz
                isActive = true // Faolligini tasdiqlaymiz
                // Mavzuni qo'shish
                topic = request.topicId?.let { topicRepository.findById(it).orElse(null) }
            }

            // QOLGANLARINI YOPISH:
            // Agar boshqa darslar (masalan, Matrix yaratgan boshqa vaqtdagi darslar) bo'lsa, ularni isActive = false qilamiz
            allActiveLessons.filter { it.id != plannedLesson.id }.forEach {
                it.isActive = false
                lessonRepository.save(it) // Statusni yangilash (O'CHIRMAYDI, shunchaki yopadi)
            }

            lessonRepository.save(plannedLesson).id!!
        } else {
            // B) YANGI DARS OCHISH:
            // Agar Matrix bu fanni rejalashtirmagan bo'lsa, hammasini yopib yangisini ochamiz
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
                isActive = true,
                topic = request.topicId?.let { topicRepository.findById(it).orElse(null) }
            )
            lessonRepository.save(newLesson).id!!
        }
    }

    fun getStudentDetailedStats(username: String): StudentAttendanceStatsDto {
        val student = studentRepository.findByUserUsername(username)
            ?: throw EntityNotFoundException("Talaba topilmadi")
        
        // 1. Talaba guruhining barcha o'tilgan darslari (startTime != null)
        val allLessonsHeld = lessonRepository.findAllByGroupsId(student.group?.id ?: 0L)
            .filter { it.startTime != null }
        
        // 2. Talabaning barcha davomat yozuvlari
        val attendanceRecords = attendanceRepository.findByStudentStudentId(student.studentId)
        val attendedLessonIds = attendanceRecords.map { it.lesson?.id }.toSet()

        // 3. Fanlar bo'yicha guruhlash
        val lessonsBySubject = allLessonsHeld.groupBy { it.subject?.id }
        
        val subjectDetails = lessonsBySubject.map { (subjectId, lessons) ->
            val subjectName = lessons.first().subject?.name ?: "Noma'lum"
            val total = lessons.size
            val present = lessons.count { attendedLessonIds.contains(it.id) }
            val missed = total - present
            val percent = if (total > 0) (missed * 100.0 / total) else 0.0
            
            val missedTopics = lessons
                .filter { !attendedLessonIds.contains(it.id) }
                .map { MissedTopicDto(it.topic?.title ?: "Mavzu ko'rsatilmagan", it.startTime?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "") }

            val risk = when {
                percent >= 23.0 -> "DANGER"
                percent >= 15.0 -> "WARNING"
                else -> "SAFE"
            }

            SubjectAttendanceDetailDto(
                subjectId = subjectId ?: 0L,
                subjectName = subjectName,
                totalLessons = total,
                presentCount = present,
                missedCount = missed,
                missedPercentage = percent,
                missedTopics = missedTopics,
                riskStatus = risk
            )
        }

        val totalMissed = subjectDetails.sumOf { it.missedCount } * 2 // Har bir para 2 soat
        val maxMissedPercent = if (subjectDetails.isNotEmpty()) subjectDetails.maxOf { it.missedPercentage } else 0.0
        
        val overallRisk = when {
            maxMissedPercent >= 23.0 -> "DANGER"
            maxMissedPercent >= 15.0 -> "WARNING"
            else -> "SAFE"
        }

        return StudentAttendanceStatsDto(
            subjects = subjectDetails,
            totalMissedHours = totalMissed,
            overallRiskStatus = overallRisk
        )
    }
}