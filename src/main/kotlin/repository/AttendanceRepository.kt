package com.uniface.repository

import com.uniface.entity.Attendance
import com.uniface.entity.Student
import com.uniface.entity.Subject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import org.springframework.stereotype.Repository

@Repository
interface AttendanceRepository : JpaRepository<Attendance, Long> {

    fun findByStudentStudentId(studentId: String): List<Attendance>

    // Talaba bugun aynan shu darsda (subject) bor-yo'qligini tekshirish
    @Query("""
        SELECT COUNT(a) > 0 FROM Attendance a 
        WHERE a.student = :student 
        AND a.subject = :subject 
        AND a.timestamp >= :startOfDay 
        AND a.timestamp <= :endOfDay
    """)
    fun existsByStudentAndSubjectToday(
        @Param("student") student: Student,
        @Param("subject") subject: Subject,
        @Param("startOfDay") startOfDay: LocalDateTime,
        @Param("endOfDay") endOfDay: LocalDateTime
    ): Boolean

}