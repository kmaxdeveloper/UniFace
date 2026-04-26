package com.uniface.integration

import com.uniface.entity.*
import com.uniface.repository.AttendanceRepository
import com.uniface.repository.StudentRepository
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@SpringBootTest
@Transactional
class AttendanceIntegrationTest {

    @Autowired
    lateinit var studentRepository: StudentRepository

    @Autowired
    lateinit var attendanceRepository: AttendanceRepository
    

    @Test
    fun `attendance saving test`() {
        // 1. Talaba yaratamiz (Konstruktoriga moslab)
        val student = studentRepository.save(Student(
            studentId = "210011",
            fullName = "Komiljon",
            faceId = "id_123",
            group = null,
            user = null
        ))

        // 2. DAVOMAT YARATISH (Sening 4 talik konstruktoringga mos)
        // (student, subject, group, teacher) - hammasini berish shart!
        val attendance = Attendance(
            student = student,
            subject = null,
            group = null,
            teacher = null,
            lesson = null
        ).apply {
            // Konstruktorda yo'q maydonlarni apply ichida beramiz
            this.timestamp = LocalDateTime.now()
            this.status = "PRESENT"
        }

        // 3. SAQLASH
        // <Attendance> deb tipini aniq ko'rsatamiz (S-parameter xatosi chiqmasligi uchun)
        val savedAttendance = attendanceRepository.save<Attendance>(attendance)

        // 4. TEKSHIRISH
        assertNotNull(savedAttendance.id)
    }
}